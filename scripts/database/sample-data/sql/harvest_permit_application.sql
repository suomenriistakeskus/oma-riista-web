CREATE TABLE import_harvest_permit_application (
  permit_number    CHAR(18) PRIMARY KEY,
  area_external_id VARCHAR(255) NOT NULL,
  application_number INT NOT NULL,
  contact_person_hunter_number CHAR(8) NOT NULL
);

\COPY import_harvest_permit_application FROM './csv/harvest_permit_application.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO harvest_permit_application (
  application_number, permit_number,
  contact_person_id, permit_holder_id, rhy_id, area_id,
  status, permit_type_code, hunting_year,
  submit_date
) SELECT
    application_number,
    permit_number,
    person.person_id,
    club.organisation_id,
    club.parent_organisation_id,
    harvest_permit_area_id,
    'ACTIVE',
    '100',
    2018,
    NOW()
  FROM import_harvest_permit_application
    LEFT JOIN harvest_permit_area
      ON (harvest_permit_area.external_id = import_harvest_permit_application.area_external_id)
    LEFT JOIN organisation club ON (harvest_permit_area.club_id = club.organisation_id)
    LEFT JOIN person ON (person.hunter_number = import_harvest_permit_application.contact_person_hunter_number);

INSERT INTO harvest_permit_application_partner (
  harvest_permit_application_id, organisation_id
) SELECT
    harvest_permit_application_id,
    harvest_permit_area.club_id
  FROM import_harvest_permit_application
    LEFT JOIN harvest_permit_application
      ON (harvest_permit_application.permit_number = import_harvest_permit_application.permit_number)
    LEFT JOIN harvest_permit_area
      ON (harvest_permit_area.external_id = import_harvest_permit_application.area_external_id);

INSERT INTO harvest_permit_application_rhy (harvest_permit_application_id, organisation_id)
  SELECT
    DISTINCT
    harvest_permit_application_id,
    organisation.organisation_id
  FROM harvest_permit_application
    JOIN harvest_permit_area ON harvest_permit_application.area_id = harvest_permit_area.harvest_permit_area_id
    JOIN zone z ON z.zone_id = harvest_permit_area.zone_id
    JOIN rhy ON ST_Intersects(rhy.geom, ST_SetSRID(z.geom, 3047))
    JOIN organisation ON organisation.official_code = rhy.id
  WHERE ST_AREA(ST_Intersection(rhy.geom, ST_SetSRID(z.geom, 3047))) > 10000;

-- INSERT INTO harvest_permit_application_species (game_species_id, harvest_permit_application_id)
--   SELECT
--     (SELECT game_species_id
--      FROM game_species
--      WHERE official_code = '47503'),
--     harvest_permit_application_id
--   FROM harvest_permit_application;
