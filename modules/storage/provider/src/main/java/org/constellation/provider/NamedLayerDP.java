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
package org.constellation.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.constellation.provider.postgis.PostGisNamedLayerDP;
import org.constellation.provider.postgrid.PostGridNamedLayerDP;
import org.constellation.provider.shapefile.ShapeFileNamedLayerDP;
import org.geotools.map.ElevationModel;

/**
 * Main data provider for MapLayer objects. This class act as a proxy for 
 * different kind of data sources, postgrid, shapefile ...
 * 
 * @author Johann Sorel (Geomatys)
 */
public class NamedLayerDP implements LayerDataProvider{

    private static NamedLayerDP instance = null;
    
    private final Collection<LayerDataProvider> dps = new ArrayList<LayerDataProvider>();
    
    
    private NamedLayerDP(){
        loadDP();
    }
    
    /**
     * {@inheritDoc }
     */
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<String>();
        for(DataProvider<String,LayerDetails> dp : dps){
            keys.addAll( dp.getKeys() );
        }
        return keys;
    }

    /**
     * {@inheritDoc }
     */
    public boolean contains(String key) {
        for(DataProvider<String,LayerDetails> dp : dps){
            if(dp.contains(key)) return true;
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    public LayerDetails get(String key) {
        for(LayerDataProvider dp : dps){
            LayerDetails layer = dp.get(key);
            if(layer != null) return layer;
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    public List<String> getFavoriteStyles(String layerName) {
        List<String> styles = new ArrayList<String>();
        for(LayerDataProvider dp : dps){
            List<String> sts = dp.get(layerName).getFavoriteStyles();
            styles.addAll(sts);
        }
        return styles;
    }

    @Override
    public ElevationModel getElevationModel(String name) {
        for(LayerDataProvider dp : dps){
            ElevationModel model = dp.getElevationModel(name);
            if(model != null) return model;
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    public void reload() {
        for(DataProvider<String,LayerDetails> dp : dps){
            dp.dispose();
        }
        dps.clear();
        loadDP();
    }

    private void loadDP(){
        //load shapefile dataproviders
        dps.addAll( ShapeFileNamedLayerDP.loadProviders() );
        
        //load postgis data providers
        dps.addAll( PostGisNamedLayerDP.loadProviders() );
        
        //load postgrid single DP
        dps.add(PostGridNamedLayerDP.getDefault());
    }
    
    /**
     * {@inheritDoc }
     */
    public void dispose() {
        for(DataProvider<String,LayerDetails> dp : dps){
            dp.dispose();
        }
        dps.clear();
    }
    
    public static NamedLayerDP getInstance(){
        if(instance == null){
            instance = new NamedLayerDP();
        }
        
        return instance;
    }

}