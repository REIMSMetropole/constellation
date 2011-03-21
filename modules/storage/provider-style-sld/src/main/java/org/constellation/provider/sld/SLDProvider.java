/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.provider.sld;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBException;

import org.constellation.provider.AbstractStyleProvider;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.sld.MutableLayer;
import org.geotoolkit.sld.MutableLayerStyle;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.MutableUserLayer;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.SymbologyEncoding;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.collection.Cache;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.util.logging.Logging;

import org.opengis.util.FactoryException;

/**
 * Style provider. index and cache MutableStyle within the given folder.
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class SLDProvider extends AbstractStyleProvider{

    public static final String KEY_FOLDER_PATH = "path";

    private static final Logger LOGGER = Logging.getLogger("org.constellation.provider.sld");
    private static final MutableStyleFactory SF = (MutableStyleFactory)FactoryFinder.getStyleFactory(
                            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
    private static final Collection<String> MASKS = new ArrayList<String>();

    static{
        MASKS.add(".xml");
        MASKS.add(".sld");
    }
    
    private final XMLUtilities sldParser = new XMLUtilities();
    private final File folder;
    private final Map<String,File> index = new HashMap<String,File>();
    private final Cache<String,MutableStyle> cache = new Cache<String, MutableStyle>(20, 20, true);
    
    
    protected SLDProvider(final SLDProviderService service,final ProviderSource source){
        super(service,source);
        folder = new File(source.parameters.get(KEY_FOLDER_PATH));

        if(folder == null || !folder.exists() || !folder.isDirectory()){
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }
        
        visit(folder);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<String> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MutableStyle get(final String key) {

        MutableStyle value = cache.peek(key);
        if (value == null) {
            final Cache.Handler<MutableStyle> handler = cache.lock(key);
            try {
                value = handler.peek();
                if (value == null) {
                    final File f = index.get(key);
                    if(f != null){
                        final String baseErrorMsg = "[PROVIDER]> SLD Style ";
                        //try SLD 1.1
                        try {
                            final MutableStyledLayerDescriptor sld = sldParser.readSLD(f, StyledLayerDescriptor.V_1_1_0);
                            value = getFirstStyle(sld);
                            if(value != null){
                                LOGGER.log(Level.FINE, baseErrorMsg + key + " is an SLD 1.1.0");
                                return value;
                            }
                        } catch (JAXBException ex) { /* dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try SLD 1.0
                        try {
                            final MutableStyledLayerDescriptor sld = sldParser.readSLD(f, StyledLayerDescriptor.V_1_0_0);
                            value = getFirstStyle(sld);
                            if(value != null){
                                LOGGER.log(Level.FINE, baseErrorMsg + key + " is an SLD 1.0.0");
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try UserStyle SLD 1.1
                        try {
                            value = sldParser.readStyle(f, SymbologyEncoding.V_1_1_0);
                            if(value != null){
                                LOGGER.log(Level.FINE, baseErrorMsg + key + " is a UserStyle SLD 1.1.0");
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try UserStyle SLD 1.0
                        try {
                            value = sldParser.readStyle(f, SymbologyEncoding.SLD_1_0_0);
                            if(value != null){
                                LOGGER.log(Level.FINE, baseErrorMsg + key + " is a UserStyle SLD 1.0.0");
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try FeatureTypeStyle SE 1.1
                        try {
                            final MutableFeatureTypeStyle fts = sldParser.readFeatureTypeStyle(f, SymbologyEncoding.V_1_1_0);
                            value = SF.style();
                            value.featureTypeStyles().add(fts);
                            if(value != null){
                                LOGGER.log(Level.FINE, baseErrorMsg + key + " is FeatureTypeStyle SE 1.1");
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        //try FeatureTypeStyle SLD 1.0
                        try {
                            final MutableFeatureTypeStyle fts = sldParser.readFeatureTypeStyle(f, SymbologyEncoding.SLD_1_0_0);
                            value = SF.style();
                            value.featureTypeStyles().add(fts);
                            if(value != null){
                                LOGGER.log(Level.FINE, baseErrorMsg + key + " is an FeatureTypeStyle SLD 1.0");
                                return value;
                            }
                        } catch (JAXBException ex) { /*dont log*/ }
                        catch (FactoryException ex) { /* dont log*/ }

                        LOGGER.log(Level.WARNING, baseErrorMsg + key + " could not be parsed");
                    }
                }
            } finally {
                handler.putAndUnlock(value);
            }
        }

        return value;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
            index.clear();
            cache.clear();
            visit(folder);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            index.clear();
            cache.clear();
        }
    }
        
    private void visit(final File file) {

        if (file.isDirectory()) {
            final File[] list = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    visit(list[i]);
                }
            }
        }else{
            test(file);
        }
    }
    
    private void test(final File candidate){
        if(candidate.isFile()){
            final String fullName = candidate.getName();
            final String lowerCase = fullName.toLowerCase();

            if(lowerCase.startsWith(".")) return;

            for(final String mask : MASKS){
                if(lowerCase.endsWith(mask)){
                    final String name = fullName.substring(0, fullName.length()-4);
                    index.put(name, candidate);
                }
            }
        }
    }
    
    private static MutableStyle getFirstStyle(final MutableStyledLayerDescriptor sld){
        if(sld == null) return null;
        for(final MutableLayer layer : sld.layers()){
            if(layer instanceof MutableNamedLayer){
                final MutableNamedLayer mnl = (MutableNamedLayer) layer;
                for(final MutableLayerStyle stl : mnl.styles()){
                    if(stl instanceof MutableStyle){
                        return (MutableStyle) stl;
                    }
                }
            }else if(layer instanceof MutableUserLayer){
                final MutableUserLayer mnl = (MutableUserLayer) layer;
                for(final MutableStyle stl : mnl.styles()){
                    return stl;
                }
            }
        }
        return null;
    }

}
