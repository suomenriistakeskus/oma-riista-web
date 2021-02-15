UPDATE import_ph
SET geom = ST_Multi(geom);

UPDATE import_ph
SET geom = ST_Multi(ST_Buffer(geom, 0))
WHERE NOT ST_IsValid(geom);

-- Add ph area geometries
UPDATE harvest_area
SET geom = (SELECT geom FROM import_ph WHERE aluenimi LIKE 'Län%')
WHERE name_finnish LIKE 'Län%'
AND type = 'PORONHOITOALUE';

UPDATE harvest_area
SET geom = (SELECT geom FROM import_ph WHERE aluenimi LIKE 'Itä%')
WHERE name_finnish LIKE 'Itäi%'
AND type = 'PORONHOITOALUE';

-- Map hallialue geometries by rhy boundaries
UPDATE harvest_area ha SET geom = (
        SELECT ST_Multi(ST_UnaryUnion(ST_CollectionHomogenize(st_Collect(geom))))
    FROM rhy
    WHERE id IN ('215','506','507','253','258','503','254','264','268','318','324','505','305','504','259','502','276','274','256','218','501','204','508')
    ) WHERE ha.official_code = '1' and ha.type = 'HALLIALUE';

UPDATE harvest_area ha SET geom = (
        SELECT ST_Multi(ST_UnaryUnion(ST_CollectionHomogenize(st_Collect(geom))))
    FROM rhy
    WHERE id IN ('560','561','563','659','661','662','665','667','668','652','655','664','653','658','571','666','566','660')
    ) WHERE ha.official_code = '2' and ha.type = 'HALLIALUE';

UPDATE harvest_area ha SET geom = (
        SELECT ST_Multi(ST_UnaryUnion(ST_CollectionHomogenize(st_Collect(geom))))
    FROM rhy
    WHERE id IN ('602','614','617','620','625','627','629','168','161','169','157','632')
    ) WHERE ha.official_code = '3' and ha.type = 'HALLIALUE';

-- rhy are stored in SRID 3047, update to 3067
UPDATE harvest_area SET geom = st_setsrid(geom, '3067') WHERE type = 'HALLIALUE';
