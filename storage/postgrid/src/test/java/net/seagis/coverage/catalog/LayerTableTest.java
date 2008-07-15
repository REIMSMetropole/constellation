/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.catalog;

import java.util.Set;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.sql.SQLException;

import org.geotools.util.NumberRange;
import net.seagis.catalog.TableTest;
import net.seagis.catalog.CatalogException;

import org.junit.*;


/**
 * Tests {@link LayerTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LayerTableTest extends TableTest {
    /**
     * The name of the layer to be tested.
     */
    public static final String SAMPLE_NAME = "SST (Monde - hebdomadaires)";

    /**
     * The name of the NetCDF layer to be tested.
     */
    public static final String NETCDF_NAME = "AO_Coriolis_(Temp)";

    /**
     * The name of a NetCDF layer with two bands to be tested.
     */
    public static final String TWO_BANDS_NAME = "Mars2D_(UV)";

    /**
     * The start time, end time, and a sample time between them.
     */
    public static final Date START_TIME, END_TIME, SAMPLE_TIME;
    static {
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            START_TIME  = format.parse("1986-01-05");
            SAMPLE_TIME = format.parse("1986-01-13");
            END_TIME    = format.parse("1986-01-20");
        } catch (ParseException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * {@link #SAMPLE_TIME} as a text.
     */
    public static final String SAMPLE_TIME_AS_TEXT = "1986-01-20";

    /**
     * A time in the NetCDF layer.
     */
    public static final String NETCDF_TIME_AS_TEXT = "2007-06-13";

    /**
     * Tests the {@link LayerTableTest#getIdentifiers} method.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testIdentifiers() throws CatalogException, SQLException {
        final LayerTable table = new LayerTable(database);
        final Set<String> identifiers = table.getIdentifiers();
        assertTrue(identifiers.contains(SAMPLE_NAME));
        assertTrue(identifiers.contains(NETCDF_NAME));
    }

    /**
     * Tests the {@link LayerTableTest#getEntry} and @link LayerTableTest#getEntries} methods.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testSelectAndList() throws CatalogException, SQLException {
        final LayerTable table = new LayerTable(database);
        table.setTimeRange(START_TIME, END_TIME);
        final Layer entry = table.getEntry(SAMPLE_NAME);
        assertEquals(SAMPLE_NAME, entry.getName());
        assertSame("Should be cached.", entry, table.getEntry(SAMPLE_NAME));
        assertEquals(START_TIME, entry.getTimeRange().getMinValue());
        assertEquals(END_TIME,   entry.getTimeRange().getMaxValue());

        final Set<Layer> entries = table.getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.contains(entry));

        final Set<Date> availableTimes = entry.getAvailableTimes();
        assertEquals(3, availableTimes.size());
        final Set<CoverageReference> references = entry.getCoverageReferences();
        assertEquals(3, references.size());
        final CoverageReference reference = entry.getCoverageReference(SAMPLE_TIME, null);
        assertTrue(references.contains(reference));
        assertSame("Should be cached.", availableTimes, entry.getAvailableTimes());

        final NumberRange[] validRanges = entry.getSampleValueRanges();
        assertNotNull(validRanges);
        assertEquals(1, validRanges.length);
        assertEquals(-2.85, validRanges[0].getMinimum(true), 1E-8);
        assertEquals(35.25, validRanges[0].getMaximum(true), 1E-8);
    }

    /**
     * Tests the layer for NetCDF images.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws ParseException   Should never happen in normal test execution.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testNetCDF() throws CatalogException, SQLException, ParseException {
        /*
         * Note: for the test above, we really want to invoke 'table.getEntry' immediately after
         * construction; we don't want to invoke 'setTimeRange' before. In some previous version,
         * we had a bug when this method was invoked early without explicit time range.
         */
        final LayerTable table = new LayerTable(database);
        final Layer entry = table.getEntry(NETCDF_NAME);
        final Set<CoverageReference> references = entry.getCoverageReferences();
        assertEquals(3, references.size());

        final NumberRange[] validRanges = entry.getSampleValueRanges();
        assertNotNull(validRanges);
        assertEquals(1, validRanges.length);
        assertEquals(-3.0, validRanges[0].getMinimum(true), 1E-8);
        assertEquals(40.0, validRanges[0].getMaximum(true), 1E-8);

        final BufferedImage legend = entry.getLegend(new Dimension(400, 30));
        assertNotNull(legend);
        assertEquals(400, legend.getWidth());
        assertEquals( 30, legend.getHeight());
    }

    /**
     * Tests the layer for NetCDF images with two bands.
     *
     * @throws SQLException     If the test can't connect to the database.
     * @throws ParseException   Should never happen in normal test execution.
     * @throws CatalogException Should never happen in normal test execution.
     */
    @Test
    public void testTwoBands() throws CatalogException, SQLException, ParseException {
        final LayerTable table = new LayerTable(database);
        final Layer entry = table.getEntry(TWO_BANDS_NAME);
        final CoverageReference reference = entry.getCoverageReference();
        assertEquals(2, reference.getSampleDimensions().length);
    }
}
