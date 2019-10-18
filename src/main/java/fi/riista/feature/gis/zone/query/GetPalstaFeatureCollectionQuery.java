package fi.riista.feature.gis.zone.query;

import fi.riista.feature.common.entity.PropertyIdentifier;
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

public class GetPalstaFeatureCollectionQuery {
    private static final String SQL = "SELECT " +
            " a.palsta_id AS id," +
            " ST_AsBinary(ST_Transform(a.geom, :crs)) AS geom," +
            " ST_Area(a.geom) AS area_size," +
            " a.palsta_tunnus AS tunnus," +
            " b.nimi AS nimi," +
            " a.is_changed, " +
            " a.diff_area," +
            " a.new_palsta_id, " +
            " a.new_palsta_tunnus" +
            " FROM zone_palsta a " +
            " LEFT JOIN kiinteisto_nimet b ON (b.tunnus = a.palsta_tunnus)" +
            " WHERE a.zone_id = :zoneId" +
            " ORDER BY a.palsta_id";

    private final NamedParameterJdbcOperations jdbcOperations;

    public GetPalstaFeatureCollectionQuery(final NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Nonnull
    private Feature mapResultToFeature(final ResultSet rs, final GISUtils.SRID srid) throws SQLException {
        final Feature feature = new Feature();

        feature.setId(String.valueOf(rs.getLong("id")));
        feature.setGeometry(readGeometry(rs.getBytes("geom"), srid));
        feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER,
                PropertyIdentifier.formatPropertyIdentifier(rs.getLong("tunnus")));
        feature.setProperty(GeoJSONConstants.PROPERTY_NAME, rs.getString("nimi"));
        feature.setProperty(GeoJSONConstants.PROPERTY_SIZE, rs.getDouble("area_size"));

        if (rs.getBoolean("is_changed")) {
            feature.setProperty(GeoJSONConstants.PROPERTY_CHANGED, true);
            feature.setProperty(GeoJSONConstants.PROPERTY_SIZE_DIFF, rs.getFloat("diff_area"));
            feature.setProperty(GeoJSONConstants.PROPERTY_PALSTA_NEW_ID, rs.getLong("new_palsta_id"));
            feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER_NEW,
                    PropertyIdentifier.formatPropertyIdentifier(rs.getLong("new_palsta_tunnus")));
        }

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
