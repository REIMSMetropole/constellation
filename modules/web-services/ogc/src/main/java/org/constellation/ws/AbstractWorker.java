/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.ws;

//J2SE dependencies
import com.sun.jersey.api.core.HttpContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.provider.configuration.ConfigDirectory;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;

/**
 * Abstract definition of a {@code Web Map Service} worker called by a facade
 * to perform the logic for a particular WMS instance.
 *
 * @version $Id: AbstractWMSWorker.java 1889 2009-10-14 16:05:52Z eclesia $
 * 
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractWorker implements Worker{

     /**
     * The default logger.
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.ws");

    /**
     * The web service unmarshaller, which will use the web service name space.
     */
    protected final MarshallerPool marshallerPool;
    
    /**
     * Contains information about the HTTP exchange of the request, for instance, 
     * the HTTP headers.
     */
    private HttpContext httpContext = null;
    /**
     * Contains authentication information related to the requesting principal.
     */
    private SecurityContext securityContext = null;
    /**
     * Defines a set of methods that a servlet uses to communicate with its servlet container,
     * for example, to get the MIME type of a file, dispatch requests, or write to a log file.
     */
    private ServletContext servletContext = null;
    /**
     * Contains the request URI and therefore any  KVP parameters it may contain.
     */
    private UriInfo uriContext = null;

    /**
     * The log level off al the informations log.
     */
    protected Level logLevel = Level.INFO;

    /**
     * A map containing the Capabilities Object already loaded from file.
     */
    private final Map<String,Object> capabilities = new HashMap<String,Object>();

    public AbstractWorker(final MarshallerPool marshallerPool) {
        this.marshallerPool = marshallerPool;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void initUriContext(final UriInfo uriInfo){
        uriContext = uriInfo;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void initHTTPContext(final HttpContext httpCtxt){
        httpContext = httpCtxt;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void initServletContext(final ServletContext servCtxt){
        servletContext = servCtxt;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void initSecurityContext(final SecurityContext secCtxt){
        securityContext = secCtxt;
    }

    protected synchronized HttpContext getHttpContext(){
        return httpContext;
    }

    protected synchronized SecurityContext getSecurityContext(){
        return securityContext;
    }

    protected synchronized ServletContext getServletContext(){
        return servletContext;
    }

    protected synchronized UriInfo getUriContext(){
        return uriContext;
    }

    /**
     * @param logLevel the logLevel to set
     */
    @Override
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param home    The home directory, where to search for configuration files.
     * @param version The version of the GetCapabilities.
     * @return The capabilities Object, or {@code null} if none.
     *
     * @throws JAXBException
     * @throws IOException
     */
    protected Object getStaticCapabilitiesObject(final String home, final String version, final String service) throws JAXBException, IOException {
        final String fileName = service + "Capabilities" + version + ".xml";
        final boolean update  = getUpdateCapabilitiesFlag(home);

        //Look if the template capabilities is already in cache.
        Object response = capabilities.get(fileName);
        if (response == null || update) {
            if (update) {
                LOGGER.log(logLevel, "updating metadata");
            }

            final File f = getFile(fileName, home);
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                // If the file is not present in the configuration directory, take the one in resource.
                if (!f.exists()) {
                    final InputStream in = getClass().getResourceAsStream(fileName);
                    response = unmarshaller.unmarshal(in);
                    in.close();
                } else {
                    response = unmarshaller.unmarshal(f);
                }

                if(response instanceof JAXBElement){
                    response = ((JAXBElement)response).getValue();
                }

                capabilities.put(fileName, response);

            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }

            storeUpdateCapabilitiesFlag(home);
        }
        return response;
    }

    protected boolean getUpdateCapabilitiesFlag(String home) throws FileNotFoundException, IOException {
        final Properties p = new Properties();

        // if the flag file is present we load the properties
        final File changeFile = getFile("change.properties", home);
        if (changeFile != null && changeFile.exists()) {
            final FileInputStream in = new FileInputStream(changeFile);
            p.load(in);
            in.close();
        } else {
            p.put("update", "false");
        }

        return  p.getProperty("update").equals("true");
    }

    protected void storeUpdateCapabilitiesFlag(final String home) throws FileNotFoundException, IOException {
        final Properties p = new Properties();
        final File changeFile = getFile("change.properties", home);
        p.put("update", "false");

        // if the flag file is present we store the properties
        if (changeFile != null && changeFile.exists()) {
            final FileOutputStream out = new FileOutputStream(changeFile);
            p.store(out, "updated from WebService");
            out.close();
        }
    }

    /**
     * Return a file located in the home directory. In this implementation, it should be
     * the WEB-INF directory of the deployed service.
     *
     * @param fileName The name of the file requested.
     * @return The specified file.
     */
    protected File getFile(final String fileName, final String home) {
         File path;
         if (home == null || !(path = new File(home)).isDirectory()) {
            path = ConfigDirectory.getConfigDirectory();
         }
         if (fileName != null)
            return new File(path, fileName);
         else return path;
    }
}
