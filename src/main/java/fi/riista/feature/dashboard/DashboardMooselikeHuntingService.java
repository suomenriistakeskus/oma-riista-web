package fi.riista.feature.dashboard;

import com.google.common.base.Joiner;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.sql.SQBasicClubHuntingSummary;
import fi.riista.sql.SQGameObservation;
import fi.riista.sql.SQGroupHuntingDay;
import fi.riista.sql.SQHarvest;
import fi.riista.sql.SQHarvestPermitSpeciesAmount;
import fi.riista.sql.SQOrganisation;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.util.Collect.indexingBy;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Service
public class DashboardMooselikeHuntingService {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<DashboardMooselikeHuntingDTO> computeMooselikeHuntingMetrics() {
        final int huntingYear = DateUtil.huntingYear();
        final List<GameSpecies> speciesList = listMooseLikeSpeciesRequiringPermit();
        final Set<Long> mooseSpeciesIds = F.getUniqueIds(speciesList);

        final Map<Long, Integer> harvestCounts = countHarvest(huntingYear, mooseSpeciesIds);
        final Map<Long, Integer> observationCounts = countObservations(huntingYear, mooseSpeciesIds);
        final Map<Long, Integer> moderatedHarvests = countModeratedHarvests(huntingYear, mooseSpeciesIds);
        final Map<Long, DashboardMooselikeHuntingDTO> permitMetrics = countPermitsAndMooseDataCardGroups(huntingYear);

        return speciesList.stream()
                .map(gameSpecies -> {
                    final DashboardMooselikeHuntingDTO dto = new DashboardMooselikeHuntingDTO();
                    dto.setSpeciesName(gameSpecies.getNameFinnish());

                    final long speciesId = gameSpecies.getId();
                    dto.setHarvestCount(harvestCounts.getOrDefault(speciesId, 0) +
                            moderatedHarvests.getOrDefault(speciesId, 0));
                    dto.setObservationCount(observationCounts.getOrDefault(speciesId, 0));

                    final DashboardMooselikeHuntingDTO permitCounts = permitMetrics.get(speciesId);

                    if (permitCounts != null) {
                        dto.setOpenPermitCount(permitCounts.getOpenPermitCount());
                        dto.setClosedPermitCount(permitCounts.getClosedPermitCount());
                        dto.setModeratorClosedPermitCount(permitCounts.getModeratorClosedPermitCount());
                        dto.setMooseDataCardGroupCount(permitCounts.getMooseDataCardGroupCount());
                    }

                    return dto;
                })
                .sorted(comparing(DashboardMooselikeHuntingDTO::getSpeciesName))
                .collect(toList());
    }

    private List<GameSpecies> listMooseLikeSpeciesRequiringPermit() {
        final QGameSpecies species = QGameSpecies.gameSpecies;

        return queryFactory.selectFrom(species)
                .where(species.officialCode.in(GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING))
                .fetch();
    }

    private Map<Long, Integer> countHarvest(final int huntingYear,
                                            final Collection<Long> includedGameSpeciesIds) {
        final SQHarvest harvest = SQHarvest.harvest;

        final Expression<Long> keyColumn = harvest.gameSpeciesId;
        final Expression<Integer> valueColumn = harvest.amount.sum();

        return sqlQueryFactory
                .select(keyColumn, valueColumn)
                .from(harvest)
                .where(sqlDateTimeInsideHuntingYear(harvest.pointOfTime, huntingYear),
                        harvest.gameSpeciesId.in(includedGameSpeciesIds),
                        harvest.groupHuntingDayId.isNotNull(),
                        harvest.groupHuntingDayId.notIn(
                                huntingDaysOfModeratedGroups(includedGameSpeciesIds, huntingYear)))
                .groupBy(keyColumn)
                .transform(GroupBy.groupBy(keyColumn).as(valueColumn));
    }

    private Map<Long, Integer> countObservations(final int huntingYear,
                                                 final Collection<Long> includedGameSpeciesIds) {
        final SQGameObservation observation = SQGameObservation.gameObservation;

        final Expression<Long> keyColumn = observation.gameSpeciesId;
        final Expression<Integer> valueColumn = observation.amount.sum();

        return sqlQueryFactory
                .select(keyColumn, valueColumn)
                .from(observation)
                .where(sqlDateTimeInsideHuntingYear(observation.pointOfTime, huntingYear),
                        observation.gameSpeciesId.in(includedGameSpeciesIds),
                        observation.groupHuntingDayId.isNotNull(),
                        observation.groupHuntingDayId.notIn(
                                huntingDaysOfModeratedGroups(includedGameSpeciesIds, huntingYear)))
                .groupBy(keyColumn)
                .transform(GroupBy.groupBy(keyColumn).as(valueColumn));
    }

    private static SQLQuery<Long> huntingDaysOfModeratedGroups(final Collection<Long> includedGameSpeciesIds,
                                                               final int huntingYear) {
        final SQGroupHuntingDay groupHuntingDay = SQGroupHuntingDay.groupHuntingDay;

        return SQLExpressions
                .select(groupHuntingDay.groupHuntingDayId)
                .from(groupHuntingDay)
                .where(groupHuntingDay.huntingGroupId.in(moderatedGroups(includedGameSpeciesIds, huntingYear)));
    }

    private static SQLQuery<Long> moderatedGroups(final Collection<Long> includedGameSpeciesIds,
                                                  final int huntingYear) {

        final SQBasicClubHuntingSummary bchs = new SQBasicClubHuntingSummary("bchs");
        final SQHarvestPermitSpeciesAmount hpsa = new SQHarvestPermitSpeciesAmount("hpsa");
        final SQOrganisation group = new SQOrganisation("group");

        return SQLExpressions
                .select(group.organisationId)
                .from(bchs)
                .join(hpsa).on(hpsa.harvestPermitSpeciesAmountId.eq(bchs.speciesAmountId))
                .join(group).on(
                        group.parentOrganisationId.eq(bchs.clubId),
                        group.organisationType.eq(Expressions.asString(OrganisationType.CLUBGROUP.name())),
                        group.gameSpeciesId.eq(hpsa.gameSpeciesId),
                        group.harvestPermitId.eq(hpsa.harvestPermitId))
                .where(hpsa.gameSpeciesId.in(includedGameSpeciesIds),
                        bchs.moderatorOverride.isTrue(),
                        bchs.huntingFinished.isTrue(),
                        sqlDateInsideHuntingYear(bchs.huntingEndDate, huntingYear),
                        group.huntingYear.eq(huntingYear));
    }

    private Map<Long, Integer> countModeratedHarvests(final int huntingYear,
                                                      final Collection<Long> includedGameSpeciesIds) {
        final SQBasicClubHuntingSummary bchs = new SQBasicClubHuntingSummary("bchs");
        final SQHarvestPermitSpeciesAmount hpsa = new SQHarvestPermitSpeciesAmount("hpsa");

        final Expression<Long> keyColumn = hpsa.gameSpeciesId;
        final Expression<Integer> valueColumn = bchs.numberOfAdultMales.sum()
                .add(bchs.numberOfAdultFemales.sum())
                .add(bchs.numberOfYoungMales.sum())
                .add(bchs.numberOfYoungFemales.sum()).as("total_amount");

        return sqlQueryFactory
                .select(keyColumn, valueColumn)
                .from(bchs)
                .join(hpsa).on(hpsa.harvestPermitSpeciesAmountId.eq(bchs.speciesAmountId))
                .where(hpsa.gameSpeciesId.in(includedGameSpeciesIds),
                        bchs.moderatorOverride.isTrue(),
                        bchs.huntingFinished.isTrue(),
                        sqlDateInsideHuntingYear(bchs.huntingEndDate, huntingYear))
                .groupBy(keyColumn)
                .transform(GroupBy.groupBy(keyColumn).as(valueColumn));
    }

    private static BooleanExpression sqlDateInsideHuntingYear(final DatePath<Date> path,
                                                              final int huntingYear) {
        return path.between(
                new java.sql.Date(DateUtil.huntingYearBeginDate(huntingYear).toDate().getTime()),
                new java.sql.Date(DateUtil.huntingYearEndDate(huntingYear).toDate().getTime()));
    }

    private static BooleanExpression sqlDateTimeInsideHuntingYear(final DateTimePath<Timestamp> path,
                                                                  final int huntingYear) {
        final Interval interval = DateUtil.huntingYearInterval(huntingYear);
        return path.between(new Timestamp(interval.getStartMillis()), new Timestamp(interval.getEndMillis()));
    }

    private Map<Long, DashboardMooselikeHuntingDTO> countPermitsAndMooseDataCardGroups(final int huntingYear) {

        final String mooselikeIds = Joiner.on(", ").join(GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING);

        final String queryStr = "SELECT \n" +
                "  gs.game_species_id                      AS species_id, \n" +
                "  count(t.permitId) - sum(t.closedReport) AS open_permit_count, \n" +
                "  sum(t.closedReport)                     AS closed_permit_count, \n" +
                "  sum(t.moderatedReport)                  AS moderator_closed_permit_count, \n" +
                "  sum(u.mooseDataCardGroupCount)          AS moose_data_card_group_count \n" +
                "FROM game_species gs \n" +
                "INNER JOIN ( \n" +
                "  SELECT \n" +
                "    hp.harvest_permit_id                                                AS permitId, \n" +
                "    hpsa.game_species_id                                                AS speciesId, \n" +
                "    CASE WHEN mhr.moose_harvest_report_id IS NOT NULL THEN 1 ELSE 0 END AS closedReport, \n" +
                "    CASE WHEN mhr.moderator_override = true THEN 1 ELSE 0 END AS moderatedReport \n" +
                "  FROM harvest_permit_species_amount hpsa \n" +
                "  INNER JOIN harvest_permit hp ON hp.harvest_permit_id = hpsa.harvest_permit_id \n" +
                "  LEFT OUTER JOIN moose_harvest_report mhr ON mhr.species_amount_id = hpsa.harvest_permit_species_amount_id \n" +
                "  WHERE \n" +
                "    hp.permit_type_code = '" + HarvestPermit.MOOSELIKE_PERMIT_TYPE + "' \n" +
                "    AND ( \n" +
                "      hpsa.begin_date >= :beginDate AND hpsa.end_date <= :endDate \n" +
                "      OR hpsa.begin_date2 IS NOT NULL AND hpsa.begin_date2 >= :beginDate \n" +
                "        AND hpsa.end_date2 IS NOT NULL AND hpsa.end_date2 <= :endDate \n" +
                "    ) \n" +
                ") t ON t.speciesId = gs.game_species_id \n" +
                "LEFT OUTER JOIN ( \n" +
                "  SELECT \n" +
                "    harvest_permit_id      AS permitId, \n" +
                "    game_species_id        AS speciesId, \n" +
                "    count(organisation_id) AS mooseDataCardGroupCount \n" +
                "  FROM organisation \n" +
                "  WHERE \n" +
                "    organisation_type = '" + OrganisationType.CLUBGROUP.name() + "' \n" +
                "    AND from_moose_data_card = true \n" +
                "    AND harvest_permit_id IS NOT NULL \n" +
                "  GROUP BY harvest_permit_id, game_species_id \n" +
                ") u ON u.speciesId = gs.game_species_id AND u.permitId = t.permitId \n" +
                "WHERE gs.official_code IN (" + mooselikeIds + ") \n" +
                "GROUP BY gs.game_species_id \n" +
                "ORDER BY gs.game_species_id \n";

        final Interval interval = DateUtil.huntingYearInterval(huntingYear);
        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("beginDate", interval.getStart().toDate(), Types.DATE);
        queryParams.addValue("endDate", interval.getEnd().toDate(), Types.DATE);

        return namedParameterJdbcTemplate.query(queryStr, queryParams, (resultSet, i) -> {
            final DashboardMooselikeHuntingDTO dto = new DashboardMooselikeHuntingDTO();
            dto.setSpeciesId(resultSet.getLong("species_id"));
            dto.setOpenPermitCount(resultSet.getInt("open_permit_count"));
            dto.setClosedPermitCount(resultSet.getInt("closed_permit_count"));
            dto.setModeratorClosedPermitCount(resultSet.getInt("moderator_closed_permit_count"));

            final int mooseDataCardGroupCount = resultSet.getInt("moose_data_card_group_count");
            dto.setMooseDataCardGroupCount(resultSet.wasNull() ? null : mooseDataCardGroupCount);

            return dto;
        }).stream().collect(indexingBy(DashboardMooselikeHuntingDTO::getSpeciesId));
    }
}
