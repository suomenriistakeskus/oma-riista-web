package fi.riista.feature.pub.statistics;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PublicHarvestPivotTableFeature {

    private static final LocalisedString RIISTAKESKUS_NAME = LocalisedString.of("Suomi", "Finland");

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private OrganisationRepository organisationRepository;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public PivotTable summary(
            final Integer gameAnimalOfficialCode, final LocalDate startDate, final LocalDate endDate) {

        final List<PivotTableRow> rows = executeCountQuery(
                DateUtil.createDateInterval(startDate, endDate),
                Optional.ofNullable(getSpecies(gameAnimalOfficialCode)),
                Optional.<Organisation>empty());

        final PivotTableRow grandTotal =
                PivotTableRow.createGrandTotal(rows, Riistakeskus.OFFICIAL_CODE, RIISTAKESKUS_NAME);

        return new PivotTable(rows, grandTotal);
    }

    @Transactional(readOnly = true)
    public PivotTable summaryForRka(final Integer gameAnimalOfficialCode,
                                    final String rkaOfficialCode,
                                    final LocalDate startDate,
                                    final LocalDate endDate) {

        final Organisation rka =
                organisationRepository.findByTypeAndOfficialCode(OrganisationType.RKA, rkaOfficialCode);

        if (rka == null) {
            throw new NotFoundException("No such RKA");
        }

        final List<PivotTableRow> rows = executeCountQuery(
                DateUtil.createDateInterval(startDate, endDate),
                Optional.ofNullable(getSpecies(gameAnimalOfficialCode)),
                Optional.of(rka));

        final PivotTableRow grandTotal =
                PivotTableRow.createGrandTotal(rows, rkaOfficialCode, rka.getNameLocalisation());

        return new PivotTable(rows, grandTotal);
    }

    private GameSpecies getSpecies(final Integer gameAnimalOfficialCode) {
        return gameAnimalOfficialCode != null
                ? gameSpeciesService.requireByOfficialCode(gameAnimalOfficialCode)
                : null;
    }

    private List<PivotTableRow> executeCountQuery(
            final Interval interval, final Optional<GameSpecies> gameSpeciesOpt, final Optional<Organisation> rkaOpt) {

        final String queryStr = nativeQueryForHarvestCounts(gameSpeciesOpt.isPresent(), rkaOpt.isPresent());

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("beginTime", interval.getStart().toDate(), Types.TIMESTAMP);
        queryParams.addValue("endTime", interval.getEnd().toDate(), Types.TIMESTAMP);

        if (gameSpeciesOpt.isPresent()) {
            queryParams.addValue("gameSpeciesId", gameSpeciesOpt.get().getId());
        }
        if (rkaOpt.isPresent()) {
            queryParams.addValue("rkaId", rkaOpt.get().getId());
        }

        return jdbcTemplate.query(queryStr, queryParams, (resultSet, i) -> new PivotTableRow(resultSet));
    }

    private static String nativeQueryForHarvestCounts(final boolean withSpeciesRestriction, final boolean withRkaId) {

        final String groupingOrgAlias = withRkaId ? "rhy" : "rka";

        return "SELECT \n" +
                "  " + groupingOrgAlias + ".official_code AS official_code, \n" +
                "  " + groupingOrgAlias + ".name_finnish AS name_finnish, \n" +
                "  " + groupingOrgAlias + ".name_swedish AS name_swedish, \n " +
                "  sum(h1.amount) AS total, \n" +
                "  sum(h1.num_adults) AS num_adults, \n" +
                "  sum(h1.num_youngs) AS num_youngs, \n" +
                "  sum(h1.amount - h1.num_adults - h1.num_youngs) AS num_unknown_ages, \n" +
                "  sum(h1.num_males) AS num_males, \n" +
                "  sum(h1.num_females) AS num_females, \n" +
                "  sum(h1.amount - h1.num_males - h1.num_females) AS num_unknown_genders \n" +
                "FROM ( \n" +
                "  SELECT \n" +
                "    h.harvest_id AS harvest_id, \n" +
                "    min(h.amount) AS amount, \n" +
                "    sum(CASE WHEN hs.age = 'ADULT' THEN 1 ELSE 0 END) AS num_adults, \n" +
                "    sum(CASE WHEN hs.age = 'YOUNG' THEN 1 ELSE 0 END) AS num_youngs, \n" +
                "    sum(CASE WHEN hs.gender = 'MALE' THEN 1 ELSE 0 END) AS num_males, \n" +
                "    sum(CASE WHEN hs.gender = 'FEMALE' THEN 1 ELSE 0 END) AS num_females \n" +
                "  FROM harvest h \n" +
                "  LEFT OUTER JOIN harvest_specimen hs ON hs.harvest_id = h.harvest_id \n" +
                (withRkaId ? "  INNER JOIN organisation rhy ON rhy.organisation_id = h.rhy_id \n" : "") +
                "  WHERE " + (withRkaId ? "rhy.parent_organisation_id = :rkaId \n" : "h.rhy_id IS NOT null \n") +
                (withSpeciesRestriction ? "    AND h.game_species_id = :gameSpeciesId \n" : "") +
                "    AND ( \n" +
                "      h.point_of_time IS NOT null \n" +
                "      AND h.point_of_time >= :beginTime \n" +
                "      AND h.point_of_time < :endTime \n" +
                "    ) \n" +
                "  GROUP BY h.harvest_id \n" +
                ") h1 \n" +
                "INNER JOIN harvest h ON h.harvest_id = h1.harvest_id \n" +
                "INNER JOIN organisation rhy ON rhy.organisation_id = h.rhy_id \n" +
                (withRkaId ? "" : "INNER JOIN organisation rka ON rka.organisation_id = rhy.parent_organisation_id \n") +
                "GROUP BY " +
                groupingOrgAlias + ".official_code, " +
                groupingOrgAlias + ".name_finnish, " +
                groupingOrgAlias + ".name_swedish \n" +
                "ORDER BY " + groupingOrgAlias + ".official_code \n";
    }

    // Result wrapper
    public static class PivotTable {
        private final List<PivotTableRow> data;
        private final PivotTableRow grandTotal;

        public PivotTable(List<PivotTableRow> data, PivotTableRow grandTotal) {
            this.data = data;
            this.grandTotal = grandTotal;
        }

        public List<PivotTableRow> getData() {
            return data;
        }

        public PivotTableRow getGrandTotal() {
            return grandTotal;
        }
    }

    // Result row
    public static class PivotTableRow {
        private final String officialCode;
        private final long genderMale;
        private final long genderFemale;
        private final long genderUnknown;
        private final long ageAdult;
        private final long ageYoung;
        private final long ageUnknown;
        private final long total;
        private final String nameFinnish;
        private final String nameSwedish;

        static PivotTableRow createGrandTotal(
                List<PivotTableRow> rows, String officialCode, LocalisedString localisedName) {

            Objects.requireNonNull(officialCode);
            Objects.requireNonNull(rows);
            Objects.requireNonNull(localisedName);

            return new PivotTableRow(rows, officialCode, localisedName);
        }

        private PivotTableRow(final ResultSet rs) throws SQLException {
            this.officialCode = rs.getString("official_code");
            this.total = rs.getLong("total");
            this.ageAdult = rs.getLong("num_adults");
            this.ageYoung = rs.getLong("num_youngs");
            this.ageUnknown = rs.getLong("num_unknown_ages");
            this.genderMale = rs.getLong("num_males");
            this.genderFemale = rs.getLong("num_females");
            this.genderUnknown = rs.getLong("num_unknown_genders");
            this.nameFinnish = rs.getString("name_finnish");
            this.nameSwedish = rs.getString("name_swedish");
        }

        private PivotTableRow(List<PivotTableRow> rows, String officialCode, LocalisedString localisedString) {
            long genderMale = 0;
            long genderFemale = 0;
            long genderUnknown = 0;
            long ageAdult = 0;
            long ageYoung = 0;
            long ageUnknown = 0;
            long total = 0;

            for (PivotTableRow row : rows) {
                genderMale += row.genderMale;
                genderFemale += row.genderFemale;
                genderUnknown += row.genderUnknown;
                ageAdult += row.ageAdult;
                ageYoung += row.ageYoung;
                ageUnknown += row.ageUnknown;
                total += row.total;
            }

            this.genderMale = genderMale;
            this.genderFemale = genderFemale;
            this.genderUnknown = genderUnknown;
            this.ageAdult = ageAdult;
            this.ageYoung = ageYoung;
            this.ageUnknown = ageUnknown;
            this.total = total;
            this.officialCode = officialCode;
            this.nameFinnish = Objects.requireNonNull(localisedString.getFinnish());
            this.nameSwedish = Objects.requireNonNull(localisedString.getSwedish());
        }

        public String getOfficialCode() {
            return officialCode;
        }

        public long getGenderMale() {
            return genderMale;
        }

        public long getGenderFemale() {
            return genderFemale;
        }

        public long getGenderUnknown() {
            return genderUnknown;
        }

        public long getAgeAdult() {
            return ageAdult;
        }

        public long getAgeYoung() {
            return ageYoung;
        }

        public long getAgeUnknown() {
            return ageUnknown;
        }

        public long getTotal() {
            return total;
        }

        public String getNameFinnish() {
            return nameFinnish;
        }

        public String getNameSwedish() {
            return nameSwedish;
        }
    }

}
