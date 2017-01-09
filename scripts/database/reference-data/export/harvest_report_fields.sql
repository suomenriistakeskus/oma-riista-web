SELECT
  g.official_code,
  h.name,
  h.used_with_permit,
  h.hunting_area_type,
  h.hunting_party,
  h.hunting_area_size,
  h.permit_number,
  h.hunting_method,
  h.weight,
  h.reported_with_phone_call,
  h.free_hunting_also,
  h.age,
  h.gender,
  h.additional_info,
  h.weight_estimated,
  h.weight_measured,
  h.fitness_class,
  h.antlers_type,
  h.antlers_width,
  h.antler_points_left,
  h.antler_points_right,
  h.harvests_as_list,
  h.not_edible
FROM harvest_report_fields h
  JOIN game_species g ON (h.game_species_id = g.game_species_id)
ORDER BY g.official_code;
