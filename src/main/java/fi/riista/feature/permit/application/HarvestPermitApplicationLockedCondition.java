package fi.riista.feature.permit.application;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.security.UserInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HarvestPermitApplicationLockedCondition {

    @Resource
    private ActiveUserService activeUserService;

    public void assertCanUpdate(final HarvestPermitApplication application) {
        if (!canUpdate(application, activeUserService.getActiveUserInfoOrNull())) {
            throw new HarvestPermitApplicationReadOnlyException(application.getId());
        }
    }

    private static boolean canUpdate(final HarvestPermitApplication application,
                                     final UserInfo activeUserInfo) {
        switch (application.getStatus()) {
            case DRAFT:
                return true;
            case AMENDING:
                return activeUserInfo.isAdminOrModerator();
            default:
            case ACTIVE:
            case HIDDEN:
                return false;
        }
    }
}
