package fi.riista.feature.common.repository;

public final class NativeQueries {

    public static final String LIST_HARVEST_PERMIT_APPLICATION_CONFLICTS = "SELECT hpa3.*" +
            "   FROM harvest_permit_area ha3" +
            "   JOIN harvest_permit_application hpa3 ON (hpa3.area_id = ha3.harvest_permit_area_id)" +
            "   WHERE ha3.zone_id IN (" +
            "  SELECT DISTINCT bbox.zone_id" +
            "  FROM (" +
            "       SELECT z2.zone_id, z1.geom AS g1, (ST_Dump(ST_Buffer(z2.geom, -0.01))).geom AS g2" +
            "       FROM harvest_permit_application hpa1" +
            "       JOIN harvest_permit_area ha1 ON (hpa1.area_id = ha1.harvest_permit_area_id)" +
            "   JOIN zone z1 ON (z1.zone_id = ha1.zone_id)" +
            "   JOIN zone z2 ON (z2.geom && z1.geom)" +
            "   WHERE hpa1.harvest_permit_application_id = :harvestPermitApplicationId" +
            "   AND z1.geom && z2.geom" +
            "   AND z1.zone_id <> z2.zone_id" +
            "   AND z2.zone_id IN (" +
            "       SELECT zone_id " +
            "       FROM harvest_permit_application hpa2 " +
            "       JOIN harvest_permit_area ha2 ON (hpa2.area_id = ha2.harvest_permit_area_id)" +
            "       WHERE ha2.hunting_year = :huntingYear" +
            "       AND hpa2.status = 'ACTIVE')" +
            ") bbox WHERE ST_Intersects(bbox.g1, bbox.g2));";

    private NativeQueries() {
        throw new AssertionError();
    }
}
