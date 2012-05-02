/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
package org.constellation.ws.soap;

// J2SE dependencies
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

// Constellation dependencies
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;

// Geotoolkit dependencies
import org.geotoolkit.util.logging.Logging;


// GeoAPI dependencies

/**
 * Abstract parent SOAP facade for all OGC web services in Constellation.
 * <p>
 * This class
 * </p>
 * <p>
 * The Open Geospatial Consortium (OGC) has defined a number of web services for
 * geospatial data such as:
 * <ul>
 *   <li><b>CSW</b> -- Catalog Service for the Web</li>
 *   <li><b>WCS</b> -- Web Coverage Service</li>
 *   <li><b>SOS</b> -- Sensor Observation Service</li>
 * </ul>
 * Many of these Web Services have been defined to work with SOAP based HTTP
 * message exchange; this class provides base functionality for those services.
 * </p>
 *
 * @version $Id$
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.7
 */
public abstract class OGCWebService<W extends Worker> {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.ws.soap");

    private final Specification specification;

    @Resource
    private volatile WebServiceContext context;

    /**
     * Initialize the basic attributes of a web serviceType.
     *
     * @param supportedVersions A list of the supported version of this serviceType.
     *                          The first version specified <strong>MUST</strong> be the highest
     *                          one, the best one.
     */
    public OGCWebService(final Specification spec) {
        LOGGER.log(Level.INFO, "Starting the SOAP {0} service facade.\n", spec.name());
        this.specification = spec;
        WSEngine.registerService(specification.name(), "SOAP");
        
        /*
         * build the map of Workers, by scanning the sub-directories of its
         * service directory.
         */
        if (!WSEngine.isSetService(specification.name())) {
            buildWorkerMap();
        } else {
            LOGGER.log(Level.INFO, "Workers already set for {0}", specification.name());
        }
    }

    private File getServiceDirectory() {
        final File configDirectory   = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.exists() && configDirectory.isDirectory()) {
            final File serviceDirectory = new File(configDirectory, specification.name());
            if (serviceDirectory.exists() && serviceDirectory.isDirectory()) {
                return serviceDirectory;
            } else {
                LOGGER.log(Level.SEVERE, "The service configuration directory: {0} does not exist or is not a directory.", serviceDirectory.getPath());
            }
        } else {
            if (configDirectory == null) {
                LOGGER.severe("The service was unable to find a config directory.");
            } else {
                LOGGER.log(Level.SEVERE, "The configuration directory: {0} does not exist or is not a directory.", configDirectory.getPath());
            }
        }
        return null;
    }

    /**
     * Scan the configuration directory to instantiate Web service workers.
     */
    private void buildWorkerMap() {
        final Map<String, Worker> workersMap = new HashMap<String, Worker>();
        final File serviceDirectory = getServiceDirectory();
        if (serviceDirectory != null) {
            for (File instanceDirectory : serviceDirectory.listFiles()) {
                /*
                 * For each sub-directory we build a new Worker.
                 */
                if (instanceDirectory.isDirectory() && !instanceDirectory.getName().startsWith(".")) {
                    final W newWorker = createWorker(instanceDirectory);
                    workersMap.put(instanceDirectory.getName(), newWorker);
                }
            }
        }
        WSEngine.setServiceInstances(specification.name(), workersMap);
    }

    /**
     * Build a new instance of Web service worker with the specified configuration directory
     *
     * @param instanceDirectory The configuration directory of the instance.
     * @return
     */
    protected abstract W createWorker(final File instanceDirectory);

    /**
     * extract the service URL (before serviceName/serviceID?)
     * @return
     */
    protected String getServiceURL() {
        final HttpServletRequest request =   (HttpServletRequest) context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        String url = "";
        if (request != null) {
            url = request.getRequestURL().toString();
            url = url.substring(0, url.lastIndexOf('/'));
            url = url.substring(0, url.lastIndexOf('/') + 1);
        } else {
            LOGGER.warning("uable to find the service URL");
        }
        return url;
    }

    /**
     * Extract the instance ID from the URL.
     * 
     * @return
     */
    private String extractWorkerID() {
        final String pathInfo            = (String) context.getMessageContext().get(MessageContext.PATH_INFO);
        final HttpServletRequest request = (HttpServletRequest) context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        if (request != null) {
            final String url = request.getRequestURL().toString();
            return url.substring(url.lastIndexOf('/') + 1);
        } else if (pathInfo != null) {
            return pathInfo.substring(pathInfo.lastIndexOf('/') + 1);
        } else {
            LOGGER.severe("Unable to extract the servletRequest");
            return null;
        }
    }

    /**
     * Return the current worker specified by the URL.
     *
     * @return
     * @throws CstlServiceException
     */
    protected W getCurrentWorker() throws CstlServiceException {
        final String serviceID = extractWorkerID();
        if (serviceID == null || !WSEngine.serviceInstanceExist(specification.name(), serviceID)) {
            LOGGER.log(Level.WARNING, "Received request on undefined instance identifier:{0}", serviceID);
            final Set<String> instanceNames = WSEngine.getInstanceNames(specification.name());
            final String msg;
            if (serviceID == null) {
                msg = "You must specify an instance id.\n available instance:" + instanceNames;
            } else {
                msg = "Undefined instance id.\n available instance:" + instanceNames;
            }
            throw new CstlServiceException(msg);
            // TODO return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return (W) WSEngine.getInstance(specification.name(), serviceID);
        }
    }

    /**
     * Return the number of instance if the web-service
     */
    protected int getWorkerMapSize() {
        return WSEngine.getInstanceSize(specification.name());
    }

    @PreDestroy
    public void destroy() {
        LOGGER.log(Level.INFO, "Shutting down the SOAP {0} service facade.", specification.name());
        WSEngine.destroyInstances(specification.name());
    }

}
