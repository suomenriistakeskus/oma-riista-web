CREATE TABLE import_game_species (
  official_code                        INTEGER PRIMARY KEY,
  name_finnish                         VARCHAR(255) NOT NULL,
  name_swedish                         VARCHAR(255) NOT NULL,
  name_english                         VARCHAR(255) NOT NULL,
  scientific_name                      VARCHAR(255) NOT NULL,
  game_category                        VARCHAR(255) NOT NULL,
  multiple_specimen_allowed_on_harvest BOOLEAN      NOT NULL,
  srva_ordinal                         INTEGER
);

\COPY import_game_species FROM './csv/game_species.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO game_species (
  official_code,
  name_finnish,
  name_swedish,
  name_english,
  scientific_name,
  "category",
  multiple_specimen_allowed_on_harvest,
  srva_ordinal
) SELECT
    official_code,
    name_finnish,
    name_swedish,
    name_english,
    scientific_name,
    game_category,
    multiple_specimen_allowed_on_harvest,
    srva_ordinal
  FROM import_game_species;
