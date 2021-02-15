package fi.riista.feature.gis.zone.query;

import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class GetCombinedFeatureCollectionQuery {
    public static final String SQL = "SELECT" +
            " zone_id, ST_AsBinary(ST_Transform(simple_geom, :crs)) AS geom" +
            " FROM zone WHERE zone_id IN (:zoneIds) AND simple_geom IS NOT NULL";

    private final NamedParameterJdbcOperations jdbcOperations;

    public GetCombinedFeatureCollectionQuery(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Nonnull
    public FeatureCollection execute(final Set<Long> zoneIds, final GISUtils.SRID srid) {
        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setCrs(srid.getGeoJsonCrs());

        if (zoneIds.isEmpty()) {
            return featureCollection;
        }

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zoneIds", zoneIds)
                .addValue("crs", srid.getValue());

        final List<Feature> features = jdbcOperations.query(SQL, params, (rs, i) -> {
            final Geometry geometry = GISUtils.readFromPostgisWkb(rs.getBytes("geom"), srid);
            final Geometry validGeometry = geometry.isValid() ? geometry : geometry.buffer(0);

            final Feature feature = new Feature();

            feature.setId(Long.toString(rs.getLong("zone_id")));
            feature.setBbox(GISUtils.getGeoJsonBBox(geometry));
            feature.setGeometry(PolygonConversionUtil.javaToGeoJSON(validGeometry));

            return feature;
        });

        if (features != null) {
            featureCollection.setFeatures(features);
        }

        return featureCollection;
    }
}
