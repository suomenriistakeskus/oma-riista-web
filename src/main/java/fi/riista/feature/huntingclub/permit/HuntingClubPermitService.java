package fi.riista.feature.huntingclub.permit;

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary_;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.summary.HuntingEndStatus;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryRepository;
import fi.riista.feature.organization.Organisation_;
import fi.riista.sql.SQBasicClubHuntingSummary;
import fi.riista.sql.SQGameSpecies;
import fi.riista.sql.SQHarvestPermit;
import fi.riista.sql.SQHarvestPermitPartners;
import fi.riista.sql.SQHarvestPermitSpeciesAmount;
import fi.riista.sql.SQMooseHuntingSummary;
import fi.riista.sql.SQOrganisation;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.hasRelationWithId;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.data.jpa.domain.Specifications.where;

@Service
public class HuntingClubPermitService {

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepo;

    @Resource
    private BasicClubHuntingSummaryRepository basicSummaryRepo;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepo;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<BasicClubHuntingSummary> findModeratorOverriddenHuntingSummaries(
            @Nonnull final HarvestPermit permit, final int speciesCode) {

        final HarvestPermitSpeciesAmount speciesAmount =
                speciesAmountRepo.getOneByHarvestPermitAndSpeciesCode(permit, speciesCode);

        return basicSummaryRepo.findModeratorOverriddenHuntingSummaries(speciesAmount);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<BasicClubHuntingSummary> findBasicHuntingSummaries(
            @Nonnull final HarvestPermit permit, final int speciesCode) {

        final HarvestPermitSpeciesAmount speciesAmount =
                speciesAmountRepo.getOneByHarvestPermitAndSpeciesCode(permit, speciesCode);

        // For moose, require moderator-override for basic summaries.
        return GameSpecies.isMoose(speciesCode)
                ? basicSummaryRepo.findModeratorOverriddenHuntingSummaries(speciesAmount)
                : basicSummaryRepo.findBySpeciesAmount(speciesAmount);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<BasicClubHuntingSummary> findBasicHuntingSummary(
            @Nonnull final HarvestPermit permit, final long clubId, final int speciesCode) {

        final HarvestPermitSpeciesAmount speciesAmount =
                speciesAmountRepo.getOneByHarvestPermitAndSpeciesCode(permit, speciesCode);

        final Specification<BasicClubHuntingSummary> basicSummarySpec =
                where(equal(BasicClubHuntingSummary_.speciesAmount, speciesAmount))
                        .and(hasRelationWithId(BasicClubHuntingSummary_.club, Organisation_.id, clubId));

        // For moose, require moderator-override for basic summary.
        return Optional.ofNullable(basicSummaryRepo.findOne(GameSpecies.isMoose(speciesCode)
                ? where(basicSummarySpec).and(equal(BasicClubHuntingSummary_.moderatorOverride, true))
                : basicSummarySpec));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isRelatedToFinishedClubHunting(@Nonnull final GameDiaryEntry diaryEntry) {
        Objects.requireNonNull(diaryEntry);

        return diaryEntry.getHuntingClubGroup()
                .map(this::hasClubHuntingFinished)
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasClubHuntingFinished(@Nonnull final HuntingClubGroup group) {
        Objects.requireNonNull(group);

        return Optional.ofNullable(group.getHarvestPermit())
                .map(permit -> hasPartnerFinishedHunting(
                        permit, group.getParentOrganisation().getId(), group.getSpecies().getOfficialCode()))
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean hasPartnerFinishedHunting(
            @Nonnull final HarvestPermit permit, final long clubId, final int speciesCode) {

        Objects.requireNonNull(permit);

        final boolean isMoose = GameSpecies.isMoose(speciesCode);

        return findBasicHuntingSummary(permit, clubId, speciesCode)
                .map(BasicClubHuntingSummary::isHuntingFinished)
                .orElseGet(() -> isMoose && mooseHuntingSummaryRepo.findByClubIdAndPermitId(clubId, permit.getId())
                        .map(MooseHuntingSummary::isHuntingFinished)
                        .orElse(false));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean allPartnersFinishedHunting(@Nonnull final HarvestPermit permit, final int speciesCode) {
        Objects.requireNonNull(permit);

        final List<BasicClubHuntingSummary> basicSummaries = findBasicHuntingSummaries(permit, speciesCode);
        final Map<Long, BasicClubHuntingSummary> basicSummaryByClubId =
                F.index(basicSummaries, summary -> summary.getClub().getId());

        // Moose summaries are fetched only if species code is set to moose.
        final Stream<MooseHuntingSummary> mooseSummaries = GameSpecies.isMoose(speciesCode)
                ? mooseHuntingSummaryRepo.findByHarvestPermit(permit).stream()
                .filter(mooseSummary -> !basicSummaryByClubId.containsKey(mooseSummary.getClub().getId()))
                : Stream.empty();

        final Stream<? extends HuntingEndStatus> endStatuses = Stream.concat(basicSummaries.stream(), mooseSummaries);

        final Set<Long> idsOfFinishedPartners = endStatuses
                .filter(HuntingEndStatus::isHuntingFinished)
                .map(HuntingEndStatus::getClubId)
                .collect(toSet());

        return F.getUniqueIds(permit.getPermitPartners()).equals(idsOfFinishedPartners);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, ClubHuntingSummaryBasicInfoDTO> getHuntingSummaryBasicInfoGroupedByClubId(
            @Nonnull final HarvestPermit permit, final int speciesCode) {

        Objects.requireNonNull(permit);

        final boolean isMoose = GameSpecies.isMoose(speciesCode);

        final SQHarvestPermit harvestPermit = SQHarvestPermit.harvestPermit;
        final SQHarvestPermitSpeciesAmount speciesAmount = SQHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final SQGameSpecies gameSpecies = SQGameSpecies.gameSpecies;
        final SQHarvestPermitPartners partner = SQHarvestPermitPartners.harvestPermitPartners;
        final SQMooseHuntingSummary mooseSummary = SQMooseHuntingSummary.mooseHuntingSummary;
        final SQBasicClubHuntingSummary basicSummary = SQBasicClubHuntingSummary.basicClubHuntingSummary;
        final SQOrganisation huntingGroup = SQOrganisation.organisation;

        final SQLQuery<Long> mooseDataCardGroupIds = SQLExpressions.select(huntingGroup.organisationId)
                .from(huntingGroup)
                .where(huntingGroup.fromMooseDataCard.eq(true)
                        .and(huntingGroup.parentOrganisationId.eq(partner.organisationId))
                        .and(huntingGroup.harvestPermitId.eq(harvestPermit.harvestPermitId)));

        final Expression<Boolean> mooseDataCardGroupExists = isMoose
                ? new CaseBuilder().when(mooseDataCardGroupIds.exists()).then(true).otherwise(false)
                : Expressions.constant(false);

        // II IS IMPORTANT THAT BASIC SUMMARY IS PRIMARY DATA SOURCE (HENCE, COALESCED FIRST).
        // IN CASE OF MOOSE HUNTING MODERATOR MUST BE ALLOWED TO OVERRIDE DATA PROVIDED BY
        // CLUB ITSELF (THROUGH BASIC SUMMARY).

        final Coalesce<Boolean> huntingFinished = basicSummary.huntingFinished.coalesce(mooseSummary.huntingFinished);
        final Coalesce<Boolean> huntingFinishedByModeration = basicSummary.moderatorOverride.coalesce(false);
        final Coalesce<Date> huntingEndDate = basicSummary.huntingEndDate.coalesce(mooseSummary.huntingEndDate);

        final Coalesce<Integer> totalHuntingArea = basicSummary.totalHuntingArea.coalesce(mooseSummary.totalHuntingArea);
        final Coalesce<Integer> effectiveHuntingArea = basicSummary.effectiveHuntingArea
                .coalesce(mooseSummary.effectiveHuntingArea);
        final NumberExpression<Float> effectiveAreaPcnt = mooseSummary.effectiveHuntingAreaPercentage.floatValue();
        final Coalesce<Integer> remainingPopulationInTotalArea = basicSummary.remainingPopulationInTotalArea
                .coalesce(mooseSummary.moosesRemainingInTotalHuntingArea);
        final Coalesce<Integer> remainingPopulationInEffectiveArea = basicSummary.remainingPopulationInEffectiveArea
                .coalesce(mooseSummary.moosesRemainingInEffectiveHuntingArea);

        final List<ClubHuntingSummaryBasicInfoDTO> list = sqlQueryFactory
                .from(harvestPermit)
                .innerJoin(harvestPermit._harvestPermitSpeciesAmountPermitFk, speciesAmount)
                .innerJoin(speciesAmount.harvestPermitSpeciesAmountGameSpeciesFk, gameSpecies).on(
                        gameSpecies.officialCode.eq(speciesCode))
                .innerJoin(harvestPermit._harvestPermitPartnersHarvestPermitFk, partner)
                .leftJoin(speciesAmount._basicClubHuntingSummarySpeciesAmountFk, basicSummary).on(
                        basicSummary.clubId.eq(partner.organisationId).and(
                                isMoose ? basicSummary.moderatorOverride.eq(true) : null)
                )
                .leftJoin(harvestPermit._mooseHuntingSummaryPermitFk, mooseSummary).on(
                        mooseSummary.clubId.eq(partner.organisationId)
                                .and(ExpressionUtils.eqConst(gameSpecies.officialCode, GameSpecies.OFFICIAL_CODE_MOOSE))
                )
                .where(harvestPermit.harvestPermitId.eq(permit.getId()))
                .select(partner.organisationId,
                        huntingFinished,
                        huntingFinishedByModeration,
                        huntingEndDate,
                        mooseDataCardGroupExists,
                        totalHuntingArea,
                        effectiveHuntingArea,
                        effectiveAreaPcnt,
                        remainingPopulationInTotalArea,
                        remainingPopulationInEffectiveArea)
                .fetch()
                .stream()
                .map(tuple -> {
                    final Long clubId = tuple.get(partner.organisationId);
                    final boolean finished = Boolean.TRUE.equals(tuple.get(huntingFinished));
                    final boolean finishedByModeration = Boolean.TRUE.equals(tuple.get(huntingFinishedByModeration));

                    final ClubHuntingSummaryBasicInfoDTO dto = new ClubHuntingSummaryBasicInfoDTO();
                    dto.setClubId(clubId);
                    dto.setGameSpeciesCode(speciesCode);

                    dto.setHuntingFinished(finished);
                    dto.setHuntingEndDate(finished ? new LocalDate(tuple.get(huntingEndDate)) : null);
                    dto.setFromMooseDataCard(tuple.get(mooseDataCardGroupExists));
                    dto.setHuntingFinishedByModeration(finishedByModeration);

                    dto.setTotalHuntingArea(tuple.get(totalHuntingArea));
                    dto.setEffectiveHuntingArea(tuple.get(effectiveHuntingArea));

                    if (dto.getEffectiveHuntingArea() == null) {
                        dto.setEffectiveHuntingArea(ClubHuntingSummaryBasicInfoDTO
                                .calculatePercentageBasedEffectiveArea(dto.getTotalHuntingArea(), tuple.get(effectiveAreaPcnt)));
                    }

                    dto.setRemainingPopulationInTotalArea(tuple.get(remainingPopulationInTotalArea));
                    dto.setRemainingPopulationInEffectiveArea(tuple.get(remainingPopulationInEffectiveArea));

                    return dto;
                })
                .collect(toList());

        final Map<Long, ClubHuntingSummaryBasicInfoDTO> results =
                F.index(list, ClubHuntingSummaryBasicInfoDTO::getClubId);

        // Need to decorate results as the predicate for basic summary (that should be part of the
        // left-join in the above query) is included as where predicate (stemming from JPQL
        // inability).
        F.getUniqueIds(permit.getPermitPartners()).stream()
                .forEach(partnerId -> results.putIfAbsent(partnerId, new ClubHuntingSummaryBasicInfoDTO()));

        return results;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, HuntingClubPermitCountDTO> getHarvestCountsGroupedByClubId(
            @Nonnull final HarvestPermit permit, final int speciesCode) {

        final List<BasicClubHuntingSummary> moderatedBasicSummaries =
                findModeratorOverriddenHuntingSummaries(permit, speciesCode);

        final Map<Long, HuntingClubPermitCountDTO> moderatedHarvestCounts = moderatedBasicSummaries.stream().collect(toMap(
                summary -> summary.getClub().getId(),
                summary -> new HuntingClubPermitCountDTO(summary.getClub().getId(), summary.getHarvestCounts())));

        final Set<Long> idsOfNonModeratedPartners = permit.getPermitPartners().stream()
                .map(HasID::getId)
                .filter(partnerId -> !moderatedHarvestCounts.containsKey(partnerId))
                .collect(toSet());

        final Map<Long, HuntingClubPermitCountDTO> nonModeratedHarvestCounts = idsOfNonModeratedPartners.isEmpty()
                ? Collections.emptyMap()
                : calculateHarvests(permit, speciesCode, idsOfNonModeratedPartners);

        return ImmutableMap.<Long, HuntingClubPermitCountDTO> builder()
                .putAll(nonModeratedHarvestCounts)
                .putAll(moderatedHarvestCounts)
                .build();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, HuntingClubPermitCountDTO> calculateHarvests(
            final HarvestPermit permit, final int speciesCode, final Set<Long> clubIds) {

        if (clubIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // TODO Transform to QueryDSL native form.

        final String sql = "SELECT club.organisation_id,\n" +
                "  coalesce(sum(CASE WHEN (hs.age='ADULT' AND hs.gender='MALE')   THEN 1 END), 0) AS adult_males,\n" +
                "  coalesce(sum(CASE WHEN (hs.age='ADULT' AND hs.gender='FEMALE') THEN 1 END), 0) AS adult_females,\n" +
                "  coalesce(sum(CASE WHEN (hs.age='YOUNG' AND hs.gender='MALE')   THEN 1 END), 0) AS young_males,\n" +
                "  coalesce(sum(CASE WHEN (hs.age='YOUNG' AND hs.gender='FEMALE') THEN 1 END), 0) AS young_females,\n" +
                "  coalesce(sum(CASE WHEN (hs.age='ADULT' AND hs.gender='MALE'   AND hs.not_edible=true) THEN 1 END), 0) AS adult_males_not_edible,\n" +
                "  coalesce(sum(CASE WHEN (hs.age='ADULT' AND hs.gender='FEMALE' AND hs.not_edible=true) THEN 1 END), 0) AS adult_females_not_edible,\n" +
                "  coalesce(sum(CASE WHEN (hs.age='YOUNG' AND hs.gender='MALE'   AND hs.not_edible=true) THEN 1 END), 0) AS young_males_not_edible,\n" +
                "  coalesce(sum(CASE WHEN (hs.age='YOUNG' AND hs.gender='FEMALE' AND hs.not_edible=true) THEN 1 END), 0) AS young_females_not_edible\n" +
                "FROM organisation club\n" +
                "  JOIN organisation clubgroup ON (clubgroup.parent_organisation_id=club.organisation_id)\n" +
                "  JOIN harvest_permit hp ON (hp.harvest_permit_id=clubgroup.harvest_permit_id)\n" +
                "  JOIN harvest_permit_species_amount hpsa ON (hpsa.harvest_permit_id=hp.harvest_permit_id)\n" +
                "  JOIN game_species g ON (g.game_species_id=hpsa.game_species_id and g.official_code=:speciesCode)\n" +
                "  LEFT JOIN group_hunting_day ghd ON (ghd.hunting_group_id=clubgroup.organisation_id)\n" +
                "  LEFT JOIN harvest h ON (h.group_hunting_day_id=ghd.group_hunting_day_id AND h.game_species_id=hpsa.game_species_id)\n" +
                "  LEFT JOIN harvest_specimen hs ON (hs.harvest_id=h.harvest_id)\n" +
                "WHERE hp.harvest_permit_id=:permitId\n" +
                "  AND club.organisation_id IN (:clubIds)\n" +
                "GROUP BY club.organisation_id\n" +
                "ORDER BY club.organisation_id;";

        final MapSqlParameterSource queryParams = new MapSqlParameterSource();
        queryParams.addValue("permitId", permit.getId());
        queryParams.addValue("speciesCode", speciesCode);
        queryParams.addValue("clubIds", clubIds);

        final List<HuntingClubPermitCountDTO> res =
                jdbcTemplate.query(sql, queryParams, (rs, rowNum) -> {
                    final long organisationId = rs.getLong("organisation_id");

                    final int adultMales = rs.getInt("adult_males");
                    final int adultFemales = rs.getInt("adult_females");
                    final int youngMales = rs.getInt("young_males");
                    final int youngFemales = rs.getInt("young_females");

                    final int adultMalesNotEdible = rs.getInt("adult_males_not_edible");
                    final int adultFemalesNotEdible = rs.getInt("adult_females_not_edible");
                    final int youngMalesNotEdible = rs.getInt("young_males_not_edible");
                    final int youngFemalesNotEdible = rs.getInt("young_females_not_edible");

                    return new HuntingClubPermitCountDTO(
                            organisationId,
                            adultMales,
                            adultFemales,
                            youngMales,
                            youngFemales,
                            adultMalesNotEdible + adultFemalesNotEdible,
                            youngMalesNotEdible + youngFemalesNotEdible,
                            adultMalesNotEdible,
                            adultFemalesNotEdible,
                            youngMalesNotEdible,
                            youngFemalesNotEdible);
                });

        final Map<Long, HuntingClubPermitCountDTO> map = F.index(res, HuntingClubPermitCountDTO::getHuntingClubId);

        // Since query does not return anything for those who have no harvests, add missing ones here
        clubIds.stream()
                .filter(id -> !map.containsKey(id))
                .forEach(id -> map.put(id, new HuntingClubPermitCountDTO(id, HasHarvestCountsForPermit.zeros())));
        return map;
    }

}
