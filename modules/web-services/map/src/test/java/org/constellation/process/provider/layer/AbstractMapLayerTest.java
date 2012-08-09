/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.provider.layer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.process.AbstractProcessTest;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.ProviderService;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opengis.feature.ComplexAttribute;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class AbstractMapLayerTest extends AbstractProcessTest {

    protected static File configDirectory;
    protected static URL EMPTY_CSV;
    // dataStore service
    protected static ProviderService DATASTORE_SERVICE;

    static {
        final Collection<LayerProviderService> availableLayerServices = LayerProviderProxy.getInstance().getServices();
        for (LayerProviderService tmpService : availableLayerServices) {
            if ("data-store".equals(tmpService.getName())) {
                DATASTORE_SERVICE = tmpService;
            }
        }
    }

    public AbstractMapLayerTest(final String str) {
        super(str);
    }

    @BeforeClass
    public static void initFolder() throws MalformedURLException {

        configDirectory = new File("AbstractMapLayerTest");

        if (configDirectory.exists()) {
            FileUtilities.deleteDirectory(configDirectory);
        }

        configDirectory.mkdir();
        File providerDirectory = new File(configDirectory, "provider");
        providerDirectory.mkdir();
        File datastore = new File(providerDirectory, "data-store.xml");
        ConfigDirectory.setConfigDirectory(configDirectory);

        File csv = new File(configDirectory, "file.csv");
        EMPTY_CSV = csv.toURI().toURL();

    }

    @AfterClass
    public static void destroyFolder() {
        FileUtilities.deleteDirectory(configDirectory);
        ConfigDirectory.setConfigDirectory(null);
    }

    /**
     * Create a CSV provider for test purpose.
     *
     * @param sercice
     * @param providerID
     * @return
     * @throws MalformedURLException
     */
    protected static ParameterValueGroup buildCSVProvider(final ProviderService sercice, final String providerID, final boolean loadAll,
            final URL url, final String layerName) throws MalformedURLException {

        ParameterDescriptorGroup desc = sercice.getServiceDescriptor();

        if (desc != null) {
            final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) desc.descriptor("source");
            final ParameterValueGroup sourceValue = sourceDesc.createValue();
            sourceValue.parameter("id").setValue(providerID);
            sourceValue.parameter("load_all").setValue(loadAll);

            final ParameterValueGroup choiceValue = sourceValue.groups("choice").get(0);
            final ParameterValueGroup csvValue = (ParameterValueGroup) choiceValue.addGroup("CSVParameters");
            csvValue.parameter("identifier").setValue("csv");
            csvValue.parameter("url").setValue(url);
            csvValue.parameter("namespace").setValue(null);
            csvValue.parameter("separator").setValue(new Character(';'));

            if (layerName != null) {
                final ParameterValueGroup layerValue = sourceValue.addGroup("Layer");
                layerValue.parameter("name").setValue(layerName);
            }

            return sourceValue;
        } else {
            //error
            return null;
        }
    }

    /**
     * Register a provider.
     * @param providerSource
     */
    protected static void addProvider(ParameterValueGroup providerSource) {
        LayerProviderProxy.getInstance().createProvider((LayerProviderService) DATASTORE_SERVICE, providerSource);
    }

    /**
     * Un-register a provider
     * @param id
     */
    protected static void removeProvider(String id) {

        LayerProvider provider = null;
        for (LayerProvider p : LayerProviderProxy.getInstance().getProviders()) {
            if (p.getId().equals(id)) {
            }
        }
        LayerProviderProxy.getInstance().removeProvider(provider);
    }

    /**
     * Build only a ParameterValueGroup for one layer.
     * @param sercice
     * @param name
     * @return
     */
    protected ParameterValueGroup buildLayer(final ProviderService sercice, final String name) {

        ParameterDescriptorGroup desc = sercice.getServiceDescriptor();

        if (desc != null) {

            final ParameterDescriptorGroup sourceDescriptor = (ParameterDescriptorGroup) desc.descriptor("source");

            final ParameterDescriptorGroup layerDescriptor = (ParameterDescriptorGroup) sourceDescriptor.descriptor("Layer");
            ComplexAttribute layer = FeatureUtilities.toFeature(layerDescriptor.createValue());
            layer.getProperty("name").setValue(name);

            return FeatureUtilities.toParameter(layer, layerDescriptor);
        } else {
            //error
            return null;
        }
    }

}