-- Remove data created by Liquibase
DELETE FROM harvest_area;

CREATE TABLE import_harvest_area (
  "type"        VARCHAR(255) NOT NULL,
  official_code CHAR(3)      NOT NULL,
  name_finnish  VARCHAR(255) NOT NULL,
  name_swedish  VARCHAR(255) NOT NULL,
  PRIMARY KEY ("type", official_code)
);

CREATE TABLE import_harvest_area_rhys (
  harvest_area_type          VARCHAR(255) NOT NULL,
  harvest_area_official_code VARCHAR(255) NOT NULL,
  rhy_official_code          CHAR(3)      NOT NULL,
  PRIMARY KEY (harvest_area_type, harvest_area_official_code, rhy_official_code)
);

\COPY import_harvest_area FROM './csv/harvest_area.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';
\COPY import_harvest_area_rhys FROM './csv/harvest_area_rhys.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO harvest_area (
  "type",
  official_code,
  name_finnish,
  name_swedish
) SELECT
    "type",
    official_code,
    name_finnish,
    name_swedish
  FROM import_harvest_area;

INSERT INTO harvest_area_rhys (harvest_area_id, organisation_id)
  SELECT
    ha.harvest_area_id,
    rhy.organisation_id
  FROM harvest_area ha
    JOIN import_harvest_area a ON (ha."type" = a."type" AND ha.official_code = a.official_code)
    JOIN import_harvest_area_rhys b
      ON (a."type" = b.harvest_area_type AND a.official_code = b.harvest_area_official_code)
    JOIN organisation rhy ON (rhy.organisation_type = 'RHY' AND rhy.official_code = b.rhy_official_code);
