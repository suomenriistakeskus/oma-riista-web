package fi.riista.integration.metsahallitus;

import au.com.bytecode.opencsv.CSVWriter;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.metsahallitus.MetsahallitusMaterialYear;
import fi.riista.util.DateUtil;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Types;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class MetsahallitusHarvestSummaryFeature {

    private static final int[] GAME_SPECIES_CODES_HIRVI = GameSpecies.ALL_GAME_SPECIES_CODES;
    private final static int[] GAME_SPECIES_CODES_PIENRIISTA = new int[]{
            26366, 27152, 27381, 27649, 27911, 37178, 37166, 37122, 27750, 27759, 200535, 33117, 27048, 26921, 26922,
            26931, 26926, 26928, 26298, 26291, 26373, 26360, 26382, 26388, 26394, 26407, 26415,
            26419, 26427, 26435, 26440, 26442, 26287, 50106, 50386, 50336, 47476, 47479, 47774, 53004, 48089, 48251,
            48250, 48537, 46542, 47507, 200556, 47329, 47230, 47240, 47223, 47243, 50114,
            46587, 46564, 47180, 47926
    };

    private final static String SQL_TEMPLATE = "WITH" +
            " meta AS (SELECT DISTINCT gid, koodi, nimi FROM @TABLE@ WHERE @TABLE@.vuosi = :materialYear)," +
            " data AS (SELECT @TABLE@_id AS gid, game_species_id, SUM(amount) AS lkm " +
            "   FROM harvest" +
            "   WHERE point_of_time BETWEEN :beginTime AND :endTime" +
            "   AND game_species_id IN (SELECT game_species_id FROM game_species WHERE official_code IN (:gameSpeciesCodes))" +
            "   GROUP BY 1, 2)" +
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

    @Resource
    private MetsahallitusMaterialYear metsahallitusMaterialYear;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_METSAHALLITUS_HARVEST')")
    public String getHirviSummary(final LocalDate startDate, final LocalDate endDate) {
        final int latestHirviYear = metsahallitusMaterialYear.getLatestHirviYear();
        return queryToCSV(createSqlParameters(startDate, endDate, GAME_SPECIES_CODES_HIRVI, latestHirviYear), SQL_REPORT_HIRVI);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_METSAHALLITUS_HARVEST')")
    public String getPienriistaSummary(LocalDate startDate, LocalDate endDate) {
        final int latestPienriistaYear = metsahallitusMaterialYear.getLatestPienriistaYear();
        return queryToCSV(createSqlParameters(startDate, endDate, GAME_SPECIES_CODES_PIENRIISTA, latestPienriistaYear), SQL_REPORT_PIENRIISTA);
    }

    private String queryToCSV(final SqlParameterSource queryParams, final String sql) {
        final int bufferSize = 16384;

        try (final StringWriter writer = new StringWriter(bufferSize);
             final CSVWriter csvWriter = new CSVWriter(writer, ';', CSVWriter.NO_QUOTE_CHARACTER)) {

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

    private static MapSqlParameterSource createSqlParameters(final @Nonnull LocalDate startDate,
                                                             final @Nonnull LocalDate endDate,
                                                             final int[] gameSpeciesCodes,
                                                             final int materialYear) {
        final Interval interval = DateUtil.createDateInterval(startDate, endDate);
        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("materialYear", materialYear);
        queryParams.addValue("gameSpeciesCodes", Arrays.stream(gameSpeciesCodes).boxed().collect(Collectors.toSet()));
        queryParams.addValue("beginTime", interval.getStart().toDate(), Types.TIMESTAMP);
        queryParams.addValue("endTime", interval.getEnd().toDate(), Types.TIMESTAMP);
        return queryParams;
    }
}
