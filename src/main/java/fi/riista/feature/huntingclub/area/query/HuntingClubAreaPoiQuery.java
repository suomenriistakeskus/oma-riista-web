package fi.riista.feature.huntingclub.area.query;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class HuntingClubAreaPoiQuery {
    private final JdbcOperations jdbcOperations;

    public HuntingClubAreaPoiQuery(final JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public List<Long> listPois(final long huntingClubAreaId) {
        final String selectSql =
                "SELECT poi_location_group_id " +
                "FROM hunting_club_area_poi " +
                "WHERE hunting_club_area_id = ? " +
                "ORDER BY poi_location_group_id";

        return jdbcOperations.queryForList(selectSql, Long.class, huntingClubAreaId);
    }

    public void insertPois(final long huntingClubAreaId, final List<Long> pois) {
        final String insertSql = "INSERT INTO hunting_club_area_poi(hunting_club_area_id, poi_location_group_id)" +
                " VALUES (?, ?)";

        jdbcOperations.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setLong(1, huntingClubAreaId);
                ps.setLong(2, pois.get(i));
            }

            @Override
            public int getBatchSize() {
                return pois.size();
            }
        });
    }

    public void removeZoneFeatures(final long zoneId) {
        jdbcOperations.update("DELETE FROM hunting_club_area_poi WHERE hunting_club_area_id = ?", zoneId);
    }

    public void removeConnectionsToPoi(final long poiGroupId) {
        jdbcOperations.update("DELETE FROM hunting_club_area_poi WHERE poi_location_group_id = ?", poiGroupId);
    }

    public Long getAreaPOICount() {
        final String sql =
                "select count(distinct hcap.hunting_club_area_id) " +
                "from hunting_club_area_poi hcap";

        return jdbcOperations.queryForObject(sql, Long.class);
    }
}
