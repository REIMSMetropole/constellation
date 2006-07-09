/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
package net.sicade.observation.coverage;

// J2SE dependencies
import java.util.Date;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;

// Other dependencies
import junit.framework.TestCase;
import org.geotools.coverage.SpatioTemporalCoverage3D;
import net.sicade.observation.sql.Database;


/**
 * Classe de base des tests qui �valueront les valeurs des pixels.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EvaluateTest extends TestCase {
    /**
     * Connexion vers la base de donn�es.
     */
    protected static Database database;

    /**
     * La couverture � tester. Sera construites par les diff�rentes m�thodes {@code testXXX}.
     */
    protected SpatioTemporalCoverage3D coverage;

    /**
     * Objet � utiliser pour lire et �crire des dates.
     * Le format attendu est de la forme "24/12/1997".
     */
    protected DateFormat dateFormat;

    /**
     * Construit la suite de tests.
     */
    public EvaluateTest(final String name) {
        super(name);
    }

    /**
     * Etablit la connexion avec la base de donn�es.
     */
    @Override
    protected void setUp() throws SQLException, IOException {
        if (database == null) {
            database = new Database();
        }
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Un test trivial, principalement parce que JUnit exige qu'il y aie au moins un test.
     */
    public void testDatabase() {
        assertNotNull(database);
    }

    /**
     * Retourne la valeur de la premi�re bande �valu�e � la position sp�cifi�e.
     * Cette m�thode est un bon endroit o� placer un point d'arr�t � des fins de d�boguage.
     */
    protected final float evaluate(final double x, final double y, final String date) throws ParseException {
        final Point2D coord = new Point2D.Double(x,y);
        final Date    time  = dateFormat.parse(date);
        float[] array=null, compare=null;
        array = coverage.evaluate(coord, time, array); //       <--- Break point ici
        if (false) {
            // TODO
            compare = coverage.getGridCoverage2D(time).evaluate(coord, compare);
            assertTrue(Arrays.equals(array, compare));
        }
        return array[0];
    }
}
