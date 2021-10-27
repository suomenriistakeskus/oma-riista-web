package fi.riista.feature.otherwisedeceased;

import fi.riista.util.F;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class OtherwiseDeceasedRepositoryImpl implements OtherwiseDeceasedRepositoryCustom {

    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<Long> findReindeerAreaLocated(Collection<OtherwiseDeceased> otherwiseDeceasedCollection) {
        if (otherwiseDeceasedCollection.isEmpty()) {
            return Collections.emptyList();
        }

        final Set<Long> otherwiseDeceasedIds = F.getUniqueIds(otherwiseDeceasedCollection);

        final String sql = "SELECT deceased.otherwise_deceased_id " +
                "FROM otherwise_deceased deceased " +
                "JOIN harvest_area area " +
                "ON ST_Contains(area.geom, ST_SetSRID(ST_MakePoint(deceased.longitude, deceased.latitude), 3067)) " +
                "WHERE deceased.otherwise_deceased_id in (:otherwiseDeceasedIds) " +
                "AND area.type = 'PORONHOITOALUE'";

        return jdbcTemplate.query(sql,
                new MapSqlParameterSource("otherwiseDeceasedIds", otherwiseDeceasedIds),
                (rs, rowNum) -> rs.getLong("otherwise_deceased_id"));
    }

}
