package fi.riista.feature.huntingclub.permit.endofhunting.basicsummary;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.CannotChangeAssociatedEntityException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.AllPartnersFinishedHuntingMailFeature;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAssertionHelper;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.feature.huntingclub.permit.endofhunting.InvalidHuntingEndDateException;
import fi.riista.feature.huntingclub.permit.partner.ClubIsNotPermitPartnerException;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerAreaService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class DeerHuntingSummaryFeature
        extends AbstractCrudFeature<Long, BasicClubHuntingSummary, BasicClubHuntingSummaryDTO> {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private BasicClubHuntingSummaryRepository basicClubHuntingSummaryRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepo;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private HarvestPermitPartnerAreaService harvestPermitPartnerAreaService;

    @Resource
    private AllPartnersFinishedHuntingMailFeature allPartnersFinishedHuntingMailFeature;

    @Transactional(readOnly = true)
    public BasicClubHuntingSummaryDTO getDeerSummary(final long clubId, final long speciesAmountId) {
        final HuntingClub club = huntingClubRepository.getOne(clubId);
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
        final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(permit, club);

        dto.setHarvestPermitId(permit.getId());
        dto.setGameSpeciesCode(speciesAmount.getGameSpecies().getOfficialCode());
        dto.setPermitAreaSize(permitAreaSize);
        dto.setTotalHuntingArea(permitAreaSize);

        return dto;
    }

    @Transactional(readOnly = true)
    public boolean isLocked(final long clubId, final long speciesAmountId) {
        final HarvestPermitSpeciesAmount speciesAmount = speciesAmountRepo.getOne(speciesAmountId);

        if (speciesAmount.isMooselikeHuntingFinished()) {
            return true;
        }

        final HuntingClub club = huntingClubRepository.getOne(clubId);

        return basicClubHuntingSummaryRepository.findByClubAndSpeciesAmount(club, speciesAmount)
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
        return toDTO(huntingFinishingService.markUnfinished(summary));
    }

    @Override
    protected void updateEntity(final BasicClubHuntingSummary entity, final BasicClubHuntingSummaryDTO dto) {
        final HarvestPermit permit = harvestPermitRepository.getOne(dto.getHarvestPermitId());

        if (entity.isNew()) {
            entity.setClub(huntingClubRepository.getOne(dto.getClubId()));
            entity.setSpeciesAmount(
                    speciesAmountRepo.getOneByHarvestPermitAndSpeciesCode(permit, dto.getGameSpeciesCode()));

            if (!isVerifiedPartner(permit, entity.getClub())) {
                throw ClubIsNotPermitPartnerException.create(dto.getClubId(), dto.getHarvestPermitId());
            }

        } else {
            CannotChangeAssociatedEntityException.assertRelationIdNotChanged(
                    entity, BasicClubHuntingSummary::getClub, dto.getClubId());
            CannotChangeAssociatedEntityException.assertRelationIdNotChanged(
                    entity, summary -> summary.getSpeciesAmount().getHarvestPermit(), dto.getHarvestPermitId());

            if (entity.isModeratorOverride()) {
                throw new IllegalArgumentException("Cannot modify moderator overridden summary");
            }
        }

        if (!entity.getSpeciesAmount().isHuntingFinishedByModerator()) {
            entity.getSpeciesAmount().assertMooselikeHuntingNotFinished();
        }

        final HarvestPermitSpeciesAmount speciesAmount = entity.getSpeciesAmount();
        final HarvestPermit harvestPermit = speciesAmount.getHarvestPermit();
        final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(entity);

        AreaSizeAssertionHelper.assertGivenAreaSizeToPermitAreaSize(permitAreaSize,
                dto.getTotalHuntingArea(), dto.getEffectiveHuntingArea());

        InvalidHuntingEndDateException.assertDateValid(entity.getSpeciesAmount(), dto.getHuntingEndDate());

        entity.setHuntingFinished(dto.isHuntingFinished());
        entity.setHuntingEndDate(dto.getHuntingEndDate());
        entity.setAreaSizeAndPopulation(dto.getAreaSizeAndRemainingPopulation());

        if (entity.isHuntingFinished()) {
            getRepository().save(entity);

            // Must flush Hibernate session before executing native SQL query
            getRepository().flush();

            allPartnersFinishedHuntingMailFeature.checkAndSend(
                    harvestPermit, speciesAmount.getGameSpecies().getOfficialCode());
        }
    }

    private static boolean isVerifiedPartner(final HarvestPermit permit, final HuntingClub club) {
        return F.getUniqueIds(permit.getPermitPartners()).contains(club.getId());
    }

    @Override
    protected BasicClubHuntingSummaryDTO toDTO(@Nonnull final BasicClubHuntingSummary entity) {
        final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(entity);
        return BasicClubHuntingSummaryDTO.transform(entity, permitAreaSize);
    }

    @Override
    protected JpaRepository<BasicClubHuntingSummary, Long> getRepository() {
        return basicClubHuntingSummaryRepository;
    }
}
