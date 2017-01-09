package fi.riista.feature.pub.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.gis.geojson.FeatureCollectionWithProperties;
import fi.riista.util.DateUtil;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Component
public class PublicBearReportFeature {

    public static final int MIN_YEAR = 2014;

    private final static String SQL = "SELECT" +
            " ST_AsGeoJSON(ST_SetSRID(ST_MakePoint(h.longitude, h.latitude), 3067)) AS geom," +
            " to_char(h.point_of_time, 'DD.MM.YYYY') AS day," +
            " hs.gender," +
            " ha.name_finnish AS area_fi, ha.name_swedish AS area_sv," +
            " mun.name_finnish AS municipality_fi, mun.name_swedish AS municipality_sv," +
            " hp.permit_type_code as permit_type_code" +
            " FROM harvest h" +
            " JOIN harvest_report hr ON (hr.harvest_report_id = h.harvest_report_id)" +
            " JOIN harvest_specimen hs on (hs.harvest_id = h.harvest_id)" +
            " LEFT JOIN harvest_quota hq ON (hq.harvest_quota_id=h.harvest_quota_id)" +
            " LEFT JOIN harvest_area ha ON (ha.harvest_area_id=hq.harvest_area_id)" +
            " LEFT JOIN municipality mun ON (mun.official_code=h.municipality_code)" +
            " LEFT JOIN harvest_permit hp ON hp.harvest_permit_id=h.harvest_permit_id" +
            " WHERE hr.state = 'APPROVED' AND h.amount = 1" +
            " AND h.point_of_time >= :beginTime" +
            " AND h.point_of_time < :endTime" +
            " AND h.game_species_id = (SELECT game_species_id FROM game_species WHERE official_code = 47348)" +
            " AND (hp.permit_type_code IS NULL OR hp.permit_type_code IN ('202', '207'))" +
            " ORDER BY hr.creation_time ASC;";

    private static final String SQL_USED_QUOTA = "SELECT ha.name_finnish AS area_fi," +
            " hq.quota AS quota," +
            " count(hr.harvest_report_id) AS used," +
            " (:currentDate BETWEEN hs.begin_date AND hs.end_date) AS season_ongoing" +
            " FROM harvest_quota hq" +
            "  JOIN harvest_area ha ON ha.harvest_area_id=hq.harvest_area_id" +
            "  JOIN harvest_season hs ON hq.harvest_season_id=hs.harvest_season_id" +
            "  JOIN harvest_report_fields hf ON hf.harvest_report_fields_id=hs.harvest_report_fields_id" +
            "  LEFT JOIN harvest h ON h.harvest_quota_id=hq.harvest_quota_id" +
            "  LEFT JOIN harvest_report hr ON hr.harvest_report_id=h.harvest_report_id" +
            " WHERE hf.game_species_id=(SELECT game_species_id FROM game_species WHERE official_code = 47348)" +
            "  AND hs.begin_date >= :beginTime" +
            "  AND hs.end_date < :endTime" +
            "  AND (hr.harvest_report_id IS NULL OR (hr.state = 'APPROVED' AND h.amount = 1))" +
            "  GROUP BY hs.name_finnish, ha.name_finnish, hq.quota, hs.begin_date, hs.end_date" +
            "  ORDER BY ha.name_finnish;";

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public FeatureCollectionWithProperties report(int year) {
        return report(year, DateUtil.today());
    }

    /**
     * For tests!
     */
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public FeatureCollectionWithProperties report(int year, LocalDate currentDate) {
        if (year < MIN_YEAR) {
            throw new IllegalArgumentException("Year must be at least " + MIN_YEAR);
        }

        List<Feature> features = jdbcTemplate.query(SQL, getBeginEndParameters(year), new RowMapper<Feature>() {
            private final ObjectMapper objectMapper = new ObjectMapper();

            @Override
            public Feature mapRow(ResultSet rs, int rowNum) throws SQLException {
                Feature feature = new Feature();
                feature.getProperties().put("day", rs.getString("day"));
                feature.getProperties().put("gender", rs.getString("gender"));
                feature.getProperties().put("area_fi", rs.getString("area_fi"));
                feature.getProperties().put("area_sv", rs.getString("area_sv"));
                feature.getProperties().put("municipality_fi", rs.getString("municipality_fi"));
                feature.getProperties().put("municipality_sv", rs.getString("municipality_sv"));
                feature.getProperties().put("permit_type_code", rs.getString("permit_type_code"));
                try {
                    feature.setGeometry(objectMapper.readValue(rs.getString("geom"), GeoJsonObject.class));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return feature;
            }
        });

        FeatureCollectionWithProperties featureCollection = new FeatureCollectionWithProperties();
        featureCollection.setFeatures(features);

        featureCollection.setProperty("usedQuotas", countUsedQuotas(year, currentDate));

        Crs crs = new Crs();
        crs.getProperties().put("name", "urn:ogc:def:crs:EPSG::3067");

        featureCollection.setCrs(crs);

        return featureCollection;
    }

    private List<Map<String, Object>> countUsedQuotas(int year, LocalDate currentDate) {
        final MapSqlParameterSource params = getBeginEndParameters(year);
        params.addValue("currentDate", currentDate.toDate(), Types.TIMESTAMP);

        return jdbcTemplate.query(SQL_USED_QUOTA, params, (rs, rowNum) -> {
            int quota = rs.getInt("quota");
            int used = rs.getInt("used");
            boolean seasonOngoing = rs.getBoolean("season_ongoing");

            int remainingQuota = quota - used;
            int remaining = seasonOngoing && remainingQuota > 0 ? remainingQuota : 0;
            return ImmutableMap.of("area_fi", rs.getString("area_fi"), "remaining", remaining);
        });
    }

    private static MapSqlParameterSource getBeginEndParameters(int year) {
        final Interval interval = DateUtil.huntingYearInterval(year);
        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("beginTime", interval.getStart().toDate(), Types.TIMESTAMP);
        queryParams.addValue("endTime", interval.getEnd().toDate(), Types.TIMESTAMP);
        return queryParams;
    }
}
