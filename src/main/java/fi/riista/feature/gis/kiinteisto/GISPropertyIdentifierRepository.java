package fi.riista.feature.gis.kiinteisto;

import fi.riista.feature.gis.GISPoint;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class GISPropertyIdentifierRepository {
    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public List<MMLRekisteriyksikonTietoja> findIntersectingWithPoint(GISPoint gisPoint) {
        final String sql = "SELECT tunnus, ktunnus FROM palstaalue a " +
                "WHERE ST_Contains(a.geom, ST_GeomFromText(:point, 3067))";

        return jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("point", gisPoint.toWellKnownText()),
                (rs, rowNum) -> {
                    final long propertyNumber = rs.getLong(1);
                    final String municipalityCode = rs.getString(2);

                    final String propertyIdentifer = StringUtils.leftPad(Long.toString(propertyNumber), 14, '0');
                    return new MMLRekisteriyksikonTietoja(propertyIdentifer, municipalityCode);
                });
    }
}
