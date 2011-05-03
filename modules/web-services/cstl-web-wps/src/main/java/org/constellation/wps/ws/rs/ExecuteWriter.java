/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.wps.ws.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wps.xml.v100.ExecuteResponse;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
public class ExecuteWriter<T extends ExecuteResponse> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.wps.ws.rs");
    private static MarshallerPool pool;
    static{
        try {
            pool = new MarshallerPool("org.geotoolkit.wps.xml.v100:org.geotoolkit.gml.xml.v311:org.geotoolkit.internal.jaxb.geometry");
        } catch (JAXBException ex) {
            Logger.getLogger(ExecuteWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return ExecuteResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
            final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        Marshaller m = null;
        try {
            m = pool.acquireMarshaller();
            m.marshal(t, out);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the feature collection", ex);
        } finally {
            if(m!=null){
                pool.release(m);
            }
        }
        
    }

}
