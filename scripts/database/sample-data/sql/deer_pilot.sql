INSERT INTO deer_pilot (
    harvest_permit_id
) SELECT
    hpsa.harvest_permit_id
FROM harvest_permit_species_amount hpsa
JOIN game_species gs ON gs.game_species_id = hpsa.game_species_id
WHERE gs.official_code = 47629;