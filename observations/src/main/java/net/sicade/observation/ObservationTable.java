/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


// Sicade dependencies
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.SingletonTable;
import net.sicade.coverage.model.DistributionEntry;
import net.sicade.coverage.model.DistributionTable;
import net.sicade.gml.ReferenceEntry;
import net.sicade.swe.AnyResultEntry;
import net.sicade.swe.AnyResultTable;
import net.sicade.swe.DataBlockDefinition;
import net.sicade.swe.DataBlockDefinitionEntry;
import net.sicade.swe.DataBlockDefinitionTable;

// OpenGis dependencies
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.Observation;

// geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Classe de base des connections vers la table des {@linkplain Observation observation}.
 * La requête SQL donné au constructeur doit répondre aux conditions suivantes:
 * <p>
 * <ul>
 *   <li>Les deux premiers arguments doivent être la {@linkplain Station station} et
 *       l'{@linkplain Observable observable} recherchés, dans cet ordre.</li>
 *   <li>Les deux premières colonnes retournées doivent aussi être les identifiants de la
 *       {@linkplain Station station} et de l'{@linkplain Observable observable}.</li>
 * </ul>
 * <p>
 * Exemple:
 *
 * <blockquote><pre>
 * SELECT station, observable FROM Observations WHERE (station = ?) AND (observable = ?)
 * </pre></blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 * @author Guilhem Legal
 */
public class ObservationTable<EntryType extends Observation> extends SingletonTable<Observation> {
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code ObservationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code ObservationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingFeatureTable stations;
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code ObservationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code ObservationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingPointTable stationPoints;
    
    /**
     * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected PhenomenonTable phenomenons;
    
    /**
     * Connexion vers la table des {@linkplain CompositePhenomenon phénomènes composés}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected CompositePhenomenonTable compositePhenomenons;
    
    /**
     * Connexion vers la table des {@linkplain Procedure procedures}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected ProcessTable procedures;
    
    /**
     * Connexion vers la table des {@linkplain Distribution distributions}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected DistributionTable distributions;
    
    /**
     * Connexion vers la table des {@linkplain AnyResult result}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected AnyResultTable results;
    
    /**
     * Connexion vers la table des {@linkplain DataBlockDefinition dataBlockdDefinition}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected DataBlockDefinitionTable dataBlockDefinitions;
    
    /**
     * Connexion vers la table des méta-données. Une table par défaut (éventuellement partagée)
     * sera construite la première fois où elle sera nécessaire.
     */
    protected MetadataTable metadata;
    
    /**
     * La station pour laquelle on veut des observations, ou {@code null} pour récupérer les
     * observations de toutes les stations.
     */
    protected SamplingFeature featureOfInterest;
    
    
    /**
     * Construit une nouvelle connexion vers la table des observations. Voyez la javadoc de
     * cette classe pour les conditions que doivent remplir la requête donnée en argument.
     *
     * @param  database Connexion vers la base de données des observations.
     */
    public ObservationTable(final Database database) {
        this(new ObservationQuery(database));
    }
    
    /**
     * Super constructeur qui est appelé par les classe specialisant ObservationTable.
     *
     * @param  database Connexion vers la base de données des observations.
     */
    protected ObservationTable(ObservationQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
    /**
     * Retourne la station pour laquelle on recherche des observations.
     */
    public final SamplingFeature getFeatureOfInterest() {
        return featureOfInterest;
    }
    
    /**
     * Définit la station pour laquelle on recherche des observations.
     * La valeur {@code null} recherche toutes les stations.
     */
    public synchronized void setStation(final SamplingFeature station) {
        if (!Utilities.equals(station, this.featureOfInterest)) {
            this.featureOfInterest = station;
            fireStateChanged("Station");
        }
    }
    
    /**
     * Configure la requête SQL spécifiée en fonction de la station et de l'observable recherchés
     * par cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     *
     * @Override
     * protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
     * super.configure(type, statement);
     * if (featureOfInterest != null) {
     * statement.setInt(STATION, featureOfInterest.getNumericIdentifier());
     * } else {
     * throw new UnsupportedOperationException("La recherche sur toutes les stations n'est pas encore impléméntée.");
     * }
     * if (observable != null) {
     * statement.setInt(OBSERVABLE, observable.getNumericIdentifier());
     * } else {
     * throw new UnsupportedOperationException("La recherche sur tous les observables n'est pas encore impléméntée.");
     * }
     * }
     */
    
    /**
     * Retourne {@code true} s'il existe au moins une entrée pour la station et l'observable
     * courant.
     *
     * public synchronized boolean exists() throws SQLException {
     * final PreparedStatement statement = getStatement(getProperty(select));
     * final ResultSet result = statement.executeQuery();
     * final boolean exists = result.next();
     * result.close();
     * return exists;
     * }
     *
     */
    
    /**
     * Retourne les observations pour la station et l'observable courants. Cette méthode
     * ne retourne jamais {@code null}, mais peut retourner un ensemble vide. L'ensemble
     * retourné ne contiendra jamais plus d'un élément si une station et un observable
     * non-nuls ont été spécifiés à cette table.
     *
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     *
     * public synchronized List<EntryType> getEntries() throws CatalogException, SQLException {
     * final List<EntryType> list = new ArrayList<EntryType>();
     * final PreparedStatement statement = getStatement(getProperty(select));
     * final ResultSet result = statement.executeQuery();
     * while (result.next()) {
     * list.add(createEntry(result));
     * }
     * result.close();
     * return list;
     * }*/
    
    /**
     * Retourne une seule observation pour la station et l'observable courants, ou {@code null}
     * s'il n'y en a pas. Cette méthode risque d'échouer si la station et l'observable n'ont pas
     * été spécifiés tous les deux à cette table avec une valeur non-nulle.
     *
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     *
     * public synchronized EntryType getEntry() throws CatalogException, SQLException {
     * final PreparedStatement statement = getStatement(getProperty(select));
     * final ResultSet results = statement.executeQuery();
     * EntryType observation = null;
     * while (results.next()) {
     * final EntryType candidate = createEntry(results);
     * if (observation == null) {
     * observation = candidate;
     * } else if (!observation.equals(candidate)) {
     * throw new DuplicatedRecordException(results, 1, String.valueOf(observation));
     * }
     * }
     * results.close();
     * return observation;
     * }
     */
    
    /**
     * Construit une observation pour l'enregistrement courant.
     */
    public Observation createEntry(final ResultSet result) throws CatalogException, SQLException {
        final ObservationQuery query = (ObservationQuery) super.query;
        
        if (distributions == null) {
            distributions = getDatabase().getTable(DistributionTable.class);
        }
        DistributionEntry distrib = distributions.getEntry(result.getString(indexOf(query.distribution)));
        
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        PhenomenonEntry pheno = (PhenomenonEntry)phenomenons.getEntry(result.getString(indexOf(query.observedProperty)));
        
        if (compositePhenomenons == null) {
            compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
        }
        CompositePhenomenonEntry compoPheno = (CompositePhenomenonEntry)compositePhenomenons.getEntry(result.getString(indexOf(query.observedPropertyComposite)));
        
        if (stations == null) {
            stations = getDatabase().getTable(SamplingFeatureTable.class);
        }
        SamplingFeatureEntry station = stations.getEntry(result.getString(indexOf(query.featureOfInterest)));
        
        if (stationPoints == null) {
            stationPoints = getDatabase().getTable(SamplingPointTable.class);
        }
        SamplingPointEntry stationPoint = stationPoints.getEntry(result.getString(indexOf(query.featureOfInterestPoint)));
        
        if (procedures == null) {
            procedures = getDatabase().getTable(ProcessTable.class);
        }
        ProcessEntry procedure = procedures.getEntry(result.getString(indexOf(query.procedure)));
        
        if (results == null) {
            results = getDatabase().getTable(AnyResultTable.class);
        }
        AnyResultEntry any = results.getEntry(result.getString(indexOf(query.result)));
        Object resultat = null;
        if (any != null) {
            if (any.getReference() == null && any.getDataBlock() != null) {
                resultat = any.getDataBlock();
            } else if (any.getReference() != null && any.getDataBlock() == null)  {
                resultat = any.getReference();
            }
        }
        if (dataBlockDefinitions == null) {
            dataBlockDefinitions = getDatabase().getTable(DataBlockDefinitionTable.class);
        }
        DataBlockDefinition dataBlockDef = dataBlockDefinitions.getEntry(result.getString(indexOf(query.resultDefinition)));
        
        if(pheno == null) pheno = compoPheno;
        if(station == null) station =  stationPoint;
        
        return new ObservationEntry(result.getString(indexOf(query.name)),
                result.getString(indexOf(query.description)),
                station,
                pheno,
                procedure,
                distrib,
                //manque quality
                resultat,
                new TemporalObjectEntry(result.getDate(indexOf(query.samplingTimeBegin)),
                result.getDate(indexOf(query.samplingTimeEnd))),
                dataBlockDef);
        
        
    }
    
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de l'observation passée en parametre si non-null)
     * et enregistre la nouvelle observation dans la base de donnée.
     *
     * @param l'observation a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final ObservationEntry obs) throws SQLException, CatalogException {
        final ObservationQuery query = (ObservationQuery) super.query;
        String id;
        if (obs.getName() != null) {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.name), obs.getName());
            ResultSet result = statement.executeQuery();
            if(result.next())
                return obs.getName();
            else
                id = obs.getName();
        } else {
            id = searchFreeIdentifier("urn:BRGM:observation:");
        }
        PreparedStatement statement = getStatement(QueryType.INSERT);
        statement.setString(indexOf(query.name),         id);
        statement.setString(indexOf(query.description),  obs.getDefinition());
        
        // on insere la distribution
        if (obs.getDistribution() == null) {
            obs.setDistribution(DistributionEntry.NORMAL);
        }
        if (distributions == null) {
            distributions = getDatabase().getTable(DistributionTable.class);
        }
        statement.setString(indexOf(query.distribution), distributions.getIdentifier(obs.getDistribution()));
        
        
        // on insere la station qui a effectué cette observation
         if (obs.getFeatureOfInterest() instanceof SamplingPointEntry){
            SamplingPointEntry station = (SamplingPointEntry)obs.getFeatureOfInterest();
            if (stationPoints == null) {
                    stationPoints = getDatabase().getTable(SamplingPointTable.class);
            }
            statement.setString(indexOf(query.featureOfInterestPoint),stationPoints.getIdentifier(station));
            statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
            
        } else if (obs.getFeatureOfInterest() instanceof SamplingFeatureEntry){
            SamplingFeatureEntry station = (SamplingFeatureEntry)obs.getFeatureOfInterest();
            if (stations == null) {
                stations = getDatabase().getTable(SamplingFeatureTable.class);
            }
            statement.setString(indexOf(query.featureOfInterest),stations.getIdentifier(station));
            statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
        } else {
            statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
            statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
        }
        
        // on insere le phenomene observé
         if(obs.getObservedProperty() instanceof CompositePhenomenonEntry){
            CompositePhenomenonEntry pheno = (CompositePhenomenonEntry)obs.getObservedProperty();
            if (compositePhenomenons == null) {
                compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
            }
            statement.setString(indexOf(query.observedPropertyComposite), compositePhenomenons.getIdentifier(pheno));
            statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);
        
         } else if(obs.getObservedProperty() instanceof PhenomenonEntry){
            PhenomenonEntry pheno = (PhenomenonEntry)obs.getObservedProperty();
            if (phenomenons == null) {
                phenomenons = getDatabase().getTable(PhenomenonTable.class);
            }
            statement.setString(indexOf(query.observedProperty), phenomenons.getIdentifier(pheno));
            statement.setNull(indexOf(query.observedPropertyComposite), java.sql.Types.VARCHAR);
       
        } else {
            statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);
            statement.setNull(indexOf(query.observedPropertyComposite), java.sql.Types.VARCHAR);
        }
        
        //on insere le capteur
        if (obs.getProcedure() != null) {
            ProcessEntry process = (ProcessEntry)obs.getProcedure();
            if (procedures == null) {
                procedures = getDatabase().getTable(ProcessTable.class);
            }
            statement.setString(indexOf(query.procedure), procedures.getIdentifier(process));
        } else {
            statement.setNull(indexOf(query.procedure), java.sql.Types.VARCHAR);
        }
        
        // on insere le resultat
        if (obs.getResult() instanceof ReferenceEntry || obs.getResult() instanceof AnyResultEntry){
            if (results == null) {
                results = getDatabase().getTable(AnyResultTable.class);
            }
            statement.setString(indexOf(query.result), results.getIdentifier(obs.getResult()));
        } else {
            statement.setNull(indexOf(query.result), java.sql.Types.VARCHAR);
        }
        
        //on insere la defintion du resultat
        if (obs.getResultDefinition() instanceof String) {
            statement.setString(indexOf(query.resultDefinition), (String)obs.getResultDefinition());
        } else {
            if (obs.getResultDefinition() instanceof DataBlockDefinitionEntry ){
                DataBlockDefinitionEntry block = (DataBlockDefinitionEntry)obs.getResultDefinition(); 
                if (dataBlockDefinitions == null) {
                   dataBlockDefinitions = getDatabase().getTable(DataBlockDefinitionTable.class);
                }
                statement.setString(indexOf(query.resultDefinition), dataBlockDefinitions.getIdentifier(block));
            } else {
                statement.setNull(indexOf(query.resultDefinition), java.sql.Types.VARCHAR);
            }
        }
        // on insere le "samplingTime""
        if (obs.getSamplingTime() != null){
            Date date = new Date(((TemporalObjectEntry)obs.getSamplingTime()).getBeginTime().getTime());
            statement.setDate(indexOf(query.samplingTimeBegin), date);
            if (((TemporalObjectEntry)obs.getSamplingTime()).getEndTime() != null) {
                date = new Date(((TemporalObjectEntry)obs.getSamplingTime()).getEndTime().getTime());           
                statement.setDate(indexOf(query.samplingTimeEnd), date);
            } else {
                statement.setNull(indexOf(query.samplingTimeEnd), java.sql.Types.DATE);
            }
        } else {
            statement.setNull(indexOf(query.samplingTimeBegin), java.sql.Types.DATE);
            statement.setNull(indexOf(query.samplingTimeEnd),   java.sql.Types.DATE);
        }
        
        insertSingleton(statement);
        return id;
    }
    
}
