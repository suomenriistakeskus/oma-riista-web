SELECT
  EXTRACT(YEAR FROM hs.begin_date) AS hunting_year,
  g.official_code                  AS game_species_official_code,
  ha."type"                        AS harvest_area_type,
  ha.official_code                 AS harvest_area_official_code,
  hq.quota                         AS harvest_quota_size
FROM harvest_quota hq
  JOIN harvest_area ha ON (ha.harvest_area_id = hq.harvest_area_id)
  JOIN harvest_season hs ON (hs.harvest_season_id = hq.harvest_season_id)
  JOIN harvest_report_fields hr ON (hs.harvest_report_fields_id = hr.harvest_report_fields_id)
  JOIN game_species g ON (hr.game_species_id = g.game_species_id)
ORDER BY 1, 2, 3, 4;
