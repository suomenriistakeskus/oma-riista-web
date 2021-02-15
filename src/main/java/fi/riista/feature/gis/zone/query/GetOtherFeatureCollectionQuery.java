package fi.riista.feature.gis.zone.query;

import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.util.GISUtils;
import fi.riista.util.PolygonConversionUtil;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GetOtherFeatureCollectionQuery {
    private static final String SQL = "SELECT " +
            " a.zone_feature_id AS id," +
            " ST_AsBinary(ST_Transform(a.geom, :crs)) AS geom," +
            " ST_Area(a.geom) AS area_size" +
            " FROM zone_feature a " +
            " WHERE a.zone_id = :zoneId" +
            " ORDER BY a.zone_feature_id";

    private final NamedParameterJdbcOperations jdbcOperations;

    public GetOtherFeatureCollectionQuery(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Nonnull
    private static Feature mapResultToFeature(final ResultSet rs, final GISUtils.SRID srid) throws SQLException {
        final Feature feature = new Feature();

        feature.setId(GeoJSONConstants.ID_PREFIX_OTHER + rs.getLong("id"));
        feature.setGeometry(readGeometry(rs.getBytes("geom"), srid));
        feature.setProperty(GeoJSONConstants.PROPERTY_SIZE, rs.getDouble("area_size"));

        return feature;
    }

    private static GeoJsonObject readGeometry(final byte[] wkb, final GISUtils.SRID srid) {
        return PolygonConversionUtil.javaToGeoJSON(GISUtils.readFromPostgisWkb(wkb, srid));
    }

    public List<Feature> execute(final Long zoneId, final GISUtils.SRID srid) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zoneId", zoneId)
                .addValue("crs", srid.getValue())
                .addValue("precision", srid.getDecimalPrecision());

        return jdbcOperations.query(SQL, params, (rs, i) -> mapResultToFeature(rs, srid));
    }

}
