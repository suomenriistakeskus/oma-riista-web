package fi.riista.feature.gis.zone.query;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.util.GISUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import javax.annotation.Nonnull;

public class GetGeometryQuery {
    private static final String SQL = "SELECT ST_AsBinary(ST_Transform(simple_geom, :crs)) AS geom" +
            " FROM zone WHERE zone_id = :zoneId";

    private final NamedParameterJdbcOperations jdbcOperations;

    public GetGeometryQuery(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Nonnull
    public Geometry execute(final long zoneId, final GISUtils.SRID srid) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("crs", srid.getValue())
                .addValue("zoneId", zoneId);

        return jdbcOperations.queryForObject(SQL, params, (rs, i) -> {
            final Geometry geometry = GISUtils.readFromPostgisWkb(rs.getBytes("geom"), srid);
            return geometry.isValid() ? geometry : geometry.buffer(0);
        });
    }
}
