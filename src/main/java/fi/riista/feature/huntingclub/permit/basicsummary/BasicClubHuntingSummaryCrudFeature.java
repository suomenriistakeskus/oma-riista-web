package fi.riista.feature.huntingclub.permit.basicsummary;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.CannotChangeAssociatedEntityException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.area.HuntingClubAreaSizeService;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryAuthorization.Permission;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportRepository;
import fi.riista.feature.huntingclub.permit.partner.AllPartnersFinishedHuntingMailFeature;
import fi.riista.feature.huntingclub.permit.summary.AreaSizeAssertionHelper;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryService;
import fi.riista.feature.huntingclub.permit.summary.InvalidHuntingEndDateException;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

@Component
public class BasicClubHuntingSummaryCrudFeature
        extends AbstractCrudFeature<Long, BasicClubHuntingSummary, BasicClubHuntingSummaryDTO> {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private BasicClubHuntingSummaryRepository basicClubHuntingSummaryRepository;

    @Resource
    private HuntingClubRepository clubRepo;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepo;

    @Resource
    private HarvestPermitRepository permitRepo;

    @Resource
    private MooseHarvestReportRepository mooseHarvestReportRepo;

    @Resource
    private ClubHuntingSummaryService huntingSummaryService;

    @Resource
    private HuntingClubAreaSizeService huntingClubAreaSizeService;

    @Resource
    private AllPartnersFinishedHuntingMailFeature huntingFinishedMailFeature;

    @Resource
    private BasicClubHuntingSummaryDTOTransformer basicClubHuntingSummaryDTOTransformer;

    @Transactional(readOnly = true)
    public BasicClubHuntingSummaryDTO getDeerSummary(final long clubId, final long speciesAmountId) {
        final HuntingClub club = clubRepo.getOne(clubId);
        final HarvestPermitSpeciesAmount speciesAmount = speciesAmountRepo.getOne(speciesAmountId);

        final BasicClubHuntingSummary summary =
                basicClubHuntingSummaryRepository.findByClubAndSpeciesAmount(club, speciesAmount).orElse(null);

        if (summary != null) {
            activeUserService.assertHasPermission(summary, EntityPermission.READ);
            return toDTO(summary);
        }

        // no summary, create blank
        final BasicClubHuntingSummaryDTO dto = new BasicClubHuntingSummaryDTO();
        dto.setClubId(clubId);

        final HarvestPermit permit = speciesAmount.getHarvestPermit();
        dto.setHarvestPermitId(permit.getId());
        dto.setGameSpeciesCode(speciesAmount.getGameSpecies().getOfficialCode());
        dto.setPermitAreaSize(Objects.requireNonNull(permit.getPermitAreaSize(), "permitAreaSize is null"));

        huntingClubAreaSizeService.getHuntingPermitAreaSize(permit, club).ifPresent(areaSize -> {
            dto.setTotalHuntingArea(areaSize.intValue() / 10_000);
        });

        return dto;
    }

    @Transactional(readOnly = true)
    public boolean isLocked(final long clubId, final long speciesAmountId) {
        final HarvestPermitSpeciesAmount speciesAmount = speciesAmountRepo.getOne(speciesAmountId);

        if (mooseHarvestReportRepo.isMooseHarvestReportDone(speciesAmount)) {
            return true;
        }

        final HuntingClub club = clubRepo.getOne(clubId);

        return Optional.ofNullable(basicClubHuntingSummaryRepository.findByClubAndSpeciesAmount(club, speciesAmount).orElse(null))
                .map(summary -> !activeUserService.checkHasPermission(summary, EntityPermission.UPDATE))
                .orElseGet(() -> {
                    final BasicClubHuntingSummary summary = new BasicClubHuntingSummary(club, speciesAmount);
                    return !activeUserService.checkHasPermission(summary, EntityPermission.CREATE);
                });
    }

    @Transactional
    public BasicClubHuntingSummaryDTO markUnfinished(final long summaryId) {
        final BasicClubHuntingSummary summary =
                requireEntityService.requireBasicClubHuntingSummary(summaryId, EntityPermission.UPDATE);
        return toDTO(huntingSummaryService.markUnfinished(summary));
    }

    @Override
    protected void updateEntity(final BasicClubHuntingSummary entity, final BasicClubHuntingSummaryDTO dto) {
        final HarvestPermit permit = permitRepo.getOne(dto.getHarvestPermitId());
        if (entity.isNew()) {

            entity.setClub(clubRepo.getOne(dto.getClubId()));
            entity.setSpeciesAmount(
                    speciesAmountRepo.getOneByHarvestPermitAndSpeciesCode(permit, dto.getGameSpeciesCode()));
        } else {
            CannotChangeAssociatedEntityException.assertRelationIdNotChanged(
                    entity, BasicClubHuntingSummary::getClub, dto.getClubId());
            CannotChangeAssociatedEntityException.assertRelationIdNotChanged(
                    entity, summary -> summary.getSpeciesAmount().getHarvestPermit(), dto.getHarvestPermitId());
        }

        mooseHarvestReportRepo.assertMooseHarvestReportNotDoneOrModeratorOverriden(entity.getSpeciesAmount());

        AreaSizeAssertionHelper.assertGivenAreaSizeToPermitAreaSize(permit.getPermitAreaSize(),
                dto.getTotalHuntingArea(), dto.getEffectiveHuntingArea());

        InvalidHuntingEndDateException.assertDateValid(entity.getSpeciesAmount(), dto.getHuntingEndDate());

        if (dto.isModeratorOverridden()) {
            entity.doModeratorOverride(
                    dto.getHuntingEndDate(), dto.getAreaSizeAndRemainingPopulation(), dto.getHarvestCounts());
        } else {
            entity.setHuntingFinished(dto.isHuntingFinished());
            entity.setHuntingEndDate(dto.getHuntingEndDate());
            entity.setAreaSizeAndPopulation(dto.getAreaSizeAndRemainingPopulation());

            if (!dto.isCreatedWithinModeration() && entity.isHuntingFinished()) {
                getRepository().saveAndFlush(entity);

                final HarvestPermitSpeciesAmount speciesAmount = entity.getSpeciesAmount();
                huntingFinishedMailFeature.checkAndSend(
                        speciesAmount.getHarvestPermit(), speciesAmount.getGameSpecies().getOfficialCode());
            }
        }
    }

    @Override
    protected BasicClubHuntingSummaryDTO toDTO(@Nonnull final BasicClubHuntingSummary entity) {
        return basicClubHuntingSummaryDTOTransformer.apply(entity);
    }

    @Override
    protected JpaRepository<BasicClubHuntingSummary, Long> getRepository() {
        return basicClubHuntingSummaryRepository;
    }

    @Override
    protected Enum<?> getCreatePermission(final BasicClubHuntingSummary entity,
                                          final BasicClubHuntingSummaryDTO dto) {
        return dto.isModeratorOverridden() ? Permission.CREATE_MODERATOR_OVERRIDDEN_SUMMARY : EntityPermission.CREATE;
    }

    @Override
    protected Enum<?> getUpdatePermission(final BasicClubHuntingSummary entity, final BasicClubHuntingSummaryDTO dto) {
        return entity.isModeratorOverride() ? Permission.UPDATE_MODERATOR_OVERRIDDEN_SUMMARY : EntityPermission.UPDATE;
    }

    @Override
    protected Enum<?> getDeletePermission(final BasicClubHuntingSummary entity) {
        return entity.isModeratorOverride() ? Permission.DELETE_MODERATOR_OVERRIDDEN_SUMMARY : EntityPermission.DELETE;
    }

}
