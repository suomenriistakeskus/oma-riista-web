package fi.riista.feature.gis.zone.query;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

public class CalculateCombinedGeometryQueries {
    private static final double BUFFER_ZONE = 0.5; // metres

    private final NamedParameterJdbcOperations jdbcOperations;

    public CalculateCombinedGeometryQueries(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void updateGeometryFromPalsta(final long zoneId) {
        // Combine geometries
        mergePalstaGeometries(zoneId);
        mergeMetsahallitusHirviGeometries(zoneId);

        // Make sure geometry is valid
        enforceCombinedGeometryValidity(zoneId);
        enforceExcludedGeometryValidity(zoneId);

        // XXX: Disabled currently for performance reasons
        //trimExcludedGeometry(zoneId);

        // Remove excluded geometry from union
        subtractExcludedGeometryFromUnion(zoneId);
    }

    public void updateGeometryFromFeatures(final long zoneId) {
        jdbcOperations.update("UPDATE zone SET geom = (" +
                        " SELECT ST_UnaryUnion(ST_Collect(ST_Buffer(zf.geom, :bufferZone)))" +
                        " FROM zone_feature zf WHERE zf.zone_id = :zoneId" +
                        ") WHERE zone_id = :zoneId",
                zoneParam(zoneId).addValue("bufferZone", BUFFER_ZONE));
    }

    private void mergePalstaGeometries(final long zoneId) {
        jdbcOperations.update("UPDATE zone SET geom = (" +
                " SELECT ST_UnaryUnion(ST_CollectionHomogenize(ST_Collect(zp.geom)))" +
                " FROM zone_palsta zp WHERE zp.zone_id = :zoneId" +
                ") WHERE zone_id = :zoneId", zoneParam(zoneId));
    }

    private void mergeMetsahallitusHirviGeometries(final long zoneId) {
        jdbcOperations.update("UPDATE zone SET geom = (" +
                "WITH mh AS (" +
                " SELECT ST_UnaryUnion(ST_CollectionHomogenize(ST_Collect(mh.geom))) AS geom" +
                " FROM zone_mh_hirvi zh " +
                " JOIN mh_hirvi mh ON (zh.zone_id = :zoneId AND zh.mh_hirvi_id = mh.gid)" +
                ") SELECT CASE " +
                " WHEN zone.geom IS NOT NULL AND mh.geom IS NOT NULL THEN ST_Union(zone.geom, mh.geom)" +
                " WHEN mh.geom IS NOT NULL THEN mh.geom" +
                " ELSE zone.geom END FROM zone, mh WHERE zone_id = :zoneId" +
                ") WHERE zone_id = :zoneId", zoneParam(zoneId));
    }

    private void enforceCombinedGeometryValidity(final long zoneId) {
        jdbcOperations.update("UPDATE zone SET geom = ST_Buffer(geom, 0)" +
                " WHERE zone_id = :zoneId AND geom IS NOT NULL", zoneParam(zoneId));
    }

    private void enforceExcludedGeometryValidity(final long zoneId) {
        jdbcOperations.update("UPDATE zone SET excluded_geom = ST_Buffer(excluded_geom, 0)" +
                " WHERE zone_id = :zoneId AND excluded_geom IS NOT NULL", zoneParam(zoneId));
    }

    private void trimExcludedGeometry(final long zoneId) {
        jdbcOperations.update("WITH g AS (SELECT ST_SubDivide((ST_Dump(geom)).geom, 256) AS geom " +
                " FROM zone WHERE zone_id = :zoneId)," +
                "e AS (SELECT ST_Transform(excluded_geom, 3067) AS geom FROM zone WHERE zone_id = :zoneId)" +
                " UPDATE zone SET excluded_geom = (" +
                " SELECT ST_Transform(ST_UnaryUnion(ST_Collect(ST_Intersection(g.geom, e.geom))), 4326) AS geom" +
                " FROM g JOIN e ON ST_Intersects(g.geom, e.geom)) WHERE zone_id = :zoneId", zoneParam(zoneId));
    }

    private void subtractExcludedGeometryFromUnion(final long zoneId) {
        // NOTE: ST_Buffer is used to avoid creating invalid line-geometries during ST_Difference
        jdbcOperations.update("UPDATE zone SET geom =" +
                        " ST_Difference(geom, ST_Buffer(ST_Transform(excluded_geom, 3067), :bufferZone))" +
                        " WHERE zone_id = :zoneId AND excluded_geom IS NOT NULL",
                zoneParam(zoneId).addValue("bufferZone", BUFFER_ZONE));
    }

    private static MapSqlParameterSource zoneParam(final long zoneId) {
        return new MapSqlParameterSource("zoneId", zoneId);
    }
}
