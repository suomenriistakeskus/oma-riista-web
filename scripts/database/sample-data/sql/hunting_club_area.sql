CREATE TABLE import_hunting_club_area (
  external_id   CHAR(10)     NOT NULL,
  official_code VARCHAR(255) NOT NULL,
  hunting_year  INTEGER      NOT NULL,
  name_finnish  VARCHAR(255) NOT NULL,
  longitude     INTEGER,
  latitude      INTEGER,
  PRIMARY KEY (external_id)
);

\COPY import_hunting_club_area FROM './csv/hunting_club_area.csv' WITH DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO hunting_club_area (
  club_id, external_id, hunting_year, metsahallitus_year, name_finnish, name_swedish, is_active
) SELECT
    club.organisation_id,
    a.external_id,
    a.hunting_year,
    a.hunting_year,
    a.name_finnish,
    a.name_finnish,
    TRUE
  FROM import_hunting_club_area a
    JOIN organisation club ON (club.official_code = a.official_code AND club.organisation_type = 'CLUB');

WITH zones AS (
  INSERT INTO zone (consistency_version)
    SELECT hunting_club_area_id
    FROM hunting_club_area
  RETURNING zone_id, consistency_version
) UPDATE hunting_club_area
SET zone_id = zones.zone_id
FROM zones
WHERE zones.consistency_version = hunting_club_area.hunting_club_area_id;

INSERT INTO zone_palsta (zone_id, palsta_id, geom, palsta_tunnus)
  SELECT
    a.zone_id,
    pa.id,
    pa.geom,
    pa.tunnus
  FROM import_hunting_club_area i
    JOIN hunting_club_area a ON (a.external_id = i.external_id)
    JOIN palstaalue pa ON ST_DWithin(pa.geom, ST_SetSrid(ST_MakePoint(i.longitude, i.latitude), 3067), 1000 * 5) AND pa.id % 6 <> 0;

WITH combined AS (
    SELECT
      hca.zone_id,
      ST_UnaryUnion(ST_CollectionHomogenize(ST_Collect(zp.geom))) AS geom
    FROM hunting_club_area hca
    JOIN zone z ON (z.zone_id = hca.zone_id)
    JOIN zone_palsta zp ON (zp.zone_id = z.zone_id)
    GROUP BY hca.zone_id
) UPDATE zone
SET geom = combined.geom, computed_area_size = COALESCE(ST_Area(combined.geom), 0)
FROM combined WHERE combined.zone_id = zone.zone_id;
