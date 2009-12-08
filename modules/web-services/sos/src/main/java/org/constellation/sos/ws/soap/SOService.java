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
package org.constellation.sos.ws.soap;

// JDK dependencies
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// JAX-WS dependencies
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import javax.xml.bind.annotation.XmlSeeAlso;
import org.constellation.ServiceDef;
import org.constellation.provider.configuration.ConfigDirectory;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.DescribeSensor;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.GetResult;
import org.geotoolkit.sos.xml.v100.GetResultResponse;
import org.geotoolkit.sos.xml.v100.InsertObservation;
import org.geotoolkit.sos.xml.v100.InsertObservationResponse;
import org.geotoolkit.sos.xml.v100.RegisterSensor;
import org.geotoolkit.sos.xml.v100.RegisterSensorResponse;
import org.constellation.sos.ws.SOSworker;
import org.constellation.util.Util;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.xml.MarshallerPool;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@WebService(name = "SOService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@XmlSeeAlso({org.geotoolkit.sml.xml.v100.ObjectFactory.class,
             org.geotoolkit.sml.xml.v101.ObjectFactory.class,
             org.geotoolkit.sampling.xml.v100.ObjectFactory.class})
public class SOService {
    
    /**
     * use for debugging purpose
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.sos");
    
    /**
     * A map containing the Capabilities Object already load from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();
    
    /**
     * a service worker
     */
    private SOSworker worker;
    
    /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    private MarshallerPool marshallerPool;
    
    /**
     * Initialize the database connection.
     */
    public SOService() throws CstlServiceException {
       worker                      = new SOSworker(null);
        try {
            marshallerPool = new MarshallerPool("org.geotoolkit.sos.xml.v100:org.geotoolkit.observation.xml.v100");
        } catch (JAXBException ex) {
           LOGGER.log(Level.SEVERE, "unable to create the JAXBContext", ex);
        }

       //TODO find real url
       worker.setServiceURL("http://localhost:8080/SOServer/SOService");
    }
    
    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     * @throws SOServiceException
     */
    @WebMethod(action="getCapabilities")
    public Capabilities getCapabilities(@WebParam(name = "GetCapabilities") GetCapabilities requestCapabilities) throws SOServiceException  {
        try {
            LOGGER.info("received SOAP getCapabilities request");
            worker.setSkeletonCapabilities((Capabilities)getCapabilitiesObject());
             
            return worker.getCapabilities(requestCapabilities);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation whitch return an sml description of the specified sensor.
     * 
     * @param requestDescSensor A document specifying the id of the sensor that we want the description.
     * @throws SOServiceException
     */
    @WebMethod(action="describeSensor")
    public AbstractSensorML describeSensor(@WebParam(name = "DescribeSensor") DescribeSensor requestDescSensor) throws SOServiceException  {
        try {
            LOGGER.info("received SOAP DescribeSensor request");
            return worker.describeSensor(requestDescSensor);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    
    /**
     * Web service operation whitch respond a collection of observation satisfying 
     * the restriction specified in the query.
     * 
     * @param requestObservation a document specifying the parameter of the request.
     * @throws SOServiceException
     */
    @WebMethod(action="getObservation")
    public ObservationCollectionEntry getObservation(@WebParam(name = "GetObservation") GetObservation requestObservation) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getObservation request");
            return (ObservationCollectionEntry) worker.getObservation(requestObservation);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation
     *
     * @throws SOServiceException
     */
    @WebMethod(action="getResult")
    public GetResultResponse getResult(@WebParam(name = "GetResult") GetResult requestResult) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getResult request");
            return worker.getResult(requestResult);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation whitch register a Sensor in the SensorML database, 
     * and initialize its observation by adding an observation template in the O&M database.
     *
     * @param requestRegSensor A request containing a SensorML File describing a Sensor,
     *                         and an observation template for this sensor.
     * @throws SOServiceException
     */
    @WebMethod(action="registerSensor")
    public RegisterSensorResponse registerSensor(@WebParam(name = "RegisterSensor") RegisterSensor requestRegSensor) throws SOServiceException {
        try {
            LOGGER.info("received SOAP registerSensor request");
            return worker.registerSensor(requestRegSensor);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation whitch insert a new Observation for the specified sensor
     * in the O&M database.
     * 
     * @param requestInsObs an InsertObservation request containing an O&M object and a Sensor id.
     * @throws SOServiceException
     */
    @WebMethod(action="InsertObservation")
    public InsertObservationResponse insertObservation(@WebParam(name = "InsertObservation") InsertObservation requestInsObs) throws SOServiceException {
        try {
            LOGGER.info("received SOAP insertObservation request");
            return worker.insertObservation(requestInsObs);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param  version the version of the service.
     * @return The capabilities Object, or {@code null} if none.
     * @throws JAXBException
     */
    public Object getCapabilitiesObject() {
        final String fileName     = "SOSCapabilities1.0.0.xml";
        Object response           = capabilities.get(fileName);
        if (response == null) {
            final String configUrl    = "sos_configuration";
            File configDir            = new File(ConfigDirectory.getConfigDirectory(), configUrl);
            if (configDir.exists()) {
                LOGGER.info("taking configuration from constellation directory: " + configDir.getPath());
            } else {
                return Util.getDirectoryFromResource(configUrl);
            }
            try {
                Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                final File f              = new File(configDir, fileName);
                LOGGER.info(f.toString());
                response                  = unmarshaller.unmarshal(f);
                marshallerPool.release(unmarshaller);
            } catch(JAXBException ex) {
                LOGGER.log(Level.SEVERE, "unable to unmarshall the capabilities file", ex);
            }
            capabilities.put(fileName, response);
            
        }
        return response;
    }
}

