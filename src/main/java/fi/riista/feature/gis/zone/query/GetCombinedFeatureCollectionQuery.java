package fi.riista.feature.gis.zone.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.riista.util.GISUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class GetCombinedFeatureCollectionQuery {

    public static final String SQL = "WITH d AS (" +
            " SELECT " +
            "  zone_id, " +
            "  ST_Transform(ST_Simplify(geom, :simplify), :crs) geom" +
            " FROM zone" +
            " WHERE zone_id IN (:zoneIds)" +
            "), e AS (" +
            " SELECT " +
            "  d.zone_id AS zone_id," +
            "  d.geom AS geom," +
            "  ST_XMin(ST_Extent(d.geom)) AS xmin, " +
            "  ST_YMin(ST_Extent(d.geom)) AS ymin, " +
            "  ST_XMax(ST_Extent(d.geom)) AS xmax, " +
            "  ST_YMax(ST_Extent(d.geom)) AS ymax" +
            " FROM d" +
            " GROUP BY zone_id, geom" +
            ") SELECT " +
            "  e.zone_id AS id," +
            "  ST_AsGeoJSON(e.geom) AS geom," +
            "  e.xmin, e.xmax, e.ymin, e.ymax" +
            " FROM e" +
            " JOIN zone z ON (e.zone_id = z.zone_id);";

    private final NamedParameterJdbcOperations jdbcOperations;
    private final ObjectMapper objectMapper;

    public GetCombinedFeatureCollectionQuery(final NamedParameterJdbcOperations jdbcOperations,
                                             final ObjectMapper objectMapper) {
        this.jdbcOperations = jdbcOperations;
        this.objectMapper = objectMapper;
    }

    @Nonnull
    private Feature mapResultToFeature(final ResultSet rs) throws SQLException {
        final Feature feature = new Feature();

        feature.setId(Long.toString(rs.getLong("id")));
        feature.setGeometry(GISUtils.parseGeoJSONGeometry(objectMapper, rs.getString("geom")));
        feature.setBbox(new double[]{
                rs.getDouble("xmin"), rs.getDouble("ymin"),
                rs.getDouble("xmax"), rs.getDouble("ymax")});

        return feature;
    }

    @Nonnull
    public FeatureCollection execute(final Set<Long> zoneIds, final GISUtils.SRID srid, final double simplifyAmount) {
        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setCrs(srid.getGeoJsonCrs());

        if (zoneIds.isEmpty()) {
            return featureCollection;
        }

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zoneIds", zoneIds)
                .addValue("crs", srid.getValue())
                .addValue("simplify", simplifyAmount);

        final List<Feature> features = jdbcOperations.query(SQL, params, (rs, i) -> mapResultToFeature(rs));

        if (features != null) {
            featureCollection.setFeatures(features);
        }

        return featureCollection;
    }
}
