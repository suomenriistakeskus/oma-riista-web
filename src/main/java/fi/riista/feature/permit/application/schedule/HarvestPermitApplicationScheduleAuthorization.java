package fi.riista.feature.permit.application.schedule;

import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_APPLICATION_SCHEDULE;

@Component
public class HarvestPermitApplicationScheduleAuthorization extends AbstractEntityAuthorization<HarvestPermitApplicationSchedule> {

    public enum Role {
        APPLICATION_SCHEDULE_MODERATOR
    }

    public HarvestPermitApplicationScheduleAuthorization() {
        allow(EntityPermission.UPDATE, ROLE_ADMIN, Role.APPLICATION_SCHEDULE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HarvestPermitApplicationSchedule schedule,
                                   @Nonnull final UserInfo userInfo) {
        collector.addAuthorizationRole(Role.APPLICATION_SCHEDULE_MODERATOR,
                () -> userInfo.hasPrivilege(MODERATE_APPLICATION_SCHEDULE));
    }
}
