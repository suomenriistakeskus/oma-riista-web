package fi.riista.feature.huntingclub.permit.summary;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.endofhunting.QMooseHarvestReport;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static java.util.stream.Collectors.toList;

@Component
public class MooseHuntingSummaryDTOTransformer extends ListTransformer<MooseHuntingSummary, MooseHuntingSummaryDTO> {

    @Resource
    protected ActiveUserService activeUserService;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Nonnull
    @Override
    protected List<MooseHuntingSummaryDTO> transform(final @Nonnull List<MooseHuntingSummary> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final Function<MooseHuntingSummary, HarvestPermit> getHarvestPermit = getSummaryToPermitMapping(list);
        final Function<MooseHuntingSummary, HuntingClub> getClub = getSummaryToClubMapping(list);
        final List<SummaryPermitClubGroupFromMooseDataCard> groupFromMooseDataCardCount = summaryPermitClubGroupFromMooseDataCardCount(list);
        final List<Long> permitsHavingMooseHarvestReport = listPermitsHavingMooseHarvestReport(list);

        final boolean isModerator = activeUserService.isModeratorOrAdmin();

        return list.stream().map(summary -> {
            final HarvestPermit permit = getHarvestPermit.apply(summary);
            final HuntingClub club = getClub.apply(summary);

            final boolean fromMooseDataCard = isFromMooseDataCard(summary, permit, club, groupFromMooseDataCardCount);
            final boolean reportDone = permitsHavingMooseHarvestReport.contains(permit.getId());
            final boolean locked = !isModerator && fromMooseDataCard || reportDone || !hasPermissionToEdit(summary)
                    || !isModerator && isPermitLockedByDate(permit);

            return create(summary, locked, permit.getPermitAreaSize());
        }).collect(toList());
    }

    private Boolean isPermitLockedByDate(final HarvestPermit permit) {
        return harvestPermitSpeciesAmountRepository.findByHarvestPermitAndSpeciesCode(permit, OFFICIAL_CODE_MOOSE)
                .stream()
                .map(Has2BeginEndDates::resolveHuntingYear)
                .map(huntingYear -> harvestPermitLockedByDateService.isPermitLockedByDateForHuntingYear(permit, huntingYear))
                .anyMatch(isLocked -> isLocked);
    }

    private boolean hasPermissionToEdit(final MooseHuntingSummary summary) {
        return activeUserService.checkHasPermission(summary, EntityPermission.UPDATE);
    }

    public MooseHuntingSummaryDTO transformBasicSummary(final BasicClubHuntingSummary summary) {
        final HarvestPermit harvestPermit = summary.getSpeciesAmount().getHarvestPermit();
        final int permitAreaSize = Objects.requireNonNull(harvestPermit.getPermitAreaSize(), "permitAreaSize is null");
        final MooseHuntingSummaryDTO dto = createInternal(summary.getBasicInfo(), false, permitAreaSize);
        Preconditions.checkState(summary.isModeratorOverride(), "moderator override required");
        Preconditions.checkArgument(F.hasId(summary.getSpeciesAmount()), "speciesAmount must have ID");
        dto.setHarvestPermitId(harvestPermit.getId());
        return dto;
    }

    private static MooseHuntingSummaryDTO create(
            @Nonnull final MooseHuntingSummary entity, final boolean locked, final int permitAreaSize) {

        final MooseHuntingSummaryDTO dto = createInternal(entity.getBasicInfo(), locked, permitAreaSize);
        DtoUtil.copyBaseFields(entity, dto);

        Preconditions.checkArgument(F.hasId(entity.getHarvestPermit()), "permit must have ID");
        dto.setHarvestPermitId(entity.getHarvestPermit().getId());

        dto.setBeginDate(entity.getBeginDate());
        dto.setEndDate(entity.getEndDate());

        dto.setHuntingAreaType(entity.getHuntingAreaType());

        dto.setNumberOfDrownedMooses(entity.getNumberOfDrownedMooses());
        dto.setNumberOfMoosesKilledByBear(entity.getNumberOfMoosesKilledByBear());
        dto.setNumberOfMoosesKilledByWolf(entity.getNumberOfMoosesKilledByWolf());
        dto.setNumberOfMoosesKilledInTrafficAccident(entity.getNumberOfMoosesKilledInTrafficAccident());
        dto.setNumberOfMoosesKilledByPoaching(entity.getNumberOfMoosesKilledByPoaching());
        dto.setNumberOfMoosesKilledInRutFight(entity.getNumberOfMoosesKilledInRutFight());
        dto.setNumberOfStarvedMooses(entity.getNumberOfStarvedMooses());
        dto.setNumberOfMoosesDeceasedByOtherReason(entity.getNumberOfMoosesDeceasedByOtherReason());
        dto.setCauseOfDeath(entity.getCauseOfDeath());

        dto.setWhiteTailedDeerAppearance(newIfNull(entity.getWhiteTailedDeerAppearance()));
        dto.setRoeDeerAppearance(newIfNull(entity.getRoeDeerAppearance()));
        dto.setWildForestReindeerAppearance(newIfNull(entity.getWildForestReindeerAppearance()));
        dto.setFallowDeerAppearance(newIfNull(entity.getFallowDeerAppearance()));
        dto.setWildBoarAppearance(newIfNull(entity.getWildBoarAppearance()));
        dto.setBeaverAppearance(entity.getBeaverAppearance());

        dto.setMooseHeatBeginDate(entity.getMooseHeatBeginDate());
        dto.setMooseHeatEndDate(entity.getMooseHeatEndDate());
        dto.setMooseFawnBeginDate(entity.getMooseFawnBeginDate());
        dto.setMooseFawnEndDate(entity.getMooseFawnEndDate());

        dto.setDeerFliesAppeared(entity.getDeerFliesAppeared());
        dto.setDateOfFirstDeerFlySeen(entity.getDateOfFirstDeerFlySeen());
        dto.setDateOfLastDeerFlySeen(entity.getDateOfLastDeerFlySeen());
        dto.setNumberOfAdultMoosesHavingFlies(entity.getNumberOfAdultMoosesHavingFlies());
        dto.setNumberOfYoungMoosesHavingFlies(entity.getNumberOfYoungMoosesHavingFlies());
        dto.setTrendOfDeerFlyPopulationGrowth(entity.getTrendOfDeerFlyPopulationGrowth());

        dto.setObservationPolicyAdhered(entity.getObservationPolicyAdhered());

        return dto;
    }

    private static MooseHuntingSummaryDTO createInternal(
            @Nonnull final ClubHuntingSummaryBasicInfo summary, final boolean locked, final int permitAreaSize) {

        Objects.requireNonNull(summary);
        Objects.requireNonNull(summary.getClubId(), "club must have ID");

        final MooseHuntingSummaryDTO dto = new MooseHuntingSummaryDTO();
        dto.setClubId(summary.getClubId());

        dto.setHuntingEndDate(summary.getHuntingEndDate());
        dto.setHuntingFinished(summary.isHuntingFinished());

        dto.setTotalHuntingArea(summary.getTotalHuntingArea());
        dto.setEffectiveHuntingArea(summary.getEffectiveHuntingArea());
        dto.setEffectiveHuntingAreaPercentage(summary.getEffectiveHuntingAreaPercentage());
        dto.setRemainingPopulationInTotalArea(summary.getRemainingPopulationInTotalArea());
        dto.setRemainingPopulationInEffectiveArea(summary.getRemainingPopulationInEffectiveArea());

        dto.setLocked(locked);
        dto.setPermitAreaSize(permitAreaSize);

        return dto;
    }

    private static SpeciesEstimatedAppearance newIfNull(final SpeciesEstimatedAppearance speciesAppearance) {
        return Optional.ofNullable(speciesAppearance).orElseGet(SpeciesEstimatedAppearance::new);
    }

    private static SpeciesEstimatedAppearanceWithPiglets newIfNull(final SpeciesEstimatedAppearanceWithPiglets speciesAppearance) {
        return Optional.ofNullable(speciesAppearance).orElseGet(SpeciesEstimatedAppearanceWithPiglets::new);
    }

    private Function<MooseHuntingSummary, HarvestPermit> getSummaryToPermitMapping(final List<MooseHuntingSummary> summaries) {
        return CriteriaUtils.singleQueryFunction(summaries, MooseHuntingSummary::getHarvestPermit, harvestPermitRepository, true);
    }

    private Function<MooseHuntingSummary, HuntingClub> getSummaryToClubMapping(final List<MooseHuntingSummary> summaries) {
        return CriteriaUtils.singleQueryFunction(summaries, MooseHuntingSummary::getClub, huntingClubRepository, true);
    }

    private List<Long> listPermitsHavingMooseHarvestReport(List<MooseHuntingSummary> summaries) {
        final QMooseHuntingSummary summary = QMooseHuntingSummary.mooseHuntingSummary;
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies species = QGameSpecies.gameSpecies;
        final QMooseHarvestReport mooseHarvestReport = QMooseHarvestReport.mooseHarvestReport;

        final BooleanExpression speciesAmountMatches = speciesAmount.in(JPAExpressions.select(speciesAmount)
                .from(summary)
                .join(summary.harvestPermit, permit)
                .join(permit.speciesAmounts, speciesAmount)
                .join(speciesAmount.gameSpecies, species)
                .where(summary.in(summaries),
                        species.officialCode.eq(OFFICIAL_CODE_MOOSE)));

        return queryFactory.select(permit.id)
                .from(mooseHarvestReport)
                .join(mooseHarvestReport.speciesAmount, speciesAmount)
                .join(speciesAmount.harvestPermit, permit)
                .where(speciesAmountMatches)
                .fetch();
    }

    private List<SummaryPermitClubGroupFromMooseDataCard> summaryPermitClubGroupFromMooseDataCardCount(final List<MooseHuntingSummary> summaries) {
        final QMooseHuntingSummary summary = QMooseHuntingSummary.mooseHuntingSummary;
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies species = QGameSpecies.gameSpecies;
        final QHuntingClub club = QHuntingClub.huntingClub;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;

        return queryFactory.select(summary.id, permit.id, club.id, group.id.count())
                .from(summary)
                .join(summary.harvestPermit, permit)
                .join(permit.speciesAmounts, speciesAmount)
                .join(speciesAmount.gameSpecies, species)
                .join(summary.club, club)
                .leftJoin(club.subOrganisations, group._super)
                // is it enough to match group by permit, no need to check that group.huntingYear matches to speciesAmount?
                .where(summary.in(summaries),
                        species.officialCode.eq(OFFICIAL_CODE_MOOSE),
                        group.harvestPermit.eq(permit),
                        group.fromMooseDataCard.eq(true))
                .groupBy(summary.id, permit.id, club.id)
                .fetch()
                .stream()
                .map(t -> new SummaryPermitClubGroupFromMooseDataCard(t.get(summary.id), t.get(permit.id), t.get(club.id), t.get(group.id.count())))
                .collect(toList());
    }

    private static boolean isFromMooseDataCard(final MooseHuntingSummary s,
                                               final HarvestPermit permit,
                                               final HuntingClub club,
                                               final List<SummaryPermitClubGroupFromMooseDataCard> data) {

        for (final SummaryPermitClubGroupFromMooseDataCard t : data) {
            if (t.matches(s, permit, club)) {
                return t.groupFromMooseDataCardCount > 0;
            }
        }
        return false;
    }

    private static class SummaryPermitClubGroupFromMooseDataCard {
        private final long summaryId;
        private final long permitId;
        private final long clubId;
        private final long groupFromMooseDataCardCount;

        SummaryPermitClubGroupFromMooseDataCard(final long summaryId,
                                                final long permitId,
                                                final long clubId,
                                                final long groupFromMooseDataCardCount) {
            this.summaryId = summaryId;
            this.permitId = permitId;
            this.clubId = clubId;
            this.groupFromMooseDataCardCount = groupFromMooseDataCardCount;
        }

        public boolean matches(final MooseHuntingSummary s, final HarvestPermit permit, final HuntingClub club) {
            return idEquals(s, summaryId) && idEquals(permit, permitId) && idEquals(club, clubId);
        }

        private static boolean idEquals(final HasID<Long> obj, long id) {
            return Objects.equals(F.getId(obj), id);
        }
    }
}
