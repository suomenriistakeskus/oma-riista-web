package fi.riista.feature.gis.zone.query;

import fi.riista.util.GISUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import javax.annotation.Nullable;

public class GetStateGeometryQuery {

    // Common SQL query which uses zone/geometry CTE named z
    private static final String STATE_LAND_IN_GEOMETRY_SQL = "z_state AS (" +
            " SELECT (ST_Dump(ST_Intersection(vm.geom, ST_Buffer(z.geom, 0)))).geom as geom" +
            " FROM z JOIN valtionmaa vm ON ST_Intersects(vm.geom, z.geom)" +
            " WHERE GeometryType(z.geom) IN ('POLYGON', 'MULTIPOLYGON')" +
            ") SELECT ST_AsBinary(ST_Transform(ST_CollectionHomogenize(ST_CollectionExtract(ST_Collect(z_state.geom)," +
            " 3)), :crs)) AS geom" +
            "  FROM z_state";

    // Get zone geometry from db
    private static final String STATE_LAND_WITHIN_ZONE_SQL = "WITH z AS (" +
            " SELECT (ST_Dump(geom)).geom AS geom" +
            " FROM zone WHERE zone_id = :zoneId), "
            + STATE_LAND_IN_GEOMETRY_SQL;

    // Use provided geometry
    private static final String STATE_LAND_WITHIN_GEOMETRY_SQL = " WITH z AS (" +
            " SELECT ST_SetSRID(ST_GeomFromWKB(:geom), :crs) AS geom ), "
            + STATE_LAND_IN_GEOMETRY_SQL;


    private final NamedParameterJdbcOperations jdbcOperations;

    public GetStateGeometryQuery(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Nullable
    public Geometry execute(final long zoneId, final GISUtils.SRID srid) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("crs", srid.getValue())
                .addValue("zoneId", zoneId);

        return jdbcOperations.queryForObject(STATE_LAND_WITHIN_ZONE_SQL, params, (rs, i) -> {
            final byte[] geomBytes = rs.getBytes("geom");
            if (geomBytes == null) {
                return null;
            }
            final Geometry geometry = GISUtils.readFromPostgisWkb(geomBytes, srid);
            return geometry.isValid() ? geometry : geometry.buffer(0);
        });
    }

    @Nullable
    public Geometry execute(final Geometry geom, final GISUtils.SRID srid) {

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("crs", srid.getValue())
                .addValue("geom", GISUtils.writeToPostgisWkb(geom));

        return jdbcOperations.queryForObject(STATE_LAND_WITHIN_GEOMETRY_SQL, params, (rs, i) -> {
            final byte[] geomBytes = rs.getBytes("geom");
            if (geomBytes == null) {
                return null;
            }
            final Geometry geometry = GISUtils.readFromPostgisWkb(geomBytes, srid);
            return geometry.isValid() ? geometry : geometry.buffer(0);
        });
    }
}
