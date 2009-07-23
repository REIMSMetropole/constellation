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

package org.constellation.lucene.filter;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.BitSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 *
 * @author Guilhem Legal
 */
public class WithinFilter extends SpatialFilter {

    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 8003022074504896029L;

    /**
     * Initialize the filter with the specified geometry and filterType.
     *
     * @param geometry   A geometry object, supported types are: GeneralEnvelope, GeneralDirectPosition, Line2D.
     * @param filterType A flag representing the type of spatial filter to apply restricted to Beyond and Dwithin.
     * @param distance   The distance to applies to this filter.
     * @param units      The unit of measure of the distance.
     */
    public WithinFilter(Object geometry, String crsName) throws NoSuchAuthorityCodeException, FactoryException  {
       super(geometry, crsName);
    }

    @Override
    public BitSet bits(IndexReader reader) throws IOException {
        // we prepare the result
        BitSet bits = new BitSet(reader.maxDoc());

        TermDocs termDocs = reader.termDocs(new Term(GEOMETRY_FIELD));

        // we are searching for matching points
        termDocs.seek(new Term(GEOMETRY_FIELD, "point"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            GeneralDirectPosition tempPoint = readPoint(reader, docNum);
            Line2D pointLine                = new Line2D.Double(tempPoint.getOrdinate(0), tempPoint.getOrdinate(1),
                                                                tempPoint.getOrdinate(0), tempPoint.getOrdinate(1));
            if (point != null && point.equals(tempPoint)) {
                bits.set(docNum);

            } else if (boundingBox != null && boundingBox.contains(tempPoint)) {
                bits.set(docNum);

            } else if (line != null && line.intersectsLine(pointLine)) {
                bits.set(docNum);
            }
        }

        //then we search for matching box
        termDocs.seek(new Term(GEOMETRY_FIELD, "boundingbox"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            GeneralEnvelope tempBox = readBoundingBox(reader, docNum);
            if (tempBox == null)
                continue;
            if (boundingBox != null && boundingBox.contains(tempBox, false)) {
                bits.set(docNum);
            }
        }

        //then we search for matching line
        termDocs.seek(new Term(GEOMETRY_FIELD, "line"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            Line2D tempLine = readLine(reader, docNum);
            GeneralDirectPosition tempPoint1 = new GeneralDirectPosition(tempLine.getX1(), tempLine.getY1());
            tempPoint1.setCoordinateReferenceSystem(geometryCRS);
            GeneralDirectPosition tempPoint2 = new GeneralDirectPosition(tempLine.getX2(), tempLine.getY2());
            tempPoint2.setCoordinateReferenceSystem(geometryCRS);
            
            if (line != null && line.intersectsLine(tempLine.getX1(), tempLine.getY1(), tempLine.getX1(), tempLine.getY1()) && line.intersectsLine(tempLine.getX2(), tempLine.getY2(), tempLine.getX2(), tempLine.getY2())) {
                bits.set(docNum);

            } else if (boundingBox != null && boundingBox.contains(tempPoint1) && boundingBox.contains(tempPoint2)) {
                bits.set(docNum);
            }
        }
        return bits;
    }
}
