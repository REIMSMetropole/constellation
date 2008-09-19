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
package org.constellation.provider.postgrid;

import java.util.Date;
import java.util.SortedSet;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.web.Service;
import org.constellation.provider.LayerDetails;

import org.geotools.map.MapLayer;
import org.geotools.style.MutableStyle;
import org.geotools.util.MeasurementRange;

import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 *
 * @version $Id$
 * @author Cédric Briançon
 */
class PostGridLayerDetails implements LayerDetails {
    /**
     * The database connection.
     */
    private final Database database;

    /**
     * Current layer to consider.
     */
    private final Layer layer;

    /**
     * Stores information about a {@linkplain Layer layer} in a {@code PostGRID}
     * {@linkplain Database database}.
     *
     * @param database The database connection.
     * @param layer The layer to consider in the database.
     */
    PostGridLayerDetails(final Database database, final Layer layer) {
        this.database = database;
        this.layer = layer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapLayer getMapLayer() {
        return new PostGridMapLayer(database, layer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapLayer getMapLayer(MutableStyle style) {
        return new PostGridMapLayer(database, layer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return layer.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQueryable(Service service) {
        return layer.isQueryable(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        return layer.getGeographicBoundingBox();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        return layer.getAvailableTimes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        return layer.getAvailableElevations();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        return layer.getSampleValueRanges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemarks() {
        return layer.getRemarks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getThematic() {
        return layer.getThematic();
    }
}
