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
import java.util.logging.Level;
import java.util.logging.Logger;

// Apache Lucene dependencies
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;

// geotools dependencies
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.Utilities;

// GeoAPI dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * A lucene filter for OGC spatial filter

 * @author Guilhem legal
 */
public abstract class SpatialFilter extends Filter {
    
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -1337271653030261124L;

    private static final Logger LOGGER = Logger.getLogger("org.constellation.lucene.filter");

    public static final String GEOMETRY_FIELD = "geometry";
    /**
     * The envelope were we search results.
     */
    protected GeneralEnvelope boundingBox;
    
    /**
     * The directPosition were we search results.
     */
    protected GeneralDirectPosition point ;
    
    /**
     * The line were we search results.
     */
    protected Line2D line;
    
    /**
     * The Coordinate reference system of the geometry filter.
     */
    protected final CoordinateReferenceSystem geometryCRS;
    
     /**
     * The name of the Coordinate reference system
     */
    private final String geometryCRSName;
    
    /**
     * an approximation to apply to the different filter in order to balance the lost of precision by the reprojection.
     */
    private static final double PRECISION = 0.01;
    
    /**
     * initialize the filter with the specified geometry and filterType.
     * 
     * @param geometry   A geometry object, supported types are: GeneralEnvelope, GeneralDirectPosition, Line2D
     * @param filterType a Flag representing the type of spatial filter to apply (EQUALS, BBOX, CONTAINS, ...)
     */
    public SpatialFilter(Object geometry, String crsName) throws NoSuchAuthorityCodeException, FactoryException  {
       
        if (geometry instanceof GeneralEnvelope) {
            boundingBox     = (GeneralEnvelope) geometry;
       
       } else if (geometry instanceof GeneralDirectPosition) {
            point           = (GeneralDirectPosition) geometry;
       
       } else if (geometry instanceof Line2D) {
            line           = (Line2D) geometry;
       
       } else {
           String type = "null type"; 
           if (geometry != null) 
               type = geometry.getClass().getSimpleName();
           
           throw new IllegalArgumentException("Unsupported geometry types:" + type + ".Supported ones are: GeneralEnvelope, GeneralDirectPosition, Line2D");
       }
       
       geometryCRSName = crsName;
       geometryCRS     = CRS.decode(crsName, true);
    }
    
    
    /**
     * Extract a boundingBox from the specified Document.
     *  
     * @param doc a Document containing a geometry of type bounding box.
     * @return a GeneralEnvelope.
     */
    protected GeneralEnvelope readBoundingBox(IndexReader reader, int docNum) throws CorruptIndexException, IOException {
        final FieldSelector fs = new BboxFieldSelector();
        final Document doc = reader.document(docNum, fs);

        String fullBBOX = doc.get("fullBBOX");
        if (fullBBOX == null)
            return null;

        final double minx = Double.parseDouble(fullBBOX.substring(0, fullBBOX.indexOf(',')));
        fullBBOX = fullBBOX.substring(fullBBOX.indexOf(',') + 1);
        final double maxx = Double.parseDouble(fullBBOX.substring(0, fullBBOX.indexOf(',')));
        fullBBOX = fullBBOX.substring(fullBBOX.indexOf(',') + 1);
        final double miny = Double.parseDouble(fullBBOX.substring(0, fullBBOX.indexOf(',')));
        fullBBOX = fullBBOX.substring(fullBBOX.indexOf(',') + 1);
        final double maxy = Double.parseDouble(fullBBOX.substring(0, fullBBOX.indexOf(',')));
        fullBBOX = fullBBOX.substring(fullBBOX.indexOf(',') + 1);
        final String sourceCRSName = fullBBOX;

        final double[] min = {minx, miny};
        final double[] max = {maxx, maxy};
        GeneralEnvelope result = null;
        
        try {
            result = new GeneralEnvelope(min, max);
        
        } catch (IllegalArgumentException e) {
            String s = "unknow";
            final Field f = doc.getField("Title");
            if (f != null)
                s = f.stringValue();
        
            LOGGER.severe("Unable to read the bouding box(minx="+ minx +" miny=" + miny + " maxx=" + maxx + " maxy=" + maxy + ")for the Document:" + s + '\n' +
                          "cause: " + e.getMessage());
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        try {
            if (result != null) {
                if (sourceCRSName.equals(geometryCRSName)) {
                    result.setCoordinateReferenceSystem(geometryCRS);
                } else {
                    final CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSName, true);
                    result.setCoordinateReferenceSystem(sourceCRS);
                    final String boxbefore = result.toString();
                    if (!CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS)) {
                        LOGGER.finer("sourceCRS:" + sourceCRS + '\n' +
                                    "geometryCRS:" + geometryCRS + '\n' +
                                    "equals? " + CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS)); 
                        result = (GeneralEnvelope) GeometricUtilities.reprojectGeometry(geometryCRSName, sourceCRSName, result);
                        LOGGER.finer("reprojecting from " + sourceCRSName + " to " + geometryCRSName + '\n' +
                                    "bbox before: " + boxbefore + '\n' +
                                    "bbox after : " + result.toString());
                    }
                }
            }
        
        } catch (NoSuchAuthorityCodeException ex) {
            LOGGER.severe("No such Authority exception while reading boundingBox:" + ex.getAuthority() + ':' + ex.getAuthorityCode());
        } catch (FactoryException ex) {
            LOGGER.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            LOGGER.severe("Transform exception while reading boundingBox");
        }

        return result;
    }
    
    /**
     * Extract a Line from the specified Document.
     *  
     * @param doc a Document containing a geometry of type line.
     * @return a Line2D.
     */
    protected Line2D readLine(IndexReader reader, int docNum) throws CorruptIndexException, IOException {
        final FieldSelector fs = new LineFieldSelector();
        final Document doc     = reader.document(docNum, fs);

        String fullLine = doc.get("fullLine");
        final double x1 = Double.parseDouble(fullLine.substring(0, fullLine.indexOf(',')));
        fullLine = fullLine.substring(fullLine.indexOf(',') + 1);
        final double y1 = Double.parseDouble(fullLine.substring(0, fullLine.indexOf(',')));
        fullLine = fullLine.substring(fullLine.indexOf(',') + 1);
        final double x2 = Double.parseDouble(fullLine.substring(0, fullLine.indexOf(',')));
        fullLine = fullLine.substring(fullLine.indexOf(',') + 1);
        final double y2 = Double.parseDouble(fullLine.substring(0, fullLine.indexOf(',')));
        fullLine = fullLine.substring(fullLine.indexOf(',') + 1);
        final String sourceCRSName = fullLine;
        
        Line2D result = new Line2D.Double(x1, y1, x2, y2);
        try {
            if (!sourceCRSName.equals(geometryCRSName)) {
                final CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSName, true);
                if (!CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS))
                    result =  (Line2D) GeometricUtilities.reprojectGeometry(geometryCRSName, sourceCRSName, result);
            }
        
        } catch (NoSuchAuthorityCodeException ex) {
            LOGGER.severe("No such Authority exception while reading boundingBox");
        } catch (FactoryException ex) {
            LOGGER.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            LOGGER.severe("Transform exception while reading boundingBox");
        }
        
        return result;
    }
    
    /**
     * Extract a Point from the specified Document.
     *  
     * @param doc a Document containing a geometry of type point.
     * @return a GeneralDirectPosition.
     */
    protected GeneralDirectPosition readPoint(IndexReader reader, int docNum) throws CorruptIndexException, IOException {
        final FieldSelector fs = new PointFieldSelector();
        final Document doc     = reader.document(docNum, fs);

        String fullPoint = doc.get("fullPoint");
        final double x = Double.parseDouble(fullPoint.substring(0, fullPoint.indexOf(',')));
        fullPoint = fullPoint.substring(fullPoint.indexOf(',') + 1);
        final double y = Double.parseDouble(fullPoint.substring(0, fullPoint.indexOf(',')));
        fullPoint = fullPoint.substring(fullPoint.indexOf(',') + 1);
        final String sourceCRSName = fullPoint;
        GeneralDirectPosition result = new GeneralDirectPosition(y, x);
        
        try {
            if (sourceCRSName.equals(geometryCRSName)) {
                
                result.setCoordinateReferenceSystem(geometryCRS);
                
            } else {
                CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSName, true);
                result.setCoordinateReferenceSystem(sourceCRS);
                if (!CRS.equalsIgnoreMetadata(geometryCRS, sourceCRS)) {
                    result = (GeneralDirectPosition) GeometricUtilities.reprojectGeometry(geometryCRSName, sourceCRSName, result);
                    result.setCoordinateReferenceSystem(geometryCRS); 
                }
            }
        
        } catch (NoSuchAuthorityCodeException ex) {
            LOGGER.severe("No such Authority exception while reading boundingBox");
        } catch (FactoryException ex) {
            LOGGER.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            LOGGER.severe("Transform exception while reading boundingBox");
        }
        
        return result; 
    }
    
    
    /**
     * Return the current geometry object.
     * 
     */
    public Object getGeometry() {
        if (boundingBox != null)
            return boundingBox;
        else if (line != null)
            return line;
        else if (point != null)
            return point;
        return null;
    }
    
    /**
     * Return a String description of the filter
     */
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[").append(this.getClass().getSimpleName()).append("]: ").append('\n');
        if (boundingBox != null) {
            s.append("geometry types: GeneralEnvelope.").append('\n').append(boundingBox);
        } else if (line != null) {
            s.append("geometry types: Line2D.").append('\n').append(GeometricUtilities.logLine2D(line));
        } else if (point != null) {
            s.append("geometry types: GeneralDirectPosition.").append('\n').append(point);
        }
        s.append("geometry CRS: ").append(geometryCRSName).append('\n');
        s.append("precision: ").append(PRECISION).append('\n');
        return s.toString();
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof SpatialFilter) {
            final SpatialFilter that = (SpatialFilter) object;

            return Utilities.equals(this.boundingBox,     that.boundingBox)     &&
                   Utilities.equals(this.geometryCRS,     that.geometryCRS)     &&
                   Utilities.equals(this.geometryCRSName, that.geometryCRSName) &&
                   Utilities.equals(this.line,            that.line)            &&
                   Utilities.equals(this.point,           that.point);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.boundingBox != null ? this.boundingBox.hashCode() : 0);
        hash = 29 * hash + (this.point != null ? this.point.hashCode() : 0);
        hash = 29 * hash + (this.line != null ? this.line.hashCode() : 0);
        hash = 29 * hash + (this.geometryCRS != null ? this.geometryCRS.hashCode() : 0);
        hash = 29 * hash + (this.geometryCRSName != null ? this.geometryCRSName.hashCode() : 0);
        return hash;
    }

    private class BboxFieldSelector implements FieldSelector {

        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = 6425032295868468704L;

        public FieldSelectorResult accept(String fieldName) {
            if (fieldName != null) {
                if (fieldName.equals("fullBBOX")) {
                    return FieldSelectorResult.LOAD_AND_BREAK;
                }
                return FieldSelectorResult.NO_LOAD;
                
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }

    private class LineFieldSelector implements FieldSelector {

        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = 7538448359239289388L;

        public FieldSelectorResult accept(String fieldName) {
            if (fieldName != null) {
                if (fieldName.equals("fullLine")) {
                    return FieldSelectorResult.LOAD_AND_BREAK;
                }
                return FieldSelectorResult.NO_LOAD;
                
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }

    private class PointFieldSelector implements FieldSelector {

        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = 3971433534031015795L;

        public FieldSelectorResult accept(String fieldName) {
            if (fieldName != null) {
                if (fieldName.equals("fullPoint")) {
                    return FieldSelectorResult.LOAD_AND_BREAK;
                }
                return FieldSelectorResult.NO_LOAD;
                
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }
}
