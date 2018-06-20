package fi.riista.feature.gis.zone.query;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.util.GISUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import javax.annotation.Nonnull;

public class GetInvertedGeometryQuery {
    private static final String FINNISH_BOUNDS = "LINESTRING(50199.4814 6582464.0358, 761274.6247 7799839.8902)";

    // Inversion is done locally inside bounds of Finland using ST_Difference
    private static final String SQL = "WITH finland AS (" +
            "  SELECT ST_Buffer(ST_Envelope(ST_GeomFromText(:worldBounds, 3067)), 0) AS geom" +
            "), d AS (" +
            "  SELECT ST_Difference(ST_Transform(finland.geom, 4326), ST_Buffer(simple_geom, 0)) AS geom" +
            "  FROM finland CROSS JOIN zone" +
            "  WHERE zone_id = :zoneId AND simple_geom IS NOT NULL" +
            ") SELECT ST_AsBinary(ST_Transform(d.geom, :crs)) AS geom FROM d;";

    private final NamedParameterJdbcOperations jdbcOperations;

    public GetInvertedGeometryQuery(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Nonnull
    public Geometry execute(final long zoneId, final GISUtils.SRID srid) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("worldBounds", FINNISH_BOUNDS)
                .addValue("crs", srid.getValue())
                .addValue("zoneId", zoneId);

        return jdbcOperations.queryForObject(SQL, params, (rs, i) -> {
            final Geometry geometry = GISUtils.readFromPostgisWkb(rs.getBytes("geom"), srid);
            return geometry.isValid() ? geometry : geometry.buffer(0);
        });
    }
}
