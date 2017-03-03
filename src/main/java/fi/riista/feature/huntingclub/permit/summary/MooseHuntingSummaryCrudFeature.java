package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.CannotChangeAssociatedEntityException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.area.HuntingClubAreaSizeService;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportRepository;
import fi.riista.feature.huntingclub.permit.partner.AllPartnersFinishedHuntingMailFeature;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component
public class MooseHuntingSummaryCrudFeature
        extends AbstractCrudFeature<Long, MooseHuntingSummary, MooseHuntingSummaryDTO> {

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepository;

    @Resource
    private AllPartnersFinishedHuntingMailFeature huntingFinishedMailFeature;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HuntingClubAreaSizeService huntingClubAreaSizeService;

    @Resource
    private ClubHuntingSummaryService huntingSummaryService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MooseHuntingSummaryDTOTransformer mooseHuntingSummaryDTOTransformer;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Override
    protected MooseHuntingSummaryDTO toDTO(@Nonnull final MooseHuntingSummary entity) {
        return mooseHuntingSummaryDTOTransformer.apply(entity);
    }

    @Override
    protected Enum<?> getCreatePermission(final MooseHuntingSummary entity,
                                          final MooseHuntingSummaryDTO dto) {
        final HarvestPermit harvestPermit = entity.getHarvestPermit();
        final HuntingClub club = entity.getClub();
        return isFromMooseDataCard(harvestPermit, club)
                ? MooseHuntingSummaryAuthorization.Permission.CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT
                : EntityPermission.CREATE;
    }

    @Override
    protected Enum<?> getUpdatePermission(final MooseHuntingSummary entity, MooseHuntingSummaryDTO dto) {
        return isFromMooseDataCard(entity.getHarvestPermit(), entity.getClub())
                ? MooseHuntingSummaryAuthorization.Permission.UPDATE_MOOSE_DATA_CARD_ORIGINATED
                : EntityPermission.UPDATE;
    }

    @Override
    protected Enum<?> getDeletePermission(final MooseHuntingSummary entity) {
        return isFromMooseDataCard(entity.getHarvestPermit(), entity.getClub())
                ? MooseHuntingSummaryAuthorization.Permission.DELETE_MOOSE_DATA_CARD_ORIGINATED
                : EntityPermission.DELETE;
    }

    private boolean isLockedForCreate(final HarvestPermit permit, final HuntingClub club) {
        return !activeUserService.isModeratorOrAdmin() && isFromMooseDataCard(permit, club)
                || !hasPermissionToCreate(permit, club)
                || isMooseHarvestReportDone(permit);
    }

    private boolean isFromMooseDataCard(final HarvestPermit permit, final HuntingClub club) {
        return huntingClubGroupRepository.isClubUsingMooseDataCardForPermit(
                club, harvestPermitSpeciesAmountRepository.getOneByHarvestPermitAndSpeciesCode(
                        permit, GameSpecies.OFFICIAL_CODE_MOOSE).resolveHuntingYear());
    }

    private boolean hasPermissionToCreate(final HarvestPermit permit, final HuntingClub club) {
        final MooseHuntingSummary summary = new MooseHuntingSummary(club, permit);
        return activeUserService.checkHasPermission(summary, EntityPermission.CREATE);
    }

    private boolean isMooseHarvestReportDone(final HarvestPermit permit) {
        final HarvestPermitSpeciesAmount speciesAmount = harvestPermitSpeciesAmountRepository
                .getOneByHarvestPermitAndSpeciesCode(permit, GameSpecies.OFFICIAL_CODE_MOOSE);
        return mooseHarvestReportRepository.isMooseHarvestReportDone(speciesAmount);
    }

    @Transactional(readOnly = true)
    public MooseHuntingSummaryDTO getMooseSummary(final long clubId, final long permitId) {
        final HuntingClub club = huntingClubRepository.getOne(clubId);
        final HarvestPermitSpeciesAmount speciesAmount = getSpeciesAmount(permitId);
        final HarvestPermit harvestPermit = speciesAmount.getHarvestPermit();

        final BasicClubHuntingSummary summary =
                basicHuntingSummaryRepository.findByClubAndSpeciesAmount(club, speciesAmount, true).orElse(null);

        if (summary != null) {
            activeUserService.assertHasPermission(summary, EntityPermission.READ);
            final MooseHuntingSummaryDTO dto = mooseHuntingSummaryDTOTransformer.transformBasicSummary(summary);
            dto.setLocked(true);
            return dto;
        }

        final MooseHuntingSummary mooseHuntingSummary =
                mooseHuntingSummaryRepository.findByClubIdAndPermitId(clubId, permitId).orElse(null);

        if (mooseHuntingSummary != null) {
            activeUserService.assertHasPermission(mooseHuntingSummary, EntityPermission.READ);
            return mooseHuntingSummaryDTOTransformer.apply(mooseHuntingSummary);
        }

        // no summary, create blank
        final MooseHuntingSummaryDTO dto = new MooseHuntingSummaryDTO();
        dto.setClubId(clubId);
        dto.setHarvestPermitId(permitId);
        dto.setLocked(isLockedForCreate(harvestPermit, club));
        dto.setPermitAreaSize(Objects.requireNonNull(harvestPermit.getPermitAreaSize(), "permitAreaSize is null"));

        huntingClubAreaSizeService.getHuntingPermitAreaSize(harvestPermit, club).ifPresent(areaSize -> {
            dto.setTotalHuntingArea(areaSize.intValue() / 10_000);
        });

        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<BasicClubHuntingSummaryDTO> getHuntingSummariesForModeration(
            final long permitId, final int speciesCode) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        return huntingSummaryService.getHuntingSummariesForModeration(permit, speciesCode);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    @Transactional
    public void processModeratorOverriddenHuntingSummaries(
            final long permitId,
            final int gameSpeciesCode,
            final boolean completeHuntingOfPermit,
            @Nonnull final List<BasicClubHuntingSummaryDTO> summaries) {

        huntingSummaryService.processModeratorOverriddenHuntingSummaries(permitId, gameSpeciesCode, completeHuntingOfPermit, summaries);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    @Transactional
    public void revokeHuntingSummaryModeration(final long permitId, final int gameSpeciesCode) {
        huntingSummaryService.revokeHuntingSummaryModeration(permitId, gameSpeciesCode);
    }

    @Transactional
    public MooseHuntingSummaryDTO markUnfinished(final long summaryId) {
        final MooseHuntingSummary summary =
                requireEntityService.requireMooseHuntingSummary(summaryId, EntityPermission.UPDATE);
        return toDTO(huntingSummaryService.markUnfinished(summary));
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

        final HarvestPermitSpeciesAmount speciesAmount = getSpeciesAmount(entity.getHarvestPermit());
        mooseHarvestReportRepository.assertMooseHarvestReportNotDone(speciesAmount);

        AreaSizeAssertionHelper.assertGivenAreaSizeToPermitAreaSize(entity.getHarvestPermit().getPermitAreaSize(),
                dto.getTotalHuntingArea(), dto.getEffectiveHuntingArea());

        InvalidHuntingEndDateException.assertDateValid(speciesAmount, dto.getHuntingEndDate());

        entity.setBeginDate(dto.getBeginDate());
        entity.setEndDate(dto.getEndDate());

        entity.setHuntingEndDate(dto.getHuntingEndDate());
        entity.setHuntingFinished(dto.isHuntingFinished());

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

        entity.setMooseHeatBeginDate(dto.getMooseHeatBeginDate());
        entity.setMooseHeatEndDate(dto.getMooseHeatEndDate());
        entity.setMooseFawnBeginDate(dto.getMooseFawnBeginDate());
        entity.setMooseFawnEndDate(dto.getMooseFawnEndDate());

        entity.setDeerFliesAppeared(dto.getDeerFliesAppeared());

        if (Boolean.FALSE.equals(entity.getDeerFliesAppeared())) {
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
            getRepository().saveAndFlush(entity);
            huntingFinishedMailFeature.checkAndSend(entity.getHarvestPermit(), GameSpecies.OFFICIAL_CODE_MOOSE);
        }
    }

    private HarvestPermitSpeciesAmount getSpeciesAmount(final HarvestPermit permit) {
        return harvestPermitSpeciesAmountRepository.getOneByHarvestPermitAndSpeciesCode(
                permit, GameSpecies.OFFICIAL_CODE_MOOSE);
    }

    private HarvestPermitSpeciesAmount getSpeciesAmount(final long permitId) {
        return harvestPermitSpeciesAmountRepository.getOneByHarvestPermitIdAndSpeciesCode(
                permitId, GameSpecies.OFFICIAL_CODE_MOOSE);
    }
}
