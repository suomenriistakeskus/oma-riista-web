package fi.riista.feature.gis.zone.query;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.gis.GISBounds;
import fi.riista.util.Collect;
import fi.riista.util.GISUtils;
import io.vavr.Tuple;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class GetBoundsQueries {
    private final NamedParameterJdbcOperations jdbcOperations;

    public GetBoundsQueries(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public GISBounds getBounds(final long zoneId, final GISUtils.SRID srid) {
        final String sql = "SELECT ST_AsBinary(ST_Extent(ST_Transform(geom, :srid))) AS geom FROM zone WHERE zone_id = :zoneId";
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("srid", srid.getValue())
                .addValue("zoneId", zoneId);

        return jdbcOperations.queryForObject(sql, params, (rs, rowNum)
                -> wkbToBounds(srid, rs.getBytes("geom")));
    }

    public Map<Long, GISBounds> getBounds(final Collection<Long> zoneIds, final GISUtils.SRID srid) {
        if (zoneIds.isEmpty()) {
            return emptyMap();
        }

        final String sql = "SELECT zone_id, ST_AsBinary(ST_Extent(ST_Transform(geom, :srid))) AS geom FROM zone WHERE zone_id IN (:zoneIds) GROUP BY zone_id";
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("srid", srid.getValue())
                .addValue("zoneIds", zoneIds);

        return jdbcOperations.query(sql, params, (rs, rowNum)
                -> Tuple.of(rs.getLong("zone_id"), wkbToBounds(srid, rs.getBytes("geom"))))
                .stream()
                .filter(tuple -> tuple._2 != null)
                .collect(Collect.tuplesToMap());
    }

    private GISBounds wkbToBounds(final GISUtils.SRID srid, final byte[] wkb) {
        return wkb != null ? getGISBounds(GISUtils.readFromPostgisWkb(wkb, srid)) : null;
    }

    private static GISBounds getGISBounds(final Geometry geometry) {
        return geometry != null && !geometry.isEmpty()
                ? GISBounds.create(geometry.getEnvelopeInternal())
                : null;
    }
}
