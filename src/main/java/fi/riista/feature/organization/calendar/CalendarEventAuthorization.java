package fi.riista.feature.organization.calendar;

import fi.riista.feature.organization.rhy.RhyRole;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;

@Component
public class CalendarEventAuthorization extends AbstractEntityAuthorization {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    public CalendarEventAuthorization() {
        super("calendarevent");

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, RhyRole.COORDINATOR, ROLE_USER);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, RhyRole.COORDINATOR);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, RhyRole.COORDINATOR);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, RhyRole.COORDINATOR);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {

        Optional.ofNullable(getOrganisation(target))
                .filter(org -> org.getOrganisationType() == OrganisationType.RHY)
                .ifPresent(rhy -> collector.addAuthorizationRole(
                        RhyRole.COORDINATOR, () -> userAuthorizationHelper.isCoordinator(rhy, userInfo)));
    }

    private Organisation getOrganisation(final EntityAuthorizationTarget target) {
        final CalendarEvent event = getCalendarEvent(target);
        if (event != null) {
            return event.getOrganisation();
        }
        final CalendarEventDTO dto = target.getAuthorizationTarget(CalendarEventDTO.class);
        if (dto != null && F.hasId(dto.getOrganisation())) {
            return organisationRepository.findOne(dto.getOrganisation().getId());
        }
        return null;
    }

    private CalendarEvent getCalendarEvent(final EntityAuthorizationTarget target) {
        if (target.getAuthorizationTargetId() != null) {
            return calendarEventRepository.findOne((Long) target.getAuthorizationTargetId());
        }
        final CalendarEvent event = target.getAuthorizationTarget(CalendarEvent.class);
        if (event != null) {
            return event;
        }

        final CalendarEventDTO dto = target.getAuthorizationTarget(CalendarEventDTO.class);

        return F.hasId(dto) ? calendarEventRepository.findOne(dto.getId()) : null;
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[] { CalendarEvent.class, CalendarEventDTO.class };
    }
}
