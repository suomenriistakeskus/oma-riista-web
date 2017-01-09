package fi.riista.feature.gis.zone.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.util.GISUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.List;

public class GetExternalFeatureCollectionQuery {

    public static final String SQL = "SELECT " +
            " property_identifier," +
            " ST_AsGeoJSON(ST_Transform(geom, :srid)) geom" +
            " FROM zone_feature" +
            " WHERE zone_id = :zoneId";

    private final NamedParameterJdbcOperations jdbcOperations;
    private final ObjectMapper objectMapper;

    public GetExternalFeatureCollectionQuery(final NamedParameterJdbcOperations jdbcOperations,
                                             final ObjectMapper objectMapper) {
        this.jdbcOperations = jdbcOperations;
        this.objectMapper = objectMapper;
    }

    public FeatureCollection execute(final long zoneId, final GISUtils.SRID srid) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zoneId", zoneId)
                .addValue("crs", srid.getValue());

        final List<Feature> features = jdbcOperations.query(SQL, params, (resultSet, i) -> {
            final Feature feature = new Feature();
            feature.setId(Long.toString(zoneId));
            feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER, resultSet.getString("property_identifier"));
            feature.setGeometry(GISUtils.parseGeoJSONGeometry(objectMapper, resultSet.getString("geom")));

            return feature;
        });

        final FeatureCollection featureCollection = new FeatureCollection();

        if (features != null) {
            featureCollection.setFeatures(features);
        }

        return featureCollection;
    }
}
