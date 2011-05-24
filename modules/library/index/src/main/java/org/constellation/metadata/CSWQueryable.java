/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;


/**
 * A container for list of queryable elements in different schemas used in CSW.
 * 
 * @author Guilhem Legal
 */
public final class CSWQueryable {

     public static final String INSPIRE  = "http://www.inspire.org";
     public static final String INSPIRE_PREFIX  = "ins";

     public static final QName DEGREE_QNAME                               = new QName(INSPIRE, "Degree",                          INSPIRE_PREFIX);
     public static final QName ACCESS_CONSTRAINTS_QNAME                   = new QName(INSPIRE, "AccessConstraints",               INSPIRE_PREFIX);
     public static final QName OTHER_CONSTRAINTS_QNAME                    = new QName(INSPIRE, "OtherConstraints",                INSPIRE_PREFIX);
     public static final QName INS_CLASSIFICATION_QNAME                   = new QName(INSPIRE, "Classification",                  INSPIRE_PREFIX);
     public static final QName CONDITION_APPLYING_TO_ACCESS_AND_USE_QNAME = new QName(INSPIRE, "ConditionApplyingToAccessAndUse", INSPIRE_PREFIX);
     public static final QName METADATA_POINT_OF_CONTACT_QNAME            = new QName(INSPIRE, "MetadataPointOfContact",          INSPIRE_PREFIX);
     public static final QName LINEAGE_QNAME                              = new QName(INSPIRE, "Lineage",                         INSPIRE_PREFIX);
     public static final QName SPECIFICATION_TITLE_QNAME                  = new QName(INSPIRE, "SpecificationTitle",              INSPIRE_PREFIX);
     public static final QName SPECIFICATION_DATE_QNAME                   = new QName(INSPIRE, "SpecificationDate",               INSPIRE_PREFIX);
     public static final QName SPECIFICATION_DATETYPE_QNAME               = new QName(INSPIRE, "SpecificationDateType",           INSPIRE_PREFIX);

     private CSWQueryable() {}
     
    /**
     * The queryable element from ISO 19115 and their path id.
     */
    public static final Map<String, List<String>> ISO_QUERYABLE;
    static {
        ISO_QUERYABLE      = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of ISO 19115
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:keyword:value");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:topicCategory");
        ISO_QUERYABLE.put("Subject", paths);
        
        //MANDATORY
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:title");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:title:value");
        ISO_QUERYABLE.put("Title", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:abstract");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:abstract:value");
        ISO_QUERYABLE.put("Abstract", paths);
        
        /*MANDATORY
        paths = new ArrayList<String>();
        ISO_QUERYABLE.put("AnyText", paths);*/
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name");
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name:value");
        paths.add("ISO 19115-2:MI_Metadata:distributionInfo:distributionFormat:name");
        paths.add("ISO 19115-2:MI_Metadata:distributionInfo:distributionFormat:name:value");
        ISO_QUERYABLE.put("Format", paths);
        
        //MANDATORY
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        paths.add("ISO 19115-2:MI_Metadata:fileIdentifier");
        ISO_QUERYABLE.put("Identifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dateStamp");
        paths.add("ISO 19115-2:MI_Metadata:dateStamp");
        ISO_QUERYABLE.put("Modified", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:hierarchyLevel");
        paths.add("ISO 19115-2:MI_Metadata:hierarchyLevel");
        ISO_QUERYABLE.put("Type", paths);
        
        /*
         * Bounding box
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
        ISO_QUERYABLE.put("WestBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
        ISO_QUERYABLE.put("EastBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
        ISO_QUERYABLE.put("NorthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
        ISO_QUERYABLE.put("SouthBoundLatitude",     paths);
        
        /*
         * CRS 
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:codeSpace");
        paths.add("ISO 19115-2:MI_Metadata:referenceSystemInfo:referenceSystemIdentifier:codeSpace");
        ISO_QUERYABLE.put("Authority",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
        paths.add("ISO 19115-2:MI_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
        ISO_QUERYABLE.put("ID",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
        paths.add("ISO 19115-2:MI_Metadata:referenceSystemInfo:referenceSystemIdentifier:code");
        ISO_QUERYABLE.put("Version",     paths);
        
        /*
         * Additional queryable Element
         */ 
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:alternateTitle");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:alternateTitle:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:alternateTitle");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:alternateTitle:value");
        ISO_QUERYABLE.put("AlternateTitle",   paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date#dateType=revision:date");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:date#dateType=revision:date");
        ISO_QUERYABLE.put("RevisionDate",  paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date#dateType=creation:date");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:date#dateType=creation:date");
        ISO_QUERYABLE.put("CreationDate",  paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:date#dateType=publication:date");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:date#dateType=publication:date");
        ISO_QUERYABLE.put("PublicationDate",  paths);
      
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName:value");
        // TODO remove the following path are not normalized
        paths.add("ISO 19115:MD_Metadata:contact:organisationName");
        paths.add("ISO 19115:MD_Metadata:contact:organisationName:value");
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributor:distributorContact:organisationName:value");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:organisationName");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:citedResponsibleParty:organisationName:value");

        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:pointOfContact:organisationName");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:pointOfContact:organisationName:value");
        // TODO remove the following path are not normalized
        paths.add("ISO 19115-2:MI_Metadata:contact:organisationName");
        paths.add("ISO 19115-2:MI_Metadata:contact:organisationName:value");
        paths.add("ISO 19115-2:MI_Metadata:distributionInfo:distributor:distributorContact:organisationName");
        paths.add("ISO 19115-2:MI_Metadata:distributionInfo:distributor:distributorContact:organisationName:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:citedResponsibleParty:organisationName");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:citedResponsibleParty:organisationName:value");
        ISO_QUERYABLE.put("OrganisationName", paths);
        
        //TODO If an instance of the class MD_SecurityConstraint exists for a resource, the “HasSecurityConstraints” is “true”, otherwise “false”
        //paths = new ArrayList<String>();
        //ISO_QUERYABLE.put("HasSecurityConstraints", paths);
        
        //TODO MD_FeatureCatalogueDescription
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:language");
        paths.add("ISO 19115-2:MI_Metadata:language");
        ISO_QUERYABLE.put("Language", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:identifier:code");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:identifier:code");
        ISO_QUERYABLE.put("ResourceIdentifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:parentIdentifier");
        paths.add("ISO 19115-2:MI_Metadata:parentIdentifier");
        ISO_QUERYABLE.put("ParentIdentifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:Type");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:Type");
        ISO_QUERYABLE.put("KeywordType", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:topicCategory");
        ISO_QUERYABLE.put("TopicCategory", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:language");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:language");
        ISO_QUERYABLE.put("ResourceLanguage", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:code");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement3:geographicIdentifier:code");
        ISO_QUERYABLE.put("GeographicDescriptionCode", paths);
        
        /*
         * spatial resolution
         */
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:equivalentScale:denominator");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:spatialResolution:equivalentScale:denominator");
        ISO_QUERYABLE.put("Denominator", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:distance");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:spatialResolution:distance");
        ISO_QUERYABLE.put("DistanceValue", paths);
        
        //TODO not existing path in MDWeb or geotoolkit (Distance is treated as a primitive type)
        paths = new ArrayList<String>();
        //paths.add("ISO 19115:MD_Metadata:identificationInfo:spatialResolution:distance:uom");
        //ISO_QUERYABLE.put("DistanceUOM", paths);
        
        /*
         * Temporal Extent
         */ 
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:position");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:temporalElement:extent:position");
        ISO_QUERYABLE.put("TempExtent_begin", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:endPosition");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:position");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:temporalElement:extent:endPosition");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:temporalElement:extent:position");
        ISO_QUERYABLE.put("TempExtent_end", paths);

        /**
         * ISO 19119 specific queryable
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:serviceType");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:serviceType");
        ISO_QUERYABLE.put("ServiceType", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:couplingType");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:couplingType");
        ISO_QUERYABLE.put("CouplingType", paths);

        //TODO  the following element are described in Service part of ISO 19139 not yet used.
        paths = new ArrayList<String>();
        ISO_QUERYABLE.put("ServiceTypeVersion", paths);
        ISO_QUERYABLE.put("Operation", paths);
        ISO_QUERYABLE.put("OperatesOn", paths);
        ISO_QUERYABLE.put("OperatesOnIdentifier", paths);
        ISO_QUERYABLE.put("OperatesOnWithOpName", paths);
    }
    
    
    
    /**
     * The queryable element from DublinCore and their path id.
     */
    public static final Map<String, List<String>> DUBLIN_CORE_QUERYABLE;
    static {
        DUBLIN_CORE_QUERYABLE = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:citation:title:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:title");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:citation:title:value");
        paths.add("Catalog Web Service:Record:title:content");
        paths.add("Ebrim v3.0:*:name:localizedString:value");
        paths.add("Ebrim v2.5:*:name:localizedString:value");
        DUBLIN_CORE_QUERYABLE.put("title", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact#role=originator:organisationName:value");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact#role=originator:organisationName");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:pointOfContact#role=originator:organisationName:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:pointOfContact#role=originator:organisationName");
        paths.add("Catalog Web Service:Record:creator:content");
        DUBLIN_CORE_QUERYABLE.put("creator", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword:value");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:topicCategory");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:keyword");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:descriptiveKeywords:keyword:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:topicCategory");
        paths.add("Catalog Web Service:Record:subject:content");
        //TODO @name = “http://purl.org/dc/elements/1.1/subject”
        paths.add("Ebrim v3.0:*:slot:valueList:value");
        paths.add("Ebrim v2.5:*:slot:valueList:value");
        DUBLIN_CORE_QUERYABLE.put("description", paths);
        DUBLIN_CORE_QUERYABLE.put("subject", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:abstract:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:abstract");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:abstract:value");
        paths.add("Catalog Web Service:Record:abstract:content");
        paths.add("Ebrim v3.0:*:description:localizedString:value");
        paths.add("Ebrim v2.5:*:description:localizedString:value");
        DUBLIN_CORE_QUERYABLE.put("abstract", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact#role=publisher:organisationName");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact#role=publisher:organisationName:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:pointOfContact#role=publisher:organisationName");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:pointOfContact#role=publisher:organisationName:value");
        paths.add("Catalog Web Service:Record:publisher:content");
        DUBLIN_CORE_QUERYABLE.put("publisher", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact#role=author:organisationName");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:pointOfContact#role=author:organisationName:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:pointOfContact#role=author:organisationName");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:pointOfContact#role=author:organisationName:value");
        paths.add("Catalog Web Service:Record:contributor:content");
        DUBLIN_CORE_QUERYABLE.put("contributor", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dateStamp");
        paths.add("ISO 19115-2:MI_Metadata:dateStamp");
        paths.add("Catalog Web Service:Record:date:content");
        DUBLIN_CORE_QUERYABLE.put("date", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:hierarchyLevel");
        paths.add("ISO 19115-2:MI_Metadata:hierarchyLevel");
        paths.add("Catalog Web Service:Record:type:content");
        paths.add("Ebrim v3.0:*:objectType");
        paths.add("Ebrim v2.5:*:objectType");
        DUBLIN_CORE_QUERYABLE.put("type", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name");
        paths.add("ISO 19115:MD_Metadata:distributionInfo:distributionFormat:name:value");
        paths.add("ISO 19115-2:MI_Metadata:distributionInfo:distributionFormat:name");
        paths.add("ISO 19115-2:MI_Metadata:distributionInfo:distributionFormat:name:value");
        paths.add("Catalog Web Service:Record:format:content");
        paths.add("Ebrim v3.0:*:mimeType");
        paths.add("Ebrim v2.5:*:mimeType");
        DUBLIN_CORE_QUERYABLE.put("format", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:fileIdentifier");
        paths.add("ISO 19115-2:MI_Metadata:fileIdentifier");
        
        paths.add("Catalog Web Service:Record:identifier:content");
        
        paths.add("ISO 19110:FC_FeatureCatalogue:id");
        
        paths.add("Ebrim v3.0:*:id");
        paths.add("Web Registry Service v1.0:ExtrinsicObject:id");
        
        paths.add("Ebrim v2.5:*:id");
        paths.add("Web Registry Service v0.9:*:id");
        DUBLIN_CORE_QUERYABLE.put("identifier", paths);
        
        paths = new ArrayList<String>();
        paths.add("Catalog Web Service:Record:source");
        DUBLIN_CORE_QUERYABLE.put("source", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:language");
        paths.add("ISO 19115-2:MI_Metadata:language");
        paths.add("Catalog Web Service:Record:language:content");
        DUBLIN_CORE_QUERYABLE.put("language", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:title");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:title:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:title");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:aggregationInfo:aggregateDataSetName:title:value");
        paths.add("Catalog Web Service:Record:relation:content");
        DUBLIN_CORE_QUERYABLE.put("relation", paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:accessConstraints");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraints:accessConstraints");
        paths.add("Catalog Web Service:Record:rights:content");
        DUBLIN_CORE_QUERYABLE.put("rights", paths);
        
        /*
         * Bounding box
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2:westBoundLongitude");
        paths.add("Catalog Web Service:Record:BoundingBox:LowerCorner[0]");
        DUBLIN_CORE_QUERYABLE.put("WestBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2:eastBoundLongitude");
        paths.add("Catalog Web Service:Record:BoundingBox:UpperCorner[0]");
        DUBLIN_CORE_QUERYABLE.put("EastBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2:northBoundLatitude");
        paths.add("Catalog Web Service:Record:BoundingBox:UpperCorner[1]");
        DUBLIN_CORE_QUERYABLE.put("NorthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:geographicElement2:southBoundLatitude");
        paths.add("Catalog Web Service:Record:BoundingBox:LowerCorner[1]");
        DUBLIN_CORE_QUERYABLE.put("SouthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        paths.add("Catalog Web Service:Record:BoundingBox:crs");
        DUBLIN_CORE_QUERYABLE.put("CRS",     paths);
    }
    
    /**
     * The queryable element from ebrim and their path id.
     * @deprecated
     */
    @Deprecated
    public static final Map<String, List<String>> EBRIM_QUERYABLE;
    static {
        EBRIM_QUERYABLE = new HashMap<String, List<String>>();
        List<String> paths;
        
        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:RegistryObject:name:localizedString:value");
        paths.add("Ebrim v3.0:RegistryPackage:name:localizedString:value");
        EBRIM_QUERYABLE.put("name", paths);
        
        //TODO verify codelist=originator
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("creator", paths);
        
        paths = new ArrayList<String>();
        //TODO @name = “http://purl.org/dc/elements/1.1/subject”
        paths.add("Ebrim v3.0:RegistryObject:slot:valueList:value");
        paths.add("Ebrim v3.0:RegistryPackage:slot:valueList:value");
        EBRIM_QUERYABLE.put("description", paths);
        EBRIM_QUERYABLE.put("subject", paths);
        
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:RegistryObject:description:localizedString:value");
        paths.add("Ebrim v3.0:RegistryPackage:description:localizedString:value");
        EBRIM_QUERYABLE.put("abstract", paths);
        
        //TODO verify codelist=publisher
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("publisher", paths);
        
        //TODO verify codelist=contributor
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("contributor", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("date", paths);
        
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:RegistryObject:objectType");
        paths.add("Ebrim v3.0:RegistryPackage:objectType");
        EBRIM_QUERYABLE.put("type", paths);
        
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:ExtrinsicObject:mimeType");
        EBRIM_QUERYABLE.put("format", paths);
        
        paths = new ArrayList<String>();
        paths.add("Ebrim v3.0:RegistryObject:id");
        paths.add("Ebrim v3.0:RegistryPackage:id");
        EBRIM_QUERYABLE.put("identifier", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("source", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("language", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("relation", paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("rigths", paths);
        
        /*
         * Bounding box
         */
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("WestBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("EastBoundLongitude",     paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("NorthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("SouthBoundLatitude",     paths);
        
        paths = new ArrayList<String>();
        EBRIM_QUERYABLE.put("CRS",     paths);
    }

     /**
     * The queryable element from DublinCore and their path id.
     */
    public static final Map<String, List<String>> INSPIRE_QUERYABLE;
    static {
        INSPIRE_QUERYABLE = new HashMap<String, List<String>>();
        List<String> paths;

        /*
         * The core queryable of DublinCore
         */
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dataQualityInfo:report:result:pass");
        paths.add("ISO 19115-2:MI_Metadata:dataQualityInfo:report:result:pass");
        INSPIRE_QUERYABLE.put("Degree", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:accessConstraints");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraints:accessConstraints");
        INSPIRE_QUERYABLE.put("AccessConstraints", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:otherConstraints");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:otherConstraints:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraints:otherConstraints");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraints:otherConstraints:value");
        INSPIRE_QUERYABLE.put("OtherConstraints", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:classification");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraints:classification");
        INSPIRE_QUERYABLE.put("Classification", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:useLimitation");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:resourceConstraints:useLimitation:value");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraints:useLimitation");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:resourceConstraints:useLimitation:value");
        INSPIRE_QUERYABLE.put("ConditionApplyingToAccessAndUse", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:contact:organisationName");
        paths.add("ISO 19115:MD_Metadata:contact:organisationName:value");
        paths.add("ISO 19115-2:MI_Metadata:contact:organisationName");
        paths.add("ISO 19115-2:MI_Metadata:contact:organisationName:value");
        INSPIRE_QUERYABLE.put("MetadataPointOfContact", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dataQualityInfo:lineage:statement");
        paths.add("ISO 19115:MD_Metadata:dataQualityInfo:lineage:statement:value");
        paths.add("ISO 19115-2:MI_Metadata:dataQualityInfo:lineage:statement");
        paths.add("ISO 19115-2:MI_Metadata:dataQualityInfo:lineage:statement:value");
        INSPIRE_QUERYABLE.put("Lineage", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:title");
        paths.add("ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:title:value");
        paths.add("ISO 19115-2:MI_Metadata:dataQualityInfo:report:result:specification:title");
        paths.add("ISO 19115-2:MI_Metadata:dataQualityInfo:report:result:specification:title:value");
        INSPIRE_QUERYABLE.put("SpecificationTitle", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:date:date");
        paths.add("ISO 19115-2:MI_Metadata:dataQualityInfo:report:result:specification:date:date");
        INSPIRE_QUERYABLE.put("SpecificationDate", paths);

        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:dataQualityInfo:report:result:specification:date:dateType");
        paths.add("ISO 19115-2:MI_Metadata:dataQualityInfo:report:result:specification:date:dateType");
        INSPIRE_QUERYABLE.put("SpecificationDateType", paths);
    }
}
