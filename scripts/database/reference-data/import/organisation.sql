CREATE TABLE import_organisation (
  organisation_type        VARCHAR(255) NOT NULL,
  official_code            VARCHAR(3)   NOT NULL,
  parent_organisation_type VARCHAR(255),
  parent_official_code     VARCHAR(3),
  name_finnish             VARCHAR(255) NOT NULL,
  name_swedish             VARCHAR(255) NOT NULL,
  longitude                INTEGER,
  latitude                 INTEGER,
  is_at_coast              BOOLEAN,
  active                   BOOLEAN NOT NULL,
  PRIMARY KEY (organisation_type, official_code)
);

\COPY import_organisation FROM './csv/organisation.csv' WITH DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO organisation (organisation_type, official_code, name_finnish, name_swedish, longitude, latitude, is_at_coast, active)
  SELECT
    organisation_type,
    official_code,
    name_finnish,
    name_swedish,
    longitude,
    latitude,
    is_at_coast,
    active
  FROM import_organisation;

UPDATE organisation
SET parent_organisation_id = p.organisation_id
FROM import_organisation e
  JOIN organisation p ON (p.official_code = e.parent_official_code AND p.organisation_type = e.parent_organisation_type)
WHERE organisation.official_code = e.official_code AND organisation.organisation_type = e.organisation_type;

UPDATE organisation
SET email = 'rhy-' || official_code || '@invalid'
WHERE organisation_type = 'RHY';
