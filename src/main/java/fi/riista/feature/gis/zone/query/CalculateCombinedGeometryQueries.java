package fi.riista.feature.gis.zone.query;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import fi.riista.util.GISUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.List;

public class CalculateCombinedGeometryQueries {
    private static final double BUFFER_ZONE = 0.5; // metres

    private final NamedParameterJdbcOperations jdbcOperations;

    public CalculateCombinedGeometryQueries(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void updateGeometry(final long zoneId) {
        // Combine geometries
        final List<Geometry> geometries = loadSplicedGeometries(zoneId);

        if (geometries.isEmpty()) {
            jdbcOperations.update("UPDATE zone SET geom = NULL WHERE zone_id = :zoneId", zoneParam(zoneId));
            return;
        }

        final Geometry union = GISUtils.computeUnionFaster(geometries);
        final WKBWriter wkbWriter = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN, true);
        jdbcOperations.update("UPDATE zone SET geom = :geom WHERE zone_id = :zoneId",
                zoneParam(zoneId).addValue("geom", wkbWriter.write(union)));

        // Make sure geometry is valid
        enforceCombinedGeometryValidity(zoneId);
        enforceExcludedGeometryValidity(zoneId);

        // XXX: Disabled currently for performance reasons
        //trimExcludedGeometry(zoneId);

        // Remove excluded geometry from union
        subtractExcludedGeometryFromUnion(zoneId);
    }

    private List<Geometry> loadSplicedGeometries(final long zoneId) {
        final GeometryFactory geometryFactory = GISUtils.getGeometryFactory(GISUtils.SRID.ETRS_TM35FIN);
        final WKBReader wkbReader = new WKBReader(geometryFactory);
        final MapSqlParameterSource params = zoneParam(zoneId)
                .addValue("chunkSize", 16384);

        return jdbcOperations.query("WITH g AS (" +
                        "   SELECT (ST_Dump(zp.geom)).geom AS geom FROM zone_palsta zp " +
                        "   WHERE zp.zone_id = :zoneId" +
                        "   UNION ALL" +
                        "   SELECT (ST_Dump(zp.geom)).geom AS geom FROM zone_feature zp " +
                        "   WHERE zp.zone_id = :zoneId" +
                        "   UNION ALL " +
                        "   SELECT (ST_Dump(mh.geom)).geom AS geom FROM zone_mh_hirvi zh " +
                        "   JOIN mh_hirvi mh ON (zh.zone_id = :zoneId AND zh.mh_hirvi_id = mh.gid)" +
                        ") SELECT ST_AsBinary(ST_SubDivide(geom, :chunkSize)) AS geom FROM g",
                params, (resultSet, i) -> {
                    final byte[] wkb = resultSet.getBytes("geom");

                    try {
                        return wkbReader.read(wkb);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
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
                        " ST_Difference(geom, ST_Buffer(ST_Transform(excluded_geom, 3067), :bufferZone, 'join=bevel endcap=flat'))" +
                        " WHERE zone_id = :zoneId AND excluded_geom IS NOT NULL",
                zoneParam(zoneId).addValue("bufferZone", BUFFER_ZONE));
    }

    private static MapSqlParameterSource zoneParam(final long zoneId) {
        return new MapSqlParameterSource("zoneId", zoneId);
    }
}
