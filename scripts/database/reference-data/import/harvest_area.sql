-- Remove data created by Liquibase
DELETE FROM harvest_area;

CREATE TABLE import_harvest_area (
  "type"        VARCHAR(255) NOT NULL,
  official_code CHAR(3)      NOT NULL,
  name_finnish  VARCHAR(255) NOT NULL,
  name_swedish  VARCHAR(255) NOT NULL,
  PRIMARY KEY ("type", official_code)
);

\COPY import_harvest_area FROM './csv/harvest_area.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

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
