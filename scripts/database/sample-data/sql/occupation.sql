CREATE TABLE import_occupation (
  ssn                        CHAR(11)     NOT NULL,
  organisation_type          VARCHAR(255) NOT NULL,
  organisation_official_code VARCHAR(255) NOT NULL,
  call_order                 INTEGER,
  occupation_type            VARCHAR(255) NOT NULL,
  PRIMARY KEY (ssn, organisation_type, organisation_official_code, occupation_type)
);

\COPY import_occupation FROM './csv/occupation.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO occupation (
  organisation_id,
  person_id,
  call_order,
  occupation_type
) SELECT
    o.organisation_id,
    p.person_id,
    a.call_order,
    a.occupation_type
  FROM import_occupation a
    JOIN organisation o
      ON (o.organisation_type = a.organisation_type AND o.official_code = a.organisation_official_code)
    JOIN person p ON (p.ssn = a.ssn);
