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
package org.constellation.process.wms;

import java.io.File;
import java.net.MalformedURLException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.LayerContext;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.map.ws.DefaultWMSWorker;
import org.constellation.process.AbstractProcessTest;
import org.constellation.ws.WSEngine;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class WMSProcessTest extends AbstractProcessTest {

    protected static File configDirectory;

    public WMSProcessTest(final String str) {
        super(str);
    }

    @BeforeClass
    public static void initFolder() throws MalformedURLException {

        configDirectory = ConfigDirectory.getConfigDirectory();
        WSEngine.registerService("WMS", "REST");
        createInstance("instance1");
        createInstance("instance2");
        createInstance("instance3");
        createInstance("instance4");

    }

    @AfterClass
    public static void destroyFolder() {
        deleteInstance("instance1");
        deleteInstance("instance2");
        deleteInstance("instance3");
        deleteInstance("instance4");
        WSEngine.destroyInstances("WMS");
    }

    protected static void createInstance(String identifier) {
        final File wms = new File(configDirectory, "WMS");
        final File instance = new File(wms, identifier);
        instance.mkdir();
        
        final File configFile = new File(instance, "layerContext.xml");
        final LayerContext configuration = new LayerContext();
        Marshaller marshaller = null;
        try {
            marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            marshaller.marshal(configuration, configFile);

        } catch (JAXBException ex) {
            //
        } finally {
            if (marshaller != null) {
                GenericDatabaseMarshallerPool.getInstance().release(marshaller);
            }
        }
    }
    
    protected static void deleteInstance(String identifier) {
        final File wms = new File(configDirectory, "WMS");
        final File instance = new File(wms, identifier);
        FileUtilities.deleteDirectory(instance);
    }
    
    protected static void startInstance(String identifier) {
        final File wms = new File(configDirectory, "WMS");
        final File instance = new File(wms, identifier);
        final DefaultWMSWorker worker = new DefaultWMSWorker(identifier, instance);
        if (worker != null) {
            WSEngine.addServiceInstance("WMS", identifier, worker);
        }
    }
}
