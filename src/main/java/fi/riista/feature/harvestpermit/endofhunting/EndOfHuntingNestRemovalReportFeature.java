package fi.riista.feature.harvestpermit.endofhunting;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestRemovalDetailsService;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestRemovalUsageDTO;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

@Service
public class EndOfHuntingNestRemovalReportFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestPermitNestRemovalDetailsService harvestPermitNestRemovalDetailsService;

    @Resource
    private EndOfHuntingReportCreateService endOfHuntingReportCreateService;

    @Transactional(readOnly = true)
    public EndOfHuntingNestRemovalReportDTO getEndOfNestRemovalPermitReport(final long permitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        Preconditions.checkState(PermitTypeCode.isNestRemovalPermitTypeCode(harvestPermit.getPermitTypeCode()), "Permit type must be nest removal");

        return getEndOfHuntingReport(harvestPermit);
    }

    @Nonnull
    private EndOfHuntingNestRemovalReportDTO getEndOfHuntingReport(final HarvestPermit harvestPermit) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        final List<HarvestPermitNestRemovalUsageDTO> usages = harvestPermitNestRemovalDetailsService.getPermitUsage(harvestPermit);

        return new EndOfHuntingNestRemovalReportDTO(harvestPermit, activeUser, usages);
    }

    @Transactional
    public EndOfHuntingNestRemovalReportDTO createEndOfHuntingReport(final long permitId,
                                                                     final EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId,
                HarvestPermitAuthorization.Permission.CREATE_REMOVE_HARVEST_REPORT);

        endOfHuntingReportCreateService.createEndOfHuntingReport(harvestPermit, endOfHuntingReportComments);

        return getEndOfHuntingReport(harvestPermit);
    }
}
