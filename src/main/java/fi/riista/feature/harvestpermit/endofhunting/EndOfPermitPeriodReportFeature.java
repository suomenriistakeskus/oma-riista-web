package fi.riista.feature.harvestpermit.endofhunting;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.harvestpermit.usage.PermitUsageDTO;
import fi.riista.feature.harvestpermit.usage.PermitUsageService;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

@Service
public class EndOfPermitPeriodReportFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private EndOfHuntingReportCreateService endOfHuntingReportCreateService;

    @Resource
    private PermitUsageService permitUsageService;

    @Transactional(readOnly = true)
    public EndOfPermitPeriodReportDTO getEndOfPermitPeriodReport(final long permitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        Preconditions.checkState(PermitTypeCode.isGameManagementPermitTypeCode(harvestPermit.getPermitTypeCode()), "Permit type must be game management");

        return getEndOfPermitPeriodReport(harvestPermit);
    }

    @Transactional
    public EndOfPermitPeriodReportDTO createEndOfPermitPeriodReport(final long permitId,
                                                                    final EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId,
                HarvestPermitAuthorization.Permission.CREATE_REMOVE_HARVEST_REPORT);

        endOfHuntingReportCreateService.createEndOfHuntingReport(harvestPermit, endOfHuntingReportComments);

        return getEndOfPermitPeriodReport(harvestPermit);
    }

    @Nonnull
    private EndOfPermitPeriodReportDTO getEndOfPermitPeriodReport(final HarvestPermit harvestPermit) {
        final SystemUser activeUser = activeUserService.requireActiveUser();

        final List<PermitUsageDTO> usages = permitUsageService.getPermitUsage(harvestPermit);

        return new EndOfPermitPeriodReportDTO(harvestPermit, activeUser, usages);
    }
}
