CREATE TABLE import_harvest_permit_partner (
  permit_number      CHAR(18) PRIMARY KEY,
  club_official_code VARCHAR(255) NOT NULL
);

\COPY import_harvest_permit_partner FROM './csv/harvest_permit_partner.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO harvest_permit_partners (
  harvest_permit_id, organisation_id
) SELECT
    harvest_permit.harvest_permit_id,
    club.organisation_id
  FROM import_harvest_permit_partner i
    JOIN harvest_permit ON (harvest_permit.permit_number = i.permit_number)
    JOIN organisation club ON (club.official_code = i.club_official_code AND club.organisation_type = 'CLUB');
