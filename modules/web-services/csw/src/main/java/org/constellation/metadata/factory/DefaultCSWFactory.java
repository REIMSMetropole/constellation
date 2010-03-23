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

package org.constellation.metadata.factory;

import java.io.File;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.metadata.index.generic.GenericIndexSearcher;
import org.constellation.metadata.index.mdweb.MDWebIndexer;
import org.constellation.metadata.index.mdweb.MDWebIndexSearcher;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.FileMetadataReader;
import org.constellation.metadata.io.FileMetadataWriter;
import org.constellation.metadata.io.MDWebCSWMetadataReader;
import org.constellation.metadata.io.MDWebCSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import static org.constellation.generic.database.Automatic.*;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.lucene.index.AbstractIndexer;

/**
 * A default implementation of the CSW factory.
 * it provide various reader / writer and  lucene indexer / searcher.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultCSWFactory extends AbstractCSWFactory {

    /*public DefaultCSWFactory() {
        super();
    }*/

    private static final String UNKNOW_DATABASE_TYPE = "Unknow database type: ";

    /**
     * Return a Metadata reader for the specified database type.
     * 
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    @Override
    public CSWMetadataReader getMetadataReader(Automatic configuration) throws MetadataIoException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebCSWMetadataReader(configuration);
            case FILESYSTEM:
                return new FileMetadataReader(configuration);
            default:
                throw new IllegalArgumentException(UNKNOW_DATABASE_TYPE + type);
        }
    }
    
    /**
     * Return a Metadata Writer for the specified database type.
     * 
     * @param configuration
     * @return
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    @Override
    public CSWMetadataWriter getMetadataWriter(Automatic configuration, AbstractIndexer indexer) throws MetadataIoException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebCSWMetadataWriter(configuration, indexer);
            case FILESYSTEM:
                return new FileMetadataWriter(configuration, indexer);
            default:
                throw new IllegalArgumentException(UNKNOW_DATABASE_TYPE + type);
        }
    }
    
    /**
     * Return a Lucene index for the specified database type.
     * 
     * @param dbType The type of the database.
     * @param reader A metadataReader (unused for MDWeb database);
     * @param MDConnection A connecton to the database (used only for MDWeb database).
     * @param configDir
     * @return
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    @Override
    public AbstractIndexer getIndexer(Automatic configuration, CSWMetadataReader reader, String serviceID) throws IndexingException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebIndexer(configuration, serviceID);
            case FILESYSTEM:
                return new GenericIndexer(reader, configuration, serviceID);
            default:
                throw new IllegalArgumentException(UNKNOW_DATABASE_TYPE + type);
        }
    }
    
    @Override
    public AbstractIndexSearcher getIndexSearcher(int dbType, File configDirectory, String serviceID) throws IndexingException {
        switch (dbType) {
            case MDWEB:
                return new MDWebIndexSearcher(configDirectory, serviceID);
            case FILESYSTEM:
                return new GenericIndexSearcher(configDirectory, serviceID);
            default:
                throw new IllegalArgumentException(UNKNOW_DATABASE_TYPE + dbType);
        }
    }
}
