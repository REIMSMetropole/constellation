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
package net.seagis.swe;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;

/**
 * The query to execute for a {@link AnyResultTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class AnyResultQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idResult, reference, dataBlock;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdResult, byDataBloc, byRef;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public AnyResultQuery(final Database database) {
        super(database, "any_results");
        final QueryType[] SI  = {SELECT, INSERT, FILTERED_LIST};
        final QueryType[] SIE = {SELECT, INSERT, EXISTS};
        final QueryType[] SE   = {SELECT, EXISTS};
        
        idResult   = addColumn("id_result",  SE);
        reference  = addColumn("reference",  SI);
        dataBlock  = addColumn("data_block", SI);
        
        byIdResult = addParameter(idResult,  SELECT, EXISTS);
        byDataBloc = addParameter(dataBlock, FILTERED_LIST);
        byRef      = addParameter(reference, FILTERED_LIST);
    }
    
}
