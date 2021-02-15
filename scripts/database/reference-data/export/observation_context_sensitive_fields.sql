SELECT
  g.official_code,
  o.metadata_version,
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
  o.mooselike_unknown_specimen_amount,
  o.observation_category,
  o.deer_hunting_type,
  o.deer_hunting_type_description
FROM observation_context_sensitive_fields o
  JOIN game_species g ON (g.game_species_id = o.game_species_id)
ORDER BY o.metadata_version, g.official_code;