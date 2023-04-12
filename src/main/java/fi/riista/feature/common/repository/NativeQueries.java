package fi.riista.feature.common.repository;

public final class NativeQueries {

    public static final String LIST_HARVEST_PERMIT_APPLICATION_CONFLICTS = "SELECT hpa3.*" +
            "   FROM harvest_permit_area ha3" +
            "   JOIN harvest_permit_application hpa3 ON (hpa3.area_id = ha3.harvest_permit_area_id)" +
            "   WHERE ha3.zone_id IN (" +
            "  SELECT DISTINCT bbox.zone_id" +
            "  FROM (" +
            "   SELECT z2.zone_id, z1.geom AS g1, (ST_Dump(z2.geom)).geom AS g2" +
            "   FROM harvest_permit_application hpa1" +
            "   JOIN harvest_permit_area ha1 ON (hpa1.area_id = ha1.harvest_permit_area_id)" +
            "   JOIN zone z1 ON (z1.zone_id = ha1.zone_id)" +
            "   JOIN zone z2 ON (z2.geom && z1.geom)" +
            "   WHERE hpa1.harvest_permit_application_id = :harvestPermitApplicationId" +
            "   AND z1.geom && z2.geom" +
            "   AND z1.zone_id <> z2.zone_id" +
            "   AND z2.zone_id IN (" +
            "       SELECT ha2.zone_id " +
            "       FROM harvest_permit_application hpa2 " +
            "       JOIN harvest_permit_area ha2 ON (hpa2.area_id = ha2.harvest_permit_area_id)" +
            "       WHERE hpa2.application_year = :applicationYear" +
            "       AND hpa2.harvest_permit_category = 'MOOSELIKE'" +
            "       AND (hpa2.status = 'ACTIVE' OR hpa2.status = 'AMENDING'))" +
            ") bbox WHERE ST_Intersects(bbox.g1, ST_Buffer(bbox.g2, -1)));";

    public static final String FIND_APPLICATIONS_WITH_ALSO_OTHER_THAN_STATE_MOOSE_LANDS_FROM_LIST = "" +
            "WITH" +
            "   zones_with_features AS (" +
            "        SELECT f.zone_id AS zone_id FROM zone_feature AS f" +
            "        GROUP BY f.zone_id" +
            "   )," +
            "   zones_with_palsta AS (" +
            "        SELECT p.zone_id AS zone_id FROM zone_palsta AS p" +
            "        GROUP BY p.zone_id" +
            "   )," +
            "   zones_with_non_state_moose_areas AS (" +
            "        SELECT zmh.zone_id AS zone_id FROM zone_mh_hirvi AS zmh" +
            "        INNER JOIN mh_hirvi AS mh" +
            "        ON zmh.mh_hirvi_id = mh.gid" +
            "        WHERE mh.koodi >= 100000" +
            "        GROUP BY zmh.zone_id" +
            "   )" +
            "SELECT DISTINCT hpa.* " +
            "   FROM harvest_permit_application hpa" +
            "   JOIN harvest_permit_area ha ON (hpa.area_id = ha.harvest_permit_area_id)" +
            "   JOIN harvest_permit_area_partner hpap ON (ha.harvest_permit_area_id = hpap.harvest_permit_area_id)" +
            "   LEFT JOIN zones_with_features zwf1 ON (ha.zone_id = zwf1.zone_id)" +
            "   LEFT JOIN zones_with_features zwf2 ON (hpap.zone_id = zwf2.zone_id)" +
            "   LEFT JOIN zones_with_palsta zwp1 ON (ha.zone_id = zwp1.zone_id)" +
            "   LEFT JOIN zones_with_palsta zwp2 ON (hpap.zone_id = zwp2.zone_id)" +
            "   LEFT JOIN zones_with_non_state_moose_areas zwnsma1 ON (ha.zone_id = zwnsma1.zone_id)" +
            "   LEFT JOIN zones_with_non_state_moose_areas zwnsma2 ON (ha.zone_id = zwnsma2.zone_id)" +
            "   WHERE hpa.harvest_permit_application_id in :applications" +
            "   AND" +
            "   (" +
            "      zwf1.zone_id is not null OR" +
            "      zwp1.zone_id is not null OR" +
            "      zwnsma1.zone_id is not null" +
            "      OR" +
            "      zwf2.zone_id is not null OR" +
            "      zwp2.zone_id is not null OR" +
            "      zwnsma2.zone_id is not null" +
            "   );";

    private NativeQueries() {
        throw new AssertionError();
    }
}
