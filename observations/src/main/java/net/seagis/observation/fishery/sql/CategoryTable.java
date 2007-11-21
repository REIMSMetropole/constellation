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

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

// Sicade dependencies
import net.seagis.catalog.ConfigurationKey;
import net.seagis.catalog.Database;
import net.seagis.catalog.SingletonTable;
import net.seagis.catalog.CatalogException;
import net.seagis.observation.fishery.Category;
import net.seagis.observation.SamplingFeatureTable;


/**
 * Intéroge la base de données pour obtenir la liste des catégories d'espèces observées.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
@Deprecated
public class CategoryTable extends SingletonTable<CategoryEntry> {
    /**
     * Requête SQL pour obtenir une catégorie à partir de son identifiant.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Categories:SELECT",
//            "SELECT symbol, identifier, phenomenon, procedure, stage, NULL as remarks\n" +
//            "  FROM \"Categories\"\n"                                                    +
//            " WHERE symbol LIKE ?\n"                                                     +
//            "   AND selected=TRUE\n"                                                     +
//            " ORDER BY identifier");

    /** Numéro de colonne. */ private static final int  FEATUREOFINTEREST= 1;
    /** Numéro de colonne. */ private static final int  PHENOMENON = 2;
    /** Numéro de colonne. */ private static final int  PROCEDURE  = 3;
    /** Numéro de colonne. */ private static final int  STAGE      = 4;
    /** Numéro de colonne. */ private static final int  REMARKS    = 5;
     /** Numéro de colonne. */ private static final int  NAME      = 6;

    /**
     * Table des stationss.
     * Ne sera construite que la première fois où elle sera nécessaire/
     */
    private transient SamplingFeatureTable stations;
    
    /**
     * Table des espèces.
     * Ne sera construite que la première fois où elle sera nécessaire/
     */
    private transient SpeciesTable species;

    /**
     * Table des stades de développement.
     * Ne sera construite que la première fois où elle sera nécessaire/
     */
    private transient StageTable stages;

    /**
     * Type de pêche.
     *
     * @todo Codé en dur pour l'instant. Devra être puisé dans une table dans une version future.
     */
    private static final FisheryTypeEntry fisheryType = new FisheryTypeEntry("pêche", null);

    /**
     * Construit une connexion vers la table des catégories.
     *
     * @param  database Connexion vers la base de données.
     */
    public CategoryTable(final Database database) {
        super(new net.seagis.catalog.Query(database, "category")); // TODO
    }

    /**
     * Construit une catégorie pour l'enregistrement courant.
     */
    protected CategoryEntry createEntry(final ResultSet result) throws SQLException, CatalogException {
        final String station     = result.getString(FEATUREOFINTEREST);
        final String phenomenon = result.getString(PHENOMENON);
        final String procedure  = result.getString(PROCEDURE);
        final String stage      = result.getString(STAGE);
        final String remarks    = result.getString(REMARKS);
        
        if (stations == null) {
            stations = getDatabase().getTable(SamplingFeatureTable.class);
        }
        if (species == null) {
            species = getDatabase().getTable(SpeciesTable.class);
        }
        if (stages == null) {
            stages = getDatabase().getTable(StageTable.class);
        }
        return new CategoryEntry(result.getString(NAME),
                                 remarks,
                                 stations.getEntry(station), 
                                 species.getEntry(phenomenon),
                                 stages.getEntry(stage),
                                 fisheryType,null,null,null,null,null,null,null);
    }
}
