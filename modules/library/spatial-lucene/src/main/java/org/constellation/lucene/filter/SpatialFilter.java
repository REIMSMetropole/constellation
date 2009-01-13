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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;

// Apache Lucene dependencies
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;

// geotools dependencies
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.Utilities;

// GeoAPI dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * A lucene filter for OGC spatial filter

 * @author Guilhem legal
 */
public class SpatialFilter extends Filter {
    
    Logger logger = Logger.getLogger("org.constellation.lucene.filter");
    
    /**
     * The envelope were we search results.
     */
    private GeneralEnvelope boundingBox;
    
    /**
     * The directPosition were we search results.
     */
    private GeneralDirectPosition point ;
    
    /**
     * The line were we search results.
     */
    private Line2D line;
    
    /**
     * The Coordinate reference system of the geometry filter.
     */
    private final CoordinateReferenceSystem geometryCRS;
    
     /**
     * The name of the Coordinate reference system
     */
    private final String geometryCRSName;
    
    /**
     * The distance used in a Dwithin or Beyond spatial filter.
     */
    private Double distance;
    
    /**
     * The unit of measure for the distance.
     */
    private String distanceUnit;
    
    /**
     * The current filter type to apply.
     */
    private int filterType;

    
    public final static int CONTAINS  = 0; //ok
    public final static int INTERSECT = 1; //ok
    public final static int EQUALS    = 2; //ok
    public final static int DISJOINT  = 3; //ok
    public final static int BBOX      = 4; //ok
    public final static int BEYOND    = 5; //ok
    public final static int CROSSES   = 6; //ok       
    public final static int DWITHIN   = 7; //ok
    public final static int WITHIN    = 8; //ok
    public final static int TOUCHES   = 9; //ok
    public final static int OVERLAPS  = 10;//todo
    
    private final static List<String> SUPPORTED_UNITS;
    static {
        SUPPORTED_UNITS = new ArrayList<String>();
        SUPPORTED_UNITS.add("kilometers");
        SUPPORTED_UNITS.add("km");
        SUPPORTED_UNITS.add("meters");
        SUPPORTED_UNITS.add("m");
        SUPPORTED_UNITS.add("centimeters");
        SUPPORTED_UNITS.add("cm");
        SUPPORTED_UNITS.add("milimeters");
        SUPPORTED_UNITS.add("mm");
        SUPPORTED_UNITS.add("miles");
        SUPPORTED_UNITS.add("mi");
    }
    
    /**
     * an approximation to apply to the different filter in order to balance the lost of precision by the reprojection.
     */
    private final double precision    = 0.01;
    
    /**
     * initialize the filter with the specified geometry and filterType.
     * 
     * @param geometry   A geometry object, supported types are: GeneralEnvelope, GeneralDirectPosition, Line2D
     * @param filterType a Flag representing the type of spatial filter to apply (EQUALS, BBOX, CONTAINS, ...)
     */
    public SpatialFilter(Object geometry, String crsName, int filterType) throws NoSuchAuthorityCodeException, FactoryException  {
       
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
       
       this.filterType = filterType;
       if (filterType > 10  || filterType < 0) {
           throw new IllegalArgumentException("The filterType is not valid.");
       } else if (filterType == DWITHIN || filterType == BEYOND) {
           throw new IllegalArgumentException("This filterType must be specfied with a distance and an unit.");
       }
       
       geometryCRSName = crsName;
       geometryCRS     = CRS.decode(crsName, true);
    }
    
    /**
     * initialize the filter with the specified geometry and filterType.
     * 
     * @param geometry   A geometry object, supported types are: GeneralEnvelope, GeneralDirectPosition, Line2D.
     * @param filterType A flag representing the type of spatial filter to apply restricted to Beyond and Dwithin.
     * @param distance   The distance to applies to this filter.
     * @param units      The unit of measure of the distance.
     */
    public SpatialFilter(Object geometry, String crsName, int filterType, Double distance, String units) throws NoSuchAuthorityCodeException, FactoryException  {
       
       this.distance = distance;
       if (!SUPPORTED_UNITS.contains(units)) {
           String msg = "Unsupported distance units. supported ones are: ";
           for (String s: SUPPORTED_UNITS) {
               msg = msg + s + ',';
           }
           msg = msg.substring(0, msg.length() - 1);
           throw new IllegalArgumentException(msg);
       } 
       this.distanceUnit = units;
       
       if (geometry instanceof GeneralEnvelope) {
            boundingBox     = (GeneralEnvelope) geometry;
       
       } else if (geometry instanceof GeneralDirectPosition) {
            point           = (GeneralDirectPosition) geometry;
       
       } else if (geometry instanceof Line2D) {
            line           = (Line2D) geometry;
       
       } else {
           throw new IllegalArgumentException("Unsupported geometry. supported ones are: GeneralEnvelope, GeneralDirectPosition, Line2D");
       }
       
       this.filterType = filterType;
       if (filterType != 5  && filterType != 7 ) {
           throw new IllegalArgumentException("The filterType is not valid: allowed ones are DWithin, Beyond");
       }
       
       geometryCRSName = crsName;
       geometryCRS     = CRS.decode(crsName, true);
    }
    
    
    @Override
    public BitSet bits(IndexReader reader) throws IOException {
        // we prepare the result
        BitSet bits = new BitSet(reader.maxDoc());
        
        TermDocs termDocs = reader.termDocs(new Term("geometry"));
                  
        // we are searching for matching points
        termDocs.seek(new Term("geometry", "point"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            GeneralDirectPosition tempPoint = readPoint(reader, docNum);
            Line2D pointLine                = new Line2D.Double(tempPoint.getOrdinate(0), tempPoint.getOrdinate(1), 
                                                                tempPoint.getOrdinate(0), tempPoint.getOrdinate(1));
            switch (filterType) {
                
                case BBOX :
                    
                    if (boundingBox != null && boundingBox.contains(tempPoint)) {
                            bits.set(docNum);
                    }
                    break;
            
                case EQUALS :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                    }
                    break;
            
                case TOUCHES :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                
                    } else if (boundingBox != null && GeometricUtilities.touches(boundingBox, tempPoint)) {
                        bits.set(docNum);
                        
                    } else if (line != null && line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
                
                case INTERSECT :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (boundingBox != null && boundingBox.contains(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (line != null && line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case DISJOINT :
                
                    if (point != null && !point.equals(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (boundingBox != null && !boundingBox.contains(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (line != null && !line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case WITHIN :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                   
                    } else if (boundingBox != null && boundingBox.contains(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (line != null && line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
            
                case CROSSES :
                    
                    if (point != null && point.equals(tempPoint)) {
                        bits.set(docNum);
                    
                    } else if (boundingBox != null && GeometricUtilities.touches(boundingBox, tempPoint)) {
                       bits.set(docNum);

                    } else if (line != null && line.intersectsLine(pointLine)) {
                        bits.set(docNum);
                    }
                    break;
                
                case DWITHIN :
                    
                    if (getDistance(tempPoint) < distance) 
                        bits.set(docNum);
                    break;
                    
                case BEYOND :
                    
                    if (getDistance(tempPoint) > distance) 
                        bits.set(docNum);
                    break;
            }
        }
        
        
        //then we search for matching box
        termDocs.seek(new Term("geometry", "boundingbox"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            GeneralEnvelope tempBox = readBoundingBox(reader, docNum);
            if (tempBox == null)
                continue;
            switch (filterType) {

                case CONTAINS:
            
                    if (boundingBox != null && tempBox.contains(boundingBox, false)) {
                        bits.set(docNum);
                        
                    } else if (line != null && GeometricUtilities.contains(tempBox, line)) {
                        bits.set(docNum);
                        
                    } else if (point != null && tempBox.contains(point)) {
                        bits.set(docNum);
                    }
                    break;
                
                case BBOX :
                    
                    if (boundingBox != null && boundingBox.intersects(tempBox, false)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case INTERSECT :
                    
                    if (boundingBox != null  && boundingBox.intersects(tempBox, false)) {
                        bits.set(docNum);
                        
                    } else if (point != null && tempBox.contains(point)){
                        bits.set(docNum);
                        
                    } else if (line != null  && GeometricUtilities.intersect(tempBox, line)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case EQUALS :
                    
                    if (boundingBox != null && boundingBox.equals(tempBox)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case DISJOINT :
                
                    if (boundingBox != null && !boundingBox.intersects(tempBox, false)) {
                        bits.set(docNum);
                        
                    } else if (point != null && !tempBox.contains(point)) {
                        bits.set(docNum);
                        
                    } else if (line != null && GeometricUtilities.disjoint(tempBox, line)) {
                        bits.set(docNum);
                        
                    }
                    break;
                
                case WITHIN :
                
                    if (boundingBox != null && boundingBox.contains(tempBox, false)) {
                        bits.set(docNum);
                    }
                    break;
                
                case CROSSES :
                
                    if (line != null && GeometricUtilities.crosses(tempBox, line)) {
                        bits.set(docNum);
                
                    } else if (point != null && GeometricUtilities.crosses(tempBox, point)) {
                        bits.set(docNum);
                    }
                    break;
                
                case TOUCHES :
                
                    if (point != null ) {
                        if (GeometricUtilities.touches(tempBox, point))
                            bits.set(docNum);
                
                    } else if (line != null && GeometricUtilities.touches(tempBox, line)) {
                        
                            bits.set(docNum);
                        
                    } else if (boundingBox != null && GeometricUtilities.touches(boundingBox, tempBox)) {
                       
                        bits.set(docNum);
                        
                    }
                    break;
                    
                case DWITHIN :
                    
                    if (getDistance(tempBox) < distance) 
                        bits.set(docNum);
                    break;
                    
                case BEYOND :
                    
                    if (getDistance(tempBox) > distance) 
                        bits.set(docNum);
                    break;
                
                case OVERLAPS : 
                    if (boundingBox != null && GeometricUtilities.overlaps(boundingBox, tempBox)) 
                        bits.set(docNum);
                    break;
            }
        }
       
        //then we search for matching line
        termDocs.seek(new Term("geometry", "line"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            
            Line2D tempLine = readLine(reader, docNum);
            GeneralDirectPosition tempPoint1 = new GeneralDirectPosition(tempLine.getX1(), tempLine.getY1());
            tempPoint1.setCoordinateReferenceSystem(geometryCRS);
            GeneralDirectPosition tempPoint2 = new GeneralDirectPosition(tempLine.getX2(), tempLine.getY2());
            tempPoint2.setCoordinateReferenceSystem(geometryCRS);
            
            switch (filterType) {
                
                case BBOX :
                    
                    if (boundingBox != null && GeometricUtilities.intersect(boundingBox, tempLine)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case INTERSECT :
                    
                    if (boundingBox != null  && GeometricUtilities.intersect(boundingBox, tempLine)) {
                        bits.set(docNum); 
                        
                    } else if (line != null  && line.intersectsLine(tempLine)){
                        bits.set(docNum);
                        
                    } else if (point != null && tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) {
                        bits.set(docNum);
                    }
                    break;
                
                case EQUALS :
                
                    if (line != null && GeometricUtilities.equalsLine(tempLine, line)) {
                        bits.set(docNum);
                    }
                    break;
                    
                case CROSSES :
                    
                    if (line != null && line.intersectsLine(tempLine)) {
                        bits.set(docNum);
                        
                    } else if (boundingBox != null && GeometricUtilities.crosses(boundingBox, tempLine)) {
                        bits.set(docNum);
                        
                    } else if (point != null && tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) {
                       bits.set(docNum);
                    }
                    break;
                    
                case TOUCHES :
                    
                    if (line != null && GeometricUtilities.touches(line, tempLine)) {
                    
                        bits.set(docNum);
                        
                    } else if (boundingBox != null && GeometricUtilities.touches(boundingBox, tempLine)) {
                        
                        bits.set(docNum);
                        
                    } else if (point !=null && tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) {
                        bits.set(docNum);
                    }
                    break;
                    
                case CONTAINS :
                    
                    if (point !=null && tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) { 
                        bits.set(docNum);
                        
                    } else if (line != null && tempLine.intersectsLine(line.getX1(), line.getY1(), line.getX1(), line.getY1()) && 
                                               tempLine.intersectsLine(line.getX2(), line.getY2(), line.getX2(), line.getY2())) {
                        bits.set(docNum);
                        
                    }
                    break;
                    
                case DISJOINT :
                
                    if (boundingBox != null && GeometricUtilities.disjoint(boundingBox, tempLine)) {
                        bits.set(docNum);
                        
                    } else if (point != null && !tempLine.intersectsLine(point.getOrdinate(0), point.getOrdinate(1), point.getOrdinate(0), point.getOrdinate(1))) {
                        bits.set(docNum);
                    
                    } else if (line != null && !line.intersectsLine(tempLine)) {
                        bits.set(docNum);
                    }
                    break;
                
                case WITHIN :
                
                    if (line != null && line.intersectsLine(tempLine.getX1(), tempLine.getY1(), tempLine.getX1(), tempLine.getY1())
                                     && line.intersectsLine(tempLine.getX2(), tempLine.getY2(), tempLine.getX2(), tempLine.getY2())) {
                        bits.set(docNum);
                
                    } else if (boundingBox != null && boundingBox.contains(tempPoint1) && boundingBox.contains(tempPoint2) ) {
                        bits.set(docNum);
                    }
                    break;
                
                case DWITHIN :
                    
                    if (getDistance(tempLine) < distance) 
                        bits.set(docNum);
                    break;
                    
                case BEYOND :
                    if (getDistance(tempLine) > distance) 
                        bits.set(docNum);
                    break;
            }
        }
        
        
        return bits;
    }
    
    
    /**
     * Extract a boundingBox from the specified Document.
     *  
     * @param doc a Document containing a geometry of type bounding box.
     * @return a GeneralEnvelope.
     */
    private GeneralEnvelope readBoundingBox(IndexReader reader, int docNum) throws CorruptIndexException, IOException {
        FieldSelector fs = new BboxFieldSelector();
        Document doc = reader.document(docNum, fs);

        double minx = Double.parseDouble(doc.get("minx"));
        double miny = Double.parseDouble(doc.get("miny"));
        double maxx = Double.parseDouble(doc.get("maxx"));
        double maxy = Double.parseDouble(doc.get("maxy"));
        String sourceCRSName = doc.get("CRS");
        
        double[] min = {minx, miny};
        double[] max = {maxx, maxy};
        GeneralEnvelope result = null;
        
        try {
            result = new GeneralEnvelope(min, max);
        
        } catch (IllegalArgumentException e) {
            String s = "unknow";
            Field f = doc.getField("Title");
            if (f != null)
                s = f.stringValue();
        
            logger.severe("Unable to read the bouding box(minx="+ minx +" miny=" + miny + " maxx=" + maxx + " maxy=" + maxy + ")for the Document:" + s + '\n' +
                          "cause: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            if (result != null) {
                if (sourceCRSName.equals(geometryCRSName)) {
                    result.setCoordinateReferenceSystem(geometryCRS);
                
                } else {
                
                    CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSName, true);
                    result.setCoordinateReferenceSystem(sourceCRS);
                    String boxbefore = result.toString(); 
                    if (!CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS)) {
                        logger.finer("sourceCRS:" + sourceCRS + '\n' +
                                    "geometryCRS:" + geometryCRS + '\n' +
                                    "equals? " + CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS)); 
                        result = (GeneralEnvelope) GeometricUtilities.reprojectGeometry(geometryCRSName, sourceCRSName, result);
                        logger.finer("reprojecting from " + sourceCRSName + " to " + geometryCRSName + '\n' +
                                    "bbox before: " + boxbefore + '\n' +
                                    "bbox after : " + result.toString());
                    }
                }
            }
        
        } catch (NoSuchAuthorityCodeException ex) {
            logger.severe("No such Authority exception while reading boundingBox:" + ex.getAuthority() + ':' + ex.getAuthorityCode());
        } catch (FactoryException ex) {
            logger.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            logger.severe("Transform exception while reading boundingBox");
        }

        return result;
    }
    
    /**
     * Extract a Line from the specified Document.
     *  
     * @param doc a Document containing a geometry of type line.
     * @return a Line2D.
     */
    private Line2D readLine(IndexReader reader, int docNum) throws CorruptIndexException, IOException {
        FieldSelector fs = new LineFieldSelector();
        Document doc= reader.document(docNum, fs);

        double x1 = Double.parseDouble(doc.get("x1"));
        double y1 = Double.parseDouble(doc.get("y1"));
        double x2 = Double.parseDouble(doc.get("x2"));
        double y2 = Double.parseDouble(doc.get("y2"));
        String sourceCRSName = doc.get("CRS");
        Line2D result = new Line2D.Double(x1, y1, x2, y2);
        try {
            if (!sourceCRSName.equals(geometryCRSName)) {
                CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSName, true);
                if (!CRS.equalsIgnoreMetadata(sourceCRS, geometryCRS))
                    result =  (Line2D) GeometricUtilities.reprojectGeometry(geometryCRSName, sourceCRSName, result);
            }
        
        } catch (NoSuchAuthorityCodeException ex) {
            logger.severe("No such Authority exception while reading boundingBox");
        } catch (FactoryException ex) {
            logger.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            logger.severe("Transform exception while reading boundingBox");
        }
        
        return result;
    }
    
    /**
     * Extract a Point from the specified Document.
     *  
     * @param doc a Document containing a geometry of type point.
     * @return a GeneralDirectPosition.
     */
    private GeneralDirectPosition readPoint(IndexReader reader, int docNum) throws CorruptIndexException, IOException {
        FieldSelector fs = new PointFieldSelector();
        Document doc= reader.document(docNum, fs);

        double x = Double.parseDouble(doc.get("x"));
        double y = Double.parseDouble(doc.get("y"));
        String sourceCRSName = doc.get("CRS");
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
            logger.severe("No such Authority exception while reading boundingBox");
        } catch (FactoryException ex) {
            logger.severe("Factory exception while reading boundingBox");
        }  catch (TransformException ex) {
            logger.severe("Transform exception while reading boundingBox");
        }
        
        return result; 
    }
    
    
    /**
     * Return the orthodromic distance between two geometric object on the earth.
     * 
     * @param geometry a geometric object.
     */
    private double getDistance(final Object geometry) {
        if (geometry instanceof GeneralDirectPosition) {

            GeneralDirectPosition tempPoint = (GeneralDirectPosition) geometry;
            if (point != null) {
                return GeometricUtilities.getOrthodromicDistance(tempPoint.getOrdinate(0), tempPoint.getOrdinate(1),
                                                                     point.getOrdinate(0),     point.getOrdinate(1), distanceUnit);

            } else if (boundingBox != null) {
                return GeometricUtilities.BBoxToPointDistance(boundingBox, tempPoint, distanceUnit);
                
            } else if (line != null) {
                return GeometricUtilities.lineToPointDistance(line, tempPoint, distanceUnit);
            } else {
                return 0;
            }
        
        } else if (geometry instanceof GeneralEnvelope) {
            
            GeneralEnvelope tempBox = (GeneralEnvelope) geometry;
            if (point != null) {
                return GeometricUtilities.BBoxToPointDistance(tempBox, point, distanceUnit);
            
            } else if (line != null) {
                return GeometricUtilities.lineToBBoxDistance(line, tempBox, distanceUnit);
                
            } else if (boundingBox != null) {
                return GeometricUtilities.BBoxToBBoxDistance(tempBox, boundingBox, distanceUnit);
            
            } else {
                return 0;
            }
        
        } else if (geometry instanceof Line2D) {
            
            Line2D tempLine = (Line2D) geometry;
            if (point != null) {
                return GeometricUtilities.lineToPointDistance(tempLine, point, distanceUnit);
            
            } else if (line != null) {
                return GeometricUtilities.lineTolineDistance(tempLine, line, distanceUnit); 
            
            } else if (boundingBox != null) {
                return GeometricUtilities.lineToBBoxDistance(tempLine, boundingBox, distanceUnit);
                
            } else {
                return 0;
            }
        } else {
            return 0;
        }   
    }
    
    /**
     * Return the current filter type.
     * 
     */
    public int getFilterType() {
        return filterType;
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
     * Return the distance units (in case of a Distance Spatial filter).
     */
    public String getDistanceUnit() {
        return this.distanceUnit;
    }
    
    /**
     * Return the distance (in case of a Distance Spatial filter).
     */
    public Double getDistance() {
        return this.distance;
    }
            
    /**
     * Return a string description of the filter type.
     */
    public static String valueOf(final int filterType) {
        switch (filterType) {
            case 0: return "CONTAINS";
            
            case 1:  return "INTERSECT";
            case 2:  return "EQUALS";
            case 3:  return "DISJOINT";
            case 4:  return "BBOX";
            case 5:  return "BEYOND";
            case 6:  return "CROSSES";       
            case 7:  return "DWITHIN";
            case 8:  return "WITHIN";
            case 9:  return "TOUCHES";
            case 10: return "OVERLAPS";
            default: return "UNKNOW FILTER TYPE";
        }
    }
    
    /**
     * Return The flag corresponding to the specified spatial operator name.
     * 
     * @param operator The spatial operator name.
     * 
     * @return a flag.
     */
    public static int valueOf(final String operator) {
        
        if (operator.equalsIgnoreCase("Intersects")) {
            return SpatialFilter.INTERSECT;
        } else if (operator.equalsIgnoreCase("Touches")) {
            return SpatialFilter.TOUCHES;
        } else if (operator.equalsIgnoreCase("Disjoint")) {
            return SpatialFilter.DISJOINT;
        } else if (operator.equalsIgnoreCase("Crosses")) {
            return SpatialFilter.CROSSES;
        } else if (operator.equalsIgnoreCase("Contains")) {
            return SpatialFilter.CONTAINS;
        } else if (operator.equalsIgnoreCase("Equals")) {
            return SpatialFilter.EQUALS;
        } else if (operator.equalsIgnoreCase("Overlaps")) {
            return SpatialFilter.OVERLAPS;
        } else if (operator.equalsIgnoreCase("Within")) {
            return SpatialFilter.WITHIN;
        } else {
            return -1;
        }
    }
    
    /**
     * Return a String description of the filter
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[SpatialFilter]: ").append(valueOf(filterType)).append('\n');
        if (boundingBox != null) {
            s.append("geometry types: GeneralEnvelope.").append('\n').append(boundingBox);
        } else if (line != null) {
            s.append("geometry types: Line2D.").append('\n').append(GeometricUtilities.logLine2D(line));
        } else if (point != null) {
            s.append("geometry types: GeneralDirectPosition.").append('\n').append(point);
        }
        s.append("geometry CRS: ").append(geometryCRSName).append('\n');
        s.append("precision: ").append(precision).append('\n');
        if (distance != null) 
            s.append("Distance: ").append(distance).append(" ").append(distanceUnit).append('\n');
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
                   Utilities.equals(this.distance,        that.distance)        &&
                   Utilities.equals(this.distanceUnit,    that.distanceUnit)    &&
                   Utilities.equals(this.geometryCRS,     that.geometryCRS)     &&
                   Utilities.equals(this.geometryCRSName, that.geometryCRSName) &&
                   Utilities.equals(this.line,            that.line)            &&
                   Utilities.equals(this.point,           that.point)           &&
                   Utilities.equals(this.precision,       that.precision)       &&
                   Utilities.equals(this.filterType,      that.filterType);
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
        hash = 29 * hash + (this.distance != null ? this.distance.hashCode() : 0);
        hash = 29 * hash + (this.distanceUnit != null ? this.distanceUnit.hashCode() : 0);
        hash = 29 * hash + this.filterType;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.precision) ^ (Double.doubleToLongBits(this.precision) >>> 32));
        return hash;
    }

    private class BboxFieldSelector implements FieldSelector {

        public FieldSelectorResult accept(String fieldName) {
            if (fieldName != null) {
                if (fieldName.equals("minx") || fieldName.equals("miny") ||
                    fieldName.equals("maxx") || fieldName.equals("maxy") ||
                    fieldName.equals("CRS")  || fieldName.equals("Title")) {
                    return FieldSelectorResult.LOAD;
                } else {
                    return FieldSelectorResult.NO_LOAD;
                }
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }

    private class LineFieldSelector implements FieldSelector {

        public FieldSelectorResult accept(String fieldName) {
            if (fieldName != null) {
                if (fieldName.equals("x1") || fieldName.equals("y1") ||
                    fieldName.equals("x2") || fieldName.equals("y2") ||
                    fieldName.equals("CRS")) {
                    return FieldSelectorResult.LOAD;
                } else {
                    return FieldSelectorResult.NO_LOAD;
                }
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }

    private class PointFieldSelector implements FieldSelector {

        public FieldSelectorResult accept(String fieldName) {
            if (fieldName != null) {
                if (fieldName.equals("x") || fieldName.equals("y") ||
                    fieldName.equals("CRS")) {
                    return FieldSelectorResult.LOAD;
                } else {
                    return FieldSelectorResult.NO_LOAD;
                }
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }
}
