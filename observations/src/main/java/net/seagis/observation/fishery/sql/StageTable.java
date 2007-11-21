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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.seagis.observation.fishery.sql;

// SQL dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

// Sicade dependencies
import net.seagis.catalog.ConfigurationKey;
import net.seagis.catalog.Database;
import net.seagis.catalog.Query;
import net.seagis.catalog.SingletonTable;


/**
 * Interroge la base de données pour obtenir la liste des stades de 
 * développement des espèces observées.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
@Deprecated
public class StageTable extends SingletonTable<StageEntry> {
    /**
     * Requête SQL pour obtenir un stage de développement à partir de son identifiant.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Stages:SELECT",
//            "SELECT name, NULL AS remarks\n" +
//            "  FROM \"Stages\"\n" +
//            " WHERE name LIKE ?");

    /** Numéro de colonne. */ private static final int  NAME    = 1;
    /** Numéro de colonne. */ private static final int  REMARKS = 2;

    /**
     * Construit une connexion vers la table des stages.
     *
     * @param  database Connexion vers la base de données.
     */
    public StageTable(final Database database) {
        super(new Query(database, "stage")); // TODO
    }

    /**
     * Construit un stage de développement pour l'enregistrement courant.
     */
    protected StageEntry createEntry(final ResultSet result) throws SQLException {
        final String name    = result.getString(NAME);
        final String remarks = result.getString(REMARKS);
        return new StageEntry(name, remarks);
    }
}
