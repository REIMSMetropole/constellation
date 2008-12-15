/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2008, Geomatys
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
package org.constellation.security.wms;

import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.ws.rs.Path;
import javax.xml.bind.JAXBException;
import org.constellation.map.ws.rs.WMSService;
import org.constellation.security.WMSSecuredWorker;


/**
 * The REST facade to this WMS Policy Enforcement Point (PEP).
 *
 * This facade covers both clients which call the service using an HTTP GET
 * message and include the request and all other parameters in the URL itself as
 * well as clients which call the service using an HTTP POST message and include
 * the request in the body of the message either as Key-Value pairs or as an XML
 * document. The latter has not yet been formalized by the OGC for WMS and so is
 * an extension of the existing standards.
 *
 * The facade calls the {@code org.constellation.security.Worker} for all the
 * complex logic.
 *
 * Access control necessitates that the user be authenticated to the container.
 * If the user has not proceeded with the authentication part, the service will
 * return a response indicating the policy which requires access constraint.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
@Path("wms-sec")
@Singleton
public class WmsRestService extends WMSService {

     public WmsRestService() throws JAXBException, SQLException, IOException, NamingException {
            worker = new WMSSecuredWorker(marshaller,unmarshaller);
            LOGGER.info("WMS service running");

    }

}