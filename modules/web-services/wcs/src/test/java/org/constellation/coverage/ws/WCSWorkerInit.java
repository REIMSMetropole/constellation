/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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
package org.constellation.coverage.ws;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.constellation.provider.configuration.Configurator;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;

/**
 * Initializes a {@link WCSWorker} for testing GetCapabilities, DescribeCoverage and GetCoverage
 * requests. Ensures that a PostGRID data preconfigured is handled by the {@link WCSWorker}.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.5
 */
public class WCSWorkerInit extends CoverageSQLTestCase {

    /**
     * The layer to test.
     */
    protected static final String LAYER_TEST = "SST_tests";

    protected static WCSWorker WORKER;

    /**
     * Initialisation of the worker and the PostGRID data provider before launching
     * the different tests.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

        ConfigurationEngine.setupTestEnvironement("WCSWorkerInit");
        
        final List<Source> sources = Arrays.asList(new Source("coverageTestSrc", true, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext config = new LayerContext(layers);
        config.getCustomParameters().put("shiroAccessible", "false");

        ConfigurationEngine.storeConfiguration("WCS", "default", config);
        ConfigurationEngine.storeConfiguration("WCS", "test", config);

        final Configurator configurator = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                final ParameterValueGroup config = service.getServiceDescriptor().createValue();

                if("coverage-sql".equals(service.getName())){
                    // Defines a PostGrid data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(COVERAGESQL_DESCRIPTOR,source);
                    srcconfig.parameter(URL_DESCRIPTOR.getName().getCode()).setValue("jdbc:postgresql://flupke.geomatys.com/coverages-test");
                    srcconfig.parameter(PASSWORD_DESCRIPTOR.getName().getCode()).setValue("test");
                    final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
                    srcconfig.parameter(ROOT_DIRECTORY_DESCRIPTOR.getName().getCode()).setValue(rootDir);
                    srcconfig.parameter(USER_DESCRIPTOR.getName().getCode()).setValue("test");
                    srcconfig.parameter(SCHEMA_DESCRIPTOR.getName().getCode()).setValue("coverages");
                    srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("coverageTestSrc");
                }

                return config;
            }

            @Override
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        LayerProviderProxy.getInstance().setConfigurator(configurator);


        WORKER = new DefaultWCSWorker("default");
        // Default instanciation of the worker' servlet context and uri context.
        WORKER.setServiceUrl("http://localhost:9090");

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigurationEngine.shutdownTestEnvironement("WCSWorkerInit");
        LayerProviderProxy.getInstance().setConfigurator(Configurator.DEFAULT);
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }
}
