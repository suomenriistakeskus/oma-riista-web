CREATE TABLE import_municipality_rhy (
  official_code CHAR(3),
  rhy_official_code CHAR(3),
  PRIMARY KEY (official_code, rhy_official_code)
);

\COPY import_municipality_rhy FROM './csv/municipality_rhy.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO municipality_rhy (
  official_code,
  rhy_official_code
) SELECT
    official_code,
    rhy_official_code
  FROM import_municipality_rhy;
