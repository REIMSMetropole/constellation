/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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
package org.constellation.metadata.io.netcdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.CSWworkerTest;
import org.constellation.util.Util;
import org.constellation.ws.MimeType;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdType;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.xml.AnchoredMarshallerPool;

import static org.constellation.test.utils.MetadataUtilities.*;

// JUnit dependencies
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.util.ComparisonMode;
import static org.junit.Assert.*;
import org.junit.*;


/**
 *
 *  @author Guilhem Legal (Geomatys)
 */
public class NetCDFCSWWorkerTest extends CSWworkerTest {
    
     private static final File configDir =  new File("NCCSWWorkerTest");
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteTemporaryFile();

        if (configDir.exists()) {
            FileUtilities.deleteDirectory(configDir);
        }
        
        if (!configDir.exists()) {
            configDir.mkdir();

            //we write the data files
            File dataDirectory = new File(configDir, "data");
            dataDirectory.mkdir();
            writeDataFile(dataDirectory, "2005092200_sst_21-24.en.nc", "2005092200_sst_21-24.en");
            
            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            Automatic configuration = new Automatic("netcdf", dataDirectory.getPath());
            final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            marshaller.marshal(configuration, configFile);
            GenericDatabaseMarshallerPool.getInstance().release(marshaller);
        }

        pool = EBRIMMarshallerPool.getInstance();
        fillPoolAnchor((AnchoredMarshallerPool) pool);

        Unmarshaller u = pool.acquireUnmarshaller();
        pool.release(u);

        worker = new CSWworker("", configDir);
        worker.setLogLevel(Level.FINER);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteTemporaryFile();
    }

    public static void deleteTemporaryFile() {
        if (worker != null) {
            worker.destroy();
        }
        FileUtilities.deleteDirectory(configDir);
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    
    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getRecordByIdTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        /*
         *  TEST 1 : getRecordById with the first metadata in ISO mode.
         */
        GetRecordByIdType request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("2005092200_sst_21-24.en"));
        GetRecordByIdResponseType result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().isEmpty());
        assertTrue(result.getAny().size() == 1);
        Object obj = result.getAny().get(0);
        assertTrue(obj instanceof DefaultMetadata);

/*        Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(obj, new File("test.xml"));*/
        
        DefaultMetadata isoResult = (DefaultMetadata) obj;

        DefaultMetadata ExpResult1 = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/2005092200_sst_21-24.en.xml"));

        metadataEquals(ExpResult1, isoResult, ComparisonMode.APPROXIMATIVE);

        /*
         *  TEST 2 : getRecordById with the first metadata in DC mode (BRIEF).
         
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.BRIEF),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof BriefRecordType);

        BriefRecordType briefResult =  (BriefRecordType) obj;

        BriefRecordType expBriefResult1 =  ((JAXBElement<BriefRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1BDC.xml"))).getValue();

        assertEquals(expBriefResult1, briefResult);

        /*
         *  TEST 3 : getRecordById with the first metadata in DC mode (SUMMARY).
         
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        SummaryRecordType sumResult =  (SummaryRecordType) obj;

        SummaryRecordType expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
        assertEquals(expSumResult1, sumResult);

        /*
         *  TEST 4 : getRecordById with the first metadata in DC mode (FULL).
         
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof RecordType);

        RecordType recordResult = (RecordType) obj;

        RecordType expRecordResult1 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1FDC.xml"))).getValue();

        assertEquals(expRecordResult1.getFormat(), recordResult.getFormat());
        assertEquals(expRecordResult1, recordResult);

        /*
         *  TEST 5 : getRecordById with two metadata in DC mode (FULL).
         
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600","42292_9s_19900610041000"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 2);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof RecordType);
        RecordType recordResult1 = (RecordType) obj;

        obj = result.getAbstractRecord().get(1);
        assertTrue(obj instanceof RecordType);
        RecordType recordResult2 = (RecordType) obj;

        RecordType expRecordResult2 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2FDC.xml"))).getValue();

        assertEquals(expRecordResult1, recordResult1);
        assertEquals(expRecordResult2, recordResult2);

        /*
         *  TEST 6 : getRecordById with the first metadata with no outputSchema.
         
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                MimeType.APPLICATION_XML, null, Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        sumResult =  (SummaryRecordType) obj;

        expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
        assertEquals(expSumResult1, sumResult);

        /*
         *  TEST 7 : getRecordById with the first metadata with no outputSchema and no ElementSetName.
         
        request = new GetRecordByIdType("CSW", "2.0.2", null,
                MimeType.APPLICATION_XML, null, Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        sumResult =  (SummaryRecordType) obj;

        expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
        assertEquals(expSumResult1, sumResult);

       */
        pool.release(unmarshaller);
    }

    
    /**
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getRecordsTest() throws Exception {
        //
    }

    
   
    /**
     * Tests the getDomain method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getDomainTest() throws Exception {
        //
    }

    public static void writeDataFile(File dataDirectory, String resourceName, String identifier) throws IOException {

        final File dataFile;
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            final String windowsIdentifier = identifier.replace(':', '-');
            dataFile = new File(dataDirectory, windowsIdentifier + ".nc");
        } else {
            dataFile = new File(dataDirectory, identifier + ".nc");
        }
        FileOutputStream fw = new FileOutputStream(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/netcdf/" + resourceName);

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(buffer, 0, size);
        }
        in.close();
        fw.close();
    }
}