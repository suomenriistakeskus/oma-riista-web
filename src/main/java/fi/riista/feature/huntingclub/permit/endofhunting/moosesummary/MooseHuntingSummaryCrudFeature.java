package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.common.CannotChangeAssociatedEntityException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.AllPartnersFinishedHuntingMailFeature;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAssertionHelper;
import fi.riista.feature.huntingclub.permit.endofhunting.InvalidHuntingEndDateException;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerAreaService;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class MooseHuntingSummaryCrudFeature
        extends AbstractCrudFeature<Long, MooseHuntingSummary, MooseHuntingSummaryDTO> {

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestPermitPartnerAreaService harvestPermitPartnerAreaService;

    @Resource
    private AllPartnersFinishedHuntingMailFeature allPartnersFinishedHuntingMailFeature;

    @Resource
    private MooseHuntingSummaryDTOTransformer mooseHuntingSummaryDTOTransformer;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Override
    protected MooseHuntingSummaryDTO toDTO(@Nonnull final MooseHuntingSummary entity) {
        return mooseHuntingSummaryDTOTransformer.transform(entity);
    }

    @Override
    protected Enum<?> getCreatePermission(final MooseHuntingSummary entity, final MooseHuntingSummaryDTO dto) {
        final HarvestPermit harvestPermit = entity.getHarvestPermit();
        final HuntingClub club = entity.getClub();

        return hasPermitPartnerSentMooseDataCard(harvestPermit, club)
                ? MooseHuntingSummaryAuthorization.Permission.CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT
                : EntityPermission.CREATE;
    }

    @Override
    protected Enum<?> getUpdatePermission(final MooseHuntingSummary entity, MooseHuntingSummaryDTO dto) {
        return hasPermitPartnerSentMooseDataCard(entity.getHarvestPermit(), entity.getClub())
                ? MooseHuntingSummaryAuthorization.Permission.UPDATE_MOOSE_DATA_CARD_ORIGINATED
                : EntityPermission.UPDATE;
    }

    @Override
    protected Enum<?> getDeletePermission(final MooseHuntingSummary entity) {
        return hasPermitPartnerSentMooseDataCard(entity.getHarvestPermit(), entity.getClub())
                ? MooseHuntingSummaryAuthorization.Permission.DELETE_MOOSE_DATA_CARD_ORIGINATED
                : EntityPermission.DELETE;
    }

    private boolean hasPermitPartnerSentMooseDataCard(final HarvestPermit permit, final HuntingClub club) {
        final HarvestPermitSpeciesAmount mooseAmount = harvestPermitSpeciesAmountRepository.getMooseAmount(permit);

        return huntingClubGroupRepository.isClubUsingMooseDataCardForPermit(club, mooseAmount.resolveHuntingYear());
    }

    @Override
    protected JpaRepository<MooseHuntingSummary, Long> getRepository() {
        return mooseHuntingSummaryRepository;
    }

    @Override
    protected void updateEntity(final MooseHuntingSummary entity, final MooseHuntingSummaryDTO dto) {
        if (entity.isNew()) {
            entity.setClub(huntingClubRepository.getOne(dto.getClubId()));
            entity.setHarvestPermit(harvestPermitRepository.getOne(dto.getHarvestPermitId()));
        } else {
            CannotChangeAssociatedEntityException.assertRelationIdNotChanged(
                    entity, MooseHuntingSummary::getClub, dto.getClubId());
            CannotChangeAssociatedEntityException.assertRelationIdNotChanged(
                    entity, MooseHuntingSummary::getHarvestPermit, dto.getHarvestPermitId());
        }

        final HarvestPermitSpeciesAmount speciesAmount =
                harvestPermitSpeciesAmountRepository.getMooseAmount(entity.getHarvestPermit());

        speciesAmount.assertMooselikeHuntingNotFinished();

        final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(entity);

        AreaSizeAssertionHelper.assertGivenAreaSizeToPermitAreaSize(permitAreaSize,
                dto.getTotalHuntingArea(), dto.getEffectiveHuntingArea());

        InvalidHuntingEndDateException.assertDateValid(speciesAmount, dto.getHuntingEndDate());

        entity.setBeginDate(dto.getBeginDate());
        entity.setEndDate(dto.getEndDate());

        entity.setHuntingEndDate(dto.getHuntingEndDate());
        entity.setHuntingFinished(dto.isHuntingFinished());
        entity.setObservationPolicyAdhered(dto.getObservationPolicyAdhered());

        entity.setAreaSizeAndPopulation(new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(dto.getTotalHuntingArea())
                .withEffectiveHuntingArea(dto.getEffectiveHuntingArea())
                .withRemainingPopulationInTotalArea(dto.getRemainingPopulationInTotalArea())
                .withRemainingPopulationInEffectiveArea(dto.getRemainingPopulationInEffectiveArea()));
        entity.setEffectiveHuntingAreaPercentage(dto.getEffectiveHuntingAreaPercentage());
        entity.setHuntingAreaType(dto.getHuntingAreaType());

        entity.setNumberOfDrownedMooses(dto.getNumberOfDrownedMooses());
        entity.setNumberOfMoosesKilledByBear(dto.getNumberOfMoosesKilledByBear());
        entity.setNumberOfMoosesKilledByWolf(dto.getNumberOfMoosesKilledByWolf());
        entity.setNumberOfMoosesKilledInTrafficAccident(dto.getNumberOfMoosesKilledInTrafficAccident());
        entity.setNumberOfMoosesKilledByPoaching(dto.getNumberOfMoosesKilledByPoaching());
        entity.setNumberOfMoosesKilledInRutFight(dto.getNumberOfMoosesKilledInRutFight());
        entity.setNumberOfStarvedMooses(dto.getNumberOfStarvedMooses());
        entity.setNumberOfMoosesDeceasedByOtherReason(dto.getNumberOfMoosesDeceasedByOtherReason());
        entity.setCauseOfDeath(dto.getCauseOfDeath());

        entity.setWhiteTailedDeerAppearance(dto.getWhiteTailedDeerAppearance());
        entity.setRoeDeerAppearance(dto.getRoeDeerAppearance());
        entity.setWildForestReindeerAppearance(dto.getWildForestReindeerAppearance());
        entity.setFallowDeerAppearance(dto.getFallowDeerAppearance());
        entity.setWildBoarAppearance(dto.getWildBoarAppearance());
        entity.setBeaverAppearance(dto.getBeaverAppearance());

        entity.setMooseHeatBeginDate(dto.getMooseHeatBeginDate());
        entity.setMooseHeatEndDate(dto.getMooseHeatEndDate());
        entity.setMooseFawnBeginDate(dto.getMooseFawnBeginDate());
        entity.setMooseFawnEndDate(dto.getMooseFawnEndDate());

        entity.setDeerFliesAppeared(dto.getDeerFliesAppeared());

        if (dto.getDeerFliesAppeared() == null || Boolean.FALSE.equals(dto.getDeerFliesAppeared())) {
            entity.setDateOfFirstDeerFlySeen(null);
            entity.setDateOfLastDeerFlySeen(null);
            entity.setNumberOfAdultMoosesHavingFlies(null);
            entity.setNumberOfYoungMoosesHavingFlies(null);
            entity.setTrendOfDeerFlyPopulationGrowth(null);
        } else {
            entity.setDateOfFirstDeerFlySeen(dto.getDateOfFirstDeerFlySeen());
            entity.setDateOfLastDeerFlySeen(dto.getDateOfLastDeerFlySeen());
            entity.setNumberOfAdultMoosesHavingFlies(dto.getNumberOfAdultMoosesHavingFlies());
            entity.setNumberOfYoungMoosesHavingFlies(dto.getNumberOfYoungMoosesHavingFlies());
            entity.setTrendOfDeerFlyPopulationGrowth(dto.getTrendOfDeerFlyPopulationGrowth());
        }

        if (entity.isHuntingFinished()) {
            getRepository().save(entity);

            // Must flush Hibernate session before executing native SQL query
            getRepository().flush();

            allPartnersFinishedHuntingMailFeature.checkAndSend(entity.getHarvestPermit(), GameSpecies.OFFICIAL_CODE_MOOSE);
        }
    }
}
