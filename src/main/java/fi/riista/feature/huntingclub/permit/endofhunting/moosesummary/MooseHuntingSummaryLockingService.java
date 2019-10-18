package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class MooseHuntingSummaryLockingService {

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isMooseHuntingSummaryLocked(final HarvestPermitSpeciesAmount mooseAmount, final HuntingClub club) {
        return MooseHuntingSummaryLockingCondition.builder()
                .withActionRequestedByModerator(activeUserService::isModeratorOrAdmin)
                .withDataOriginatingFromMooseDataCard(() -> {
                    final int huntingYear = mooseAmount.resolveHuntingYear();
                    return huntingClubGroupRepository.isClubUsingMooseDataCardForPermit(club, huntingYear);
                })
                .withPermitHolderFinishedHunting(mooseAmount::isMooselikeHuntingFinished)
                .withPermitLockDatePassed(() -> harvestPermitLockedByDateService.isPermitLocked(mooseAmount))
                .withUserHavingOccupationGrantingPermission(() -> {
                    final MooseHuntingSummary summary = new MooseHuntingSummary(club, mooseAmount.getHarvestPermit());
                    return activeUserService.checkHasPermission(summary, EntityPermission.CREATE);
                })
                .build()
                .isLocked();
    }
}
