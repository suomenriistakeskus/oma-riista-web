INSERT INTO harvest_permit_species_amount (
  harvest_permit_id,
  game_species_id,
  amount,
  begin_date,
  end_date,
  begin_date2,
  end_date2
) SELECT
    harvest_permit_id,
    gs.game_species_id,
    import_harvest_permit.permit_count,
    make_date(SUBSTRING(import_harvest_permit.permit_number, 0, 5) :: INT, 8, 1),
    make_date(SUBSTRING(import_harvest_permit.permit_number, 0, 5) :: INT, 12, 31),
    make_date(SUBSTRING(import_harvest_permit.permit_number, 0, 5) :: INT + 1, 1, 1),
    make_date(SUBSTRING(import_harvest_permit.permit_number, 0, 5) :: INT + 1, 7, 31)
  FROM harvest_permit
    JOIN import_harvest_permit ON (import_harvest_permit.permit_number = harvest_permit.permit_number)
    JOIN game_species gs ON (import_harvest_permit.game_species_official_code = gs.official_code);
