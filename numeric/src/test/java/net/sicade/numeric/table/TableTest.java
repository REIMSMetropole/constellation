/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
package net.sicade.numeric.table;

// J2SE dependencies
import java.util.Arrays;
import java.util.Random;
import java.io.IOException;
import java.nio.DoubleBuffer;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Teste les sous-classes de {@link Table}.
 *
 * @author Martin Desruisseaux
 */
public class TableTest extends TestCase {
    /**
     * Ex�cute la suite de tests � partir de la ligne de commande.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Retourne la suite de tests.
     */
    public static Test suite() {
        return new TestSuite(TableTest.class);
    }

    /**
     * Construit une nouvelle suite de tests.
     */
    public TableTest() {
    }

    /**
     * Teste en utilisant l'interpolation de type "plus proche voisin".
     */
    public void testNearest() throws ExtrapolationException, IOException {
        /*
         * Construit un vecteur de valeurs al�atoires. Note: la valeur 'seed' utilis� ci-dessous
         * a �t� choisie empiriquement de mani�re � �viter de produire des valeurs trop proches
         * des limites du tableau. On �vite ainsi de compliquer les v�rifications des m�thodes
         * test�es ici. La v�rification des m�thodes aux limites devrait �tre effectu�e par un
         * code explicite.
         */
        final Random random = new Random(4895268);
        final double[] x = new double[1000];
        final double[] y = new double[x.length];
        for (int i=0; i<x.length; i++) {
            x[i] = i + (random.nextDouble() - 0.5);
            y[i] = i + random.nextGaussian();
        }
        final double EPS = 1E-8; // Petite valeur pour les comparaisons qui se veulent exactes.
        final Table table = TableFactory.getDefault().create(x, y, Interpolation.NEAREST);
        assertEquals(x.length, table.getNumRow());
        assertEquals(2,        table.getNumCol());
        assertFalse (          table.isIdentity());
        /*
         * Teste 'locate'.
         */
        final int[] index = new int[3];
        table.locate(75, index);
        assertEquals(74, index[0]);
        assertEquals(75, index[1]);
        assertEquals(76, index[2]);
        /*
         * Teste 'interpolate'
         */
        assertEquals(y[300], table.interpolate(300), EPS);
    }
}
