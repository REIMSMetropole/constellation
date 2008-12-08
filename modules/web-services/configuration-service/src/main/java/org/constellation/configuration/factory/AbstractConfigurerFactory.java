/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.configuration.factory;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.configuration.ws.rs.AbstractCSWConfigurer;
import org.constellation.ws.rs.ContainerNotifierImpl;

/**
 * An abstract factory for configurer.
 * 
 * @author Guilhem Legal
 */
public abstract class AbstractConfigurerFactory {
    
    public abstract AbstractCSWConfigurer getCSWConfigurer(Marshaller marshaller, Unmarshaller unmarshaller, ContainerNotifierImpl cn) throws ConfigurationException;

}
