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
package org.constellation.ws.embedded;

// JUnit dependencies
import org.constellation.configuration.ExceptionReport;
import javax.xml.bind.Marshaller;
import java.io.File;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.xml.MarshallerPool;
import javax.xml.bind.JAXBException;
import java.net.URLConnection;
import java.net.URL;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ServiceReport;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurationRequestTest extends AbstractTestRequest {
    
    @BeforeClass
    public static void initPool() throws JAXBException {
        // Get the list of layers
        pool = new MarshallerPool("org.constellation.configuration:"
                                + "org.constellation.generic.database:"
                                + "org.geotoolkit.ows.xml.v110:"
                                + "org.geotoolkit.csw.xml.v202:"
                                + "org.geotoolkit.internal.jaxb.geometry:"
                                + "org.geotoolkit.ows.xml.v100");
    }
    
    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }
    
    @Test
    public void testRestart() throws Exception {
     
        URL niUrl = new URL("http://localhost:9090/configuration?request=restart");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "services succefully restarted");
        assertEquals(expResult, obj);
    }
    
    @Test
    public void testDownloadFile() throws Exception {

        URL niUrl = new URL("http://localhost:9090/configuration?request=download");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);
        ExceptionReport expResult = new ExceptionReport("Download operation not implemented",  
                                                         StringUtilities.transformCodeName(OWSExceptionCode.OPERATION_NOT_SUPPORTED.name()));
        assertEquals(expResult, obj);
    }
    
    @Test
    public void testCSWRefreshIndex() throws Exception {

        /*
         * try to get a missing parameter error
         */
        URL niUrl = new URL("http://localhost:9090/configuration?request=refreshIndex");
        
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);
        
        assertTrue(obj instanceof ExceptionReport);
        ExceptionReport exception = new ExceptionReport("The parameter ID must be specified",  
                                                         OWSExceptionCode.MISSING_PARAMETER_VALUE.name());
        assertEquals(exception, obj);
        
        
        // first we make a getRecords request to count the number of record
        niUrl = new URL("http://localhost:9090/csw/default?request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");
        
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);
        
        assertTrue(obj instanceof GetRecordsResponseType);
        GetRecordsResponseType response = (GetRecordsResponseType) obj;
        
        assertEquals(12, response.getSearchResults().getNumberOfRecordsMatched());
        
        // build 2 new metadata file
        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("urn_test00"));
        File f = new File(ConfigDirectory.getConfigDirectory(), "CSW/default/data/urn_test00.xml");
        RecordType record2 = new RecordType();
        record2.setIdentifier(new SimpleLiteral("urn_test01"));
        File f2 = new File(ConfigDirectory.getConfigDirectory(), "CSW/default/data/urn_test01.xml");
        
        
        Marshaller m = pool.acquireMarshaller();
        m.marshal(record, f);
        m.marshal(record2, f2);
        pool.release(m);
        
        
        niUrl = new URL("http://localhost:9090/configuration?request=refreshIndex&id=default");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "CSW index succefully recreated");
        assertEquals(expResult, obj);
        
        niUrl = new URL("http://localhost:9090/csw/default?request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");
        
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);
        
        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;
        
        assertEquals(14, response.getSearchResults().getNumberOfRecordsMatched());
    }
    
    @Test
    public void testCSWAddToIndex() throws Exception {
        
        // first we make a getRecords request to count the number of record
        URL niUrl = new URL("http://localhost:9090/csw/default?request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");
        
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);
        
        assertTrue(obj instanceof GetRecordsResponseType);
        GetRecordsResponseType response = (GetRecordsResponseType) obj;
        
        assertEquals(14, response.getSearchResults().getNumberOfRecordsMatched());
        
        // build a new metadata file
        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("urn_test"));
        File f = new File(ConfigDirectory.getConfigDirectory(), "CSW/default/data/urn_test.xml");
        
        Marshaller m = pool.acquireMarshaller();
        m.marshal(record, f);
        pool.release(m);
        
        // add a metadata to the index
        niUrl = new URL("http://localhost:9090/configuration?request=addToIndex&id=default&identifiers=urn_test");

        // for a POST request
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);
        
        assertTrue(obj instanceof AcknowlegementType);
        AcknowlegementType expResult = new AcknowlegementType("Success",  "The specified record have been added to the CSW index");
        assertEquals(expResult, obj);
        
        
        //normally we don't have to restart the CSW TODO
        niUrl = new URL("http://localhost:9090/csw/admin?request=restart&id=default");
        conec = niUrl.openConnection();
        obj = unmarshallResponse(conec);
        
        
         // verify that the number of record have increased
        niUrl = new URL("http://localhost:9090/csw/default?request=getRecords&version=2.0.2&service=CSW&typenames=csw:Record");
        
        conec = niUrl.openConnection();

        obj = unmarshallResponse(conec);
        
        assertTrue(obj instanceof GetRecordsResponseType);
        response = (GetRecordsResponseType) obj;
        
        assertEquals(15, response.getSearchResults().getNumberOfRecordsMatched());
    }
    
    @Test
    public void testUpdateVocabularies() throws Exception {

        URL niUrl = new URL("http://localhost:9090/configuration?request=UpdateVocabularies");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);
        ExceptionReport expResult = new ExceptionReport("The method updateVocabularies is not supported by the current implementation.",  
                                                         StringUtilities.transformCodeName(OWSExceptionCode.OPERATION_NOT_SUPPORTED.name()));
        assertEquals(expResult, obj);
    }
    
    @Test
    public void testUpdateContacts() throws Exception {

        URL niUrl = new URL("http://localhost:9090/configuration?request=UpdateContacts");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);
        ExceptionReport expResult = new ExceptionReport("The method updateContacts is not supported by the current implementation.",  
                                                         StringUtilities.transformCodeName(OWSExceptionCode.OPERATION_NOT_SUPPORTED.name()));
        assertEquals(expResult, obj);
    }
    
    @Test
    public void testListAvailableService() throws Exception {
        URL niUrl = new URL("http://localhost:9090/configuration?request=ListAvailableService");


        // for a POST request
        URLConnection conec = niUrl.openConnection();

        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ServiceReport);
        final ServiceReport result = (ServiceReport) obj;
        assertTrue(result.getAvailableServices().contains("WMS"));
        assertTrue(result.getAvailableServices().contains("SOS"));
        assertTrue(result.getAvailableServices().contains("CSW"));
        assertTrue(result.getAvailableServices().contains("WCS"));
        assertTrue(result.getAvailableServices().contains("WFS"));
        
        assertEquals(result.getAvailableServices().toString(), 5, result.getAvailableServices().size());
        
        
    }
}
