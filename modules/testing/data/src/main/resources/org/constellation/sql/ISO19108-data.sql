
/*-------------------------------------------------*
 *--------------  Classe AbstractGML --------------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractGML',NULL,'ISO 19108',NULL,1,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('description',  NULL, 'ISO 19108', NULL, 0, 1,'AbstractGML','CharacterString', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('name',         NULL, 'ISO 19108', NULL, 0, 1,'AbstractGML','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('id',           NULL, 'ISO 19108', NULL, 0, 1,'AbstractGML','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19108', ' ');


/*-------------------------------------------------*
 *--------------  Classe AbstractGeometry ---------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractGeometry',NULL,'ISO 19108',NULL,1,'AbstractGML','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('gid',          NULL, 'ISO 19108', NULL, 0, 1,'AbstractGeometry','CharacterString', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('srsName',      NULL, 'ISO 19108', NULL, 0, 1,'AbstractGeometry','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('srsDimension', NULL, 'ISO 19108', NULL, 0, 1,'AbstractGeometry','Integer',         NULL, 'O',2 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('axisLabels',   NULL, 'ISO 19108', NULL, 0, 2147483647,'AbstractGeometry','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('uomLabels',    NULL, 'ISO 19108', NULL, 0, 2147483647,'AbstractGeometry','CharacterString', NULL, 'O',4 , 'ISO 19103','ISO 19108', ' ');

/*-------------------------------------------------*
 *--------------  Classe DirectPositionList -------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('DirectPositionList',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('count',        NULL, 'ISO 19108', NULL, 0, 1,'DirectPositionList','Integer', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('srsName',      NULL, 'ISO 19108', NULL, 0, 1,'DirectPositionList','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('srsDimension', NULL, 'ISO 19108', NULL, 0, 1,'DirectPositionList','Integer',         NULL, 'O',2 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('axisLabels',   NULL, 'ISO 19108', NULL, 0, 2147483647,'DirectPositionList','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('uomLabels',    NULL, 'ISO 19108', NULL, 0, 2147483647,'DirectPositionList','CharacterString', NULL, 'O',4 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value',        NULL, 'ISO 19108', NULL, 0, 1,'DirectPositionList','Double', NULL, 'O',5 , 'ISO 19103','ISO 19108', ' ');

/*-------------------------------------------------*
 *--------------  Classe Coordinates ---------*
 *-------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Coordinates',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('value',        NULL, 'ISO 19108', NULL, 0, 1,'Coordinates','CharacterString', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('decimal',      NULL, 'ISO 19108', NULL, 0, 1,'Coordinates','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('cs',           NULL, 'ISO 19108', NULL, 0, 1,'Coordinates','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('ts',           NULL, 'ISO 19108', NULL, 0, 1,'Coordinates','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe AbstractGeometricAggregate ---------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractGeometricAggregate',NULL,'ISO 19108',NULL,1,'AbstractGeometry','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe AbstractGeometricPrimitive ---------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractGeometricPrimitive',NULL,'ISO 19108',NULL,1,'AbstractGeometry','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe AbstractSurface      ---------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractSurface',NULL,'ISO 19108',NULL,1,'AbstractGeometricPrimitive','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe SurfaceArrayProperty ---------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SurfaceArrayProperty',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractSurface', NULL, 'ISO 19108', NULL, 0, 2147483647,'SurfaceArrayProperty','AbstractSurface', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');


/*-----------------------------------------------------------*
 *--------------  Classe SurfaceProperty --------------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SurfaceProperty',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractSurface', NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','AbstractSurface', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('remoteSchema',    NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('type',            NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href',            NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role',            NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','CharacterString', NULL, 'O',4 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('arcrole',         NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','CharacterString', NULL, 'O',5 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('title',           NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','CharacterString', NULL, 'O',6 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('show',            NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','CharacterString', NULL, 'O',7 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('actuate',         NULL, 'ISO 19108', NULL, 0, 1,'SurfaceProperty','CharacterString', NULL, 'O',8 , 'ISO 19103','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe MultiSurface -----------------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('MultiSurface',NULL,'ISO 19108',NULL,0,'AbstractGeometricAggregate','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('surfaceMember',  NULL, 'ISO 19108', NULL, 0, 1,'MultiSurface','SurfaceProperty', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('surfaceMembers', NULL, 'ISO 19108', NULL, 0, 2147483647,'MultiSurface','SurfaceArrayProperty', NULL, 'O',1 , 'ISO 19108','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe AbstractSurfacePatch ---------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractSurfacePatch',NULL,'ISO 19108',NULL,1,NULL,NULL, ' ');

/*-------------------------------------------------*
 *--------------  CodeList SurfaceInterpolation to complete ---*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."CodeLists" VALUES ('SurfaceInterpolation', 'SurfaceInterpolation', 'ISO 19108', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements" VALUES ('none',      NULL, 'ISO 19108', NULL, 0, 1, 'SurfaceInterpolation', 'SurfaceInterpolation', 'SurfaceInterpolation', 'C', 0, 'ISO 19108', 'ISO 19108', ' ', 1);
INSERT INTO "Schemas"."CodeListElements" VALUES ('planar',    NULL, 'ISO 19108', NULL, 0, 1, 'SurfaceInterpolation', 'SurfaceInterpolation', 'SurfaceInterpolation', 'C', 0, 'ISO 19108', 'ISO 19108', ' ', 2);
INSERT INTO "Schemas"."CodeListElements" VALUES ('spherical', NULL, 'ISO 19108', NULL, 0, 1, 'SurfaceInterpolation', 'SurfaceInterpolation', 'SurfaceInterpolation', 'C', 0, 'ISO 19108', 'ISO 19108', ' ', 3);

/*-------------------------------------------------*
 *--------------  CodeList CurveInterpolation to complete ---*
 *-------------------------------------------------*/
INSERT INTO "Schemas"."CodeLists" VALUES ('CurveInterpolation', 'CurveInterpolation', 'ISO 19108', NULL, 0, 'CodeList', NULL, ' ');
INSERT INTO "Schemas"."CodeListElements" VALUES ('linear',      NULL, 'ISO 19108', NULL, 0, 1, 'CurveInterpolation', 'CurveInterpolation', 'CurveInterpolation', 'C', 0, 'ISO 19108', 'ISO 19108', ' ', 1);
INSERT INTO "Schemas"."CodeListElements" VALUES ('geodesic',    NULL, 'ISO 19108', NULL, 0, 1, 'CurveInterpolation', 'CurveInterpolation', 'CurveInterpolation', 'C', 0, 'ISO 19108', 'ISO 19108', ' ', 2);
INSERT INTO "Schemas"."CodeListElements" VALUES ('circularArc3Points', NULL, 'ISO 19108', NULL, 0, 1, 'CurveInterpolation', 'CurveInterpolation', 'CurveInterpolation', 'C', 0, 'ISO 19108', 'ISO 19108', ' ', 3);


/*-----------------------------------------------------------*
 *--------------  Classe AbstractCurveSegment   -------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractCurveSegment',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('numDerivativesAtStart', NULL, 'ISO 19108', NULL, 0, 1,'AbstractCurveSegment','Integer', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('numDerivativesAtEnd',   NULL, 'ISO 19108', NULL, 0, 1,'AbstractCurveSegment','Integer', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('numDerivativeInterior', NULL, 'ISO 19108', NULL, 0, 1,'AbstractCurveSegment','Integer', NULL, 'O',0 , 'ISO 19103','ISO 19108', ' ');

 /*-----------------------------------------------------------*
 *--------------  Classe LineStringSegment   -----------------*
 *------------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('LineStringSegment',NULL,'ISO 19108',NULL,0,'AbstractCurveSegment','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('posList',               NULL, 'ISO 19108', NULL, 0, 1,'LineStringSegment','DirectPositionList', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('coordinates',           NULL, 'ISO 19108', NULL, 0, 1,'LineStringSegment','Coordinates', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('interpolation',         NULL, 'ISO 19108', NULL, 0, 1,'LineStringSegment',NULL, 'CurveInterpolation', 'O',0 , 'ISO 19108','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe CurveSegmentArrayProperty ----------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('CurveSegmentArrayProperty',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractCurveSegment', NULL, 'ISO 19108', NULL, 0, 2147483647,'CurveSegmentArrayProperty', 'AbstractCurveSegment', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');


/*-----------------------------------------------------------*
 *--------------  Classe AbstractCurve ---------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractCurve',NULL,'ISO 19108',NULL,1,'AbstractGeometricPrimitive','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe Curve    ---------------------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Curve',NULL,'ISO 19108',NULL,0,'AbstractCurve','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('segments', NULL, 'ISO 19108', NULL, 0, 1,'Curve','CurveSegmentArrayProperty', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');


/*-----------------------------------------------------------*
 *--------------  Classe CurveProperty --------------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('CurveProperty',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractCurve',   NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','AbstractCurve',   NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('remoteSchema',    NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','CharacterString', NULL, 'O',1 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('type',            NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','CharacterString', NULL, 'O',2 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('href',            NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','CharacterString', NULL, 'O',3 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('role',            NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','CharacterString', NULL, 'O',4 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('arcrole',         NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','CharacterString', NULL, 'O',5 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('title',           NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','CharacterString', NULL, 'O',6 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('show',            NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','CharacterString', NULL, 'O',7 , 'ISO 19103','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('actuate',         NULL, 'ISO 19108', NULL, 0, 1,'CurveProperty','CharacterString', NULL, 'O',8 , 'ISO 19103','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe AbstractRing         ---------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractRing',NULL,'ISO 19108',NULL,1,'AbstractGeometry','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe Ring    ----------------------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Ring',NULL,'ISO 19108',NULL,0,'AbstractRing','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('curveMember', NULL, 'ISO 19108', NULL, 0, 1,'Ring','CurveProperty', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe AbstractRingProperty ---------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('AbstractRingProperty',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractRing', NULL, 'ISO 19108', NULL, 0, 1,'AbstractRingProperty','AbstractRing', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');


/*-----------------------------------------------------------*
 *--------------  Classe PolygonPatch ---------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('PolygonPatch',NULL,'ISO 19108',NULL,0,'AbstractSurfacePatch','ISO 19108', ' ');
INSERT INTO "Schemas"."Properties"  VALUES('interpolation', NULL, 'ISO 19108', NULL, 0, 1,'PolygonPatch',NULL, 'SurfaceInterpolation', 'O',0 , 'ISO 19108','ISO 19108', ' ');
INSERT INTO "Schemas"."Properties"  VALUES('exterior',      NULL, 'ISO 19108', NULL, 0, 1,'PolygonPatch','AbstractRingProperty', NULL, 'O',1 , 'ISO 19108','ISO 19108', ' ');
INSERT INTO "Schemas"."Properties"  VALUES('interior',      NULL, 'ISO 19108', NULL, 0, 2147483647,'PolygonPatch','AbstractRingProperty', NULL, 'O',2 , 'ISO 19108','ISO 19108', ' ');

/*-----------------------------------------------------------*
 *--------------  Classe SurfacePatchArrayProperty ----------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('SurfacePatchArrayProperty',NULL,'ISO 19108',NULL,0,NULL,NULL, ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('abstractSurfacePatch', NULL, 'ISO 19108', NULL, 0, 2147483647,'SurfacePatchArrayProperty','AbstractSurfacePatch', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');


/*-----------------------------------------------------------*
 *--------------  Classe Surface ----------------------------*
 *-----------------------------------------------------------*/
 INSERT INTO "Schemas"."Classes"  VALUES('Surface',NULL,'ISO 19108',NULL,0,'AbstractSurface','ISO 19108', ' ');
 INSERT INTO "Schemas"."Properties"  VALUES('patches', NULL, 'ISO 19108', NULL, 0, 1,'Surface','SurfacePatchArrayProperty', NULL, 'O',0 , 'ISO 19108','ISO 19108', ' ');


INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:id','id', 'ISO 19108', 'MultiSurface', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:srsName','srsName', 'ISO 19108', 'MultiSurface', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember','surfaceMember', 'ISO 19108', 'MultiSurface', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface','abstractSurface', 'ISO 19108', 'SurfaceProperty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches','patches', 'ISO 19108', 'Surface', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch','abstractSurfacePatch', 'ISO 19108', 'SurfacePatchArrayProperty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior','exterior', 'ISO 19108', 'PolygonPatch', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing','abstractRing', 'ISO 19108', 'AbstractRingProperty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember','curveMember', 'ISO 19108', 'Ring', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve','abstractCurve', 'ISO 19108', 'CurveProperty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve:segments','segments', 'ISO 19108', 'Curve', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve:segments:abstractCurveSegment','abstractCurveSegment', 'ISO 19108', 'CurveSegmentArrayProperty', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve:segments', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve:segments:abstractCurveSegment:posList','posList', 'ISO 19108', 'LineStringSegment', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve:segments:abstractCurveSegment', 'ISO 19108');
INSERT INTO "Schemas"."Paths"  VALUES  ('ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve:segments:abstractCurveSegment:posList:value','value', 'ISO 19108', 'DirectPositionList', 'ISO 19115:MD_Metadata:identificationInfo:extent:geographicElement4:polygon:surfaceMember:abstractSurface:patches:abstractSurfacePatch:exterior:abstractRing:curveMember:abstractCurve:segments:abstractCurveSegment:posList', 'ISO 19108');
