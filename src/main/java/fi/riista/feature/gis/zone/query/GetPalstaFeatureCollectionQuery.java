package fi.riista.feature.gis.zone.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.util.GISUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GetPalstaFeatureCollectionQuery {
    private static final String SQL = "SELECT " +
            " b.palsta_id AS id," +
            " ST_AsGeoJSON(ST_Transform(b.geom, :crs)) AS geom," +
            " ST_Area(b.geom) AS area_size," +
            " b.palsta_tunnus AS tunnus," +
            " b.is_changed, " +
            " b.diff_area," +
            " b.new_palsta_id, " +
            " b.new_palsta_tunnus" +
            " FROM zone_palsta b " +
            " LEFT JOIN palstaalue a ON (a.id = b.palsta_id)" +
            " WHERE b.zone_id = :zoneId";

    private final NamedParameterJdbcOperations jdbcOperations;
    private final ObjectMapper objectMapper;

    public GetPalstaFeatureCollectionQuery(final NamedParameterJdbcOperations jdbcOperations,
                                           final ObjectMapper objectMapper) {
        this.jdbcOperations = jdbcOperations;
        this.objectMapper = objectMapper;
    }

    @Nonnull
    private Feature mapResultToFeature(final ResultSet rs) throws SQLException {
        final Feature feature = new Feature();

        feature.setId(String.valueOf(rs.getLong("id")));
        feature.setGeometry(GISUtils.parseGeoJSONGeometry(objectMapper, rs.getString("geom")));
        feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER, PropertyIdentifier.formatPropertyIdentifier(rs.getLong("tunnus")));
        feature.setProperty(GeoJSONConstants.PROPERTY_SIZE, rs.getDouble("area_size"));

        if (rs.getBoolean("is_changed")) {
            feature.setProperty(GeoJSONConstants.PROPERTY_CHANGED, true);
            feature.setProperty(GeoJSONConstants.PROPERTY_SIZE_DIFF, rs.getFloat("diff_area"));
            feature.setProperty(GeoJSONConstants.PROPERTY_PALSTA_NEW_ID, rs.getLong("new_palsta_id"));
            feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER_NEW, PropertyIdentifier.formatPropertyIdentifier(rs.getLong("new_palsta_tunnus")));
        }

        return feature;
    }

    public FeatureCollection execute(final Long zoneId, final GISUtils.SRID srid) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zoneId", zoneId)
                .addValue("crs", srid.getValue());

        final List<Feature> features = jdbcOperations.query(SQL, params, (rs, i) -> mapResultToFeature(rs));
        final FeatureCollection featureCollection = new FeatureCollection();

        if (features != null) {
            featureCollection.setFeatures(features);
        }

        return featureCollection;
    }
}
