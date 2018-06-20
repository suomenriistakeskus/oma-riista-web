CREATE TABLE import_harvest_permit_area (
  club_official_code VARCHAR(255) NOT NULL,
  external_id        CHAR(10)     NOT NULL,
  hunting_year       INT          NOT NULL,
  name_finnish       VARCHAR(255) NOT NULL
);

CREATE TABLE import_harvest_permit_area_partner (
  external_id           CHAR(10) NOT NULL,
  club_area_external_id CHAR(10) NOT NULL,
  PRIMARY KEY (external_id, club_area_external_id)
);

\COPY import_harvest_permit_area FROM './csv/harvest_permit_area.csv' WITH DELIMITER ';' NULL '' ENCODING 'UTF-8';
\COPY import_harvest_permit_area_partner FROM './csv/harvest_permit_area_partner.csv' WITH DELIMITER ';' NULL '' ENCODING 'UTF-8';

ALTER TABLE import_harvest_permit_area
  ADD COLUMN id BIGSERIAL PRIMARY KEY;

-- Use (consistency_version+100) as temporary foreign key to hunting_club_area
WITH zones AS (
  INSERT INTO zone (consistency_version)
    SELECT 100 + id
    FROM import_harvest_permit_area
  RETURNING zone_id, consistency_version
)
INSERT INTO harvest_permit_area
(zone_id, club_id, external_id, hunting_year, name_finnish, name_swedish, status, status_time)
  SELECT
    zones.zone_id,
    club.organisation_id,
    import_harvest_permit_area.external_id,
    import_harvest_permit_area.hunting_year,
    import_harvest_permit_area.name_finnish,
    import_harvest_permit_area.name_finnish,
    'READY',
    NOW()
  FROM import_harvest_permit_area
    JOIN organisation club
      ON (club.organisation_type = 'CLUB' AND club.official_code = import_harvest_permit_area.club_official_code)
    JOIN zones ON (import_harvest_permit_area.id + 100 = zones.consistency_version);

INSERT INTO harvest_permit_area_partner
(harvest_permit_area_id, source_area_id, zone_id)
  SELECT
    harvest_permit_area_id,
    hunting_club_area_id,
    hunting_club_area.zone_id
  FROM import_harvest_permit_area_partner
    JOIN harvest_permit_area ON (harvest_permit_area.external_id = import_harvest_permit_area_partner.external_id)
    JOIN hunting_club_area ON (
      hunting_club_area.external_id = import_harvest_permit_area_partner.club_area_external_id);

-- Make copy of hunting_club_area zones
WITH zones AS (
  INSERT INTO zone (consistency_version)
    SELECT DISTINCT 200 + harvest_permit_area_partner_id
    FROM harvest_permit_area_partner
  RETURNING zone_id, consistency_version
)
INSERT INTO zone_palsta (zone_id, palsta_id, geom, palsta_tunnus)
  SELECT
    z2.zone_id,
    zp.palsta_id,
    zp.geom,
    zp.palsta_tunnus
  FROM harvest_permit_area_partner hpap
    JOIN zones z2 ON (z2.consistency_version = hpap.harvest_permit_area_partner_id + 200)
    JOIN zone_palsta zp ON (zp.zone_id = hpap.zone_id);

UPDATE harvest_permit_area_partner
SET zone_id = z2.zone_id
FROM zone z2
WHERE z2.consistency_version = harvest_permit_area_partner_id + 200;

-- Calculate partner union
WITH combined AS (
    SELECT
      hpap.zone_id,
      ST_UnaryUnion(ST_CollectionHomogenize(ST_Collect(zp.geom))) AS geom
    FROM harvest_permit_area ha
      JOIN harvest_permit_area_partner hpap
        ON (hpap.harvest_permit_area_id = ha.harvest_permit_area_id)
      JOIN zone z ON (z.zone_id = hpap.zone_id)
      JOIN zone_palsta zp ON (zp.zone_id = z.zone_id)
    GROUP BY hpap.zone_id
) UPDATE zone
SET geom = combined.geom, computed_area_size = COALESCE(ST_Area(combined.geom), 0)
FROM combined WHERE combined.zone_id = zone.zone_id;

-- Calculate permit union
WITH unions AS (
    SELECT
      ha.zone_id,
      ST_UnaryUnion(ST_Collect(z2.geom)) geom
    FROM harvest_permit_area ha
      JOIN harvest_permit_area_partner hpap
        ON (hpap.harvest_permit_area_id = ha.harvest_permit_area_id)
      JOIN zone z2 ON (z2.zone_id = hpap.zone_id)
    GROUP BY ha.zone_id
)
UPDATE zone
SET geom = unions.geom, computed_area_size = COALESCE(ST_Area(unions.geom), 0)
FROM unions
WHERE unions.zone_id = zone.zone_id;
