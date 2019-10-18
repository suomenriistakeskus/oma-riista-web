package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerAreaService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Service
public class MooseHuntingSummaryFeature {

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private MooseHuntingSummaryRepository mooseHuntingSummaryRepository;

    @Resource
    private BasicClubHuntingSummaryRepository basicHuntingSummaryRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitPartnerAreaService harvestPermitPartnerAreaService;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private MooseHuntingSummaryLockingService lockConditionService;

    @Resource
    private MooseHuntingSummaryDTOTransformer dtoTransformer;

    @Transactional
    public MooseHuntingSummaryDTO markUnfinished(final long summaryId) {
        final MooseHuntingSummary summary =
                requireEntityService.requireMooseHuntingSummary(summaryId, EntityPermission.UPDATE);
        return dtoTransformer.transform(huntingFinishingService.markUnfinished(summary));
    }

    @Nonnull
    @Transactional(readOnly = true)
    public MooseHuntingSummaryDTO resolveMooseHuntingSummary(final long clubId, final long permitId) {
        final HuntingClub club = huntingClubRepository.getOne(clubId);
        final HarvestPermit harvestPermit = harvestPermitRepository.getOne(permitId);
        final HarvestPermitSpeciesAmount speciesAmount = harvestPermitSpeciesAmountRepository.getMooseAmount(harvestPermit);

        return basicHuntingSummaryRepository.findByClubAndSpeciesAmount(club, speciesAmount, true).map(basicSummary -> {
            activeUserService.assertHasPermission(basicSummary, EntityPermission.READ);
            return dtoTransformer.transformBasicSummary(basicSummary);

        }).orElseGet(() -> mooseHuntingSummaryRepository.findByClubIdAndPermitId(clubId, permitId).map(mooseSummary -> {
            activeUserService.assertHasPermission(mooseSummary, EntityPermission.READ);
            return dtoTransformer.transform(mooseSummary);

        }).orElseGet(() -> {
            // No summary previously exists, return blank base data
            return getSummaryTemplate(speciesAmount, club);
        }));
    }

    private MooseHuntingSummaryDTO getSummaryTemplate(final HarvestPermitSpeciesAmount speciesAmount,
                                                      final HuntingClub club) {
        final HarvestPermit permit = speciesAmount.getHarvestPermit();
        final int permitAreaSize = harvestPermitPartnerAreaService.getPermitAreaSizeLookupWithFallback(permit, club);

        final MooseHuntingSummaryDTO dto = new MooseHuntingSummaryDTO();
        dto.setClubId(club.getId());
        dto.setHarvestPermitId(permit.getId());
        dto.setPermitAreaSize(permitAreaSize);
        dto.setTotalHuntingArea(permitAreaSize);
        dto.setLocked(lockConditionService.isMooseHuntingSummaryLocked(speciesAmount, club));

        return dto;
    }

}
