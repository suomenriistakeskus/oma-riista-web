package fi.riista.feature.harvestpermit.season;

import fi.riista.config.properties.DataSourceProperties;
import fi.riista.feature.gis.GISPoint;
import fi.riista.util.GISUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
public class GISHarvestAreaRepository {

    @Resource
    private DataSourceProperties dataSourceProperties;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public List<Long> queryAreaIdByPoint(final HarvestArea.HarvestAreaType areaType, final GISPoint gisPoint) {
        Objects.requireNonNull(areaType);
        Objects.requireNonNull(gisPoint);

        if (!dataSourceProperties.isGisQuerySupported()) {
            return Collections.emptyList();
        }

        final String sql = "SELECT harvest_area_id" +
                " FROM harvest_area WHERE ST_Contains(geom, ST_GeomFromText(?, ?)) AND type = ?;";

        return jdbcTemplate.query(
                sql,
                new Object[]{gisPoint.toWellKnownText(), GISUtils.SRID.ETRS_TM35FIN.value, areaType.name()},
                (resultSet, i) -> resultSet.getLong("harvest_area_id"));
    }

}
