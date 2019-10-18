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

    // 5 = Include CRS and bounding box in GeoJSON output, 7 = decimal precision
    private static final String SELECT_FROM = "SELECT a.id," +
            " a.tunnus," +
            " b.nimi as nimi," +
            " ST_Area(a.geom) as area_size," +
            " ST_AsGeoJSON(ST_Transform(a.geom, :srid), 7, 5) AS geom" +
            " FROM palstaalue a" +
            " LEFT JOIN kiinteisto_nimet b ON (a.tunnus = b.tunnus)";

    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findOne(final Long id, final GISUtils.SRID srid) {
        final String sql = SELECT_FROM + " WHERE a.id = :id";

        return query(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("srid", srid.getValue()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findAll(final List<Long> ids, final GISUtils.SRID srid) {
        final String sql = SELECT_FROM + " WHERE a.id IN (:ids)";

        return query(sql, new MapSqlParameterSource()
                .addValue("ids", ids)
                .addValue("srid", srid.getValue()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findByPropertyIdentifier(final String propertyIdentifier, final GISUtils.SRID srid) {
        final String sql = SELECT_FROM + " WHERE a.tunnus = :propertyIdentifier";

        return query(sql, new MapSqlParameterSource()
                .addValue("propertyIdentifier", Long.parseLong(propertyIdentifier))
                .addValue("srid", srid.getValue()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findIntersectingWithPoint(final double lat, final double lng, final GISUtils.SRID srid) {
        final String sql = SELECT_FROM + " WHERE ST_Contains(a.geom, ST_Transform(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), 3067))";

        return query(sql, new MapSqlParameterSource()
                .addValue("lat", lat)
                .addValue("lng", lng)
                .addValue("srid", srid.getValue()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollection findByBounds(final GISBounds bounds, int maxResults, final GISUtils.SRID srid) {
        final String sql = SELECT_FROM + " WHERE ST_Intersects(a.geom, ST_Transform(" +
                "ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326), 3067))" +
                " LIMIT :maxResults;";

        return query(sql, new MapSqlParameterSource()
                .addValue("minLon", bounds.getMinLng())
                .addValue("minLat", bounds.getMinLat())
                .addValue("maxLon", bounds.getMaxLng())
                .addValue("maxLat", bounds.getMaxLat())
                .addValue("maxResults", maxResults)
                .addValue("srid", srid.getValue()));
    }

    private FeatureCollection query(final String sql, MapSqlParameterSource parameterSource) {
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
        public Feature mapRow(ResultSet resultSet, int i) throws SQLException {
            final Feature feature = new Feature();

            feature.setId(String.valueOf(resultSet.getLong("id")));
            feature.setGeometry(GISUtils.parseGeoJSONGeometry(objectMapper, resultSet.getString("geom")));
            feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER,
                    StringUtils.leftPad(Long.toString(resultSet.getLong("tunnus")), 14, '0'));
            feature.setProperty(GeoJSONConstants.PROPERTY_NAME, resultSet.getString("nimi"));
            feature.setProperty(GeoJSONConstants.PROPERTY_SIZE, resultSet.getDouble("area_size"));
            feature.setBbox(feature.getGeometry().getBbox());
            feature.getGeometry().setBbox(null);

            return feature;
        }
    };
}
