CREATE TABLE import_municipality (
  official_code CHAR(3) PRIMARY KEY,
  name_finnish  VARCHAR(255) NOT NULL,
  name_swedish  VARCHAR(255) NOT NULL,
  is_active     BOOLEAN NOT NULL
);

\COPY import_municipality FROM './csv/municipality.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO municipality (
  official_code,
  name_finnish,
  name_swedish,
  is_active
) SELECT
    official_code,
    name_finnish,
    name_swedish,
    is_active
  FROM import_municipality;
