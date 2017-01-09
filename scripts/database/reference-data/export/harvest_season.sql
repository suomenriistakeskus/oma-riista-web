SELECT
  EXTRACT(YEAR FROM hs.begin_date) AS hunting_year,
  g.official_code,
  hs.name_finnish,
  hs.name_swedish,
  hs.begin_date,
  hs.end_date,
  hs.end_of_reporting_date,
  hs.begin_date2,
  hs.end_date2,
  hs.end_of_reporting_date2
FROM harvest_season hs
  JOIN harvest_report_fields hr ON (hr.harvest_report_fields_id = hs.harvest_report_fields_id)
  JOIN game_species g ON (hr.game_species_id = g.game_species_id)
ORDER BY 1, 2;
