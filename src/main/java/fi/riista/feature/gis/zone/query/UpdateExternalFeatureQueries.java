package fi.riista.feature.gis.zone.query;

import fi.riista.feature.huntingclub.area.zone.HuntingClubAreaFeatureDTO;
import fi.riista.util.GISUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UpdateExternalFeatureQueries {

    private final JdbcOperations jdbcOperations;

    public UpdateExternalFeatureQueries(final JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void insertZoneFeatures(final long zoneId,
                                   final GISUtils.SRID crs,
                                   final List<HuntingClubAreaFeatureDTO> featureCollection) {
        final String insertSql = "INSERT INTO zone_feature(zone_id, property_identifier, included_species, geom)" +
                " VALUES (?, ?, ?, ST_Buffer(ST_Transform(ST_GeomFromText(?, ?), 3067), 0))";

        jdbcOperations.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                final HuntingClubAreaFeatureDTO feature = featureCollection.get(i);
                ps.setLong(1, zoneId);
                ps.setLong(2, Long.parseLong(feature.getPropertyIdentifier()));
                if (feature.getValidSpecies().isEmpty()) {
                    ps.setArray(3, null);
                } else {
                    ps.setArray(3, ps.getConnection().createArrayOf("int4", feature.getValidSpecies().toArray()));
                }
                ps.setString(4, feature.getGeometry().toText());
                ps.setInt(5, crs.getValue());
            }

            @Override
            public int getBatchSize() {
                return featureCollection.size();
            }
        });
    }

    public void removeZoneFeatures(final long zoneId) {
        jdbcOperations.update("DELETE FROM zone_feature WHERE zone_id = ?", zoneId);
    }

}
