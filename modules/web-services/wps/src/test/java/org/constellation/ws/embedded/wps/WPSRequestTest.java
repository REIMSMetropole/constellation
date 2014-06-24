/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.ws.embedded.wps;

// JUnit dependencies
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.ProcessFactory;
import org.constellation.configuration.Processes;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.wps.ws.soap.WPSService;
import org.constellation.ws.embedded.AbstractGrizzlyServer;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.9
 */
@RunWith(SpringTestRunner.class)
public class WPSRequestTest  extends AbstractGrizzlyServer {

    private static final String WPS_GETCAPABILITIES ="request=GetCapabilities&service=WPS&version=1.0.0";

    private static final String WPS_GETCAPABILITIES2 ="request=GetCapabilities&service=WpS&version=1.0.0";

    @Inject
    private ServiceBusiness serviceBusiness;
    
    @PostConstruct
    public void initLayerList() {
        try {
            ConfigurationEngine.setupTestEnvironement("WPSRequestTest");
            
            final List<ProcessFactory> process = Arrays.asList(new ProcessFactory("jts", true));
            final Processes processes = new Processes(process);
            final ProcessContext config = new ProcessContext(processes);
            config.getCustomParameters().put("shiroAccessible", "false");
            
            serviceBusiness.create("WPS", "default", config, null, null);
            serviceBusiness.create("WPS", "test",    config, null, null);
            
            final Map<String, Object> map = new HashMap<>();
            map.put("wps", new WPSService());
            initServer(new String[] {
                "org.constellation.wps.ws.rs",
                "org.constellation.configuration.ws.rs",
                "org.constellation.ws.rs.provider"}, map);
            pool = WPSMarshallerPool.getInstance();
        } catch (Exception ex) {
            Logger.getLogger(WPSRequestTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void shutDown() {
        ConfigurationEngine.shutdownTestEnvironement("WPSRequestTest");
        finish();
    }

    /**
     * Ensures that a valid GetCapabilities request returns indeed a valid GetCapabilities
     */
    @Test
    public void testWPSGetCapabilities() throws Exception {

        waitForStart();
        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?" + WPS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WPSCapabilitiesType.
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue("was " + obj, obj instanceof WPSCapabilitiesType);

        WPSCapabilitiesType responseCaps = (WPSCapabilitiesType)obj;


        String currentUrl = responseCaps.getOperationsMetadata().getOperation("getCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?", currentUrl);

        try {
            getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wps/test?" + WPS_GETCAPABILITIES2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WPSCapabilitiesType.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WPSCapabilitiesType);

        responseCaps = (WPSCapabilitiesType)obj;

        currentUrl = responseCaps.getOperationsMetadata().getOperation("getCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wps/test?", currentUrl);


        try {
            getCapsUrl = new URL(WPS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WPSCapabilitiesType.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WPSCapabilitiesType);

        responseCaps = (WPSCapabilitiesType)obj;

        currentUrl = responseCaps.getOperationsMetadata().getOperation("getCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wps/default?", currentUrl);
    }

}
