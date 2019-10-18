package fi.riista.feature.gis.zone.query;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class UpdatePalstaFeatureQueries {
    private static final String SELECT_SQL = "SELECT palsta_id FROM zone_palsta WHERE zone_id = ?";
    private static final String DELETE_ALL_SQL = "DELETE FROM zone_palsta WHERE zone_id = ?";
    private static final String DELETE_SQL = "DELETE FROM zone_palsta WHERE zone_id = ? AND palsta_id = ?";
    private static final String INSERT_SQL = "INSERT INTO zone_palsta (zone_id, palsta_id, geom, palsta_tunnus)" +
            " SELECT ?, pa.id, pa.geom, pa.tunnus FROM palstaalue pa WHERE pa.id = ?";

    private final JdbcOperations jdbcOperations;

    public UpdatePalstaFeatureQueries(final JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public List<Integer> list(final long zoneId) {
        return jdbcOperations.queryForList(SELECT_SQL, Integer.class, zoneId);
    }

    public void insert(final long zoneId, final List<Integer> palstaIds) {
        executeUpdateForEachZonePalstaId(zoneId, palstaIds, INSERT_SQL);
    }

    public void removeAll(final long zoneId) {
        jdbcOperations.update(DELETE_ALL_SQL, zoneId);
    }

    public void removeById(final long zoneId, final List<Integer> toRemoveArray) {
        executeUpdateForEachZonePalstaId(zoneId, toRemoveArray, DELETE_SQL);
    }

    private void executeUpdateForEachZonePalstaId(final long zoneId, final List<Integer> palstaIds, final String sql) {
        jdbcOperations.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setLong(1, zoneId);
                ps.setInt(2, palstaIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return palstaIds.size();
            }
        });
    }

}
