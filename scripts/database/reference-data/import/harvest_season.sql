DROP TABLE IF EXISTS import_harvest_season;

CREATE TABLE import_harvest_season (
  hunting_year INTEGER NOT NULL,
  game_species_official_code INTEGER NOT NULL,
  name_finnish VARCHAR(255) NOT NULL,
  name_swedish VARCHAR(255) NOT NULL,
  begin_date DATE NOT NULL,
  end_date DATE NOT NULL,
  end_of_reporting_date DATE NOT NULL,
  begin_date2 DATE,
  end_date2 DATE,
  end_of_reporting_date2 DATE,
  PRIMARY KEY (hunting_year, game_species_official_code)
);

\COPY import_harvest_season FROM './csv/harvest_season.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO harvest_season (
  begin_date,
  end_date,
  end_of_reporting_date,
  begin_date2,
  end_date2,
  end_of_reporting_date2,
  name_finnish,
  name_swedish,
  harvest_report_fields_id
) SELECT
    hs.begin_date,
    hs.end_date,
    hs.end_of_reporting_date,
    hs.begin_date2,
    hs.end_date2,
    hs.end_of_reporting_date2,
    hs.name_finnish,
    hs.name_swedish,
    hr.harvest_report_fields_id
  FROM import_harvest_season hs
    JOIN game_species g ON (hs.game_species_official_code = g.official_code)
    JOIN harvest_report_fields hr ON (hr.game_species_id = g.game_species_id)
  WHERE hr.used_with_permit = FALSE
        AND hr.harvests_as_list = FALSE
        AND (g.official_code <> 47348 OR
             (g.official_code = 47348 AND reported_with_phone_call = 'YES' AND hunting_year <= 2014) OR
             (g.official_code = 47348 AND reported_with_phone_call = 'NO' AND hunting_year >= 2015));
