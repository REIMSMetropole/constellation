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

package org.constellation.menu.provider;

/**
 * PostGIS configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGISBean extends AbstractProviderConfigBean{

    public static final String SERVICE_NAME = "postgis";
    
    public PostGISBean(){
        super(SERVICE_NAME,
              "/provider/postgis.xhtml",
              "/provider/postgisConfig.xhtml",
              "/provider/postgisLayerConfig.xhtml");
        addBundle("provider.postgis");
    }

}
