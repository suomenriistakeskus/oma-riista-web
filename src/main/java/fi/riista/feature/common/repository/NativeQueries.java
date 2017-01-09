package fi.riista.feature.common.repository;

public final class NativeQueries {
    public static final String UPDATE_UNCLAIMED_CLUBS_FROM_LH_ORGS = "UPDATE organisation SET" +
            "  latitude = lh.latitude," +
            "  longitude = lh.longitude," +
            "  geolocation_source = (CASE WHEN lh.latitude IS NOT NULL THEN 'MANUAL' ELSE NULL END)," +
            "  accuracy = (CASE WHEN lh.latitude IS NOT NULL THEN 0 ELSE NULL END)," +
            "  altitude = NULL," +
            "  altitude_accuracy = NULL," +
            "  name_finnish = COALESCE(lh.name_finnish, organisation.name_finnish)," +
            "  name_swedish = COALESCE(lh.name_swedish, organisation.name_swedish)," +
            "  moose_area_id = hta.gid," +
            "  hunting_area_size = lh.area_size," +
            " parent_organisation_id = COALESCE(rhy.organisation_id, organisation.parent_organisation_id)" +
            " FROM lh_org lh" +
            " LEFT JOIN organisation rhy ON (rhy.organisation_type = 'RHY' AND rhy.official_code = lh.rhy_official_code)" +
            " LEFT JOIN hta hta ON (hta.numero=lh.moose_area_code)" +
            " WHERE organisation.organisation_type = 'CLUB'" +
            " AND organisation.official_code = lh.official_code" +
            " AND organisation.organisation_id NOT IN (" +
            "   SELECT organisation_id" +
            "   FROM occupation" +
            "   WHERE occupation_type = 'SEURAN_YHDYSHENKILO'" +
            "   AND occupation.deletion_time IS NULL" +
            "   AND CURRENT_DATE BETWEEN COALESCE(begin_date, CURRENT_DATE) AND COALESCE(end_date, CURRENT_DATE)" +
            ")";

    public static final String COUNT_PERMITS_REQUIRING_ACTION = "SELECT count(hp.*) FROM harvest_permit hp WHERE \n" +
            "-- permit is not mooselike permit\n" +
            "hp.permit_type_code NOT IN ('100','190')\n" +
            "-- is contact person\n" +
            "AND (hp.original_contact_person_id = :personId OR  exists(SELECT 1 FROM harvest_permit_contact_person hpr WHERE hpr.contact_person_id = :personId AND hpr.harvest_permit_id = hp.harvest_permit_id))\n" +
            "-- hunting started\n" +
            "AND EXISTS(SELECT 1 FROM harvest_permit_species_amount hpsa\n" +
            "  WHERE (hpsa.begin_date <= NOW() OR hpsa.begin_date2 <= NOW())\n" +
            "  AND hpsa.harvest_permit_id = hp.harvest_permit_id)\n" +
            "-- report not done or quota not used\n" +
            "AND (\n" +
            "  hp.harvests_as_list = true AND hp.end_of_hunting_report_id IS NULL\n" +
            "  OR (\n" +
            "    hp.harvests_as_list = false AND hp.end_of_hunting_report_id IS NULL\n" +
            "    AND (\n" +
            "      -- has reports and quota not met\n" +
            "      exists(\n" +
            "        SELECT 1\n" +
            "        FROM harvest h, harvest_permit_species_amount hpsa\n" +
            "        WHERE hpsa.harvest_permit_id = hp.harvest_permit_id\n" +
            "              AND hpsa.game_species_id = h.game_species_id\n" +
            "              AND h.harvest_report_id IN (SELECT harvest_report_id\n" +
            "                                          FROM harvest_report hr\n" +
            "                                          WHERE hr.state NOT IN ('DELETED', 'REJECTED') AND\n" +
            "                                                hr.harvest_permit_id = hp.harvest_permit_id)\n" +
            "        GROUP BY hpsa.harvest_permit_species_amount_id, hpsa.amount\n" +
            "        HAVING sum(h.amount) < hpsa.amount\n" +
            "      )\n" +
            "--       no reports \n" +
            "      OR NOT EXISTS (SELECT 1 FROM harvest_report hr\n" +
            "                      WHERE hr.state NOT IN ('DELETED', 'REJECTED') AND\n" +
            "                      hr.harvest_permit_id = hp.harvest_permit_id)\n" +
            "    )\n" +
            "  )\n" +
            ")";

    private NativeQueries() {
        throw new AssertionError();
    }
}
