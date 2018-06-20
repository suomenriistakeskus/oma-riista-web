CREATE TABLE import_observation_context_sensitive_fields (
  official_code                     INTEGER      NOT NULL,
  metadata_version                  INTEGER      NOT NULL,
  within_moose_hunting              BOOLEAN      NOT NULL,
  observation_type                  VARCHAR(255) NOT NULL,
  amount                            VARCHAR(255) NOT NULL,
  age                               VARCHAR(255) NOT NULL,
  extended_age_range                BOOLEAN      NOT NULL,
  gender                            VARCHAR(255) NOT NULL,
  wounded                           VARCHAR(255) NOT NULL,
  dead                              VARCHAR(255) NOT NULL,
  on_carcass                        VARCHAR(255) NOT NULL,
  collar_or_radio                   VARCHAR(255) NOT NULL,
  legring_or_wingmark               VARCHAR(255) NOT NULL,
  earmark                           VARCHAR(255) NOT NULL,
  mooselike_male_amount             VARCHAR(255) NOT NULL,
  mooselike_female_amount           VARCHAR(255) NOT NULL,
  mooselike_calf_amount             VARCHAR(255) NOT NULL,
  mooselike_female_1_calf_amount    VARCHAR(255) NOT NULL,
  mooselike_female_2_calfs_amount   VARCHAR(255) NOT NULL,
  mooselike_female_3_calfs_amount   VARCHAR(255) NOT NULL,
  mooselike_female_4_calfs_amount   VARCHAR(255) NOT NULL,
  mooselike_unknown_specimen_amount VARCHAR(255) NOT NULL,
  PRIMARY KEY (metadata_version, official_code, within_moose_hunting, observation_type)
);

\COPY import_observation_context_sensitive_fields FROM './csv/observation_context_sensitive_fields.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO observation_context_sensitive_fields (
  metadata_version,
  game_species_id,
  within_moose_hunting,
  observation_type,
  amount,
  age,
  extended_age_range,
  gender,
  wounded,
  dead,
  on_carcass,
  collar_or_radio,
  legring_or_wingmark,
  earmark,
  mooselike_male_amount,
  mooselike_female_amount,
  mooselike_calf_amount,
  mooselike_female_1_calf_amount,
  mooselike_female_2_calfs_amount,
  mooselike_female_3_calfs_amount,
  mooselike_female_4_calfs_amount,
  mooselike_unknown_specimen_amount
) SELECT
    o.metadata_version,
    g.game_species_id,
    o.within_moose_hunting,
    o.observation_type,
    o.amount,
    o.age,
    o.extended_age_range,
    o.gender,
    o.wounded,
    o.dead,
    o.on_carcass,
    o.collar_or_radio,
    o.legring_or_wingmark,
    o.earmark,
    o.mooselike_male_amount,
    o.mooselike_female_amount,
    o.mooselike_calf_amount,
    o.mooselike_female_1_calf_amount,
    o.mooselike_female_2_calfs_amount,
    o.mooselike_female_3_calfs_amount,
    o.mooselike_female_4_calfs_amount,
    o.mooselike_unknown_specimen_amount
  FROM import_observation_context_sensitive_fields o
    JOIN game_species g ON (g.official_code = o.official_code);
