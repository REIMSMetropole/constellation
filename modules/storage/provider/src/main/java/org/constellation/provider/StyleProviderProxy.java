/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
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
package org.constellation.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.filter.FilterFactory2;


/**
 * Main Data provider for styles objects. This class act as a proxy for
 * several SLD folder providers.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public final class StyleProviderProxy extends AbstractProviderProxy
        <String,MutableStyle,StyleProvider,StyleProviderService> {

    public static final MutableStyleFactory STYLE_FACTORY = (MutableStyleFactory)
            FactoryFinder.getStyleFactory(new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
    public final FilterFactory2 FILTER_FACTORY = (FilterFactory2)FactoryFinder.getFilterFactory(
                            new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));

    private static final Collection<StyleProviderService> SERVICES;
    static {
        final List<StyleProviderService> cache = new ArrayList<>();
        final ServiceLoader<StyleProviderService> loader = ServiceLoader.load(StyleProviderService.class);
        for(final StyleProviderService service : loader){
            cache.add(service);
        }
        SERVICES = Collections.unmodifiableCollection(cache);
    }

    private static final StyleProviderProxy INSTANCE = new StyleProviderProxy();

    private StyleProviderProxy(){
        super(String.class, MutableStyle.class);
    }

    @Override
    public Collection<StyleProviderService> getServices() {
        return SERVICES;
    }

    /**
     * Returns the current instance of {@link StyleProviderProxy}.
     */
    public static StyleProviderProxy getInstance(){
        return INSTANCE;
    }

}
