DROP TABLE IF EXISTS import_municipality;

CREATE TABLE import_municipality (
  official_code CHAR(3) PRIMARY KEY,
  name_finnish  VARCHAR(255) NOT NULL,
  name_swedish  VARCHAR(255) NOT NULL
);

\COPY import_municipality FROM './csv/municipality.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO municipality (
  official_code,
  name_finnish,
  name_swedish
) SELECT
    official_code,
    name_finnish,
    name_swedish
  FROM import_municipality;
