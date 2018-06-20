CREATE TABLE import_observation_base_fields (
  official_code        INTEGER      NOT NULL,
  metadata_version     INTEGER      NOT NULL,
  within_moose_hunting VARCHAR(255) NOT NULL,
  PRIMARY KEY (metadata_version, official_code)
);

\COPY import_observation_base_fields FROM './csv/observation_base_fields.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO observation_base_fields (metadata_version, game_species_id, within_moose_hunting)
  SELECT
    o.metadata_version,
    g.game_species_id,
    o.within_moose_hunting
  FROM import_observation_base_fields o
    JOIN game_species g ON (g.official_code = o.official_code);
