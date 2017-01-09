DROP TABLE IF EXISTS import_harvest_quota;

CREATE TABLE import_harvest_quota (
  hunting_year               INTEGER      NOT NULL,
  game_species_official_code INTEGER      NOT NULL,
  harvest_area_type          VARCHAR(255) NOT NULL,
  harvest_area_code          VARCHAR(255) NOT NULL,
  quota                      INTEGER      NOT NULL,
  PRIMARY KEY (hunting_year, game_species_official_code, harvest_area_type, harvest_area_code)
);

\COPY import_harvest_quota FROM './csv/harvest_quota.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO harvest_quota (quota, hunting_suspended, harvest_season_id, harvest_area_id)
  SELECT
    quota,
    FALSE,
    hs.harvest_season_id,
    ha.harvest_area_id
  FROM import_harvest_quota hq
    JOIN harvest_area ha
      ON (ha."type" = hq.harvest_area_type AND ha.official_code = hq.harvest_area_code)
    JOIN game_species g
      ON (hq.game_species_official_code = g.official_code)
    JOIN harvest_season hs
      ON (hq.hunting_year = EXTRACT(YEAR FROM hs.begin_date))
    JOIN harvest_report_fields hr
      ON (hr.harvest_report_fields_id = hs.harvest_report_fields_id)
  WHERE hr.game_species_id = g.game_species_id;
