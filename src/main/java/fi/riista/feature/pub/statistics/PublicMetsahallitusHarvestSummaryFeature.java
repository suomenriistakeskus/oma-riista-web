package fi.riista.feature.pub.statistics;

import au.com.bytecode.opencsv.CSVWriter;
import fi.riista.util.DateUtil;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Types;

@Component
public class PublicMetsahallitusHarvestSummaryFeature {
    private final static String SQL_TEMPLATE = "WITH" +
            " meta AS (SELECT DISTINCT gid, koodi, nimi FROM @TABLE@)," +
            " data AS (SELECT @TABLE@_id AS gid, game_species_id, SUM(amount) AS lkm " +
            "   FROM harvest WHERE @TABLE@_id IS NOT NULL " +
            "   AND point_of_time BETWEEN :beginTime AND :endTime GROUP BY 1, 2)" +
            " SELECT" +
            " data.lkm AS total_amount, " +
            " meta.koodi AS area_code, " +
            " meta.nimi AS area_name, " +
            " game_species.official_code AS species_official_code, " +
            " game_species.name_finnish AS species_name_finnish" +
            " FROM data" +
            "  JOIN meta USING (gid)" +
            "  JOIN game_species USING (game_species_id)" +
            " ORDER BY 1 DESC";

    private final static String SQL_REPORT_HIRVI;
    private final static String SQL_REPORT_PIENRIISTA;

    static {
        SQL_REPORT_HIRVI = SQL_TEMPLATE.replaceAll("@TABLE@", "mh_hirvi");
        SQL_REPORT_PIENRIISTA = SQL_TEMPLATE.replaceAll("@TABLE@", "mh_pienriista");
    }

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public String hirviSummary(final LocalDate startDate, final LocalDate endDate) {
        return queryToCSV(createSqlParameters(startDate, endDate), SQL_REPORT_HIRVI);
    }

    @Transactional(readOnly = true)
    public Object pienriistaSummary(LocalDate startDate, LocalDate endDate) {
        return queryToCSV(createSqlParameters(startDate, endDate), SQL_REPORT_PIENRIISTA);
    }

    private String queryToCSV(final SqlParameterSource queryParams, final String sql) {
        final int bufferSize = 16384;

        try (final StringWriter writer = new StringWriter(bufferSize);
                final CSVWriter csvWriter = new CSVWriter(writer, ';')) {

            jdbcTemplate.query(sql, queryParams, resultSet -> {
                try {
                    csvWriter.writeAll(resultSet, true);
                    csvWriter.flush();

                    return null;

                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            });

            return writer.getBuffer().toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MapSqlParameterSource createSqlParameters(LocalDate startDate, LocalDate endDate) {
        final Interval interval = DateUtil.createDateInterval(startDate, endDate);
        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("beginTime", interval.getStart().toDate(), Types.TIMESTAMP);
        queryParams.addValue("endTime", interval.getEnd().toDate(), Types.TIMESTAMP);
        return queryParams;
    }
}
