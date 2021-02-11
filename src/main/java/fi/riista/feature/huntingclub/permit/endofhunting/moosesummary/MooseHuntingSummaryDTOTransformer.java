package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerAreaService;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@Component
public class MooseHuntingSummaryDTOTransformer {

    @Resource
    private MooseHuntingSummaryLockingService lockConditionService;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestPermitPartnerAreaService harvestPermitPartnerAreaService;

    public MooseHuntingSummaryDTO transform(final MooseHuntingSummary summary) {
        final HarvestPermit harvestPermit = summary.getHarvestPermit();
        final HarvestPermitSpeciesAmount mooseAmount =
                harvestPermitSpeciesAmountRepository.getMooseAmount(harvestPermit);
        final boolean locked = lockConditionService.isMooseHuntingSummaryLocked(mooseAmount, summary.getClub());
        final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(summary);

        return create(summary, locked, permitAreaSize);
    }

    public MooseHuntingSummaryDTO transformBasicSummary(final @Nonnull BasicClubHuntingSummary entity) {
        Objects.requireNonNull(entity);

        checkState(entity.isModeratorOverride(), "moderator override required");

        final HarvestPermitSpeciesAmount speciesAmount = entity.getSpeciesAmount();
        checkArgument(F.hasId(speciesAmount), "speciesAmount must have ID");

        final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(entity);

        final MooseHuntingSummaryDTO dto = new MooseHuntingSummaryDTO();

        dto.setClubId(entity.getClub().getId());
        dto.setHarvestPermitId(speciesAmount.getHarvestPermit().getId());
        dto.setHuntingEndDate(entity.getHuntingEndDate());
        dto.setHuntingFinished(entity.isHuntingFinished());
        dto.setPermitAreaSize(permitAreaSize);
        dto.setLocked(true);

        final AreaSizeAndRemainingPopulation ap = entity.getAreaSizeAndPopulation();

        dto.setTotalHuntingArea(ap.getTotalHuntingArea());
        dto.setEffectiveHuntingArea(ap.getEffectiveHuntingArea());
        dto.setEffectiveHuntingAreaPercentage(null);
        dto.setRemainingPopulationInTotalArea(ap.getRemainingPopulationInTotalArea());
        dto.setRemainingPopulationInEffectiveArea(ap.getRemainingPopulationInEffectiveArea());

        return dto;
    }

    private static MooseHuntingSummaryDTO create(final @Nonnull MooseHuntingSummary entity,
                                                 final boolean locked, final int permitAreaSize) {
        Objects.requireNonNull(entity);
        Preconditions.checkArgument(F.hasId(entity.getHarvestPermit()), "permit must have ID");

        final MooseHuntingSummaryDTO dto = new MooseHuntingSummaryDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setClubId(entity.getClub().getId());
        dto.setHarvestPermitId(entity.getHarvestPermit().getId());
        dto.setHuntingEndDate(entity.getHuntingEndDate());
        dto.setHuntingFinished(entity.isHuntingFinished());
        dto.setLocked(locked);
        dto.setPermitAreaSize(permitAreaSize);
        dto.setHuntingAreaType(entity.getHuntingAreaType());
        dto.setBeginDate(entity.getBeginDate());
        dto.setEndDate(entity.getEndDate());

        final AreaSizeAndRemainingPopulation ap = entity.getAreaSizeAndPopulation();
        dto.setTotalHuntingArea(ap.getTotalHuntingArea());
        dto.setEffectiveHuntingArea(ap.getEffectiveHuntingArea());
        dto.setEffectiveHuntingAreaPercentage(entity.getEffectiveHuntingAreaPercentage());
        dto.setRemainingPopulationInTotalArea(ap.getRemainingPopulationInTotalArea());
        dto.setRemainingPopulationInEffectiveArea(ap.getRemainingPopulationInEffectiveArea());

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

    private static SpeciesEstimatedAppearance newIfNull(final SpeciesEstimatedAppearance speciesAppearance) {
        return Optional.ofNullable(speciesAppearance).orElseGet(SpeciesEstimatedAppearance::new);
    }

    private static SpeciesEstimatedAppearanceWithPiglets newIfNull(final SpeciesEstimatedAppearanceWithPiglets speciesAppearance) {
        return Optional.ofNullable(speciesAppearance).orElseGet(SpeciesEstimatedAppearanceWithPiglets::new);
    }

}
