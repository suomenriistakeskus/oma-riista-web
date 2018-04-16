INSERT INTO harvest_permit_species_amount (
  harvest_permit_id,
  game_species_id,
  amount,
  begin_date,
  end_date,
  begin_date2,
  end_date2,
  creditor_reference
) SELECT
    harvest_permit_id,
    gs.game_species_id,
    10.0,
    make_date(EXTRACT(YEAR FROM CURRENT_DATE) :: INT, 1, 1),
    make_date(EXTRACT(YEAR FROM CURRENT_DATE) :: INT, 7, 31),
    make_date(EXTRACT(YEAR FROM CURRENT_DATE) :: INT, 8, 1),
    make_date(EXTRACT(YEAR FROM CURRENT_DATE) :: INT, 12, 31),
    '1234567897'
  FROM harvest_permit
    JOIN game_species gs ON (gs.official_code = '47503')
  WHERE permit_type_code = '100';
