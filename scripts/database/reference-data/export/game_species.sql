SELECT
  g.official_code,
  g.name_finnish,
  g.name_swedish,
  g.name_english,
  g.scientific_name,
  g.category,
  g.multiple_specimen_allowed_on_harvest,
  g.srva_ordinal
FROM game_species g
ORDER BY g.official_code;
