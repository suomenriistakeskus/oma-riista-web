SELECT
  ha.type,
  ha.official_code,
  ha.name_finnish,
  ha.name_swedish
FROM harvest_area ha
ORDER BY ha.type, ha.official_code;
