SELECT
  o.organisation_type,
  o.official_code,
  p.organisation_type AS parent_organisation_type,
  p.official_code     AS parent_official_code,
  o.name_finnish,
  o.name_swedish,
  o.longitude,
  o.latitude,
  o.poronhoitoalue_id,
  o.hallialue_id,
  o.is_at_coast
FROM organisation o
  LEFT JOIN organisation p ON (p.organisation_id = o.parent_organisation_id)
WHERE o.organisation_type IN ('RK', 'RKA', 'VRN', 'ARN', 'RHY')
ORDER BY CASE
         WHEN o.organisation_type = 'RK'
           THEN 1
         WHEN o.organisation_type = 'RKA'
           THEN 2
         WHEN o.organisation_type = 'VRN'
           THEN 3
         WHEN o.organisation_type = 'ARN'
           THEN 4
         ELSE 5
         END,
  o.official_code;
