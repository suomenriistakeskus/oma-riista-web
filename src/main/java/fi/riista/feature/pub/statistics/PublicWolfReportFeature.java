package fi.riista.feature.pub.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.riista.util.DateUtil;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Component
public class PublicWolfReportFeature {

    public static final int MIN_YEAR = 2014;
    public static final int MAX_YEAR = 2015;

    private final static String SQL = "SELECT" +
            " ST_AsGeoJSON(h.geom) AS geom, " +
            " to_char(h.point_of_time, 'DD.MM.YYYY') as day, " +
            " hs.gender, hs.age, h.luke_status, o.official_code as rhy_code," +
            " o.name_finnish as rhy_fi, o.name_swedish as rhy_sv" +
            " FROM harvest as h" +
            " JOIN organisation as o on (o.organisation_id = h.rhy_id)" +
            " JOIN harvest_permit as hp on (hp.harvest_permit_id = h.harvest_permit_id)" +
            " JOIN harvest_specimen AS hs on (hs.harvest_id = h.harvest_id)" +
            " WHERE h.harvest_report_state = 'APPROVED' AND h.amount = 1" +
            " AND h.game_species_id = (select game_species_id FROM game_species WHERE official_code = 46549)" +
            " AND hp.permit_type_code = :permitTypeCode" +
            " AND h.point_of_time >= :beginTime" +
            " AND h.point_of_time < :endTime" +
            " ORDER BY h.harvest_report_date asc";

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public FeatureCollection report(int year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("Year must be between" + MIN_YEAR + " and " + MAX_YEAR);
        }

        List<Feature> features = jdbcTemplate.query(SQL, getSqlParameterSource(year, "209"), new RowMapper<Feature>() {
            private final ObjectMapper objectMapper = new ObjectMapper();

            @Override
            public Feature mapRow(ResultSet rs, int rowNum) throws SQLException {
                Feature feature = new Feature();
                feature.getProperties().put("day", rs.getString("day"));
                feature.getProperties().put("gender", rs.getString("gender"));
                feature.getProperties().put("age", rs.getString("age"));
                feature.getProperties().put("luke_status", rs.getString("luke_status"));
                feature.getProperties().put("rhy_code", rs.getString("rhy_code"));
                feature.getProperties().put("rhy_fi", rs.getString("rhy_fi"));
                feature.getProperties().put("rhy_sv", rs.getString("rhy_sv"));
                try {
                    feature.setGeometry(objectMapper.readValue(rs.getString("geom"), GeoJsonObject.class));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return feature;
            }
        });

        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setFeatures(features);

        Crs crs = new Crs();
        crs.getProperties().put("name", "urn:ogc:def:crs:EPSG::3067");

        featureCollection.setCrs(crs);

        return featureCollection;
    }

    private static MapSqlParameterSource getSqlParameterSource(int year, String permitTypeCode) {
        final Interval interval = DateUtil.huntingYearInterval(year);
        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("permitTypeCode", permitTypeCode);
        queryParams.addValue("beginTime", interval.getStart().toDate(), Types.TIMESTAMP);
        queryParams.addValue("endTime", interval.getEnd().toDate(), Types.TIMESTAMP);
        return queryParams;
    }

}
