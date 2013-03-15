/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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

package org.constellation.sos.io.filesystem;


import java.util.Map;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.constellation.sos.factory.OMFactory;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.ObservationReader;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.TemporalPrimitive;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileObservationReader implements ObservationReader {

     /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * The base for observation id.
     */
    protected final String observationIdBase;

    protected final String phenomenonIdBase;
    
    private File offeringDirectory;

    private File phenomenonDirectory;

    private File observationDirectory;

    private File observationTemplateDirectory;

    private File sensorDirectory;

    private File foiDirectory;

    private static final MarshallerPool MARSHALLER_POOL;
    static {
        MARSHALLER_POOL = SOSMarshallerPool.getInstance();
    }

    private static final String FILE_EXTENSION = ".xml";

    public FileObservationReader(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.phenomenonIdBase  = (String) properties.get(OMFactory.PHENOMENON_ID_BASE);
        final File dataDirectory = configuration.getDataDirectory();
        if (dataDirectory != null && dataDirectory.exists()) {
            offeringDirectory            = new File(dataDirectory, "offerings");
            phenomenonDirectory          = new File(dataDirectory, "phenomenons");
            observationDirectory         = new File(dataDirectory, "observations");
            observationTemplateDirectory = new File(dataDirectory, "observationTemplates");
            sensorDirectory              = new File(dataDirectory, "sensors");
            foiDirectory                 = new File(dataDirectory, "features");
        } else {
            throw new CstlServiceException("There is no data Directory", NO_APPLICABLE_CODE);
        }
        if (MARSHALLER_POOL == null) {
            throw new CstlServiceException("JAXB exception while initializing the file observation reader", NO_APPLICABLE_CODE);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getOfferingNames(final String version) throws CstlServiceException {
        final List<String> offeringNames = new ArrayList<String>();
        if (offeringDirectory.isDirectory()) {
            final File offeringVersionDir = new File(observationDirectory, version);
            if (offeringVersionDir.isDirectory()) {
                for (File offeringFile: offeringVersionDir.listFiles()) {
                    String offeringName = offeringFile.getName();
                    offeringName = offeringName.substring(0, offeringName.indexOf(FILE_EXTENSION));
                    offeringNames.add(offeringName);
                }
            }
        }
        return offeringNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final List<String> offeringNames, final String version) throws CstlServiceException {
        final List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName, version));
        }
        return offerings;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationOffering getObservationOffering(final String offeringName, final String version) throws CstlServiceException {
        final File offeringVersionDir = new File(offeringDirectory, version); 
        if (offeringVersionDir.isDirectory()) {
            final File offeringFile = new File(offeringVersionDir, offeringName + FILE_EXTENSION);
            if (offeringFile.exists()) {
                try {
                    final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                    Object obj = unmarshaller.unmarshal(offeringFile);
                    MARSHALLER_POOL.release(unmarshaller);
                    if (obj instanceof JAXBElement) {
                        obj = ((JAXBElement)obj).getValue();
                    }
                    if (obj instanceof ObservationOffering) {
                        return (ObservationOffering) obj;
                    }
                    throw new CstlServiceException("The file " + offeringFile + " does not contains an offering Object.", NO_APPLICABLE_CODE);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("Unable to unmarshall The file " + offeringFile, ex, NO_APPLICABLE_CODE);
                }
            }
        } else {
            throw new CstlServiceException("Unsuported version:" + version);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final String version) throws CstlServiceException {
        final List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
        if (offeringDirectory.exists()) {
            final File offeringVersionDir = new File(offeringDirectory, version); 
            if (offeringVersionDir.isDirectory()) {
                for (File offeringFile: offeringVersionDir.listFiles()) {
                    try {
                        final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                        Object obj = unmarshaller.unmarshal(offeringFile);
                        MARSHALLER_POOL.release(unmarshaller);
                        if (obj instanceof JAXBElement) {
                            obj = ((JAXBElement)obj).getValue();
                        }
                        if (obj instanceof ObservationOffering) {
                            offerings.add((ObservationOffering) obj);
                        } else {
                            throw new CstlServiceException("The file " + offeringFile + " does not contains an offering Object.", NO_APPLICABLE_CODE);
                        }
                    } catch (JAXBException ex) {
                        String msg = ex.getMessage();
                        if (msg == null && ex.getCause() != null) {
                            msg = ex.getCause().getMessage();
                        }
                        LOGGER.warning("Unable to unmarshall The file " + offeringFile + " cause:" + msg);
                    }
                }
            } else {
                throw new CstlServiceException("Unsuported version:" + version);
            }
        }
        return offerings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getProcedureNames() throws CstlServiceException {
        final List<String> sensorNames = new ArrayList<String>();
        if (sensorDirectory.exists()) {
            for (File sensorFile: sensorDirectory.listFiles()) {
                String sensorName = sensorFile.getName();
                sensorName = sensorName.substring(0, sensorName.indexOf(FILE_EXTENSION));
                sensorNames.add(sensorName);
            }
        }
        return sensorNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getPhenomenonNames() throws CstlServiceException {
        final List<String> phenomenonNames = new ArrayList<String>();
        if (phenomenonDirectory.exists()) {
            for (File phenomenonFile: phenomenonDirectory.listFiles()) {
                String phenomenonName = phenomenonFile.getName();
                phenomenonName = phenomenonName.substring(0, phenomenonName.indexOf(FILE_EXTENSION));
                phenomenonNames.add(phenomenonName);
            }
        }
        return phenomenonNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existPhenomenon(String phenomenonName) throws CstlServiceException {
        // we remove the phenomenon id base
        if (phenomenonName.indexOf(phenomenonIdBase) != -1) {
            phenomenonName = phenomenonName.replace(phenomenonIdBase, "");
        }
        final File phenomenonFile = new File(phenomenonDirectory, phenomenonName + FILE_EXTENSION);
        return phenomenonFile.exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getFeatureOfInterestNames() throws CstlServiceException {
        final List<String> foiNames = new ArrayList<String>();
        if (foiDirectory.exists()) {
            for (File foiFile: foiDirectory.listFiles()) {
                String foiName = foiFile.getName();
                foiName = foiName.substring(0, foiName.indexOf(FILE_EXTENSION));
                foiNames.add(foiName);
            }
        }
        return foiNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SamplingFeature getFeatureOfInterest(final String samplingFeatureName, final String version) throws CstlServiceException {
        final File samplingFeatureFile = new File(foiDirectory, samplingFeatureName + FILE_EXTENSION);
        if (samplingFeatureFile.exists()) {
            try {
                final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(samplingFeatureFile);
                MARSHALLER_POOL.release(unmarshaller);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof SamplingFeature) {
                    return (SamplingFeature) obj;
                }
                throw new CstlServiceException("The file " + samplingFeatureFile + " does not contains an foi Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + samplingFeatureFile, ex, NO_APPLICABLE_CODE);
            } 
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observation getObservation(final String identifier, final QName resultModel, final ResponseModeType mode, final String version) throws CstlServiceException {
        File observationFile = new File(observationDirectory, identifier + FILE_EXTENSION);
        if (!observationFile.exists()) {
            observationFile = new File(observationTemplateDirectory, identifier + FILE_EXTENSION);
        }
        if (observationFile.exists()) {
            try {
                final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(observationFile);
                MARSHALLER_POOL.release(unmarshaller);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof Observation) {
                    return (Observation) obj;
                }
                throw new CstlServiceException("The file " + observationFile + " does not contains an observation Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + observationFile, ex, NO_APPLICABLE_CODE);
            }
        }
        throw new CstlServiceException("The file " + observationFile + " does not exist", NO_APPLICABLE_CODE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult(final String identifier, final QName resutModel, final String version) throws CstlServiceException {
        final File anyResultFile = new File(observationDirectory, identifier + FILE_EXTENSION);
        if (anyResultFile.exists()) {
            
            try {
                final Unmarshaller unmarshaller = MARSHALLER_POOL.acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(anyResultFile);
                MARSHALLER_POOL.release(unmarshaller);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                if (obj instanceof Observation) {
                    final Observation obs = (Observation) obj;
                    final DataArrayProperty arrayP = (DataArrayProperty) obs.getResult();
                    return arrayP.getDataArray();
                }
                throw new CstlServiceException("The file " + anyResultFile + " does not contains an observation Object.", NO_APPLICABLE_CODE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to unmarshall The file " + anyResultFile, ex, NO_APPLICABLE_CODE);
            }
        }
        throw new CstlServiceException("The file " + anyResultFile + " does not exist", NO_APPLICABLE_CODE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existProcedure(final String href) throws CstlServiceException {
        if (sensorDirectory.exists()) {
            for (File sensorFile: sensorDirectory.listFiles()) {
                String sensorName = sensorFile.getName();
                sensorName = sensorName.substring(0, sensorName.indexOf(FILE_EXTENSION));
                if (sensorName.equals(href)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewObservationId() throws CstlServiceException {
        String obsID = null;
        boolean exist = true;
        int i = observationDirectory.list().length;
        while (exist) {
            obsID = observationIdBase + i;
            final File newFile = new File(observationDirectory, obsID);
            exist = newFile.exists();
            i++;
        }
        return obsID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventTime() throws CstlServiceException {
        return Arrays.asList("undefined", "now");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalPrimitive getFeatureOfInterestTime(final String samplingFeatureName, final String version) throws CstlServiceException {
        throw new CstlServiceException("The Filesystem implementation of SOS does not support GetFeatureofInterestTime");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // nothing to destroy
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Filesystem O&M Reader 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResponseModeType> getResponseModes() throws CstlServiceException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResponseFormats() throws CstlServiceException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }

}
