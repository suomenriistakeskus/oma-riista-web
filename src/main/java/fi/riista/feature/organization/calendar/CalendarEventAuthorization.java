package fi.riista.feature.organization.calendar;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class CalendarEventAuthorization extends AbstractEntityAuthorization<CalendarEvent> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public CalendarEventAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(EntityPermission.READ, ROLE_USER);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final CalendarEvent calendarEvent,
                                   @Nonnull final UserInfo userInfo) {
        Optional.ofNullable(calendarEvent.getOrganisation())
                .filter(org -> org.getOrganisationType() == OrganisationType.RHY)
                .ifPresent(rhy -> collector.addAuthorizationRole(TOIMINNANOHJAAJA, () ->
                        userAuthorizationHelper.isCoordinator(rhy, userInfo)));
    }
}
