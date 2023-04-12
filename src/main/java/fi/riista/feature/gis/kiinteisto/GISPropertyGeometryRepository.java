package fi.riista.feature.gis.kiinteisto;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.util.GISUtils;
import org.apache.commons.lang.StringUtils;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class GISPropertyGeometryRepository {

    /**
     * 1. pick one of palsta selectors
     */
    private static final String WITH_PALSTA_COORDINATES =
            "WITH palsta as (" +
            " SELECT * FROM palstaalue" +
            " WHERE ST_Contains(geom, ST_Transform(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), 3067))),";

    private static final String WITH_PALSTA_ID =
            "WITH palsta as (" +
            " SELECT * FROM palstaalue" +
            " WHERE id = :id),";

    private static final String WITH_PALSTA_IDS =
            "WITH palsta as (" +
            " SELECT * FROM palstaalue" +
            " WHERE id in (:ids)),";

    private static final String WITH_PALSTA_TUNNUS =
            "WITH palsta as (" +
            " SELECT * FROM palstaalue" +
            " WHERE tunnus = :propertyIdentifier),";

    private static final String WITH_PALSTA_BOUNDS =
            "WITH palsta as (" +
            " SELECT * FROM palstaalue" +
            " WHERE ST_Intersects(geom, ST_Transform(" +
            " ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326), 3067))" +
            " LIMIT :maxResults),";

    /**
     * 2. calculate water area or not
     */
    private static final String WITH_WATER_AREA =
            " water AS (" +
            "  SELECT p.tunnus, SUM(ST_Area(ST_Intersection(p.geom, va.geom))) AS area" +
            "  FROM palsta p" +
            "  LEFT JOIN vesialue va ON ST_Intersects(p.geom, va.geom)" +
            "  GROUP BY p.tunnus)";

    private static final String WITH_NO_WATER =
            " water AS (" +
            "  SELECT DISTINCT p.tunnus, -1 AS area" +
            "  FROM palsta p)";

    /**
     * 3. collect the data
     */
    private static final String SELECT_FROM =
            "SELECT a.id," +
            " a.tunnus," +
            " b.nimi as nimi," +
            " ST_Area(a.geom) as area_size," +
            " COALESCE(w.area, 0) as water_area," +
            // 5 = Include CRS and bounding box in GeoJSON output, 7 = decimal precision
            " ST_AsGeoJSON(ST_Transform(a.geom, :srid), 7, 5) AS geom" +
            " FROM palsta a" +
            " LEFT JOIN kiinteisto_nimet b ON (a.tunnus = b.tunnus)" +
            " LEFT JOIN water w ON w.tunnus = a.tunnus";

    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findOne(final Long id, final boolean withWaterArea, final GISUtils.SRID srid) {
        final String sql = WITH_PALSTA_ID + (withWaterArea ? WITH_WATER_AREA : WITH_NO_WATER) + SELECT_FROM;

        return query(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("srid", srid.getValue()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findAll(final List<Long> ids, final GISUtils.SRID srid) {
        final String sql = WITH_PALSTA_IDS + WITH_WATER_AREA + SELECT_FROM;

        return query(sql, new MapSqlParameterSource()
                .addValue("ids", ids)
                .addValue("srid", srid.getValue()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findByPropertyIdentifier(final String propertyIdentifier, final GISUtils.SRID srid) {
        final String sql = WITH_PALSTA_TUNNUS + WITH_NO_WATER + SELECT_FROM;

        return query(sql, new MapSqlParameterSource()
                .addValue("propertyIdentifier", Long.parseLong(propertyIdentifier))
                .addValue("srid", srid.getValue()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findIntersectingWithPoint(final double lat, final double lng, final boolean withWaterArea,
                                                       final GISUtils.SRID srid) {
        final String sql = WITH_PALSTA_COORDINATES + (withWaterArea ? WITH_WATER_AREA : WITH_NO_WATER) + SELECT_FROM;

        return query(sql, new MapSqlParameterSource()
                .addValue("lat", lat)
                .addValue("lng", lng)
                .addValue("srid", srid.getValue()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findByBounds(final GISBounds bounds, final int maxResults, final GISUtils.SRID srid) {
        final String sql = WITH_PALSTA_BOUNDS + WITH_NO_WATER + SELECT_FROM;

        return query(sql, new MapSqlParameterSource()
                .addValue("minLon", bounds.getMinLng())
                .addValue("minLat", bounds.getMinLat())
                .addValue("maxLon", bounds.getMaxLng())
                .addValue("maxLat", bounds.getMaxLat())
                .addValue("maxResults", maxResults)
                .addValue("srid", srid.getValue()));
    }

    private FeatureCollection query(final String sql, final MapSqlParameterSource parameterSource) {
        final FeatureCollection featureCollection = new FeatureCollection();

        featureCollection.setFeatures(jdbcTemplate.query(sql, parameterSource, ROW_MAPPER));

        final Crs crs = new Crs();
        crs.getProperties().put("name", "urn:ogc:def:crs:EPSG::4326");
        featureCollection.setCrs(crs);

        return featureCollection;
    }

    private static RowMapper<Feature> ROW_MAPPER = new RowMapper<Feature>() {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Feature mapRow(final ResultSet resultSet, final int i) throws SQLException {
            final Feature feature = new Feature();

            feature.setId(String.valueOf(resultSet.getLong("id")));
            feature.setGeometry(GISUtils.parseGeoJSONGeometry(objectMapper, resultSet.getString("geom")));
            feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER,
                    StringUtils.leftPad(Long.toString(resultSet.getLong("tunnus")), 14, '0'));
            feature.setProperty(GeoJSONConstants.PROPERTY_NAME, resultSet.getString("nimi"));
            feature.setProperty(GeoJSONConstants.PROPERTY_SIZE, resultSet.getDouble("area_size"));
            feature.setProperty(GeoJSONConstants.PROPERTY_WATER_AREA_SIZE, resultSet.getDouble("water_area"));
            feature.setBbox(feature.getGeometry().getBbox());
            feature.getGeometry().setBbox(null);

            return feature;
        }
    };
}
