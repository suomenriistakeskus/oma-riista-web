SELECT
  m.official_code,
  m.name_finnish,
  m.name_swedish
  m.is_active
FROM municipality m
ORDER BY m.official_code;
