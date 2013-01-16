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
package org.constellation.sos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.NoSuchTableException;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.constellation.gml.v311.EnvelopeTable;
import org.geotoolkit.gml.xml.v311.BoundingShapeType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.gml.xml.TimeIndeterminateValueType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonType;
import org.geotoolkit.sos.xml.v100.OfferingProcedureType;
import org.geotoolkit.sos.xml.v100.OfferingResponseModeType;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureType;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import org.geotoolkit.swe.xml.v101.PhenomenonPropertyType;
import org.geotoolkit.xml.Namespaces;

/**
 *
 * @author Guilhem Legal
 */
public class ObservationOfferingTable extends SingletonTable<ObservationOfferingType> implements Cloneable {

    /**
     * a link to the offering procedure.
     */
    private OfferingProcedureTable procedures;

    /**
     * a link to the offering phenomenon table.
     */
    private OfferingPhenomenonTable phenomenons;

    /**
     * a link to the offering station table.
     */
    private OfferingSamplingFeatureTable stations;

    /**
     * a link to the offering response mode table
     */
    private OfferingResponseModeTable responseModes;

    /**
     * a link to the envelope table.
     */
    private EnvelopeTable envelopes;

    /**
     * Build a new observation offering table.
     *
     * @param  database Connexion to database.
     */
    public ObservationOfferingTable(final Database database) {
        this(new ObservationOfferingQuery(database));
    }

    /**
     * Initialize the table identifier.
     */
    private ObservationOfferingTable(final ObservationOfferingQuery query) {
        super(query, query.byId);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private ObservationOfferingTable(final ObservationOfferingTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected ObservationOfferingTable clone() {
        return new ObservationOfferingTable(this);
    }

    /**
     * Return the procedure table for offering.
     */
     public OfferingProcedureTable getProcedures() throws NoSuchTableException {
        if (procedures == null) {
            procedures =  getDatabase().getTable(OfferingProcedureTable.class);
         }
        return procedures;
    }

    /**
     * Return the phenomenon table for offering.
     */
    public OfferingPhenomenonTable getPhenomenons() throws NoSuchTableException {
        if (phenomenons == null) {
            phenomenons =  getDatabase().getTable(OfferingPhenomenonTable.class);
        }
        return phenomenons;
    }

    /**
     * Return the station table for offering.
     */
    public OfferingSamplingFeatureTable getStations() throws NoSuchTableException {
        if (stations == null) {
            stations =  getDatabase().getTable(OfferingSamplingFeatureTable.class);
        }
        return stations;
    }

    public OfferingResponseModeTable getResponseModes() throws NoSuchTableException {
        if (responseModes == null) {
            responseModes =  getDatabase().getTable(OfferingResponseModeTable.class);
        }
        return responseModes;
    }

    /**
     *  Create a new offering from the database.
     *
     * @param results a resultSet obtain by a "SELECT" SQL request.
     *
     * @return A observationOffering object.
     *
     * @throws org.constellation.catalog.CatalogException
     * @throws java.sql.SQLException
     */
    @Override
    protected ObservationOfferingType createEntry(final LocalCache lc, ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
         final ObservationOfferingQuery query = (ObservationOfferingQuery) super.query;
         final String idOffering              = results.getString(indexOf(query.id));

         if (envelopes == null) {
             envelopes = getDatabase().getTable(EnvelopeTable.class);
         }
         final EnvelopeType envelope       = envelopes.getEntry(results.getString(indexOf(query.boundedBy)));
         final BoundingShapeType boundedBy = new  BoundingShapeType(envelope);

         getPhenomenons().setIdOffering(idOffering);
         
         final Collection<OfferingPhenomenonType> entries1 = getPhenomenons().getEntries();

         final List<PhenomenonPropertyType> phenos = new ArrayList<PhenomenonPropertyType>();

         Iterator i = entries1.iterator();
         while(i.hasNext()) {
            final OfferingPhenomenonType c =(OfferingPhenomenonType) i.next();
            phenos.add(new PhenomenonPropertyType(c.getComponent()));
         }

         getProcedures().setIdOffering(idOffering);
         final Collection<OfferingProcedureType> entries2 = getProcedures().getEntries();

         final List<ReferenceType> process = new ArrayList<ReferenceType>();

         i = entries2.iterator();
         while(i.hasNext()) {
            final OfferingProcedureType c =(OfferingProcedureType) i.next();
            process.add(c.getComponent());
         }

         getStations().setIdOffering(idOffering);
         final Collection<OfferingSamplingFeatureType> entries3 = stations.getEntries();

         final List<ReferenceType> sampling = new ArrayList<ReferenceType>();

         i = entries3.iterator();
         while(i.hasNext()) {
            final OfferingSamplingFeatureType c =(OfferingSamplingFeatureType) i.next();
            sampling.add(c.getComponent());
         }
         TimePositionType beginPosition = null;
         final TimePositionType endPosition;

         if (results.getTimestamp(indexOf(query.eventTimeBegin)) != null) {
            final Timestamp begin =  results.getTimestamp(indexOf(query.eventTimeBegin));
            if (begin != null) {
                //we normalize the timeStamp by replacing the space by 'T'
                final String normalizedBegin = begin.toString().replace(' ', 'T');
                beginPosition = new TimePositionType(normalizedBegin);
            }
         }

         if (results.getTimestamp(indexOf(query.eventTimeEnd)) != null) {
            final Timestamp end =  results.getTimestamp(indexOf(query.eventTimeEnd));
            if (end != null){
                 //we normalize the timeStamp by replacing the space by 'T'
                final String normalizedEnd = end.toString().replace(' ', 'T');
                endPosition = new TimePositionType(normalizedEnd);
            } else {
                endPosition = new TimePositionType(TimeIndeterminateValueType.NOW);
            }
         } else {
             endPosition = new TimePositionType(TimeIndeterminateValueType.NOW);
         }

         final TimePeriodType eventTime = new TimePeriodType(beginPosition, endPosition);
         eventTime.setId("time-" + idOffering);

         getResponseModes().setIdOffering(idOffering);
         final Collection<OfferingResponseModeType> entries4 = getResponseModes().getEntries();
         final List<ResponseModeType> modes = new ArrayList<ResponseModeType>();
         i = entries4.iterator();

         while(i.hasNext()) {
            final OfferingResponseModeType c =(OfferingResponseModeType) i.next();
            modes.add(c.getMode());
         }
         final List<String> responseFormat = new ArrayList<String>();
         responseFormat.add(results.getString(indexOf(query.responseFormat)));

         final List<QName> resultModel = new ArrayList<QName>();
         final String namespace        = results.getString(indexOf(query.resultModelNamespace));
         resultModel.add(new QName(namespace,
                                   results.getString(indexOf(query.resultModelLocalPart)),
                                   Namespaces.getPreferredPrefix(namespace, "")));

         // Dirty Hack to add Measurement QNAMe
         resultModel.add(new QName(namespace,
                                   "Measurement",
                                   Namespaces.getPreferredPrefix(namespace, "")));

         final List<String> srsName = new ArrayList<String>();
         srsName.add(results.getString(indexOf(query.srsName)));

         return new ObservationOfferingType(idOffering,
                                             results.getString(indexOf(query.name)),
                                             results.getString(indexOf(query.description)),
                                             null,
                                             boundedBy,
                                             srsName,
                                             eventTime,
                                             process,
                                             phenos,
                                             sampling,
                                             responseFormat,
                                             resultModel,
                                             modes);

    }

      /**
     * Retourne un nouvel identifier (ou l'identifier de l'offering passée en parametre si non-null)
     * et enregistre la nouvelle offering dans la base de donnée.
     *
     * @param off l'ofeering a inserer dans la base de donnée.
     */
    public String getIdentifier(final ObservationOfferingType off) throws SQLException, CatalogException {
        final ObservationOfferingQuery query = (ObservationOfferingQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (off.getName() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.id), off.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return off.getId();
                    } else {
                        id = off.getId();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "urn:ogc:object:offering:");
                }
                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.name), off.getName());
                statement.statement.setString(indexOf(query.id), id);
                if (off.getDescription() != null) {
                    statement.statement.setString(indexOf(query.description),  off.getDescription());
                } else {
                    statement.statement.setNull(indexOf(query.description), java.sql.Types.VARCHAR);
                }
                if (off.getSrsName() != null && off.getSrsName().size() > 0) {
                   statement.statement.setString(indexOf(query.srsName), off.getSrsName().get(0));
                } else {
                   statement.statement.setNull(indexOf(query.srsName), java.sql.Types.VARCHAR);
                }
                // on insere le "eventTime""
                if (off.getTime() != null) {
                    if (off.getTime() instanceof TimePeriodType) {
                        final TimePeriodType time = (TimePeriodType)off.getTime();
                        final String s            = time.getBeginPosition().getValue();
                        Timestamp date            = Timestamp.valueOf(s);
                        statement.statement.setTimestamp(indexOf(query.eventTimeBegin), date);

                        if (time.getEndPosition().getIndeterminatePosition() == null) {

                            time.getEndPosition().getValue();
                            date = Timestamp.valueOf(s);
                            statement.statement.setTimestamp(indexOf(query.eventTimeEnd),  date);

                        } else {
                            statement.statement.setNull(indexOf(query.eventTimeEnd),   java.sql.Types.DATE);
                        }
                    } else if (off.getTime() instanceof TimeInstantType) {

                        final TimeInstantType time = (TimeInstantType)off.getTime();
                        final String s             = time.getTimePosition().getValue();
                        final Timestamp date       = Timestamp.valueOf(s);
                        statement.statement.setTimestamp(indexOf(query.eventTimeBegin),  date);
                        statement.statement.setNull(indexOf(query.eventTimeEnd), java.sql.Types.DATE);

                    } else {
                        throw new IllegalArgumentException("type allowed for sampling time: TimePeriod or TimeInstant");
                    }
                } else {
                    statement.statement.setNull(indexOf(query.eventTimeBegin), java.sql.Types.TIMESTAMP);
                    statement.statement.setNull(indexOf(query.eventTimeEnd),   java.sql.Types.TIMESTAMP);
                }

                // on insere l'envellope qui borde l'offering
                if (off.getBoundedBy() != null && off.getBoundedBy().getEnvelope() != null) {
                    if (envelopes == null) {
                        envelopes = getDatabase().getTable(EnvelopeTable.class);
                    }
                    statement.statement.setString(indexOf(query.boundedBy), envelopes.getIdentifier(off.getBoundedBy().getEnvelope()));
                } else {
                    statement.statement.setNull(indexOf(query.boundedBy), java.sql.Types.VARCHAR);
                }
                // TODO transform in list
                statement.statement.setString(indexOf(query.responseFormat), off.getResponseFormat().get(0));
                statement.statement.setString(indexOf(query.resultModelNamespace), off.getResultModel().get(0).getNamespaceURI());
                statement.statement.setString(indexOf(query.resultModelLocalPart), off.getResultModel().get(0).getLocalPart());

                updateSingleton(statement.statement);
                release(lc, statement);

                // we insert the response mode
                if (off.getResponseMode() != null && !off.getResponseMode().isEmpty()){
                    for (ResponseModeType mode:off.getResponseMode()) {
                        getResponseModes().getIdentifier(new OfferingResponseModeType(off.getId(), mode));
                    }
                }
                // on insere la liste de station qui a effectué cette observation
                if (off.getFeatureOfInterest() != null && !off.getFeatureOfInterest().isEmpty()) {
                    for (ReferenceType station:off.getFeatureOfInterest()) {
                        getStations().getIdentifier(new OfferingSamplingFeatureType(off.getId(), station));
                    }
                }

                // on insere les phenomene observé
                if(off.getObservedProperty() != null && !off.getObservedProperty().isEmpty()){
                    for (PhenomenonType pheno: off.getObservedProperty()){
                        getPhenomenons().getIdentifier(new OfferingPhenomenonType(off.getId(), pheno));
                    }
                }

                //on insere les capteur
                if (off.getProcedure() != null) {
                    for (ReferenceType process:off.getProcedure()){
                        getProcedures().getIdentifier(new OfferingProcedureType(off.getId(), process));
                    }
                }
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
}
