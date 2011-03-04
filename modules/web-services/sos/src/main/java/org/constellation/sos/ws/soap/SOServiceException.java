/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.sos.ws.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;
import org.geotoolkit.ows.xml.v110.ExceptionReport;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SOServiceException", namespace="http://soap.webservice.sos.constellation.org/")
@WebFault
public class SOServiceException extends WebServiceException {
    private static final long serialVersionUID = 6093245564310503955L;
    
    /**
     * An OGC Web ServiceType exception report
     */
    private final ExceptionReport exception;
    
    SOServiceException() {
        super();
        exception = null;
    }
            
    public SOServiceException(String message, String code, String v) {
        super(message);
        this.exception = new ExceptionReport(message, code, null,  v);
        
        this.setStackTrace(new StackTraceElement[0]);
    }
    
    public ExceptionReport getException() {
        return exception;
    }
}
