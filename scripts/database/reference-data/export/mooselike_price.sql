SELECT
  m.hunting_year,
  gs.official_code,
  m.adult_price,
  m.young_price,
  m.iban,
  m.bic,
  m.recipient_name
FROM mooselike_price m
  JOIN game_species gs ON (m.game_species_id = gs.game_species_id)
ORDER BY m.hunting_year, gs.official_code;
