/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotoolkit.util.StringUtilities;

// Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Legal Guilhem (Geomatys)
 */
public class UtilTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void md5EncoderTest() throws Exception {

        String unencoded = "adminadmin";
        String result = StringUtilities.MD5encode(unencoded);
        String expresult = "f6fdffe48c908deb0f4c3bd36c032e72";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void cleanSpecialCharacterTest() throws Exception {

        String dirty = "lé oiseaux chantè à l'aube OLÉÉÉÉÉÉÉ";
        String result = StringUtilities.cleanSpecialCharacter(dirty);
        String expresult = "le oiseaux chante a l'aube OLEEEEEEE";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void transformCodeNameTest() throws Exception {

        String dirty = "MISSING_PARAMETER_VALUE";
        String result = StringUtilities.transformCodeName(dirty);
        String expresult = "MissingParameterValue";
        assertEquals(expresult, result);

        dirty = "INVALID_PARAMETER_VALUE";
        result = StringUtilities.transformCodeName(dirty);
        expresult = "InvalidParameterValue";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void removePrefixTest() throws Exception {

        String dirty = "ns2:what_ever";
        String result = StringUtilities.removePrefix(dirty);
        String expresult = "what_ever";
        assertEquals(expresult, result);

        dirty = "csw:GetRecord";
        result = StringUtilities.removePrefix(dirty);
        expresult = "GetRecord";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void cleanStringsTest() throws Exception {

        List<String> dirtys = new ArrayList<String>();
        dirtys.add("\t blabla              truc machin");
        dirtys.add("  boouu         \n   tc   \n mach");
        dirtys.add("                                 bcbcbcbcbcbcbcbcbc\n");

        List<String> expResults = new ArrayList<String>();
        expResults.add("blablatrucmachin");
        expResults.add("boouutcmach");
        expResults.add("bcbcbcbcbcbcbcbcbc");

        List<String> results = StringUtilities.cleanStrings(dirtys);
        assertEquals(expResults, results);

    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void matchesStringfromListTest() throws Exception {
        List<String> dirtys = new ArrayList<String>();
        dirtys.add("whatever");
        dirtys.add("SOMeTHING");
        dirtys.add("oTher");

        assertTrue(StringUtilities.matchesStringfromList(dirtys, "something"));

        dirtys = new ArrayList<String>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertTrue(StringUtilities.matchesStringfromList(dirtys, "something"));

        dirtys = new ArrayList<String>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertTrue(StringUtilities.matchesStringfromList(dirtys, "othe"));

        dirtys = new ArrayList<String>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertFalse(StringUtilities.matchesStringfromList(dirtys, "whateveri"));

    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void firstToUpperTest() throws Exception {

        String tmp = "hola";
        assertEquals("Hola", StringUtilities.firstToUpper(tmp));

        tmp = "Bonjour";
        assertEquals("Bonjour", StringUtilities.firstToUpper(tmp));

        tmp = "STUFF";
        assertEquals("STUFF", StringUtilities.firstToUpper(tmp));

        tmp = "sTUFF";
        assertEquals("STUFF", StringUtilities.firstToUpper(tmp));

    }

     /**
     * @throws java.lang.Exception
     */
    @Test
    public void replacePrefixTest() throws Exception {

        String tmp = "<ns2:Mark1>something<ns2:Mark1>" + '\n' +
                     "<ns2:Mark2>otherthing<ns2:Mark2>";

        String result = StringUtilities.replacePrefix(tmp, "Mark1", "csw");

        String expResult = "<csw:Mark1>something<csw:Mark1>" + '\n' +
                           "<ns2:Mark2>otherthing<ns2:Mark2>";

        assertEquals(expResult, result);

        result = StringUtilities.replacePrefix(tmp, "Mark3", "csw");

        assertEquals(tmp, result);

        tmp = "<ns2:Mark1>something<ns2:Mark1>" + '\n' +
              "<ns2:Mark2>otherthing<ns2:Mark2>"+ '\n' +
              "<ns2:Mark1>stuff<ns2:Mark1>";

        expResult = "<csw:Mark1>something<csw:Mark1>" + '\n' +
                    "<ns2:Mark2>otherthing<ns2:Mark2>" + '\n' +
                    "<csw:Mark1>stuff<csw:Mark1>";

        result = StringUtilities.replacePrefix(tmp, "Mark1", "csw");
        assertEquals(expResult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void sortStringListTest() throws Exception {
        String s1 = "bonjour";
        String s2 = "banjo";
        String s3 = "zebre";
        String s4 = "alabama";
        String s5 = "horrible";
        List<String> toSort = new ArrayList<String>();
        toSort.add(s1);
        toSort.add(s2);
        toSort.add(s3);
        toSort.add(s4);
        toSort.add(s5);

        Collections.sort(toSort);

        List<String> expResult = new ArrayList<String>();
        expResult.add(s4);
        expResult.add(s2);
        expResult.add(s1);
        expResult.add(s5);
        expResult.add(s3);

        assertEquals(expResult, toSort);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void toCommaSeparatedValuesTest() throws Exception {
        List<String> l = new ArrayList<String>();
        l.add("par");
        l.add("le");
        l.add("pouvoir");
        l.add("de");
        l.add("la");
        l.add("lune");

        String result    = StringUtilities.toCommaSeparatedValues(l);
        String expResult = "par,le,pouvoir,de,la,lune";
        assertEquals(expResult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void toStringListTest() throws Exception {
        List<String> result    = StringUtilities.toStringList("par,le,pouvoir,de,la,lune");
        List<String> expResult = new ArrayList<String>();
        expResult.add("par");
        expResult.add("le");
        expResult.add("pouvoir");
        expResult.add("de");
        expResult.add("la");
        expResult.add("lune");
        assertEquals(expResult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void ContainsMatchTest() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("par");
        list.add("le tres grand ");
        list.add("pouvoir magique ");
        list.add("de");
        list.add("la");
        list.add("super lune");

        assertTrue(StringUtilities.matchesStringfromList(list, "magique"));
        assertTrue(StringUtilities.matchesStringfromList(list, "super"));
        assertTrue(StringUtilities.matchesStringfromList(list, "tres grand"));
        assertFalse(StringUtilities.matchesStringfromList(list, "boulette"));
        assertFalse(StringUtilities.matchesStringfromList(list, "petit"));
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Test
    public void ContainsIgnoreCaseTest() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("par");
        list.add("le tres grand ");
        list.add("pouvoir magique ");
        list.add("de");
        list.add("la");
        list.add("super lune");

        assertTrue(StringUtilities.containsIgnoreCase(list, "PAR"));
        assertTrue(StringUtilities.containsIgnoreCase(list, "Le TrEs GrAnD "));
        assertTrue(StringUtilities.containsIgnoreCase(list, "super lune"));
        assertFalse(StringUtilities.containsIgnoreCase(list, "pouvoir"));
        assertFalse(StringUtilities.containsIgnoreCase(list, "petit"));
        assertFalse(StringUtilities.containsIgnoreCase(list, "GRAND"));
    }


}
