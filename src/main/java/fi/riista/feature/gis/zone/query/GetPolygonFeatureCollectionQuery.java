package fi.riista.feature.gis.zone.query;

import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.locationtech.jts.geom.Geometry;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class GetPolygonFeatureCollectionQuery {
    public static final String SQL = "WITH d AS (" +
            " SELECT (ST_Dump(geom)).geom geom" +
            " FROM zone WHERE zone_id = :zoneId" +
            ")" +
            "SELECT ST_AsBinary(ST_Transform(d.geom, :crs)) AS geom," +
            " ST_Area(d.geom) AS area_size," +
            " ST_Geohash(ST_Transform(ST_PointOnSurface(d.geom), 4326), 8) AS hash" +
            " FROM d";

    private final NamedParameterJdbcOperations jdbcOperations;

    public GetPolygonFeatureCollectionQuery(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Nonnull
    public FeatureCollection execute(final Long zoneId, final GISUtils.SRID srid) {
        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setCrs(srid.getGeoJsonCrs());

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zoneId", zoneId)
                .addValue("crs", srid.getValue());

        final List<Feature> features = jdbcOperations.query(SQL, params, (rs, i) -> {
            final Geometry geometry = GISUtils.readFromPostgisWkb(rs.getBytes("geom"), srid);

            final Feature feature = new Feature();

            feature.setId(UUID.randomUUID().toString());
            feature.setProperty(GeoJSONConstants.PROPERTY_AREA_SIZE, rs.getDouble("area_size"));
            feature.setProperty(GeoJSONConstants.PROPERTY_HASH, rs.getString("hash"));
            feature.setBbox(GISUtils.getGeoJsonBBox(geometry));
            feature.setGeometry(PolygonConversionUtil.javaToGeoJSON(geometry));

            return feature;
        });

        if (features != null) {
            featureCollection.setFeatures(features);
        }

        return featureCollection;
    }

}
