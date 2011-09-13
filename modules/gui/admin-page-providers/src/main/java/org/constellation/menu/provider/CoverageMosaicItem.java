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

import org.constellation.admin.service.ConstellationServer;
import org.constellation.bean.AbstractMenuItem;
import org.constellation.configuration.ProvidersReport;


/**
 * Add an overview page for providers.
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageMosaicItem extends AbstractMenuItem{

    public CoverageMosaicItem() {
        super(new String[]{
                "/provider/coveragemosaic.xhtml",
                "/provider/coveragemosaicConfig.xhtml",
                "/provider/coveragemosaicLayerConfig.xhtml"},
            "provider.coveragemosaic",
            new Path(PROVIDERS_DATA_PATH,"Coverage-Mosaic", "/provider/coveragemosaic.xhtml", null,800)
            );
    }
    
    @Override
    public boolean isAvailable(final ConstellationServer server) {
        if(server == null) return false;
        final ProvidersReport report = server.providers.listProviders();
        if(report == null) return false;
        return report.getProviderService(CoverageMosaicBean.SERVICE_NAME) != null;
    }

}
