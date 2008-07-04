/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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

package net.seagis.metadata;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// Lucene dependencies
import net.seagis.lucene.Filter.SpatialQuery;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.LockObtainFailedException;

// MDWeb dependencies
import org.mdweb.lucene.AbstractIndex;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.sql.Reader;

// SeaGIS dependencies
import static net.seagis.metadata.CSWworker.*;

/**
 *
 * @author legal
 */
public class IndexLucene extends AbstractIndex {

    private final Logger logger = Logger.getLogger("net.seagis.coverage");
    
    /**
     * The Reader of this lucene index.
     */
    private final Reader reader;
    
    /**
     * A lucene analyser.
     */
    private final Analyzer analyzer;
    
    /**
     * A default Query requesting all the document
     */
    private final Query simpleQuery = new TermQuery(new Term("metafile", "doc"));
    
    /**
     * Creates a new Lucene Index.
     * 
     * @param reader An mdweb reader for read the metadata database.
     * @param configDirectory A directory where the index can write indexation file. 
     */
    public IndexLucene(Reader reader, File configDirectory) throws SQLException {
        
        this.reader = reader;
        analyzer    = new StandardAnalyzer();
        
        // we get the configuration file
        File f = new File(configDirectory, "index");
        
        setFileDirectory(f);
        
        //if the index File exists we don't need to index the documents again.
        if(!getFileDirectory().exists()){
            logger.info("Creating lucene index for the first time...");
            long time = System.currentTimeMillis();
            IndexWriter writer;
            int nbCatalogs = 0;
            int nbForms    = 0; 
            try {
                writer = new IndexWriter(getFileDirectory(), analyzer, true);
                
                // getting the objects list and index avery item in the IndexWriter.
                List<Catalog> cats = reader.getCatalogs();
                nbCatalogs = cats.size();
                List<Form> results = reader.getAllForm(cats);
                nbForms    =  results.size();
                for (Form form : results) {
                    indexDocument(writer, form);
                }
                writer.optimize();
                writer.close();
                
            } catch (CorruptIndexException ex) {
                ex.printStackTrace();
            } catch (LockObtainFailedException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            logger.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' + 
                        "catalogs: " + nbCatalogs + " documents indexed: " + nbForms);
        } else {
            logger.info("Index already created");
        }
    }
    
    /**
     * This method add to index of lucene a new document based on Form object.
     * (implements AbstractIndex.indexDocument() )
     * object must be a Form.
     * 
     * @param writer A lucene Index Writer.
     * @param object A MDweb formular.
     */
    public void indexDocument(IndexWriter writer, Object object) {
        Form r;
        if (object instanceof Form) {
            r = (Form) object;
        } else {
            throw new IllegalArgumentException("Unexpected type, supported one is: org.mdweb.model.storage.Form");
        }
        try {
            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(r));
        } catch (SQLException ex) {
            logger.severe("SQLException " + ex.getMessage());
            ex.printStackTrace();
        } catch (CorruptIndexException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Return a string description for the specified terms
     * 
     * @param term An ISO queryable term defined in CSWWorker (like Title, Subject, Abstract,...)
     * @param form An MDWeb formular from whitch we extract the values correspounding to the specified term.
     * 
     * @return A string concataining the differents values correspounding to the specified term, coma separated.
     */
    private String getValues(String term, Form form) throws SQLException {
        StringBuilder response  = new StringBuilder("");
        for (String pathID: ISO_QUERYABLE.get(term)) {
            Path path   = reader.getPath(pathID);
            List<Value> values = form.getValueFromPath(path);
            for (Value v: values) {
                if (v instanceof TextValue) {
                    TextValue tv = (TextValue) v;
                    response.append(tv.getValue()).append(','); 
                }
            }
        }
        if (response.toString().equals("")) {
            response.append("null");
        } else {
            // we remove the last ','
            response.delete(response.length() - 1, response.length()); 
        }
        return response.toString();
    }
    
   /**
    * Makes a document for a MDWeb formular.
    * 
    * @param Form An MDweb formular to index.
    * @return A Lucene document.
    */
    private Document createDocument(Form form) throws SQLException {
        
        // make a new, empty document
        Document doc = new Document();
        
        doc.add(new Field("id",    form.getId() + "", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("Title", form.getTitle(),   Field.Store.YES, Field.Index.TOKENIZED));
        
        //TODO add ANyText
        for (String term :ISO_QUERYABLE.keySet()) {
            doc.add(new Field(term, getValues(term,  form),   Field.Store.YES, Field.Index.TOKENIZED));
        }
        
        //we add the geometry parts
        String coord = "null";
        try {
            coord = getValues("WestBoundLongitude", form);
            double minx = Double.parseDouble(coord);
            
            coord = getValues("EastBoundLongitude", form);
            double maxx = Double.parseDouble(coord);
            
            coord = getValues("NorthBoundLatitude", form);
            double maxy = Double.parseDouble(coord);
            
            coord = getValues("SouthBoundLatitude", form);
            double miny = Double.parseDouble(coord);
            
            addBoundingBox(doc, minx, maxx, miny, maxy, "EPSG:4326");
            
        } catch (NumberFormatException e) {
            if (!coord.equals("null"))
                logger.severe("unable to spatially index form: " + form.getTitle() + '\n' +
                              "cause:  unable to parse double: " + coord);
        }    
        // add a default meta field to make searching all documents easy 
	doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.TOKENIZED));
        
        return doc;
    }
    
    /**
     * This method proceed a lucene search and returns a list of ID.
     *
     * @param query   The lucene query string with spatials filters.
     * 
     * @return                A List of id.
     */
    public List<String> doSearch(SpatialQuery spatialQuery) throws CorruptIndexException, IOException, ParseException {
        
        List<String> results = new ArrayList<String>();
        
        IndexReader ireader = IndexReader.open(getFileDirectory());
        Searcher searcher   = new IndexSearcher(ireader);
        String field        = "Title";
        QueryParser parser  = new QueryParser(field, analyzer);
        
        Query query = parser.parse(spatialQuery.getQuery());
        Filter f    = spatialQuery.getSpatialFilter();
        logger.info("Searching for: "    + query.toString(field) + '\n' +
                    " with the filter: " + f);
        
        Hits hits = searcher.search(query, f);
        
        logger.info(hits.length() + " total matching documents");
        
        for (int i = 0; i < hits.length(); i ++) {
            
            Document doc = hits.doc(i);
            results.add(doc.get("Identifier"));
        }
        ireader.close();
        
        return results;
    }  
    
    /**
     * Add a boundingBox geometry to the specified Document.
     * 
     * @param doc  The document to add the geometry
     * @param minx the minimun X coordinate of the bounding box.
     * @param maxx the maximum X coordinate of the bounding box.
     * @param miny the minimun Y coordinate of the bounding box.
     * @param maxy the maximum Y coordinate of the bounding box.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addBoundingBox(Document doc, double minx, double maxx, double miny, double maxy, String crsName) {

        // convert the corner of the box to lucene fields
        doc.add(new Field("geometry" , "boundingbox", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("minx"     , minx + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("maxx"     , maxx + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("miny"     , miny + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("maxy"     , maxy + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName  ,     Field.Store.YES, Field.Index.UN_TOKENIZED));
        logger.info("added boundingBox");
    }
    
    /**
     *  Add a point geometry to the specified Document.
     * 
     * @param doc     The document to add the geometry
     * @param x       The x coordinate of the point.
     * @param y       The y coordinate of the point.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addPoint(Document doc, double y, double x, String crsName) {

        // convert the lat / long to lucene fields
        doc.add(new Field("geometry" , "point", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x"        , x + "" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y"        , y + "" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName, Field.Store.YES, Field.Index.UN_TOKENIZED));
       
    }
    
    /**
     * Add a Line geometry to the specified Document.
     * 
     * @param doc The document to add the geometry
     * @param x1  the X coordinate of the first point of the line.
     * @param y1  the Y coordinate of the first point of the line.
     * @param x2  the X coordinate of the second point of the line.
     * @param y2  the Y coordinate of the first point of the line.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addLine(Document doc, double x1, double y1, double x2, double y2, String crsName) {

        
        // convert the corner of the box to lucene fields
        doc.add(new Field("geometry" , "line" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x1"       , x1 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y1"       , y1 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x2"       , x2 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y2"       , y2 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName, Field.Store.YES, Field.Index.UN_TOKENIZED));
    }
   
}
