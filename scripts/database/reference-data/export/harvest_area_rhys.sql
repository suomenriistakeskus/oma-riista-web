SELECT
  ha.type           AS harvest_area_type,
  ha.official_code  AS harvest_area_official_code,
  rhy.official_code AS rhy_official_code
FROM harvest_area_rhys har
  JOIN harvest_area ha ON (ha.harvest_area_id = har.harvest_area_id)
  JOIN organisation rhy ON (rhy.organisation_type = 'RHY' AND rhy.organisation_id = har.organisation_id)
ORDER BY ha.type, ha.official_code, rhy.official_code;
