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
    import_harvest_permit.permit_count,
    make_date(EXTRACT(YEAR FROM CURRENT_DATE) :: INT - 1, 8, 1),
    make_date(EXTRACT(YEAR FROM CURRENT_DATE) :: INT - 1, 12, 31),
    make_date(EXTRACT(YEAR FROM CURRENT_DATE) :: INT, 1, 1),
    make_date(EXTRACT(YEAR FROM CURRENT_DATE) :: INT , 7, 31),
    '1234567897'
  FROM harvest_permit
    JOIN import_harvest_permit ON (import_harvest_permit.permit_number = harvest_permit.permit_number)
    JOIN game_species gs ON (import_harvest_permit.game_species_official_code = gs.official_code);
