/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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

package net.seagis.coverage.wms;

import java.io.File;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.units.Unit;

// jersey dependencies
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response;
import com.sun.ws.rest.spi.resource.Singleton;

// JAXB xml binding dependencies
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

//seagis dependencies
import net.seagis.catalog.CatalogException;
import net.seagis.sld.DescribeLayerResponseType;
import net.seagis.sld.LayerDescriptionType;
import net.seagis.sld.StyledLayerDescriptor;
import net.seagis.wms.Layer;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.web.WebServiceWorker;
import net.seagis.gml.DirectPositionType;
import net.seagis.gml.PointType;
import net.seagis.se.OnlineResourceType;
import net.seagis.sld.TypeNameType;
import net.seagis.wms.AbstractWMSCapabilities;
import net.seagis.wms.BoundingBox;
import net.seagis.wms.Dimension;
import net.seagis.wms.EXGeographicBoundingBox;
import net.seagis.wms.LegendURL;
import net.seagis.wms.OnlineResource;
import net.seagis.wms.Style;

//geotools dependencies
import org.geotools.util.MeasurementRange;
import org.opengis.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 * WMS 1.3.0 web service implementing the operation getMap, getFeatureInfo and getCapabilities.
 *
 * @version
 * @author Guilhem Legal
 */
@UriTemplate("wms")
@Singleton
public class WMService extends WebService {
    
    /**
     * The http context containing the request parameter
     */
    @HttpContext
    private UriInfo context;
    
            
    /** 
     * Build a new instance of the webService and initialise the JAXB marshaller. 
     */
    public WMService() throws JAXBException, WebServiceException {
        super("WMS", "1.3.0","1.1.1");

        //we build the JAXB marshaller and unmarshaller to bind java/xml
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.ogc:net.seagis.wms:net.seagis.sld:net.seagis.gml");
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl("http://www.opengis.net/wms"));
        unmarshaller = jbcontext.createUnmarshaller();
        
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        
    }
   
    
    /**
     * Treat the incomming GET request.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @HttpMethod("GET")
    public Response doGET() throws JAXBException  {

        return treatIncommingRequest();
    }
    
    /**
     * Treat the incomming POST request.
     * 
     * @param request The url request.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @HttpMethod("POST")
    public Response doPOST(String request) throws JAXBException  {
        logger.info("request: " + request);
        final StringTokenizer tokens = new StringTokenizer(request, "&");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            String paramName  = token.substring(0, token.indexOf('='));
            String paramValue = token.substring(token.indexOf('=')+ 1);
            logger.info("put: " + paramName + "=" + paramValue);
            context.getQueryParameters().add(paramName, paramValue);
        }
        
        return treatIncommingRequest();
    }
    
    
    /**
     * Treat the incomming request and call the right function.
     * 
     * @return an image or xml response.
     * @throw JAXBException
     */
    @Override
    public Response treatIncommingRequest() throws JAXBException {
        
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        try {
            String request = (String) getParameter("REQUEST", true);
            if (request.equalsIgnoreCase("GetMap")) {
                    
                return Response.Builder.representation(getMap(), webServiceWorker.getMimeType()).build();
                    
            } else if (request.equals("GetFeatureInfo")) {
                    
                return getFeatureInfo();
                    
            } else if (request.equalsIgnoreCase("GetCapabilities")) {
                    
                return getCapabilities();
                        
            } else if (request.equalsIgnoreCase("DescribeLayer")) {
                    
                return Response.Builder.representation(describeLayer(), "text/xml").build();
                    
            } else if (request.equalsIgnoreCase("GetLegendGraphic")) {
                    
                return Response.Builder.representation(getLegendGraphic(), webServiceWorker.getMimeType()).build();
                    
            } else {
                throw new WebServiceException("The operation " + request + " is not supported by the service",
                                              WMSExceptionCode.OPERATION_NOT_SUPPORTED, getCurrentVersion());
            }
        } catch (WebServiceException ex) {
            ex.printStackTrace();
            StringWriter sw = new StringWriter();    
            marshaller.marshal(ex.getServiceExceptionReport(), sw);
            return Response.Builder.representation(sw.toString(), "text/xml").build();
        }
    }
    
    
    /**
     * Return a map for the specified parameters in the query.
     * 
     * @return
     * @throws fr.geomatys.wms.WebServiceException
     */
    private File getMap() throws  WebServiceException {
        logger.info("getMap request received");
        verifyBaseParameter(0);
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we set the attribute od the webservice worker with the parameters.
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        webServiceWorker.setFormat(getParameter("FORMAT", true));
        webServiceWorker.setLayer(getParameter("LAYERS", true));
        webServiceWorker.setColormapRange(getParameter("DIM_RANGE", false));
        
        String crs;
        if (getCurrentVersion().toString().equals("1.3.0")) {
            crs = getParameter("CRS", true);
        } else {
            crs = getParameter("SRS", true);
        }
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(getParameter("BBOX", true));
        webServiceWorker.setElevation(getParameter("ELEVATION", false));
        webServiceWorker.setTime(getParameter("TIME", false));
        webServiceWorker.setDimension(getParameter("WIDTH", true), getParameter("HEIGHT", true));
        webServiceWorker.setBackgroundColor(getParameter("BGCOLOR", false));
        webServiceWorker.setTransparency(getParameter("TRANSPARENT", false));
        
        
        //this parameters are not yet used
        String styles      = getParameter("STYLES", true);
        
        //extended parameter of the specification SLD
        String sld           = getParameter("SLD", false);
        String remoteOwsType = getParameter("REMOTE_OWS_TYPE", false);
        String remoteOwsUrl  = getParameter("REMOTE_OWS_URL", false);
        
        return webServiceWorker.getImageFile();
    }
    
    /**
     * Return the value of a point in a map.
     *  
     * @return text, HTML , XML or GML code.
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     */
    private Response getFeatureInfo() throws WebServiceException, JAXBException {
        logger.info("getFeatureInfo request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        verifyBaseParameter(0);
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        String layer = getParameter("QUERY_LAYERS", true);
        webServiceWorker.setLayer(layer);
        
        String crs;
        if (getCurrentVersion().toString().equals("1.3.0")){
            crs = getParameter("CRS", true);
        } else {
            crs = getParameter("SRS", true);
        }
        webServiceWorker.setCoordinateReferenceSystem(crs);
        webServiceWorker.setBoundingBox(getParameter("BBOX", true));
        webServiceWorker.setElevation(getParameter("ELEVATION", false));
        webServiceWorker.setTime(getParameter("TIME", false));
        webServiceWorker.setDimension(getParameter("WIDTH", true), getParameter("HEIGHT", true));
        

        final String i, j;
        if (getCurrentVersion().toString().equals("1.3.0")) {
            i = getParameter("I", true);
            j = getParameter("J", true);
        } else {
            i = getParameter("X", true);
            j = getParameter("Y", true);
        }
        
        String infoFormat  = getParameter("INFO_FORMAT", false); // TODO true);
        if (infoFormat != null) {
            if(!(infoFormat.equals("text/plain") 
              || infoFormat.equals("text/html") 
              || infoFormat.equals("application/vnd.ogc.gml") 
              || infoFormat.equals("text/xml"))){
                
                throw new WebServiceException("This MIME type " + infoFormat + " is not accepted by the service",
                                              WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
            }
        } else {
            infoFormat = "text/plain";
        }
        String feature_count = getParameter("FEATURE_COUNT", false);
        
        String exception = getParameter("EXCEPTIONS", false);
        if ( exception == null)
            exception = "XML";
        
        double result = webServiceWorker.evaluatePixel(i,j);
        
        // there is many return type possible
        String response;
        
        // if we return html
        if (infoFormat.equals("text/html")) {
            response = "<html>"                                       +
                       "    <head>"                                   +
                       "        <title>GetFeatureInfo output</title>" +
                       "    </head>"                                  +
                       "    <body>"                                   +
                       "    <table>"                                  +
                       "        <tr>"                                 +
                       "            <th>" + layer + "</th>"           +
                       "        </tr>"                                +
                       "        <tr>"                                 +
                       "            <th>" + result + "</th>"          +
                       "        </tr>"                                +
                       "    </table>"                                 +
                       "    </body>"                                  +
                       "</html>";
        }
        //if we return xml or gml
        else if (infoFormat.equals("text/xml") || infoFormat.equals("application/vnd.ogc.gml")) {
            DirectPosition inputCoordinate = webServiceWorker.getCoordinates();
            List<Double> coord = new ArrayList<Double>();
            for (Double d:inputCoordinate.getCoordinates()) {
                coord.add(d);
            }
            coord.add(result);
            List<String> axisLabels = new ArrayList<String>();
            axisLabels.add("X");
            axisLabels.add("Y");
            axisLabels.add("RESULT");
            DirectPositionType pos = new DirectPositionType(crs, 3, axisLabels, coord);
            PointType pt = new PointType(layer, pos);
            
            //we marshall the response and return the XML String
            StringWriter sw = new StringWriter();    
            marshaller.marshal(pt, sw);
            response = sw.toString();
        }
        
        //if we return text
        else {
            response = "result for " + layer + " is:" + result;
        }
        return Response.Builder.representation(response, infoFormat).build();
    }
    
    /**
     * Describe the capabilities and the layers available of this service.
     * 
     * @return a WMSCapabilities XML document describing the capabilities of the service.
     * 
     * @throws net.seagis.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private Response getCapabilities() throws WebServiceException, JAXBException {
        logger.info("getCapabilities request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        //we begin by extract the mandatory attribute
        if (!getParameter("SERVICE", true).equalsIgnoreCase("WMS")) {
            throw new WebServiceException("The parameters SERVICE=WMS must be specify",
                                         WMSExceptionCode.MISSING_PARAMETER_VALUE, getCurrentVersion());
        }
        
        //and the the optional attribute
        String inputVersion = getParameter("VERSION", false);
        if(inputVersion != null && inputVersion.equals("1.3.0")) {
            setCurrentVersion("1.3.0");
        } else {
            setCurrentVersion("1.1.1");
        } 
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        String format = getParameter("FORMAT", false);
        if (format == null ) {
            format = "text/xml";
        } else if (!(format.equals("text/xml") || format.equals("application/vnd.ogc.wms_xml"))) {
            throw new WebServiceException("Allowed format for GetCapabilities are : text/xml or application/vnd.ogc.wms_xml.",
                      WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
        }
        
        // the service shall return WMSCapabilities marshalled
        AbstractWMSCapabilities response = (AbstractWMSCapabilities)getCapabilitiesObject(getCurrentVersion());
        
        //we update the url in the static part.
        response.getCapability().getRequest().getGetCapabilities().getDCPType().get(0).getHTTP().getGet().getOnlineResource().setHref(getServiceURL() + "wms?REQUEST=GetCapabilities");
        response.getCapability().getRequest().getGetFeatureInfo().getDCPType().get(0).getHTTP().getGet().getOnlineResource().setHref(getServiceURL() + "wms?REQUEST=GetFeatureInfo");
        response.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGet().getOnlineResource().setHref(getServiceURL() + "wms?REQUEST=GetMap");
        response.getCapability().getRequest().getExtendedOperation().get(0).getValue().getDCPType().get(0).getHTTP().getGet().getOnlineResource().setHref(getServiceURL() + "wms?REQUEST=DescribeLayer");
        response.getCapability().getRequest().getExtendedOperation().get(1).getValue().getDCPType().get(0).getHTTP().getGet().getOnlineResource().setHref(getServiceURL() + "wms?REQUEST=GetLegendGraphic");
        
        //we build the layers object of the document
        
        //we get the list of layers
        List<Layer> layers = new ArrayList<Layer>();
        for (net.seagis.coverage.catalog.Layer inputLayer: webServiceWorker.getLayers()) {
            try {
                
                List<String> crs = new ArrayList<String>();
                
                Integer code = 4326;
                //code = CRS.lookupEpsgCode(inputLayer.getCoverage().getEnvelope().getCoordinateReferenceSystem(), false);
                
                if(code != null)
                    crs.add(code.toString());
                
                GeographicBoundingBox inputGeoBox = inputLayer.getGeographicBoundingBox();
               
                /*
                 * TODO
                 * Envelope inputBox                 = inputLayer.getCoverage().getEnvelope();
                 */
                BoundingBox outputBBox = null;
                if(inputGeoBox != null) {
                    outputBBox = new BoundingBox(code.toString(), 
                                                 inputGeoBox.getWestBoundLongitude(),
                                                 inputGeoBox.getEastBoundLongitude(),
                                                 inputGeoBox.getSouthBoundLatitude(),
                                                 inputGeoBox.getNorthBoundLatitude(),
                                                 0.0, 0.0,
                                                 getCurrentVersion());
                }
                //we add the list od available date and elevation
                List<Dimension> dimensions = new ArrayList<Dimension>();
                
                
                //the available date
                String defaut = null;
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                Dimension dim;
                String value = "";
                SortedSet<Date> dates = inputLayer.getAvailableTimes();
                if (dates.size() > 0) {
                    defaut = df.format(dates.last());
                
                
                    dim = new Dimension("time", "ISO8601", defaut, null);
                    for (Date d:dates){
                        value += df.format(d) + ','; 
                    }
                    dim.setValue(value);
                    dimensions.add(dim);
                }
                
                //the available elevation
                defaut = null;
                SortedSet<Number> elevations = inputLayer.getAvailableElevations();
                if (elevations.size() > 0) {
                    defaut = elevations.first().toString();
                
                    dim = new Dimension("elevation", "EPSG:5030", defaut, null);
                    value = "";
                    for (Number n:elevations){
                        value += n.toString() + ','; 
                    }
                    dim.setValue(value);
                    dimensions.add(dim);
                }
                
                //the dimension range
                defaut      = null;
                String unit = null;
                MeasurementRange[] ranges = inputLayer.getSampleValueRanges();
                if (ranges!= null && ranges.length>0 && ranges[0]!= null) {
                    defaut = ranges[0].getMinimum() + "," + ranges[0].getMaximum();
                    Unit u = ranges[0].getUnits();
                    if (u != null)
                        unit = u.toString();
                    dim = new Dimension("dim_range", unit, defaut, ranges[0].getMinimum() + "," + ranges[0].getMaximum());
                    dimensions.add(dim);
                }
                
                // we build a Style Object
                OnlineResource or = new OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/png&LAYER=" + inputLayer.getName());
                LegendURL legendURL1 = new LegendURL("image/png", or);

                or = new OnlineResource(getServiceURL() + "wms?REQUEST=GetLegendGraphic&VERSION=1.1.0&FORMAT=image/gif&LAYER=" + inputLayer.getName());
                LegendURL legendURL2 = new LegendURL("image/gif", or);
                Style style = new Style("Style1", "default Style", null, null, null,legendURL1,legendURL2);
                
                //we build and add a layer 
                Layer outputLayer = new Layer(inputLayer.getName(), 
                                              inputLayer.getRemarks(),
                                              inputLayer.getThematic(), 
                                              crs, 
                                              new EXGeographicBoundingBox(inputGeoBox.getWestBoundLongitude(), 
                                                                          inputGeoBox.getEastBoundLongitude(), 
                                                                          inputGeoBox.getSouthBoundLatitude(), 
                                                                          inputGeoBox.getNorthBoundLatitude()), 
                                              outputBBox,  
                                              true,
                                              dimensions,
                                              style,
                                              getCurrentVersion());
                layers.add(outputLayer);
                
            } catch (CatalogException exception) {
                throw new WebServiceException(exception, WMSExceptionCode.NO_APPLICABLE_CODE, getCurrentVersion());
            }
        }
       
        
        //we build the list of accepted crs
        List<String> crs = new ArrayList<String>();
        crs.add("EPSG:4326");crs.add("EPSG:3395");crs.add("EPSG:27574");
        
        //we build a general boundingbox
        EXGeographicBoundingBox exGeographicBoundingBox = null;
        //we build the general layer and add it to the document
        Layer layer = new Layer("Seagis Web Map Layer", 
                                "description of the service(need to be fill)", 
                                crs, 
                                exGeographicBoundingBox, 
                                layers,
                                getCurrentVersion());
        
        response.getCapability().setLayer(layer);
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
         
        return Response.Builder.representation(sw.toString(), format).build();
        
    }
    
    
    
    /**
     * 
     * @return
     * @throws net.seagis.coverage.web.WebServiceException
     * @throws javax.xml.bind.JAXBException
     */
    private String describeLayer() throws WebServiceException, JAXBException {
        logger.info("describeLayer request received");
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        verifyBaseParameter(2);
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        
        OnlineResourceType or = new OnlineResourceType(getServiceURL() + "wcs?");
        List<LayerDescriptionType> layersDescriptions = new ArrayList<LayerDescriptionType>();
        String layers = getParameter("LAYERS", true);
        Set<String> registredLayers = webServiceWorker.getLayerNames();
        final StringTokenizer tokens = new StringTokenizer(layers, ",");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            if (registredLayers.contains(token)) {
                TypeNameType t = new TypeNameType(token);
                LayerDescriptionType outputLayer = new LayerDescriptionType(or,t);
                layersDescriptions.add(outputLayer);
            } else {
                throw new WebServiceException("This layer is not registred: " + token,
                      WMSExceptionCode.INVALID_PARAMETER_VALUE, getCurrentVersion());
            }
        }
                 
        DescribeLayerResponseType response = new DescribeLayerResponseType(getSldVersion().toString(), layersDescriptions);
       
        //we marshall the response and return the XML String
        StringWriter sw = new StringWriter();    
        marshaller.marshal(response, sw);
        return sw.toString();
    }
    
    
    private File getLegendGraphic() throws WebServiceException, JAXBException {
        final WebServiceWorker webServiceWorker = this.webServiceWorker.get();
        
        verifyBaseParameter(2);
        webServiceWorker.setService("WMS", getCurrentVersion().toString());
        webServiceWorker.setLayer(getParameter("LAYER", true));
        webServiceWorker.setFormat(getParameter("FORMAT", false));
        webServiceWorker.setDimension(getParameter("WIDTH", false), getParameter("HEIGHT", false));

        
        String style = getParameter("STYLE", false);
       
        String featureType   = getParameter("FEATURETYPE", false);
        String remoteSld     = getParameter("SLD", false);
        String remoteOwsType = getParameter("REMOTE_OWS_TYPE", false);
        String remoteOwsUrl  = getParameter("REMOTE_OWS_URL", false);
        String coverage      = getParameter("COVERAGE", false);
        String rule          = getParameter("RULE", false);
        String scale         = getParameter("SCALE", false);
        
        StyledLayerDescriptor sld = (StyledLayerDescriptor) getComplexParameter("SLD_BODY", false);
        
        return  webServiceWorker.getLegendFile();
        
    }
    
    /**
     * Return the current Http context. 
     */
    @Override
    protected UriInfo getContext() {
        return this.context;
    }
   
}
