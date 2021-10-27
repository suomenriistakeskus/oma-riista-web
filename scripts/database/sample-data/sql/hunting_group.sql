INSERT INTO organisation (
  parent_organisation_id,
  organisation_type,
  official_code,
  name_finnish,
  name_swedish,
  hunting_year,
  hunting_area_id,
  game_species_id,
  harvest_permit_id
) SELECT
    club.organisation_id,
    'CLUBGROUP',
    '' || (parent_organisation_id + club.hunting_year),
    gs.name_finnish || ' ' || a.external_id || ' ryhmä',
    gs.name_swedish || ' ' || a.external_id || ' grupp',
    a.hunting_year,
    a.hunting_club_area_id,
    hpsa.game_species_id,
    hpp.harvest_permit_id
  FROM harvest_permit_species_amount hpsa
    JOIN harvest_permit_partners hpp ON (hpp.harvest_permit_id = hpsa.harvest_permit_id)
    JOIN organisation club ON (club.organisation_id = hpp.organisation_id)
    JOIN hunting_club_area a ON (a.club_id = club.organisation_id)
    JOIN game_species gs ON (gs.game_species_id = hpsa.game_species_id);


INSERT INTO occupation (
  occupation_type,
  call_order,
  organisation_id,
  person_id,
  name_visibility,
  phone_number_visibility,
  email_visibility
) SELECT
    'RYHMAN_METSASTYKSENJOHTAJA',
    1,
    clubgroup.organisation_id,
    club_occ.person_id,
    true,
    true,
    true
  FROM
    organisation clubgroup
    JOIN occupation club_occ ON (club_occ.organisation_id = clubgroup.parent_organisation_id)
  WHERE clubgroup.organisation_type = 'CLUBGROUP'
        AND club_occ.occupation_type = 'SEURAN_YHDYSHENKILO';
