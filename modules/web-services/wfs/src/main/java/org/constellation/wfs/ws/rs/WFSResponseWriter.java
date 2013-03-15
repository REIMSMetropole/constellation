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

package org.constellation.wfs.ws.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wfs.xml.WFSMarshallerPool;
import org.geotoolkit.wfs.xml.WFSResponse;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces("application/xml,text/xml,*/*")
public class WFSResponseWriter<T extends WFSResponse> implements MessageBodyWriter<T>  {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.metadata.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return WFSResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            final Marshaller m = WFSMarshallerPool.getInstance().acquireMarshaller();
            if (t instanceof WFSResponseWrapper) {
                m.marshal(((WFSResponseWrapper)t).getResponse(), out);
            } else {
                m.marshal(t, out);
            }
             WFSMarshallerPool.getInstance().release(m);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the WFS response", ex);
        }
    }
}
