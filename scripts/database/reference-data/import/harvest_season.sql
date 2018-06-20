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
  game_species_id
) SELECT
    hs.begin_date,
    hs.end_date,
    hs.end_of_reporting_date,
    hs.begin_date2,
    hs.end_date2,
    hs.end_of_reporting_date2,
    hs.name_finnish,
    hs.name_swedish,
    g.game_species_id
  FROM import_harvest_season hs
    JOIN game_species g ON (hs.game_species_official_code = g.official_code);
