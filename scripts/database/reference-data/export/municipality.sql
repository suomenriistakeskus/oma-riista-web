SELECT
  m.official_code,
  m.name_finnish,
  m.name_swedish
FROM municipality m
ORDER BY m.official_code;
