INSERT INTO hunting_club_area (
  club_id, hunting_year, metsahallitus_year, name_finnish, name_swedish, is_active
) SELECT
    club.organisation_id,
    years.year,
    years.year,
    'Alue ' || year,
    'Alue ' || year,
    TRUE
  FROM (SELECT EXTRACT(YEAR FROM CURRENT_DATE) +
               (CASE
                WHEN EXTRACT(MONTH FROM CURRENT_DATE) < 8
                  THEN -1
                ELSE 0
                END) as year) years
    JOIN organisation club ON (club.organisation_type = 'CLUB');
