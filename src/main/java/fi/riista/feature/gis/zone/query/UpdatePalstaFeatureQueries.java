package fi.riista.feature.gis.zone.query;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdatePalstaFeatureQueries {
    private final JdbcOperations jdbcOperations;

    public UpdatePalstaFeatureQueries(final JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }


    public void updateZonePalstaList(final long zoneId, final int[] boundaryIds) {
        final String insertSql = "INSERT INTO zone_palsta (zone_id, palsta_id, geom, palsta_tunnus)" +
                " SELECT ?, pa.id, pa.geom, pa.tunnus FROM palstaalue pa WHERE pa.id = ?";

        jdbcOperations.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                final Integer palstaId = boundaryIds[i];

                ps.setLong(1, zoneId);
                ps.setInt(2, palstaId);
            }

            @Override
            public int getBatchSize() {
                return boundaryIds.length;
            }
        });
    }

    public void removeZonePalsta(final long zoneId) {
        jdbcOperations.update("DELETE FROM zone_palsta WHERE zone_id = ?", zoneId);
    }

    public void removeZonePalsta(final long zoneId, final int[] toRemoveArray) {
        final String deleteSql = "DELETE FROM zone_palsta WHERE zone_id = ? AND palsta_id = ?";

        jdbcOperations.batchUpdate(deleteSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setLong(1, zoneId);
                ps.setInt(2, toRemoveArray[i]);
            }

            @Override
            public int getBatchSize() {
                return toRemoveArray.length;
            }
        });
    }


}
