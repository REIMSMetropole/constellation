/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009 - 2010, Geomatys
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
package org.constellation.tile.ws;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Color;
import java.awt.BasicStroke;
import java.math.BigInteger;
import javax.measure.unit.Unit;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.Date;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;

import org.constellation.tile.visitor.GMLGraphicVisitor;
import org.constellation.tile.visitor.HTMLGraphicVisitor;
import org.constellation.tile.visitor.CSVGraphicVisitor;
import org.constellation.tile.visitor.TextGraphicVisitor;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.util.Util;
import org.constellation.provider.CoverageLayerDetails;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.register.RegisterException;
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.provider.LayerDetails;
import org.constellation.ws.MimeType;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.util.TimeParser;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.display2d.service.VisitDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.ext.DefaultBackgroundTemplate;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.ext.legend.DefaultLegendTemplate;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.PeriodUtilities;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.ServiceProvider;
import org.geotoolkit.ows.xml.v110.ServiceIdentification;
import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.wmts.xml.v100.TileMatrix;
import org.geotoolkit.wmts.xml.v100.DimensionNameValue;
import org.geotoolkit.wmts.xml.v100.ContentsType;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;
import org.geotoolkit.wmts.xml.v100.GetTile;
import org.geotoolkit.wmts.xml.v100.Themes;
import org.geotoolkit.wmts.xml.v100.TileMatrixSet;
import org.geotoolkit.wmts.xml.v100.TileMatrixSetLink;
import org.geotoolkit.wmts.xml.v100.Dimension;
import org.geotoolkit.wmts.xml.v100.LayerType;
import org.geotoolkit.wmts.xml.v100.Style;
import org.geotoolkit.wmts.xml.v100.LegendURL;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.coverage.Coverage;
import org.opengis.feature.type.Name;
import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 * Working part of the WMTS service.
 *
 * @todo Implements it.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
public class DefaultWMTSWorker extends AbstractWorker implements WMTSWorker {

    /**
     * The current MIME type of return
     */
    private String outputFormat;

    /**
     * A list of supported MIME type
     */
    private static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = Arrays.asList(MimeType.TEXT_XML,
                                                MimeType.APP_XML,
                                                MimeType.TEXT_PLAIN);
    }

    private static final LegendTemplate LEGEND_TEMPLATE = new DefaultLegendTemplate(
                    new DefaultBackgroundTemplate(
                        new BasicStroke(1), 
                        Color.LIGHT_GRAY,
                        Color.WHITE,
                        new Insets(4, 4, 4, 4),
                        10),
                        5,
                        new java.awt.Dimension(30, 24),
                        new Font("Arial", Font.PLAIN, 10),
                        false,
                        new Font("Arial", Font.BOLD, 12));

    /**
     * Instanciates the working class for a SOAP client, that do request on a SOAP PEP service.
     */
    public DefaultWMTSWorker(String id, File configurationDirectory) {
        super(id, configurationDirectory);
        LOGGER.info("WMTS Service running");
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WMTSMarshallerPool.getInstance();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities getCapabilities(GetCapabilities requestCapabilities) throws CstlServiceException {
        LOGGER.log(logLevel, "getCapabilities request processing\n");
        final long start = System.currentTimeMillis();

        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals("WMTS")) {
                throw new CstlServiceException("service must be \"WMTS\"!",
                                                 INVALID_PARAMETER_VALUE, "service");
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, "service");
        }
        final AcceptVersionsType versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains("1.0.0")){
                 throw new CstlServiceException("version available : 1.0.0",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }
        final AcceptFormatsType formats = requestCapabilities.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 ) {
            boolean found = false;
            for (String form: formats.getOutputFormat()) {
                if (ACCEPTED_OUTPUT_FORMATS.contains(form)) {
                    outputFormat = form;
                    found = true;
                }
            }
            if (!found) {
                throw new CstlServiceException("accepted format : text/xml, application/xml",
                                                 INVALID_PARAMETER_VALUE, "acceptFormats");
            }

        } else {
            this.outputFormat = MimeType.APP_XML;
        }

        //we prepare the response document
        Capabilities c           = null;
        ServiceIdentification si = null;
        ServiceProvider       sp = null;
        OperationsMetadata    om = null;
        ContentsType        cont = null;
        List<Themes>      themes = null;

        SectionsType sections = requestCapabilities.getSections();
        if (sections == null) {
            sections = new SectionsType(SectionsType.getExistingSections("1.1.1"));
        }

        // we load the skeleton capabilities
        Capabilities skeletonCapabilities;
        try {
            skeletonCapabilities = (Capabilities) getStaticCapabilitiesObject("1.0.0", "WMTS");
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        if (skeletonCapabilities == null) {
            throw new CstlServiceException("the service was unable to find the metadata for capabilities operation", NO_APPLICABLE_CODE);
        }

        //we enter the information for service identification.
        if (sections.containsSection("ServiceIdentification") || sections.containsSection("All")) {

            si = skeletonCapabilities.getServiceIdentification();
        }

        //we enter the information for service provider.
        if (sections.containsSection("ServiceProvider") || sections.containsSection("All")) {

            sp = skeletonCapabilities.getServiceProvider();
        }

        //we enter the operation Metadata
        if (sections.containsSection("OperationsMetadata") || sections.containsSection("All")) {

           om = WMTSConstant.OPERATIONS_METADATA;

           //we update the URL
           om.updateURL(getServiceUrl(), "WMTS");

        }

        if (sections.containsSection("Contents") || sections.containsSection("All")) {
            
            final List<LayerDetails> layerRefs = getAllLayerReferences();

            // Build the list of layers
            final List<LayerType> layers = new ArrayList<LayerType>();
            // and the list of matrix set
            final List<TileMatrixSet> tileSets = new ArrayList<TileMatrixSet>();

            for (LayerDetails layer : layerRefs){
                if (!layer.isQueryable(ServiceDef.Query.WMTS_ALL) || !(layer instanceof CoverageLayerDetails)) {
                    continue;
                }
                final CoverageLayerDetails coverageLayer = (CoverageLayerDetails) layer;
            
                /*
                 *  TODO
                 * code = CRS.lookupEpsgCode(inputLayer.getCoverageReference().getCoordinateReferenceSystem(), false);
                 */
                final GeographicBoundingBox inputGeoBox;
                try {
                    inputGeoBox = layer.getGeographicBoundingBox();
                } catch (DataStoreException exception) {
                    throw new CstlServiceException(exception, NO_APPLICABLE_CODE);
                }
            
                if (inputGeoBox == null) {
                    // The layer does not contain geometric information, we do not want this layer
                    // in the capabilities response.
                    continue;
                }

                // List of elevations, times and dim_range values.
                final List<Dimension> dimensions = new ArrayList<Dimension>();

                //the available date
                String defaut = null;
                Dimension dim;
                SortedSet<Date> dates = null;
                try {
                    dates = layer.getAvailableTimes();
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.INFO, "Error retrieving dates values for the layer :"+ layer.getName(), ex);
                    dates = null;
                }
                if (dates != null && !(dates.isEmpty())) {
                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    final PeriodUtilities periodFormatter = new PeriodUtilities(df);
                    defaut = df.format(dates.last());
                    dim = new Dimension("time", "ISO8601", defaut);

                    dim.setValue(periodFormatter.getDatesRespresentation(dates));
                    dimensions.add(dim);
                }

                //the available elevation
                defaut = null;
                SortedSet<Number> elevations = null;
                try {
                    elevations = layer.getAvailableElevations();
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.INFO, "Error retrieving elevation values for the layer :"+ layer.getName(), ex);
                    elevations = null;
                }
                if (elevations != null && !(elevations.isEmpty())) {
                    defaut = elevations.first().toString();
                    dim = new Dimension("elevation", "EPSG:5030", defaut);
                    final StringBuilder elevs = new StringBuilder();
                    for (final Iterator<Number> it = elevations.iterator(); it.hasNext();) {
                        final Number n = it.next();
                        elevs.append(n.toString());
                        if (it.hasNext()) {
                            elevs.append(',');
                        }
                    }
                    dim.setValue(elevs.toString());
                    dimensions.add(dim);
                }

                //the dimension range
                defaut = null;
                final MeasurementRange<?>[] ranges = layer.getSampleValueRanges();
                /* If the layer has only one sample dimension, then we can apply the dim_range
                 * parameter. Otherwise it can be a multiple sample dimensions layer, and we
                 * don't apply the dim_range.
                 */
                if (ranges != null && ranges.length == 1 && ranges[0] != null) {
                    final MeasurementRange<?> firstRange = ranges[0];
                    final double minRange = firstRange.getMinimum();
                    final double maxRange = firstRange.getMaximum();
                    defaut = minRange + "," + maxRange;
                    final Unit<?> u = firstRange.getUnits();
                    final String unit = (u != null) ? u.toString() : null;
                    dim = new Dimension("dim_range", unit, defaut, minRange + "," + maxRange);
                    dimensions.add(dim);
                }

                // LegendUrl generation
                //TODO: Use a StringBuilder or two
                final Name fullLayerName = layer.getName();
                final String layerName;
                if (fullLayerName.getNamespaceURI() != null) {
                    layerName = fullLayerName.getNamespaceURI() + ':' + fullLayerName.getLocalPart();
                } else {
                    layerName = fullLayerName.getLocalPart();
                }
                String url = null;
                final String beginLegendUrl = url + "wms?REQUEST=GetLegendGraphic&" +
                                                        "VERSION=1.1.1&" +
                                                        "FORMAT=";
                final String legendUrlGif = beginLegendUrl + MimeType.IMAGE_GIF + "&LAYER=" + layerName;
                final String legendUrlPng = beginLegendUrl + MimeType.IMAGE_PNG + "&LAYER=" + layerName;

            /*
             * TODO
             * Envelope inputBox = inputLayer.getCoverage().getEnvelope();
             */
            final BoundingBoxType outputBBox =
                    new BoundingBoxType("EPSG:4326", inputGeoBox.getWestBoundLongitude(),
                            inputGeoBox.getSouthBoundLatitude(), inputGeoBox.getEastBoundLongitude(),
                            inputGeoBox.getNorthBoundLatitude());

                // we build The Style part

                final List<String> stylesName = layer.getFavoriteStyles();
                final List<Style> styles = new ArrayList<Style>();
                if (stylesName != null && !stylesName.isEmpty()) {
                    // For each styles defined for the layer, get the dimension of the getLegendGraphic response.
                    for (String styleName : stylesName) {
                        final MutableStyle ms = StyleProviderProxy.getInstance().get(styleName);
                        final java.awt.Dimension dimLegend;
                        try {
                            dimLegend = layer.getPreferredLegendSize(LEGEND_TEMPLATE, ms);
                        } catch (PortrayalException ex) {
                            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                        }
                        final LegendURL legendURL1 = new LegendURL(MimeType.IMAGE_PNG,
                                BigInteger.valueOf(dimLegend.width), BigInteger.valueOf(dimLegend.height), 0.0, 0.0);
                        legendURL1.setHref(legendUrlPng);

                        final LegendURL legendURL2 = new LegendURL(MimeType.IMAGE_GIF,
                                BigInteger.valueOf(dimLegend.width), BigInteger.valueOf(dimLegend.height), 0.0, 0.0);
                        legendURL2.setHref(legendUrlGif);

                        final Style style = new Style(new CodeType(styleName), Arrays.asList(legendURL1, legendURL2));
                        styles.add(style);
                    }
                }

                final LayerType outputLayer = new LayerType(layerName, coverageLayer.getRemarks(), outputBBox, styles, dimensions);

            /**
             * Hard coded part
             */
            final TileMatrixSet outputMatrixSet = DefaultTileExample.getTileMatrixSet(layerName);
            if (outputMatrixSet != null) {
                final TileMatrixSetLink tmsl = new TileMatrixSetLink();
                tmsl.setTileMatrixSet(layerName);
                outputLayer.getTileMatrixSetLink().add(tmsl);
                tileSets.add(outputMatrixSet);
            }
            layers.add(outputLayer);
            }
            
            cont = new ContentsType();
            cont.setLayers(layers);
            cont.setTileMatrixSet(tileSets);
            
        }
        
        if (sections.containsSection("Themes") || sections.containsSection("All")) {
            // TODO
            
            themes = new ArrayList<Themes>();
        }

        c = new Capabilities(si, sp, om, "1.0.0", null, cont, themes);

        LOGGER.log(logLevel, "getCapabilities processed in " + (System.currentTimeMillis() - start) + "ms.\n");
        return c;

        
    }

    private static List<LayerDetails> getAllLayerReferences() throws CstlServiceException {

        List<LayerDetails> layerRefs;
        try { // WE catch the exception from either service version
            layerRefs = Cstl.getRegister().getAllLayerReferences(ServiceDef.WMTS_1_0_0);

        } catch (RegisterException regex) {
            throw new CstlServiceException(regex, LAYER_NOT_DEFINED);
        }
        return layerRefs;
    }

    private static List<String> getRootDirectories() throws CstlServiceException {

        List<String> rootDirectories;
        try { // WE catch the exception from either service version
            rootDirectories = Cstl.getRegister().getRootDirectory();

        } catch (RegisterException regex) {
            throw new CstlServiceException(regex, LAYER_NOT_DEFINED);
        }
        return rootDirectories;
    }


    public static LayerDetails getLayerReference(Name name) throws CstlServiceException {

        LayerDetails layerRef;
        try { // WE catch the exception from either service version
            layerRef = Cstl.getRegister().getLayerReference(ServiceDef.WMTS_1_0_0, name);

        } catch (RegisterException regex) {
            throw new CstlServiceException(regex, LAYER_NOT_DEFINED);
        }
        return layerRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFeatureInfo(GetFeatureInfo request) throws CstlServiceException {
       
        //       -- get the List of layer references
        final GetTile getTile = request.getGetTile();

        final Name layerName        = Util.parseLayerName(getTile.getLayer());
        final LayerDetails layerRef = getLayerReference(layerName);

        Coverage c = null;

        // build an equivalent style List
        final String styleName = getTile.getStyle();

        final MutableStyle style        = getStyle(styleName);
        //       -- create the rendering parameter Map
        Double elevation =  null;
        Date time        = null;
        List<DimensionNameValue> dimensions = getTile.getDimensionNameValue();
        for (DimensionNameValue dimension : dimensions) {
            if (dimension.getName().equalsIgnoreCase("elevation")) {
                try {
                    elevation = Double.parseDouble(dimension.getValue());
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("Unable to perse the elevation value", INVALID_PARAMETER_VALUE, "elevation");
                }
            }
            if (dimension.getName().equalsIgnoreCase("time")) {
                try {
                    time = TimeParser.toDate(dimension.getValue());
                } catch (ParseException ex) {
                    throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, "time");
                }
            }
        }
        final Map<String, Object> params       = new HashMap<String, Object>();
        params.put("ELEVATION", elevation);
        params.put("TIME", time);
        final SceneDef sdef = new SceneDef();

        try {
            final MapContext context = PortrayalUtil.createContext(layerRef, style, params);
            sdef.setContext(context);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        // 2. VIEW
        final JTSEnvelope2D refEnv             = new JTSEnvelope2D(c.getEnvelope());
        final double azimuth                   = 0;//request.getAzimuth();
        final ViewDef vdef = new ViewDef(refEnv,azimuth);


        // 3. CANVAS
        final java.awt.Dimension canvasDimension = null;//request.getSize();
        final Color background = null;
        final CanvasDef cdef = new CanvasDef(canvasDimension,background);

        // 4. SHAPE
        //     a
        final int pixelTolerance = 3;
        final int i = request.getI();
        final int j = request.getJ();
        if (i < 0 || i > canvasDimension.width) {
            throw new CstlServiceException("The requested point has an invalid X coordinate.", INVALID_POINT);
        }
        if (j < 0 || j > canvasDimension.height) {
            throw new CstlServiceException("The requested point has an invalid Y coordinate.", INVALID_POINT);
        }
        final Rectangle selectionArea = new Rectangle( request.getI()-pixelTolerance,
        		                               request.getJ()-pixelTolerance,
        		                               pixelTolerance*2,
        		                               pixelTolerance*2);

        // 5. VISITOR
        String infoFormat = request.getInfoFormat();
        if (infoFormat == null) {
            //Should not happen since the info format parameter is mandatory for the GetFeatureInfo request.
            infoFormat = MimeType.TEXT_PLAIN;
        }
        final TextGraphicVisitor visitor;
        if (infoFormat.equalsIgnoreCase(MimeType.TEXT_PLAIN)) {
            // TEXT / PLAIN
            visitor = new CSVGraphicVisitor(request);
        } else if (infoFormat.equalsIgnoreCase(MimeType.TEXT_HTML)) {
            // TEXT / HTML
            visitor = new HTMLGraphicVisitor(request);
        } else if (infoFormat.equalsIgnoreCase(MimeType.APP_GML) || infoFormat.equalsIgnoreCase(MimeType.TEXT_XML) ||
                   infoFormat.equalsIgnoreCase(MimeType.APP_XML) || infoFormat.equalsIgnoreCase("xml") ||
                   infoFormat.equalsIgnoreCase("gml"))
        {
            // GML
            visitor = new GMLGraphicVisitor(request);
        } else {
            throw new CstlServiceException("MIME type " + infoFormat + " is not accepted by the service.\n" +
                    "You have to choose between: "+ MimeType.TEXT_PLAIN +", "+ MimeType.TEXT_HTML +", "+ MimeType.APP_GML +", "+ "gml" +
                    ", "+ MimeType.APP_XML +", "+ "xml"+", "+ MimeType.TEXT_XML,
                    INVALID_FORMAT, "infoFormat");
        }

        final VisitDef visitDef = new VisitDef();
        visitDef.setArea(selectionArea);
        visitDef.setVisitor(visitor);


        // We now build the response, according to the format chosen.
        try {
        	Cstl.getPortrayalService().visit(sdef,vdef,cdef,visitDef);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        return visitor.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getTile(GetTile request) throws CstlServiceException {
        
        //1 LAYER NOT USED FOR NOW
        final Name layerName = Util.parseLayerName(request.getLayer());

        // 2. PARAMETERS NOT USED FOR NOW
        Double elevation =  null;
        Date time        = null;
        List<DimensionNameValue> dimensions = request.getDimensionNameValue();
        for (DimensionNameValue dimension : dimensions) {
            if (dimension.getName().equalsIgnoreCase("elevation")) {
                try {
                    elevation = Double.parseDouble(dimension.getValue());
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("Unable to parse the elevation value", INVALID_PARAMETER_VALUE, "elevation");
                }
            }
            if (dimension.getName().equalsIgnoreCase("time")) {
                try {
                    time = TimeParser.toDate(dimension.getValue());
                } catch (ParseException ex) {
                    throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, "time");
                }
            }
        }

        // 3 STYLE NOT USED FOR NOW
        final String styleName    = request.getStyle();
        final MutableStyle style  = getStyle(styleName);


        // 4. We get the parameters
        final int columnIndex         = request.getTileCol();
        final int rowIndex            = request.getTileRow();
        final String matrixSetName    = request.getTileMatrixSet();
        final String level            = request.getTileMatrix();
        
        
        // 5. we verify the parameters
        if (columnIndex < 0 || rowIndex < 0) {
            throw new CstlServiceException("TileCol and TileRow must be > 0", INVALID_PARAMETER_VALUE);
        }

        final TileMatrixSet matrixSet = DefaultTileExample.getTileMatrixSet(matrixSetName);
        if (matrixSet == null) {
            throw new CstlServiceException("Undefined matrixSet:" + matrixSetName + " for layer:" + layerName, INVALID_PARAMETER_VALUE, "tilematrixset");
        }
        final TileMatrix matrix       = matrixSet.getTileMatrixByName(level);

        if (matrix == null) {
            throw new CstlServiceException("Undefined matrix:" + level + " for matrixSet:" + matrixSetName, INVALID_PARAMETER_VALUE, "tilematrix");
        }
        if (columnIndex >= matrix.getMatrixWidth()) {
            throw new CstlServiceException("TileCol out of band" + columnIndex + " > " +  matrix.getMatrixWidth(), INVALID_PARAMETER_VALUE, "tilecol");
        }
        if (rowIndex >= matrix.getMatrixHeight()) {
            throw new CstlServiceException("TileRow out of band" + rowIndex + " > " +  matrix.getMatrixHeight(), INVALID_PARAMETER_VALUE, "tilerow");
        }

        // we transform the parameters to get the correct tile file
        final String col              = getLettersFromInt(columnIndex, matrix.getMatrixWidth()); // letter
        final String line             = getNumbersFromInt(rowIndex, matrix.getMatrixHeight()); // number
        final List<String> rootDir    = getRootDirectories();
        final DefaultTileExample.Path path = DefaultTileExample.getPathForMatrixSet(matrixSetName);
        final String fileName;
        if (path.isAbsolute) {
            fileName         = path.path + level + '_' + col + line + ".png";
        } else {
            fileName         = rootDir.get(0) + path.path + level + '_' + col + line + ".png";
        }

        final File f = new File(fileName);
        if (f.exists()) {
            LOGGER.info("returning existing file:" + f.getPath());
        } else {
            LOGGER.info("file does not exist:" + f.getPath());
            return new File(rootDir + "blank.png");
            //throw new CstlServiceException("The correspounding file has not been found", NO_APPLICABLE_CODE);
        }

        return f;
    }

    /*private String getRootDir() throws CstlServiceException {
        InputStream is = Util.getResourceAsStream("wmts.properties");
        if (is == null) {
            throw new CstlServiceException("Unable to find the wmts.properties file");
        }
        Properties p = new Properties();
        try {
            p.load(is);
            return p.getProperty("path");
        } catch (IOException ex) {
             throw new CstlServiceException("Unable to load the wmts.properties file", ex, NO_APPLICABLE_CODE);
        }
    }*/
    
    protected static String getNumbersFromInt(Integer i, int max) {
        if (i != null) {
            i = i + 1;
            final int nbChar;
            if (max < 10) {
                nbChar = 1;
            } else if (max < 100 ){
                nbChar = 2;
            } else if (max < 1000 ){
                nbChar = 3;
            } else if (max < 10000 ){
                nbChar = 4;
            } else if (max < 100000 ){
                nbChar = 5;
            } else if (max < 1000000 ){
                nbChar = 6;
            } else if (max < 10000000 ){
                nbChar = 7;
            } else {
                nbChar = 8;
            }
            StringBuffer result = new StringBuffer().append(i);
            while (result.length() < nbChar) {
                result.insert(0, "0");
            }
            return result.toString();
        }
        return null;
    }

    protected static String getLettersFromInt(Integer i, int max) {
        if (i != null) {
            final int nbChar = (int) Math.floor(Math.log(max) / 3.258096538 + 1E-6) + 1;
            final StringBuffer buffer = new StringBuffer();
            while (i != 0) {
                buffer.insert(0, (char)('A' + (i % 26)));
                i /= 26;
            }
            for (int j=buffer.length(); j<nbChar; j++) {
                buffer.insert(0, 'A');
            }
            return buffer.toString();
        }
        return null;
    }

    private static MutableStyle getStyle(final String styleName) throws CstlServiceException {
        final MutableStyle style;
        if (styleName != null && !styleName.isEmpty()) {
            //try to grab the style if provided
            //a style has been given for this layer, try to use it
            style = StyleProviderProxy.getInstance().get(styleName);
            if (style == null) {
                throw new CstlServiceException("Style provided not found.", STYLE_NOT_DEFINED);
            }
        } else {
            //no defined styles, use the favorite one, let the layer get it himself.
            style = null;
        }
        return style;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }
}
