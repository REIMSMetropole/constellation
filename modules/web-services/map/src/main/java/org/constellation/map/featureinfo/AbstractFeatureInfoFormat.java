/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
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
package org.constellation.map.featureinfo;

import org.constellation.configuration.GetFeatureInfoCfg;
import org.constellation.provider.LayerDetails;
import org.geotoolkit.display.SearchArea;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.GraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.display2d.service.*;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.ows.xml.GetFeatureInfo;
import org.opengis.display.primitive.Graphic;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Quentin Boileau (Geomatys)
 */
public abstract class AbstractFeatureInfoFormat implements FeatureInfoFormat {

    /**
     * Contains the values for all coverage layers requested.
     */
    protected final Map<String, List<ProjectedCoverage>> coverages = new HashMap<String, List<ProjectedCoverage>>();

    /**
     * Contains all features that cover the point requested, for feature layers.
     */
    protected final Map<String, java.util.List<ProjectedFeature>> features = new HashMap<String, java.util.List<ProjectedFeature>>();

    /**
     * GetFeatureInfo configuration.
     */
    private GetFeatureInfoCfg configuration;

    /**
     * MapContext Layers details
     */
    private List<LayerDetails> layersDetails;

    /**
     * {@inheritDoc}
     */
    @Override
    public GetFeatureInfoCfg getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(GetFeatureInfoCfg conf) {
        this.configuration = conf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LayerDetails> getLayersDetails() {
        return layersDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLayersDetails(List<LayerDetails> layers) {
        this.layersDetails = layers;
    }

    /**
     * Visit all intersected Graphic objects and call {link #nextProjectedFeature}
     * or {link #nextProjectedCoverage}.
     *
     *
     * @param sDef {@link org.geotoolkit.display2d.service.SceneDef}
     * @param vDef {@link org.geotoolkit.display2d.service.ViewDef}
     * @param cDef {@link org.geotoolkit.display2d.service.CanvasDef}
     * @param searchArea {@link java.awt.Rectangle} of the searching area
     * @param maxCandidat
     * @throws PortrayalException
     * @see #nextProjectedFeature(ProjectedFeature, RenderingContext2D, SearchAreaJ2D)
     * @see #nextProjectedCoverage(ProjectedCoverage, RenderingContext2D, SearchAreaJ2D)
     */
    protected void getCandidates(final SceneDef sDef, final ViewDef vDef, final CanvasDef cDef, final Rectangle searchArea,
                                 final Integer maxCandidat) throws PortrayalException {

        final VisitDef visitDef = new VisitDef();
        visitDef.setArea(searchArea);
        visitDef.setVisitor(new GraphicVisitor() {

            int idx = 0;
            @Override
            public void startVisit() {
            }

            @Override
            public void endVisit() {
            }

            @Override
            public void visit(Graphic graphic, RenderingContext context, SearchArea area) {
                if(graphic == null ) return;

                if(graphic instanceof ProjectedFeature){
                    nextProjectedFeature((ProjectedFeature) graphic, (RenderingContext2D)context, (SearchAreaJ2D)area);
                }else if(graphic instanceof ProjectedCoverage){
                    nextProjectedCoverage((ProjectedCoverage) graphic, (RenderingContext2D)context, (SearchAreaJ2D)area);
                }
                idx++;
            }

            @Override
            public boolean isStopRequested() {
                if (maxCandidat != null) {
                    return (idx == maxCandidat);
                } else {
                    return false;
                }
            }
        });

        DefaultPortrayalService.visit(cDef, sDef, vDef, visitDef);
    }

    /**
     * Store the {@link ProjectedFeature} in a list
     *
     * @param graphic {@link ProjectedFeature}
     * @param context rendering context
     * @param queryArea area of the search
     */
    protected void nextProjectedFeature(final ProjectedFeature graphic, final RenderingContext2D context,
                                        final SearchAreaJ2D queryArea) {

        final FeatureMapLayer layer = graphic.getLayer();
        final String layerName = layer.getName();
        List<ProjectedFeature> feat = features.get(layerName);
        if (feat == null) {
            feat = new ArrayList<ProjectedFeature>();
            features.put(layerName, feat);
        }
        feat.add(graphic);
    }

    /**
     * Store the {@link ProjectedCoverage} in a list
     *
     * @param graphic {@link ProjectedCoverage}
     * @param context rendering context
     * @param queryArea area of the search
     */
    protected void nextProjectedCoverage(final ProjectedCoverage graphic, final RenderingContext2D context,
                                         final SearchAreaJ2D queryArea) {

        final CoverageMapLayer layer = graphic.getLayer();
        final String layerName = layer.getName();
        List<ProjectedCoverage> cov = coverages.get(layerName);
        if (cov == null) {
            cov = new ArrayList<ProjectedCoverage>();
            coverages.put(layerName, cov);
        }
        cov.add(graphic);
    }

    /**
     * Extract the max feature count number from WMS {@link GetFeatureInfo} requests or null.
     *
     * @param gfi {@link GetFeatureInfo} request
     * @return max Feature count if WMS {@link GetFeatureInfo} or null otherwise.
     */
    protected Integer getFeatureCount(GetFeatureInfo gfi) {
        if (gfi != null && gfi instanceof org.geotoolkit.wms.xml.GetFeatureInfo) {
            org.geotoolkit.wms.xml.GetFeatureInfo wmsGFI = (org.geotoolkit.wms.xml.GetFeatureInfo) gfi;
            return wmsGFI.getFeatureCount();
        }
        return null;
    }

}