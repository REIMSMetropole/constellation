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
package org.constellation.swe.v101;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.geotoolkit.swe.xml.DataBlockDefinition;
import org.geotoolkit.swe.xml.v101.DataBlockDefinitionEntry;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordEntry;
import org.geotoolkit.swe.xml.v101.TextBlockEntry;
import org.geotoolkit.swe.xml.v101.AbstractEncodingPropertyType;

/**
 * Connexion vers la table des {@linkplain DataBlockDefinition dataBlockDefintion}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataBlockDefinitionTable extends SingletonTable<DataBlockDefinition>{
    
    /**
     * Connexion vers la table des {@linkplain TextBlock text block encoding}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected TextBlockTable textBlockEncodings;
    
    /**
     * Connexion vers la table des {@linkplain TextBlock text block encoding}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected SimpleDataRecordTable dataRecords;
    
    
    /**
     * Construit une table des data blocks.
     *
     * @param  database Connexion vers la base de données.
     */
    public DataBlockDefinitionTable(final Database database) {
          this(new DataBlockDefinitionQuery(database)); 
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private DataBlockDefinitionTable(final DataBlockDefinitionQuery query) {
        super(query);
        setIdentifierParameters(query.byId, null);
    }

    /**
     * Construit un data block pour l'enregistrement courant.
     */
    @Override
    protected DataBlockDefinition createEntry(final ResultSet results) throws SQLException, CatalogException {
        final DataBlockDefinitionQuery query = (DataBlockDefinitionQuery) super.query;
        final String idDataBlock = results.getString(indexOf(query.id));
        
        if (dataRecords == null) {
            dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
            dataRecords = new SimpleDataRecordTable(dataRecords);
        }
        dataRecords.setIdDataBlock(idDataBlock);
        final Collection<SimpleDataRecordEntry> entries = dataRecords.getEntries();
        
        if (textBlockEncodings == null) {
            textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
        }
        
        final TextBlockEntry encoding = textBlockEncodings.getEntry(results.getString(indexOf(query.encoding)));
        
        return new DataBlockDefinitionEntry(idDataBlock, entries, encoding);
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du datablockDefinition passée en parametre si non-null)
     * et enregistre le nouveau datablockDefinition dans la base de donnée si il n'y est pas deja.
     *
     * @param databloc le datablockDefinition a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final DataBlockDefinitionEntry databloc) throws SQLException, CatalogException {
        final DataBlockDefinitionQuery query  = (DataBlockDefinitionQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (databloc.getId() != null) {
                final PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.id), databloc.getId());
                final ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return databloc.getId();
                } else {
                    id = databloc.getId();
                }
            } else {
                id = searchFreeIdentifier("datablockDef");
            }
        
            final PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.id), id);

            if (textBlockEncodings == null) {
                textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
            }
            AbstractEncodingPropertyType encProp = databloc.getEncoding();
            statement.setString(indexOf(query.encoding), textBlockEncodings.getIdentifier((TextBlockEntry) encProp.getEncoding()));
            updateSingleton(statement);
        
            if (dataRecords == null) {
                dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
                dataRecords = new SimpleDataRecordTable(dataRecords);
                dataRecords.setIdDataBlock(id);
            } else {
                dataRecords.setIdDataBlock(id);
            }
            final Iterator i = databloc.getComponents().iterator();
            while (i.hasNext()) {
                dataRecords.getIdentifier((SimpleDataRecordEntry) i.next(), id);
            }
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
