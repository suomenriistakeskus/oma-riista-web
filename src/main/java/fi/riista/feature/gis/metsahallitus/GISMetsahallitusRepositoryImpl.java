package fi.riista.feature.gis.metsahallitus;

import com.google.common.collect.Iterables;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.geojson.GeoJSONConstants;
import fi.riista.util.GISUtils;
import org.geojson.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

@Repository
public class GISMetsahallitusRepositoryImpl implements GISMetsahallitusRepositoryCustom {
    private static final Logger LOG = LoggerFactory.getLogger(GISMetsahallitusRepository.class);

    private static final int SIMPLIFY_AMOUNT = 5;

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer findHirviAlueId(GeoLocation geoLocation, int year) {
        return internalQueryKohdeId(geoLocation, year, "SELECT gid FROM mh_hirvi" +
                " WHERE ST_Intersects(geom, ST_SetSRID(ST_MakePoint(?, ?), 3067)) AND vuosi = ?");
    }

    @Override
    @Transactional(readOnly = true)
    public Integer findPienriistaAlueId(GeoLocation geoLocation, int year) {
        return internalQueryKohdeId(geoLocation, year, "SELECT kohde_id FROM mh_pienriista" +
                " WHERE ST_Intersects(geom, ST_SetSRID(ST_MakePoint(?, ?), 3067)) AND vuosi = ?");
    }

    private Integer internalQueryKohdeId(final GeoLocation geoLocation, final int year, final String sql) {
        // XXX: Do not use queryForObject which can throw EmptyResultDataAccessException
        final List<Integer> resultSet = jdbcTemplate.queryForList(sql, Integer.class,
                geoLocation.getLongitude(), geoLocation.getLatitude(), year);

        if (resultSet.size() > 1) {
            LOG.error("Multiple results for GIS query on geoLocation={}", geoLocation);

            return null;
        }

        return Iterables.getFirst(resultSet, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GISMetsahallitusHirviDTO> listHirvi(final int year) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("year", year);

        final String sql = "SELECT gid, koodi, nimi, pinta_ala FROM mh_hirvi WHERE vuosi = :year";

        return namedParameterJdbcTemplate.query(sql, params, (resultSet, i) -> {
            final int id = resultSet.getInt("gid");
            final int code = resultSet.getInt("koodi");
            final String name = resultSet.getString("nimi");
            final long areaSize = resultSet.getLong("pinta_ala") * 10_000;

            return new GISMetsahallitusHirviDTO(id, year, code, name, areaSize);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Feature getHirviFeature(final int id, final GISUtils.SRID srid) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("srid", srid.getValue())
                .addValue("simplify", SIMPLIFY_AMOUNT);

        final String sql = SELECT_MH_HIRVI + " FROM mh_hirvi mh WHERE gid = :id;";

        return namedParameterJdbcTemplate.queryForObject(sql, params, ROW_MAPPER_HIRVI);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feature> listZoneHirviFeatures(final long zoneId, final GISUtils.SRID srid) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("zoneId", zoneId)
                .addValue("srid", srid.getValue())
                .addValue("simplify", SIMPLIFY_AMOUNT);

        final String sql = SELECT_MH_HIRVI +
                " FROM zone_mh_hirvi zmh" +
                " JOIN mh_hirvi mh ON (zmh.mh_hirvi_id = mh.gid) " +
                " WHERE zmh.zone_id = :zoneId;";

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER_HIRVI);
    }

    private static final String SELECT_MH_HIRVI = "SELECT mh.gid, mh.vuosi, mh.koodi, mh.nimi, mh.pinta_ala," +
            " ST_AsGeoJSON(ST_Transform(ST_Simplify(mh.geom, :simplify), :srid)) AS geom";

    private final RowMapper<Feature> ROW_MAPPER_HIRVI = (resultSet, i) -> {
        final Feature feature = new Feature();

        feature.setId(GeoJSONConstants.ID_PREFIX_MH_HIRVI + resultSet.getInt("gid"));
        feature.setProperty(GeoJSONConstants.PROPERTY_YEAR, resultSet.getString("vuosi"));
        feature.setProperty(GeoJSONConstants.PROPERTY_NUMBER, resultSet.getInt("koodi"));
        feature.setProperty(GeoJSONConstants.PROPERTY_NAME, resultSet.getString("nimi"));
        feature.setProperty(GeoJSONConstants.PROPERTY_SIZE, resultSet.getLong("pinta_ala") * 10_000);
        feature.setGeometry(GISUtils.parseGeoJSONGeometry(objectMapper, resultSet.getString("geom")));

        return feature;
    };
}
