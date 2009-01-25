/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.coverage.ws.rs;

// J2SE dependencies
import static org.constellation.query.Query.APP_XML;
import static org.constellation.query.Query.EXCEPTIONS_INIMAGE;
import static org.constellation.query.Query.KEY_EXCEPTIONS;
import static org.constellation.query.Query.KEY_REQUEST;
import static org.constellation.query.Query.KEY_SERVICE;
import static org.constellation.query.Query.KEY_VERSION;
import static org.constellation.query.Query.TEXT_XML;
import static org.constellation.query.wcs.WCSQuery.DESCRIBECOVERAGE;
import static org.constellation.query.wcs.WCSQuery.GEOTIFF;
import static org.constellation.query.wcs.WCSQuery.GETCAPABILITIES;
import static org.constellation.query.wcs.WCSQuery.GETCOVERAGE;
import static org.constellation.query.wcs.WCSQuery.KEY_BBOX;
import static org.constellation.query.wcs.WCSQuery.KEY_BOUNDINGBOX;
import static org.constellation.query.wcs.WCSQuery.KEY_COVERAGE;
import static org.constellation.query.wcs.WCSQuery.KEY_CRS;
import static org.constellation.query.wcs.WCSQuery.KEY_DEPTH;
import static org.constellation.query.wcs.WCSQuery.KEY_FORMAT;
import static org.constellation.query.wcs.WCSQuery.KEY_GRIDBASECRS;
import static org.constellation.query.wcs.WCSQuery.KEY_GRIDCS;
import static org.constellation.query.wcs.WCSQuery.KEY_GRIDOFFSETS;
import static org.constellation.query.wcs.WCSQuery.KEY_GRIDORIGIN;
import static org.constellation.query.wcs.WCSQuery.KEY_GRIDTYPE;
import static org.constellation.query.wcs.WCSQuery.KEY_HEIGHT;
import static org.constellation.query.wcs.WCSQuery.KEY_IDENTIFIER;
import static org.constellation.query.wcs.WCSQuery.KEY_INTERPOLATION;
import static org.constellation.query.wcs.WCSQuery.KEY_RANGESUBSET;
import static org.constellation.query.wcs.WCSQuery.KEY_RESPONSE_CRS;
import static org.constellation.query.wcs.WCSQuery.KEY_RESX;
import static org.constellation.query.wcs.WCSQuery.KEY_RESY;
import static org.constellation.query.wcs.WCSQuery.KEY_RESZ;
import static org.constellation.query.wcs.WCSQuery.KEY_SECTION;
import static org.constellation.query.wcs.WCSQuery.KEY_STORE;
import static org.constellation.query.wcs.WCSQuery.KEY_TIME;
import static org.constellation.query.wcs.WCSQuery.KEY_TIMESEQUENCE;
import static org.constellation.query.wcs.WCSQuery.KEY_WIDTH;
import static org.constellation.query.wcs.WCSQuery.MATRIX;
import static org.constellation.query.wcs.WCSQuery.NETCDF;
import static org.constellation.ws.ExceptionCode.INVALID_CRS;
import static org.constellation.ws.ExceptionCode.INVALID_PARAMETER_VALUE;
import static org.constellation.ws.ExceptionCode.LAYER_NOT_DEFINED;
import static org.constellation.ws.ExceptionCode.MISSING_PARAMETER_VALUE;
import static org.constellation.ws.ExceptionCode.NO_APPLICABLE_CODE;
import static org.constellation.ws.ExceptionCode.OPERATION_NOT_SUPPORTED;
import static org.constellation.ws.ExceptionCode.VERSION_NEGOTIATION_FAILED;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Series;
import org.constellation.gml.v311.CodeListType;
import org.constellation.gml.v311.CodeType;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.GridEnvelopeType;
import org.constellation.gml.v311.GridLimitsType;
import org.constellation.gml.v311.GridType;
import org.constellation.gml.v311.RectifiedGridType;
import org.constellation.gml.v311.TimePositionType;
import org.constellation.ows.AbstractGetCapabilities;
import org.constellation.ows.v100.ExceptionReport;
import org.constellation.ows.v110.AcceptFormatsType;
import org.constellation.ows.v110.AcceptVersionsType;
import org.constellation.ows.v110.BoundingBoxType;
import org.constellation.ows.v110.KeywordsType;
import org.constellation.ows.v110.LanguageStringType;
import org.constellation.ows.v110.OperationsMetadata;
import org.constellation.ows.v110.SectionsType;
import org.constellation.ows.v110.ServiceIdentification;
import org.constellation.ows.v110.ServiceProvider;
import org.constellation.ows.v110.WGS84BoundingBoxType;
import org.constellation.portrayal.CstlPortrayalService;
import org.constellation.portrayal.Portrayal;
import org.constellation.provider.LayerDetails;
import org.constellation.query.QueryAdapter;
import org.constellation.register.RegisterException;
import org.constellation.util.Util;
import org.constellation.wcs.AbstractDescribeCoverage;
import org.constellation.wcs.AbstractGetCoverage;
import org.constellation.wcs.v100.ContentMetadata;
import org.constellation.wcs.v100.CoverageDescription;
import org.constellation.wcs.v100.CoverageOfferingBriefType;
import org.constellation.wcs.v100.CoverageOfferingType;
import org.constellation.wcs.v100.DCPTypeType;
import org.constellation.wcs.v100.DomainSetType;
import org.constellation.wcs.v100.Keywords;
import org.constellation.wcs.v100.LonLatEnvelopeType;
import org.constellation.wcs.v100.RangeSet;
import org.constellation.wcs.v100.RangeSetType;
import org.constellation.wcs.v100.SpatialSubsetType;
import org.constellation.wcs.v100.SupportedCRSsType;
import org.constellation.wcs.v100.SupportedFormatsType;
import org.constellation.wcs.v100.SupportedInterpolationsType;
import org.constellation.wcs.v100.WCSCapabilitiesType;
import org.constellation.wcs.v100.DCPTypeType.HTTP.Get;
import org.constellation.wcs.v100.DCPTypeType.HTTP.Post;
import org.constellation.wcs.v100.WCSCapabilityType.Request;
import org.constellation.wcs.v111.Capabilities;
import org.constellation.wcs.v111.Contents;
import org.constellation.wcs.v111.CoverageDescriptionType;
import org.constellation.wcs.v111.CoverageDescriptions;
import org.constellation.wcs.v111.CoverageDomainType;
import org.constellation.wcs.v111.CoverageSummaryType;
import org.constellation.wcs.v111.FieldType;
import org.constellation.wcs.v111.GridCrsType;
import org.constellation.wcs.v111.InterpolationMethodType;
import org.constellation.wcs.v111.InterpolationMethods;
import org.constellation.wcs.v111.RangeType;
import org.constellation.wcs.v111.RangeSubsetType.FieldSubset;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.ServiceExceptionReport;
import org.constellation.ws.ServiceExceptionType;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.rs.OGCWebService;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.display.exception.PortrayalException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.sun.jersey.spi.resource.Singleton;


/**
 * The Web Coverage Service (WCS) for Constellation, this service implements the 
 * {@code GetCoverage}, {@code DescribeCoverage}, and {@code GetCapabilities} 
 * methods of the Open Geospatial Consortium (OGC) specifications.
 * <p>
 * This service follows the specification sof the Open Geospatial Consortium 
 * (OGC). As of Constellation 0.3, this Web Coverage Service complies with the 
 * specification version 1.0.0 (OGC document 03-065r6) and mostly complies with
 * specification version 1.1.1 (OGC document 06-083r8).
 * </p>
 *
 * @version $Id$
 * @author Guilhem Legal
 * @author Cédric Briançon
 */
@Path("wcs")
@Singleton
public class WCSService extends OGCWebService {
    /*
     * Set to true for CITE tests.
     */
    private final static boolean CITE_TESTING = false;

    /**
     * Build a new instance of the webService and initialize the JAXB marshaller.
     *
     * @throws JAXBException if the initialization of the {@link JAXBContext} fails.
     */
    public WCSService() throws JAXBException {
        
        super("WCS", new ServiceVersion(ServiceType.WCS, "1.1.1"), 
                     new ServiceVersion(ServiceType.WCS, "1.0.0"));

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext( "org.constellation.ws" +
                      ":org.constellation.wcs.v100" +
                      ":org.constellation.wcs.v111",
                      "http://www.opengis.net/wcs");

        LOGGER.info("WCS service running");
    }

    /**
     * Treat the incoming request and call the right function.
     *
     * @param objectRequest the request received.
     * @return an image or xml response.
     * @throws JAXBException
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        try {
            final String request = (String) getParameter(KEY_REQUEST, true);
            LOGGER.info("New request: " + request);
            logParameters();

            if (DESCRIBECOVERAGE.equalsIgnoreCase(request) ||
                    (objectRequest instanceof AbstractDescribeCoverage))
            {
                AbstractDescribeCoverage dc = (AbstractDescribeCoverage)objectRequest;
                verifyBaseParameter(0);

                //this wcs does not implement "store" mechanism
                String store = getParameter(KEY_STORE, false);
                if (store!= null && store.trim().equalsIgnoreCase("true")) {
                    throw new CstlServiceException("The service does not implement the store mechanism",
                                   NO_APPLICABLE_CODE, getActingVersion(), "store");
                }
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (dc == null) {
                    dc = createNewDescribeCoverageRequest();
                }
                return Response.ok(describeCoverage(dc), TEXT_XML).build();
            }
            if (GETCAPABILITIES.equalsIgnoreCase(request) || 
                    (objectRequest instanceof AbstractGetCapabilities))
            {
                AbstractGetCapabilities gc = (AbstractGetCapabilities)objectRequest;
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    gc = createNewGetCapabilitiesRequest();
                }
                return getCapabilities(gc);
            }
            if (GETCOVERAGE.equalsIgnoreCase(request) || 
                    (objectRequest instanceof AbstractGetCoverage))
            {
                AbstractGetCoverage gc = (AbstractGetCoverage)objectRequest;
                verifyBaseParameter(0);
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {

                    gc = createNewGetCoverageRequest();

                }
                return getCoverage(gc);
            }
            throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                           OPERATION_NOT_SUPPORTED, getActingVersion(), "request");
        } catch (CstlServiceException ex) {
            final Object report;
            if (getActingVersion().isOWS()) {
                final String code = Util.transformCodeName(ex.getExceptionCode().name());
                report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(), getActingVersion().toString());
            } else {
                report = new ServiceExceptionReport(getActingVersion(),
                                                    new ServiceExceptionType(ex.getMessage(), 
                                                    (ExceptionCode) ex.getExceptionCode()));
            }
            
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)   &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)&& 
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)&& 
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED))
            {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            } else {
                LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getLocalizedMessage() + '\n');
            }
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(Util.cleanSpecialCharacter(sw.toString()), TEXT_XML).build();
        }
    }

    /**
     * Build a new {@linkplain AbstractGetCapabilities GetCapabilities} request from
     * a kvp request.
     *
     * @return a marshallable GetCapabilities request.
     * @throws CstlServiceException
     */
    private AbstractGetCapabilities createNewGetCapabilitiesRequest() throws CstlServiceException {

        if (!getParameter(KEY_SERVICE, true).equalsIgnoreCase("WCS")) {
            throw new CstlServiceException("The parameters SERVICE=WCS must be specified",
                    MISSING_PARAMETER_VALUE, getActingVersion(), "service");
        }
        String inputVersion = getParameter(KEY_VERSION, false);
        if (inputVersion == null) {
            inputVersion = getParameter("acceptversions", false);
            if (inputVersion == null) {
                inputVersion = "1.1.1";
            } else {
                //we verify that the version id supported
                isVersionSupported(inputVersion);
            }
        }

        this.setActingVersion(getBestVersion(inputVersion).toString());

        if (getActingVersion().toString().equals("1.0.0")) {
            return new org.constellation.wcs.v100.GetCapabilities(getParameter(KEY_SECTION, false),
                                                           null);
        } else {
            AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));

            //We transform the String of sections in a list.
            //In the same time we verify that the requested sections are valid.
            String section = getParameter("Sections", false);
            List<String> requestedSections = new ArrayList<String>();
            if (section != null) {
                final StringTokenizer tokens = new StringTokenizer(section, ",;");
                while (tokens.hasMoreTokens()) {
                    final String token = tokens.nextToken().trim();
                    if (SectionsType.getExistingSections("1.1.1").contains(token)) {
                        requestedSections.add(token);
                    } else {
                        throw new CstlServiceException("The section " + token + " does not exist",
                                INVALID_PARAMETER_VALUE, getActingVersion());
                    }
                }
            } else {
                //if there is no requested Sections we add all the sections
                requestedSections = SectionsType.getExistingSections("1.1.1");
            }
            SectionsType sections = new SectionsType(requestedSections);
            AcceptVersionsType versions = new AcceptVersionsType("1.1.1");
            return new org.constellation.wcs.v111.GetCapabilities(versions,
                                                           sections,
                                                           formats,
                                                           null);
        }
    }

    /**
     * Build a new {@linkplain AbstractDescribeCoverage DescribeCoverage} request from
     * a kvp request.
     *
     * @return a marshallable DescribeCoverage request.
     * @throws CstlServiceException
     */
    private AbstractDescribeCoverage createNewDescribeCoverageRequest() throws CstlServiceException {
        if (getActingVersion().toString().equals("1.0.0")) {
            return new org.constellation.wcs.v100.DescribeCoverage(getParameter(KEY_COVERAGE, true));
        } else {
            return new org.constellation.wcs.v111.DescribeCoverage(getParameter(KEY_IDENTIFIER, true));
        }
    }

    /**
     * Build a new DescribeCoverage request from a kvp request
     */
    private AbstractGetCoverage createNewGetCoverageRequest() throws CstlServiceException {
        String width  = getParameter(KEY_WIDTH,  false);
        String height = getParameter(KEY_HEIGHT, false);
        String depth  = getParameter(KEY_DEPTH,  false);

        String resx = getParameter(KEY_RESX, false);
        String resy = getParameter(KEY_RESY, false);
        @SuppressWarnings("unused")
		String resz = getParameter(KEY_RESZ, false);

        if (getActingVersion().toString().equals("1.0.0")) {
            // temporal subset
            org.constellation.wcs.v100.TimeSequenceType temporal = null;
            final String timeParameter = getParameter(KEY_TIME, false);
            if (timeParameter != null) {
                final TimePositionType time = new TimePositionType(timeParameter);
                temporal = new org.constellation.wcs.v100.TimeSequenceType(time);
            }

            /*
             * spatial subset
             */
            // the boundingBox/envelope
            final List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
            final String bbox = getParameter(KEY_BBOX, true);
            if (bbox != null) {
                final List<String> bboxValues = QueryAdapter.toStringList(bbox);
                pos.add(new DirectPositionType(QueryAdapter.toDouble(bboxValues.get(0)),
                                               QueryAdapter.toDouble(bboxValues.get(2))));
                pos.add(new DirectPositionType(QueryAdapter.toDouble(bboxValues.get(1)),
                                               QueryAdapter.toDouble(bboxValues.get(3))));
                if (bboxValues.size() > 4) {
                    pos.add(new DirectPositionType(QueryAdapter.toDouble(bboxValues.get(4)),
                                                   QueryAdapter.toDouble(bboxValues.get(5))));
                }
            }
            final EnvelopeEntry envelope = new EnvelopeEntry(pos, getParameter(KEY_CRS, true));

            if ((width == null || height == null) && (resx == null || resy == null)) {
                    throw new CstlServiceException("The parameters WIDTH and HEIGHT or RESX and RESY have to be specified" ,
                                   INVALID_PARAMETER_VALUE, getActingVersion());
            }

            final List<String> axis = new ArrayList<String>();
            axis.add("width");
            axis.add("height");
            final List<BigInteger> low = new ArrayList<BigInteger>();
            low.add(new BigInteger("0"));
            low.add(new BigInteger("0"));
            final List<BigInteger> high = new ArrayList<BigInteger>();
            high.add(new BigInteger(width));
            high.add(new BigInteger(height));
            if (depth != null) {
                axis.add("depth");
                low.add(new BigInteger("0"));
                high.add(new BigInteger(depth));
            }
            final GridLimitsType limits = new GridLimitsType(low, high);
            final GridType grid = new GridType(limits, axis);

            org.constellation.wcs.v100.SpatialSubsetType spatial = new org.constellation.wcs.v100.SpatialSubsetType(envelope, grid);

            //domain subset
            org.constellation.wcs.v100.DomainSubsetType domain   = new org.constellation.wcs.v100.DomainSubsetType(temporal, spatial);

            //range subset (not yet used)
            org.constellation.wcs.v100.RangeSubsetType  range    = null;

            //interpolation method
            org.constellation.wcs.v100.InterpolationMethod interpolation =
                    org.constellation.wcs.v100.InterpolationMethod.fromValue(getParameter(KEY_INTERPOLATION, false));

            //output
            org.constellation.wcs.v100.OutputType output         = new org.constellation.wcs.v100.OutputType(getParameter(KEY_FORMAT, true),
                                                                                                     getParameter(KEY_RESPONSE_CRS, false));

            return new org.constellation.wcs.v100.GetCoverage(getParameter(KEY_COVERAGE, true),
                                                       domain,
                                                       range,
                                                       interpolation,
                                                       output);
         } else {

            // temporal subset
            org.constellation.wcs.v111.TimeSequenceType temporal = null;
            String timeParameter = getParameter(KEY_TIMESEQUENCE, false);
            if (timeParameter != null) {
                if (timeParameter.indexOf('/') == -1) {
                    temporal = new org.constellation.wcs.v111.TimeSequenceType(new TimePositionType(timeParameter));
                } else {
                    throw new CstlServiceException("The service does not handle TimePeriod" ,
                                   INVALID_PARAMETER_VALUE, getActingVersion());
                }
            }

            /*
             * spatial subset
             */
             // the boundingBox/envelope
             String bbox          = getParameter(KEY_BOUNDINGBOX, true);
             final String crs;
             if (bbox.indexOf(',') != -1) {
                crs  = bbox.substring(bbox.lastIndexOf(',') + 1, bbox.length());
                bbox = bbox.substring(0, bbox.lastIndexOf(','));
             } else {
                throw new CstlServiceException("The correct pattern for BoundingBox parameter are crs,minX,minY,maxX,maxY,CRS",
                                INVALID_PARAMETER_VALUE, getActingVersion(), "boundingbox");
             }
             BoundingBoxType envelope = null;

             if (bbox != null) {
                final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
                final Double[] coordinates   = new Double[tokens.countTokens()];
                int i = 0;
                while (tokens.hasMoreTokens()) {
                    coordinates[i] = parseDouble(tokens.nextToken());
                    i++;
                }
                 if (i < 4) {
                     throw new CstlServiceException("The correct pattern for BoundingBox parameter are crs,minX,minY,maxX,maxY,CRS",
                             INVALID_PARAMETER_VALUE, getActingVersion(), "boundingbox");
                 }
                envelope = new BoundingBoxType(crs, coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
             }

             //domain subset
             org.constellation.wcs.v111.DomainSubsetType domain   = new org.constellation.wcs.v111.DomainSubsetType(temporal, envelope);

             //range subset.
             org.constellation.wcs.v111.RangeSubsetType  range = null;
             String rangeSubset = getParameter(KEY_RANGESUBSET, false);
             if (rangeSubset != null) {
                //for now we don't handle Axis Identifiers
                if (rangeSubset.indexOf('[') != -1 || rangeSubset.indexOf(']') != -1) {
                    throw new CstlServiceException("The service does not handle axis identifiers",
                            INVALID_PARAMETER_VALUE, getActingVersion(), "axis");
                }

                final StringTokenizer tokens = new StringTokenizer(rangeSubset, ";");
                final List<FieldSubset> fields = new ArrayList<FieldSubset>(tokens.countTokens());
                while (tokens.hasMoreTokens()) {
                    final String value = tokens.nextToken();
                    String interpolation = null;
                    String rangeIdentifier = null;
                    if (value.indexOf(':') != -1) {
                        rangeIdentifier = value.substring(0, rangeSubset.indexOf(':'));
                        interpolation = value.substring(rangeSubset.indexOf(':') + 1);
                    } else {
                        rangeIdentifier = value;
                    }
                    fields.add(new FieldSubset(rangeIdentifier, interpolation));
                }

                range = new org.constellation.wcs.v111.RangeSubsetType(fields);
            }


            String gridType = getParameter(KEY_GRIDTYPE, false);
            if (gridType == null) {
                gridType = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid";
            }
            String gridOrigin = getParameter(KEY_GRIDORIGIN, false);
            if (gridOrigin == null) {
                gridOrigin = "0.0,0.0";
            }
            
            StringTokenizer tokens = new StringTokenizer(gridOrigin, ",;");
            final List<Double> origin = new ArrayList<Double>(tokens.countTokens());
            while (tokens.hasMoreTokens()) {
                Double value = parseDouble(tokens.nextToken());
                origin.add(value);
            }

            String gridOffsets = getParameter(KEY_GRIDOFFSETS, false);
            List<Double> offset = new ArrayList<Double>();
            if (gridOffsets != null) {
                tokens = new StringTokenizer(gridOffsets, ",;");
                while (tokens.hasMoreTokens()) {
                    Double value = parseDouble(tokens.nextToken());
                    offset.add(value);
                }
            }
            String gridCS = getParameter(KEY_GRIDCS, false);
            if (gridCS == null) {
                gridCS = "urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS";
            }

            //output
            final CodeType codeCRS = new CodeType(crs);
            final GridCrsType grid = new GridCrsType(codeCRS, getParameter(KEY_GRIDBASECRS, false), gridType,
                    origin, offset, gridCS, "");
            org.constellation.wcs.v111.OutputType output = new org.constellation.wcs.v111.OutputType(grid, getParameter(KEY_FORMAT, true));

            return new org.constellation.wcs.v111.GetCoverage(new org.constellation.ows.v110.CodeType(getParameter(KEY_IDENTIFIER, true)),
                    domain, range, output);
        }
    }

    /**
     * Describe the capabilities and the layers available for the WCS service.
     *
     * @param abstractRequest The request done by the user.
     * @return a WCSCapabilities XML document describing the capabilities of this service.
     *
     * @throws CstlServiceException
     * @throws JAXBException when unmarshalling the default GetCapabilities file.
     */
    public Response getCapabilities(AbstractGetCapabilities abstractRequest) throws JAXBException, CstlServiceException {
        //we begin by extract the base attribute
        String inputVersion = abstractRequest.getVersion();
        if (inputVersion == null) {
            setActingVersion("1.1.1");
        } else {
           isVersionSupported(inputVersion);
           setActingVersion(inputVersion);
        }
        Capabilities        responsev111 = null;
        WCSCapabilitiesType responsev100 = null;
        boolean contentMeta              = false;
        String format                    = TEXT_XML;
        if (getActingVersion().toString().equals("1.1.1")) {

            org.constellation.wcs.v111.GetCapabilities request = (org.constellation.wcs.v111.GetCapabilities) abstractRequest;

            // if the user have specified one format accepted (only one for now != spec)
            AcceptFormatsType formats = request.getAcceptFormats();
            if (formats == null || formats.getOutputFormat().size() == 0) {
                format = TEXT_XML;
            } else {
                format = formats.getOutputFormat().get(0);
                if (!format.equals(TEXT_XML) && !format.equals(APP_XML)){
                    throw new CstlServiceException("This format " + format + " is not allowed",
                                   INVALID_PARAMETER_VALUE, getActingVersion(), "format");
                }
            }

            //if the user have requested only some sections
            List<String> requestedSections = SectionsType.getExistingSections("1.1.1");

            if (request.getSections() != null && request.getSections().getSection().size() > 0) {
                requestedSections = request.getSections().getSection();
                for (String sec:requestedSections) {
                    if (!SectionsType.getExistingSections("1.1.1").contains(sec)){
                       throw new CstlServiceException("This sections " + sec + " is not allowed",
                                       INVALID_PARAMETER_VALUE, getActingVersion());
                    }
                }
            }

            // we unmarshall the static capabilities document
            Capabilities staticCapabilities = null;
            try {
                staticCapabilities = (Capabilities)getStaticCapabilitiesObject();
            } catch(IOException e)   {
                throw new CstlServiceException("IO exception while getting Services Metadata: " + e.getMessage(),
                               INVALID_PARAMETER_VALUE, getActingVersion());

            }
            ServiceIdentification si = null;
            ServiceProvider       sp = null;
            OperationsMetadata    om = null;

            //we add the static sections if the are included in the requested sections
            if (requestedSections.contains("ServiceProvider") || requestedSections.contains("All"))
                sp = staticCapabilities.getServiceProvider();
            if (requestedSections.contains("ServiceIdentification") || requestedSections.contains("All"))
                si = staticCapabilities.getServiceIdentification();
            if (requestedSections.contains("OperationsMetadata") || requestedSections.contains("All")) {
                om = staticCapabilities.getOperationsMetadata();
                //we update the url in the static part.
                updateOWSURL(om.getOperation(), getServiceURL(), "WCS");
            }
            responsev111 = new Capabilities(si, sp, om, "1.1.1", null, null);

            // if the user does not request the contents section we can return the result.
            if (!requestedSections.contains("Contents") && !requestedSections.contains("All")) {
                StringWriter sw = new StringWriter();
                marshaller.marshal(responsev111, sw);
                return Response.ok(sw.toString(), format).build();
            }

        } else {

            org.constellation.wcs.v100.GetCapabilities request = (org.constellation.wcs.v100.GetCapabilities) abstractRequest;

            /*
             * In WCS 1.0.0 the user can request only one section
             * ( or all by omitting the parameter section)
             */
            String section = request.getSection();
            String requestedSection = null;
            if (section != null) {
                if (SectionsType.getExistingSections("1.0.0").contains(section)){
                    requestedSection = section;
                } else {
                    throw new CstlServiceException("The section " + section + " does not exist",
                                   INVALID_PARAMETER_VALUE, getActingVersion());
               }
               contentMeta = requestedSection.equals("/WCS_Capabilities/ContentMetadata");
            }
            WCSCapabilitiesType staticCapabilities = null;
            try {
                staticCapabilities = (WCSCapabilitiesType)((JAXBElement<?>)getStaticCapabilitiesObject()).getValue();
            } catch(IOException e)   {
                throw new CstlServiceException("IO exception while getting Services Metadata: " + e.getMessage(),
                               INVALID_PARAMETER_VALUE, getActingVersion());

            }
            if (requestedSection == null || requestedSection.equals("/WCS_Capabilities/Capability") || requestedSection.equals("/")) {
                //we update the url in the static part.
                Request req = staticCapabilities.getCapability().getRequest();
                updateURL(req.getGetCapabilities().getDCPType());
                updateURL(req.getDescribeCoverage().getDCPType());
                updateURL(req.getGetCoverage().getDCPType());
            }

            if (requestedSection == null || contentMeta  || requestedSection.equals("/")) {
                responsev100 = staticCapabilities;
            } else {
                if (requestedSection.equals("/WCS_Capabilities/Capability")) {
                    responsev100 = new WCSCapabilitiesType(staticCapabilities.getCapability());
                } else if (requestedSection.equals("/WCS_Capabilities/Service")) {
                    responsev100 = new WCSCapabilitiesType(staticCapabilities.getService());
                }

                StringWriter sw = new StringWriter();
                marshaller.marshal(responsev100, sw);
                return Response.ok(sw.toString(), format).build();
            }
        }
        
        //TODO: document by what circumstances we are here.
        
        Contents contents = null;
        ContentMetadata contentMetadata = null;

        //we get the list of layers
        List<CoverageSummaryType>        summary = new ArrayList<CoverageSummaryType>();
        List<CoverageOfferingBriefType> offBrief = new ArrayList<CoverageOfferingBriefType>();

        org.constellation.wcs.v111.ObjectFactory wcs111Factory = new org.constellation.wcs.v111.ObjectFactory();
        org.constellation.wcs.v100.ObjectFactory wcs100Factory = new org.constellation.wcs.v100.ObjectFactory();
        org.constellation.ows.v110.ObjectFactory owsFactory = new org.constellation.ows.v110.ObjectFactory();
        
        
        //NOTE: ADRIAN HACKED HERE
        final List<LayerDetails> layerRefs = getAllLayerReferences();
        
        
        try {
            for (LayerDetails layer: layerRefs){
                
                List<LanguageStringType> title = new ArrayList<LanguageStringType>();
                title.add(new LanguageStringType(layer.getName()));
                List<LanguageStringType> remark = new ArrayList<LanguageStringType>();
                remark.add(new LanguageStringType(Util.cleanSpecialCharacter(layer.getRemarks())));

                CoverageSummaryType       cs = new CoverageSummaryType(title, remark);
                CoverageOfferingBriefType co = new CoverageOfferingBriefType();

                co.addRest(wcs100Factory.createName(layer.getName()));
                co.addRest(wcs100Factory.createLabel(layer.getName()));

                GeographicBoundingBox inputGeoBox = layer.getGeographicBoundingBox();

                if(inputGeoBox != null) {
                    final String srsName = "urn:ogc:def:crs:OGC:1.3:CRS84";
                    if (getActingVersion().toString().equals("1.1.1")){
                        WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(
                                                     inputGeoBox.getWestBoundLongitude(),
                                                     inputGeoBox.getSouthBoundLatitude(),
                                                     inputGeoBox.getEastBoundLongitude(),
                                                     inputGeoBox.getNorthBoundLatitude());

                        cs.addRest(owsFactory.createWGS84BoundingBox(outputBBox));
                    } else {
                        final SortedSet<Number> elevations = layer.getAvailableElevations();
                        List<Double> pos1 = new ArrayList<Double>();
                        pos1.add(inputGeoBox.getWestBoundLongitude());
                        pos1.add(inputGeoBox.getSouthBoundLatitude());

                        List<Double> pos2 = new ArrayList<Double>();
                        pos2.add(inputGeoBox.getEastBoundLongitude());
                        pos2.add(inputGeoBox.getNorthBoundLatitude());

                        if (elevations != null && elevations.size() >= 2) {
                            pos1.add(elevations.first().doubleValue());
                            pos2.add(elevations.last().doubleValue());
                        }
                        List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
                        pos.add(new DirectPositionType(pos1));
                        pos.add(new DirectPositionType(pos2));
                        LonLatEnvelopeType outputBBox = new LonLatEnvelopeType(pos, srsName);
                        final SortedSet<Date> dates = layer.getAvailableTimes();
                        if (dates != null && dates.size() >= 2) {
                            /*
                             * Adds the first and last date available, since in the WCS GetCapabilities,
                             * it is a brief description of the capabilities.
                             * To get the whole available values, the describeCoverage request has to be
                             * done on a specific coverage.
                             */
                            final Date firstDate = dates.first();
                            final Date lastDate = dates.last();
                            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            df.setTimeZone(TimeZone.getTimeZone("UTC"));
                            outputBBox.getTimePosition().add(new TimePositionType(df.format(firstDate)));
                            outputBBox.getTimePosition().add(new TimePositionType(df.format(lastDate)));
                        }
                        co.setLonLatEnvelope(outputBBox);
                    }

                }
                cs.addRest(wcs111Factory.createIdentifier(layer.getName()));
                summary.add(cs);
                offBrief.add(co);
            }

            /**
             * FOR CITE TEST we put the first data mars because of ifremer overlapping data
             * TODO delete when overlapping problem is solved
             */
            if (CITE_TESTING) {
            CoverageSummaryType temp = summary.get(10);
            summary.remove(10);
            summary.add(0, temp);
            }

            contents        = new Contents(summary, null, null, null);
            contentMetadata = new ContentMetadata("1.0.0", offBrief);//TODO: really 1.0.0?
        } catch (CatalogException exception) {
            throw new CstlServiceException(exception, NO_APPLICABLE_CODE, getActingVersion());
        }


        StringWriter sw = new StringWriter();
        if (getActingVersion().toString().equals("1.1.1")) {
            responsev111.setContents(contents);
            marshaller.marshal(responsev111, sw);
        } else {
            if (contentMeta) {
                responsev100 = new WCSCapabilitiesType(contentMetadata);
            } else {
                responsev100.setContentMetadata(contentMetadata);
            }
            marshaller.marshal(responsev100, sw);
        }
        return Response.ok(sw.toString(), format).build();

    }


    /**
     * Get the coverage values for a specific coverage specified.
     * According to the output format chosen, the response could be an
     * {@linkplain RenderedImage image} or data representation.
     *
     * @param abstractRequest The request done by the user.
     * @return An {@linkplain RenderedImage image}, or a data representation.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    public Response getCoverage(AbstractGetCoverage abstractRequest) throws JAXBException,
                                                                      CstlServiceException
    {
        final String format;
        final String coverage;
        final GeneralEnvelope objEnv;
        final CoordinateReferenceSystem crs;
        final Dimension dimension;
        final int width;
        final int height;
        final int depth;
        final Double elevation;
        final String time;

        final String inputVersion = abstractRequest.getVersion();
        if(inputVersion == null) {
            throw new CstlServiceException("The parameter version must be specified",
                           MISSING_PARAMETER_VALUE, getActingVersion(), "version");
        } else {
           isVersionSupported(inputVersion);
           setActingVersion(inputVersion);
        }

        // TODO: better handle those parameters
        String interpolation = null;
        String exceptions = null;
        String resx  = null;
        String resy  = null;
        String resz  = null;
        String gridType;
        String gridOrigin = "";
        String gridOffsets = "";
        String gridCS;
        String gridBaseCrs;
        String responseCRS = null;

       if (getActingVersion().toString().equals("1.1.1")) {
            final org.constellation.wcs.v111.GetCoverage request = (org.constellation.wcs.v111.GetCoverage)abstractRequest;

            if (request.getIdentifier() != null) {
                coverage = request.getIdentifier().getValue();
            } else {
                throw new CstlServiceException("The parameter identifier must be specified",
                               MISSING_PARAMETER_VALUE, getActingVersion(), "identifier");
            }

            /*
             * Domain subset: - spatial subSet
             *                - temporal subset
             *
             * spatial subset: - BoundingBox
             * here the boundingBox parameter contain the crs.
             * we must extract it before calling webServiceWorker.setBoundingBox(...)
             *
             * temporal subSet: - timeSequence
             *
             */
            final org.constellation.wcs.v111.DomainSubsetType domain = request.getDomainSubset();
            if (domain == null) {
                throw new CstlServiceException("The DomainSubset must be specify",
                               MISSING_PARAMETER_VALUE, getActingVersion());
            }

            final BoundingBoxType boundingBox = (domain.getBoundingBox() != null) ?
                                                 domain.getBoundingBox().getValue() :
                                                 null;
            
            if (boundingBox != null && 
                boundingBox.getLowerCorner() != null &&
                boundingBox.getUpperCorner() != null &&
                boundingBox.getLowerCorner().size() >= 2 &&
                boundingBox.getUpperCorner().size() >= 2){
                final String crsName = boundingBox.getCrs();
                try {
                    crs  = CRS.decode((crsName.startsWith("EPSG:")) ? crsName : "EPSG:" + crsName);
                } catch (FactoryException ex) {
                    throw new CstlServiceException(ex, INVALID_CRS, getActingVersion());
                }
                objEnv = new GeneralEnvelope(crs);
                objEnv.setRange(0, boundingBox.getLowerCorner().get(0), boundingBox.getUpperCorner().get(0));
                objEnv.setRange(1, boundingBox.getLowerCorner().get(1), boundingBox.getUpperCorner().get(1));
            } else {
                throw new CstlServiceException("The BoundingBox is not well-formed",
                               INVALID_PARAMETER_VALUE, getActingVersion(), "boundingbox");
            }

            if (domain.getTemporalSubset() != null) {
                final List<Object> timeSeq = domain.getTemporalSubset().getTimePositionOrTimePeriod();
                // TODO: handle period values
                if (timeSeq != null && !timeSeq.isEmpty()) {
                    final Object obj = timeSeq.get(0);
                    if (obj instanceof TimePositionType) {
                        time = ((TimePositionType)obj).getValue();
                    } else if (obj instanceof org.constellation.wcs.v111.TimePeriodType) {
                        throw new CstlServiceException("The service does not handle time Period type",
                                       INVALID_PARAMETER_VALUE, getActingVersion());
                    } else {
                        time = null;
                    }
                } else {
                    time = null;
                }
            } else {
                time = null;
            }
            /*
             * Range subSet.
             * contain the sub fields : fieldSubset
             * for now we handle only one field to change the interpolation method.
             *
             * FieldSubset: - identifier
             *              - interpolationMethodType
             *              - axisSubset (not yet used)
             *
             * AxisSubset:  - identifier
             *              - key
             */
            org.constellation.wcs.v111.RangeSubsetType rangeSubset = request.getRangeSubset();
            
            
            //NOTE ADRIAN HACKED HERE
            final LayerDetails currentLayer = getLayerReference(coverage);
            
            
            if (rangeSubset != null) {
                List<String> requestedField = new ArrayList<String>();
                for(org.constellation.wcs.v111.RangeSubsetType.FieldSubset field: rangeSubset.getFieldSubset()) {
                    if (field.getIdentifier().equalsIgnoreCase(currentLayer.getThematic())){
                        interpolation = field.getInterpolationType();
                        
                        //we look that the same field is not requested two times
                        if (!requestedField.contains(field.getIdentifier())) {
                            requestedField.add(field.getIdentifier());
                        } else {
                            throw new CstlServiceException("The field " + field.getIdentifier() + " is already present in the request",
                                       INVALID_PARAMETER_VALUE, getActingVersion());
                        }

                        //if there is some AxisSubset we send an exception
                        if (field.getAxisSubset().size() != 0) {
                            throw new CstlServiceException("The service does not handle AxisSubset",
                                       INVALID_PARAMETER_VALUE, getActingVersion());
                        }
                    } else {
                        throw new CstlServiceException("The field " + field.getIdentifier() + " is not present in this coverage",
                                       INVALID_PARAMETER_VALUE, getActingVersion());
                    }
                }

            } else {
                interpolation = null;
            }

            /*
             * output subSet:  - format
             *                 - GridCRS
             *
             * Grid CRS: - GridBaseCRS (not yet used)
             *           - GridOffsets
             *           - GridType (not yet used)
             *           - GridOrigin
             *           - GridCS (not yet used)
             *
             */

            org.constellation.wcs.v111.OutputType output = request.getOutput();
            if (output == null) {
                throw new CstlServiceException("The OUTPUT must be specify" ,
                               MISSING_PARAMETER_VALUE, getActingVersion(), "output");
            }
            format = output.getFormat();
            if (format == null) {
                throw new CstlServiceException("The FORMAT must be specify" ,
                               MISSING_PARAMETER_VALUE, getActingVersion(), "format");
            }

            final GridCrsType grid = output.getGridCRS();
            if (grid != null) {
                gridBaseCrs = grid.getGridBaseCRS();
                gridType = grid.getGridType();
                gridCS = grid.getGridCS();

                for (Double d: grid.getGridOffsets()) {
                    gridOffsets += d.toString() + ',';
                }
                if (gridOffsets.length() > 0) {
                    gridOffsets = gridOffsets.substring(0, gridOffsets.length() - 1);
                } else {
                    gridOffsets = null;
                }

                for (Double d: grid.getGridOrigin()) {
                    gridOrigin += d.toString() + ',';
                }
                if (gridOrigin.length() > 0) {
                    gridOrigin = gridOrigin.substring(0, gridOrigin.length() - 1);
                }
            } else {
                // TODO the default value for gridOffsets is temporary until we get the right treatment
                gridOffsets = "1.0,0.0,0.0,1.0"; // = null;
                gridOrigin  = "0.0,0.0";
            }
            /* TODO: get the width and height parameter from the calculation using the grid origin, the size
             * of the envelope and the grid offsets.
             */
            width = Integer. parseInt(getParameter(KEY_WIDTH, false));
            height = Integer.parseInt(getParameter(KEY_HEIGHT, false));
            exceptions = getParameter(KEY_EXCEPTIONS, false);

            // TODO: get the elevation value from the third dimension of the BBOX3D
            elevation = null;

        } else {

            // parameter for 1.0.0 version
            org.constellation.wcs.v100.GetCoverage request = (org.constellation.wcs.v100.GetCoverage)abstractRequest;
            if (request.getOutput().getFormat()!= null) {
                format = request.getOutput().getFormat().getValue();
            } else {
                throw new CstlServiceException("The parameters FORMAT have to be specified",
                                                 MISSING_PARAMETER_VALUE, getActingVersion(), "format");
            }

            coverage = request.getSourceCoverage();
            if (coverage == null) {
                throw new CstlServiceException("The parameters SOURCECOVERAGE have to be specified",
                                                 MISSING_PARAMETER_VALUE, getActingVersion(), "sourceCoverage");
            }
            interpolation = (request.getInterpolationMethod() != null) ?
                interpolation = request.getInterpolationMethod().value() : null;

            exceptions = getParameter(KEY_EXCEPTIONS, false);
            if (request.getOutput().getCrs() != null){
                responseCRS   = request.getOutput().getCrs().getValue();
            }

            //for now we only handle one time parameter with timePosition type
            final org.constellation.wcs.v100.TimeSequenceType temporalSubset =
                    request.getDomainSubset().getTemporalSubSet();
            if (temporalSubset != null) {
                final Object timeObj = temporalSubset.getTimePositionOrTimePeriod().get(0);
                if (timeObj instanceof TimePositionType) {
                    time = ((TimePositionType) timeObj).getValue();
                } else {
                    time = null;
                }
            } else {
                time = null;
            }

            final SpatialSubsetType spatial = request.getDomainSubset().getSpatialSubSet();
            final EnvelopeEntry env = spatial.getEnvelope();
            final String crsName = env.getSrsName();
            //TODO: This will fail when we start working with OGC urn ids
            //      we will need to be much more sophisticated.
            try {
                crs = CRS.decode((crsName.startsWith("EPSG:")) ? crsName : "EPSG:" + crsName);
            } catch (FactoryException ex) {
                throw new CstlServiceException(ex, INVALID_CRS, getActingVersion());
            }
            objEnv = new GeneralEnvelope(crs);
            final List<DirectPositionType> positions = env.getPos();
            final DirectPositionType lonPos = positions.get(0);
            final DirectPositionType latPos = positions.get(1);
            objEnv.setRange(0, lonPos.getValue().get(0), lonPos.getValue().get(1));
            objEnv.setRange(1, latPos.getValue().get(0), latPos.getValue().get(1));

            //HACK: we actually need to build the envelope and then go find the
            //      data which the envelope intersects. Only then can we make an
            //      arbitrary choice.
            if (positions.size() > 2) {
                elevation = positions.get(2).getValue().get(0);
            } else {
                elevation = null;
            }

            if (temporalSubset == null && positions.size() == 0) {
                        throw new CstlServiceException("The parameters BBOX or TIME have to be specified",
                                       MISSING_PARAMETER_VALUE, getActingVersion());
            }
            /* here the parameter width and height (and depth for 3D matrix)
             *  have to be fill. If not they can be replace by resx and resy
             * (resz for 3D grid)
             */
            final GridType grid = spatial.getGrid();
            if (grid instanceof RectifiedGridType){
                resx = getParameter(KEY_RESX,  false);
                resy = getParameter(KEY_RESY,  false);
                resz = getParameter(KEY_RESZ,  false);

                width = Integer.parseInt(resx);
                height = Integer.parseInt(resy);
                if (grid.getDimension() > 2) {
                    depth = Integer.parseInt(resz);
                }
            } else {
                GridEnvelopeType gridEnv = grid.getLimits().getGridEnvelope();
                if (gridEnv.getHigh().size() > 0) {
                    width         = gridEnv.getHigh().get(0).intValue();
                    height        = gridEnv.getHigh().get(1).intValue();
                    
                    if (gridEnv.getHigh().size() == 3) {
                        depth     = gridEnv.getHigh().get(2).intValue();
                    }
                } else {
                     throw new CstlServiceException("you must specify grid size or resolution",
                                                      MISSING_PARAMETER_VALUE, getActingVersion());
                }
            }
        }

        Date date = null;
        try {
            date = QueryAdapter.toDate(time);
        } catch (ParseException ex) {
            LOGGER.log(Level.INFO, "Parsing of the date failed. Please verify that the specified" +
                    " date is compliant with the ISO-8601 standard.", ex);
        }

        dimension = new Dimension(width, height);
        
        /*
         * Generating the response.
         * It can be a text one (format MATRIX) or an image one (image/png, image/gif ...).
         */
        if ( format.equalsIgnoreCase(MATRIX) ) {
            
            //NOTE ADRIAN HACKED HERE
            final LayerDetails layerRef = getLayerReference(coverage);

            final RenderedImage image;
            try {
                final GridCoverage2D gridCov = layerRef.getCoverage(objEnv, dimension, elevation, date);
                image = gridCov.getRenderedImage();
            } catch (IOException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE, getActingVersion());
            } catch (CatalogException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE, getActingVersion());
            }

            final String mime = "application/matrix";
            return Response.ok(image, mime).build();
            
        } else if( format.equalsIgnoreCase(NETCDF) ){
            
            throw new CstlServiceException(new IllegalArgumentException(
                        "Constellation does not support netcdf writing."), NO_APPLICABLE_CODE, getActingVersion());
            
        } else if( format.equalsIgnoreCase(GEOTIFF) ){
            
            throw new CstlServiceException(new IllegalArgumentException(
                        "Constellation does not support geotiff writing."), NO_APPLICABLE_CODE, getActingVersion());
            
        } else {
            // We are in the case of an image format requested.
        	// TODO: This should be the fall through, add formats.
        	
            //NOTE: ADRIAN HACKED HERE
            
            // SCENE
            LayerDetails layerRef = getLayerReference(coverage);
              //if styles were defined they should be handled here.
            final Object style = null;
              //final MutableStyledLayerDescriptor mutSLD= null;
            final Map<String, Object> renderParameters = new HashMap<String, Object>();
            renderParameters.put(KEY_TIME, date);
            renderParameters.put("ELEVATION", elevation);
            Portrayal.SceneDef sdef = new Portrayal.SceneDef(layerRef, style, renderParameters);
            
            // VIEW
            final ReferencedEnvelope refEnvel = new ReferencedEnvelope(objEnv);
            final Double azimuth =  0.0; //HARD CODED SINCE PROTOCOL DOES NOT ALLOW
            Portrayal.ViewDef vdef = new Portrayal.ViewDef(refEnvel,azimuth);
            
            // CANVAS
            Portrayal.CanvasDef cdef = new Portrayal.CanvasDef(dimension,null);
            
            // IMAGE
            BufferedImage img;
            try {
                img = CstlPortrayalService.getInstance().portray(sdef, vdef, cdef);
            } catch (PortrayalException ex) {
                if (exceptions != null && exceptions.equalsIgnoreCase(EXCEPTIONS_INIMAGE)) {
                    img = CstlPortrayalService.getInstance().writeInImage(ex, dimension);
                } else {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE, getActingVersion());
                }
            }
            
            return Response.ok(img, format).build();
        }
    }


    /**
     * <p>The DescribeCoverage operation returns an XML file, containing the complete
     * description of a specific coverage.
     * </p>
     * <p>This method retrieves lots of supplementaries coverage definitions, and come
     * in addition to the GetCapabilities operation.
     * </p>
     *
     * @param abstractRequest a {@linkplain AbstractDescribeCoverage describe coverage request}
     *                        done by the user.
     * @return an XML document giving the full description of a coverage.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    //TODO: need to handle a list of coverages.
    public String describeCoverage(AbstractDescribeCoverage abstractRequest)
                                  throws JAXBException, CstlServiceException
    {
        LOGGER.info("describeCoverage request processing");

        //we begin by extracting the base attribute
        String inputVersion = abstractRequest.getVersion();
        if (inputVersion == null) {
            throw new CstlServiceException("The parameter SERVICE must be specified.",
                           MISSING_PARAMETER_VALUE, getActingVersion(), "version");
        } else {
           isVersionSupported(inputVersion);
           setActingVersion(inputVersion);
        }

        //we prepare the response object to return
        Object response;

        if (getActingVersion().toString().equals("1.0.0")) {
            final org.constellation.wcs.v100.DescribeCoverage request =
                    (org.constellation.wcs.v100.DescribeCoverage) abstractRequest;
            if (request.getCoverage().size() == 0) {
                throw new CstlServiceException("The parameter COVERAGE must be specified.",
                        MISSING_PARAMETER_VALUE, getActingVersion(), "coverage");
            }
            
            //TODO: we should loop over the list
            final LayerDetails layerRef = getLayerReference( request.getCoverage().get(0) );
            
            final List<CoverageOfferingType> coverages = new ArrayList<CoverageOfferingType>();
            final Set<Series> series = layerRef.getSeries();
            if (series == null || series.isEmpty()) {
                throw new CstlServiceException("The coverage " + layerRef.getName() + " is not defined.",
                        LAYER_NOT_DEFINED, getActingVersion());
            }
            final GeographicBoundingBox inputGeoBox;
            try {
                inputGeoBox = layerRef.getGeographicBoundingBox();
            } catch (CatalogException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, getActingVersion());
            }
            final String srsName = "urn:ogc:def:crs:OGC:1.3:CRS84";
            final LonLatEnvelopeType llenvelope;
            if (inputGeoBox != null) {
                final SortedSet<Number> elevations;
                try {
                    elevations = layerRef.getAvailableElevations();
                } catch (CatalogException ex) {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }
                List<Double> pos1 = new ArrayList<Double>();
                pos1.add(inputGeoBox.getWestBoundLongitude());
                pos1.add(inputGeoBox.getSouthBoundLatitude());

                List<Double> pos2 = new ArrayList<Double>();
                pos2.add(inputGeoBox.getEastBoundLongitude());
                pos2.add(inputGeoBox.getNorthBoundLatitude());

                if (elevations != null && elevations.size() >= 2) {
                    pos1.add(elevations.first().doubleValue());
                    pos2.add(elevations.last().doubleValue());
                }
                List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
                pos.add(new DirectPositionType(pos1));
                pos.add(new DirectPositionType(pos2));
                llenvelope = new LonLatEnvelopeType(pos, srsName);
            } else {
                throw new CstlServiceException("The geographic bbox for the layer is null !", NO_APPLICABLE_CODE);
            }
            final Keywords keywords = new Keywords("WCS", layerRef.getName(),
                    Util.cleanSpecialCharacter(layerRef.getThematic()));

            //Spatial metadata
            final org.constellation.wcs.v100.SpatialDomainType spatialDomain =
                    new org.constellation.wcs.v100.SpatialDomainType(llenvelope);

            // temporal metadata
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            final List<Object> times = new ArrayList<Object>();
            final SortedSet<Date> dates;
            try {
               dates = layerRef.getAvailableTimes();
            } catch (CatalogException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE, getActingVersion());
            }
            for (Date d : dates) {
                times.add(new TimePositionType(df.format(d)));
            }
            final org.constellation.wcs.v100.TimeSequenceType temporalDomain =
                    new org.constellation.wcs.v100.TimeSequenceType(times);

            final DomainSetType domainSet = new DomainSetType(spatialDomain, temporalDomain);

            //TODO complete
            final RangeSetType rangeSetT = new RangeSetType(null, layerRef.getName(),
                    layerRef.getName(),
                    null,
                    null,
                    null,
                    null);
            final RangeSet rangeSet = new RangeSet(rangeSetT);
            //supported CRS
            final SupportedCRSsType supCRS = new SupportedCRSsType(new CodeListType("EPSG:4326"));

            // supported formats
            final Set<CodeListType> formats = new LinkedHashSet<CodeListType>();
            formats.add(new CodeListType("matrix"));
            formats.add(new CodeListType("jpeg"));
            formats.add(new CodeListType("png"));
            formats.add(new CodeListType("gif"));
            formats.add(new CodeListType("bmp"));
            String nativeFormat = "unknow";
            Iterator<Series> it = layerRef.getSeries().iterator();
            if (it.hasNext()) {
                Series s = it.next();
                nativeFormat = s.getFormat().getImageFormat();
            }
            final SupportedFormatsType supForm = new SupportedFormatsType(nativeFormat, new ArrayList<CodeListType>(formats));

            //supported interpolations
            final List<org.constellation.wcs.v100.InterpolationMethod> interpolations =
                    new ArrayList<org.constellation.wcs.v100.InterpolationMethod>();
            interpolations.add(org.constellation.wcs.v100.InterpolationMethod.BILINEAR);
            interpolations.add(org.constellation.wcs.v100.InterpolationMethod.BICUBIC);
            interpolations.add(org.constellation.wcs.v100.InterpolationMethod.NEAREST_NEIGHBOR);
            final SupportedInterpolationsType supInt =
                    new SupportedInterpolationsType(org.constellation.wcs.v100.InterpolationMethod.NEAREST_NEIGHBOR, interpolations);

            //we build the coverage offering for this layer/coverage
            final CoverageOfferingType coverage = new CoverageOfferingType(null,
                    layerRef.getName(),
                    layerRef.getName(),
                    Util.cleanSpecialCharacter(layerRef.getRemarks()),
                    llenvelope,
                    keywords,
                    domainSet,
                    rangeSet,
                    supCRS,
                    supForm,
                    supInt);

            coverages.add(coverage);
            response = new CoverageDescription(coverages, "1.0.0");

        // describeCoverage version 1.1.1
        } else {
            org.constellation.wcs.v111.DescribeCoverage request = (org.constellation.wcs.v111.DescribeCoverage) abstractRequest;
            if (request.getIdentifier().size() == 0) {
                throw new CstlServiceException("The parameter IDENTIFIER must be specified",
                        MISSING_PARAMETER_VALUE, getActingVersion(), "identifier");
            }
            
            //TODO: we should loop over the list
            final LayerDetails layer = getLayerReference(request.getIdentifier().get(0));
            
            final org.constellation.ows.v110.ObjectFactory owsFactory = new org.constellation.ows.v110.ObjectFactory();
            final List<CoverageDescriptionType> coverages = new ArrayList<CoverageDescriptionType>();
            if (layer.getSeries().size() == 0) {
                throw new CstlServiceException("the coverage " + layer.getName() +
                        " is not defined", LAYER_NOT_DEFINED, getActingVersion());
            }
            final GeographicBoundingBox inputGeoBox;
            try {
                inputGeoBox = layer.getGeographicBoundingBox();
            } catch (CatalogException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, getActingVersion());
            }
            List<JAXBElement<? extends BoundingBoxType>> bboxs = new ArrayList<JAXBElement<? extends BoundingBoxType>>();
            if (inputGeoBox != null) {
                WGS84BoundingBoxType outputBBox = new WGS84BoundingBoxType(
                        inputGeoBox.getWestBoundLongitude(),
                        inputGeoBox.getSouthBoundLatitude(),
                        inputGeoBox.getEastBoundLongitude(),
                        inputGeoBox.getNorthBoundLatitude());
                bboxs.add(owsFactory.createWGS84BoundingBox(outputBBox));

                String crs = "EPSG:4326";
                BoundingBoxType outputBBox2 = new BoundingBoxType(crs,
                        inputGeoBox.getWestBoundLongitude(),
                        inputGeoBox.getSouthBoundLatitude(),
                        inputGeoBox.getEastBoundLongitude(),
                        inputGeoBox.getNorthBoundLatitude());

                bboxs.add(owsFactory.createBoundingBox(outputBBox2));
            }

            //general metadata
            final List<LanguageStringType> title = new ArrayList<LanguageStringType>();
            title.add(new LanguageStringType(layer.getName()));
            final List<LanguageStringType> _abstract = new ArrayList<LanguageStringType>();
            _abstract.add(new LanguageStringType(Util.cleanSpecialCharacter(layer.getRemarks())));
            final List<KeywordsType> keywords = new ArrayList<KeywordsType>();
            keywords.add(new KeywordsType(new LanguageStringType("WCS"),
                    new LanguageStringType(layer.getName())));

            // spatial metadata
            final org.constellation.wcs.v111.SpatialDomainType spatial =
                    new org.constellation.wcs.v111.SpatialDomainType(bboxs);

            // temporal metadata
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            final List<Object> times = new ArrayList<Object>();
            final SortedSet<Date> dates;
            try {
               dates = layer.getAvailableTimes();
            } catch (CatalogException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE, getActingVersion());
            }
            for (Date d : dates) {
                times.add(new TimePositionType(df.format(d)));
            }
            final org.constellation.wcs.v111.TimeSequenceType temporalDomain =
                    new org.constellation.wcs.v111.TimeSequenceType(times);

            final CoverageDomainType domain = new CoverageDomainType(spatial, temporalDomain);

            //supported interpolations
            final List<InterpolationMethodType> intList = new ArrayList<InterpolationMethodType>();
            intList.add(new InterpolationMethodType(
                    org.constellation.wcs.v111.InterpolationMethod.BILINEAR.value(), null));
            intList.add(new InterpolationMethodType(
                    org.constellation.wcs.v111.InterpolationMethod.BICUBIC.value(), null));
            intList.add(new InterpolationMethodType(
                    org.constellation.wcs.v111.InterpolationMethod.NEAREST_NEIGHBOR.value(), null));
            final InterpolationMethods interpolations =
                    new InterpolationMethods(intList, org.constellation.wcs.v111.InterpolationMethod.NEAREST_NEIGHBOR.value());
            final RangeType range = new RangeType(new FieldType(Util.cleanSpecialCharacter(layer.getThematic()),
                    null,
                    new org.constellation.ows.v110.CodeType("0.0"),
                    interpolations));

            //supported CRS
            final List<String> supportedCRS = new ArrayList<String>();
            supportedCRS.add("EPSG:4326");

            //supported formats
            final List<String> supportedFormat = new ArrayList<String>();
            supportedFormat.add("application/matrix");
            supportedFormat.add("image/png");
            supportedFormat.add("image/jpeg");
            supportedFormat.add("image/bmp");
            supportedFormat.add("image/gif");
            final CoverageDescriptionType coverage = new CoverageDescriptionType(title,
                    _abstract,
                    keywords,
                    layer.getName(),
                    domain,
                    range,
                    supportedCRS,
                    supportedFormat);

            coverages.add(coverage);

            response = new CoverageDescriptions(coverages);
        }

        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();
        marshaller.marshal(response, sw);
        return sw.toString();
    }

    /**
     * update The URL in capabilities document with the service actual URL.
     */
    private void updateURL(List<DCPTypeType> dcpList) {
        for(DCPTypeType dcp: dcpList) {
           for (Object obj: dcp.getHTTP().getGetOrPost()){
               if (obj instanceof Get){
                   Get getMethod = (Get)obj;
                   getMethod.getOnlineResource().setHref(getServiceURL() + "wcs?SERVICE=WCS&");
               } else if (obj instanceof Post){
                   Post postMethod = (Post)obj;
                   postMethod.getOnlineResource().setHref(getServiceURL() + "wcs?SERVICE=WCS&");
               }
           }
        }
    }


    /**
     * Parses a value as a floating point.
     *
     * @throws CstlServiceException if the value can't be parsed.
     */
    private double parseDouble(String value) throws CstlServiceException {
        value = value.trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new CstlServiceException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, value) + "cause:" +
                           exception.getMessage(), INVALID_PARAMETER_VALUE, getActingVersion());
        }
    }
    
    
    //TODO: handle the null value in the exception.
    //TODO: harmonize with the method getLayerReference().
    private List<LayerDetails> getAllLayerReferences() throws CstlServiceException {

    	List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
    	try { // WE catch the exception from either service version
	        if ( getActingVersion().toString().equals("1.0.0") ) {
	        	layerRefs = Cstl.Register.getAllLayerReferences(ServiceDef.WCS_1_0_0 );
	        } else if ( getActingVersion().toString().equals("1.1.0") ) {
	        	layerRefs = Cstl.Register.getAllLayerReferences(ServiceDef.WCS_1_1_0 );
	        } else if ( getActingVersion().toString().equals("1.1.1") ) {
	        	layerRefs = Cstl.Register.getAllLayerReferences(ServiceDef.WCS_1_1_1 );
	        } else if ( getActingVersion().toString().equals("1.1.2") ) {
	        	layerRefs = Cstl.Register.getAllLayerReferences(ServiceDef.WCS_1_1_2 );
	        } else {
	        	throw new CstlServiceException("WCS acting according to no known version.", null, getActingVersion());
	        }
        } catch (RegisterException regex ){
        	throw new CstlServiceException("Could not obtain the requested coverage.", INVALID_PARAMETER_VALUE, getActingVersion());
        }
        return layerRefs;
    }
    
    //TODO: handle the null value in the exception.
    //TODO: harmonize with the method getAllLayerReferences().
    //TODO: distinguish exceptions: layer doesn't exist and layer could not be obtained.
    private LayerDetails getLayerReference(String layerName) throws CstlServiceException {

    	LayerDetails layerRef;
    	try { // WE catch the exception from either service version
        	if ( getActingVersion().toString().equals("1.0.0") ){
        		layerRef = Cstl.Register.getLayerReference(ServiceDef.WCS_1_0_0, layerName);
        	} else if ( getActingVersion().toString().equals("1.1.0") ) {
        		layerRef = Cstl.Register.getLayerReference(ServiceDef.WCS_1_1_0, layerName);
        	} else if ( getActingVersion().toString().equals("1.1.2") ) {
        		layerRef = Cstl.Register.getLayerReference(ServiceDef.WCS_1_1_2, layerName);
        	} else {
        		throw new CstlServiceException("WCS acting according to no known version.", null, getActingVersion());
        	}
        } catch (RegisterException regex ){
        	throw new CstlServiceException("Could not obtain the requested coverage.", INVALID_PARAMETER_VALUE, getActingVersion());
        }
        return layerRef;
    }
    
    
    
    @PreDestroy
    @Override
    public void destroy() {
        LOGGER.info("Destroying WCS service");
    }
}
