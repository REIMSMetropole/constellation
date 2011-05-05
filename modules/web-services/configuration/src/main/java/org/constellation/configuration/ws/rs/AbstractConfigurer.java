/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.configuration.ws.rs;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;

import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import static org.constellation.ws.ExceptionCode.*;

import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractConfigurer {
    
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.configuration.ws.rs");
    
    /**
     * A container notifier allowing to restart the webService. 
     */
    protected ContainerNotifierImpl containerNotifier;
    
    /**
     * Because the injectable fields are null at initialization time
     * @param containerNotifier
     */
    public void setContainerNotifier(final ContainerNotifierImpl containerNotifier) {
        this.containerNotifier = containerNotifier;
    }
    
    /**
     * Extracts the value, for a parameter specified, from a query.
     *
     * @param parameterName The name of the parameter.
     *
     * @return the parameter, or {@code null} if not specified.
     */
    private List<String> getParameter(final String parameterName, final MultivaluedMap<String,String> parameters) {
        List<String> values = parameters.get(parameterName);

        //maybe the parameterName is case sensitive.
        if (values == null) {
            for(final Entry<String, List<String>> key : parameters.entrySet()){
                if(key.getKey().equalsIgnoreCase(parameterName)){
                    values = key.getValue();
                    break;
                }
            }
        }
        return values;
    }
    
    /**
     * Extracts the value, for a parameter specified, from a query.
     * If it is a mandatory one, and if it is {@code null}, it throws an exception.
     * Otherwise returns {@code null} in the case of an optional parameter not found.
     *
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional.
      *
     * @return the parameter, or {@code null} if not specified and not mandatory.
     * @throw CstlServiceException
     */
    protected String getParameter(final String parameterName, final boolean mandatory, final MultivaluedMap<String,String> parameters) throws CstlServiceException {

        final List<String> values = getParameter(parameterName, parameters);
        if (values == null) {
            if (mandatory) {
                throw new CstlServiceException("The parameter " + parameterName + " must be specified",
                        MISSING_PARAMETER_VALUE, parameterName.toLowerCase());
            }
            return null;
        } else {
            final String value = values.get(0);
            if ((value == null || value.isEmpty()) && mandatory) {
                throw new CstlServiceException("The parameter " + parameterName + " should have a value",
                        MISSING_PARAMETER_VALUE, parameterName.toLowerCase());
            } else {
                return value;
            }
        }
    }
    
    /**
     * destroy all the resource and close the connection.
     */
    public void destroy() {
       // do nothing must be overriden if needed 
    }
    
    public abstract Object treatRequest(final String request, final MultivaluedMap<String,String> parameters) throws CstlServiceException;
    
    public boolean isLock() {
        return false;
    }
}
