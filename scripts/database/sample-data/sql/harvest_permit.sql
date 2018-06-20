CREATE TABLE import_harvest_permit (
  permit_number                    CHAR(18) PRIMARY KEY,
  permit_type_code                 CHAR(3)      NOT NULL,
  permit_type                      VARCHAR(255) NOT NULL,
  rhy_official_code                CHAR(3)      NOT NULL,
  permit_holder_club_official_code VARCHAR(255),
  contact_person_ssn               CHAR(11)     NOT NULL,
  game_species_official_code       INT          NOT NULL,
  permit_count                     INT          NOT NULL
);

\COPY import_harvest_permit FROM './csv/harvest_permit.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO harvest_permit (
  permit_number,
  permit_type_code,
  permit_type,
  permit_area_size,
  rhy_id,
  permit_holder_id,
  original_contact_person_id
) SELECT
    permit_number,
    permit_type_code,
    permit_type,
    123456 AS permit_area_size,
    rhy.organisation_id,
    club.organisation_id,
    person.person_id
  FROM import_harvest_permit
    JOIN organisation rhy ON (rhy.official_code = rhy_official_code AND rhy.organisation_type = 'RHY')
    JOIN person person ON (person.ssn = contact_person_ssn)
    LEFT JOIN organisation club
      ON (club.official_code = permit_holder_club_official_code AND club.organisation_type = 'CLUB');
