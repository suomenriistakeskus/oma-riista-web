SELECT
  g.official_code,
  o.metadata_version,
  o.within_moose_hunting,
  o.within_deer_hunting
FROM observation_base_fields o
  JOIN game_species g ON (g.game_species_id = o.game_species_id)
ORDER BY o.metadata_version, g.official_code;
