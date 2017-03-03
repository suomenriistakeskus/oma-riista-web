package fi.riista.feature.announcement;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Optional;

@Component
public class AnnouncementAuthorization extends AbstractEntityAuthorization<Announcement> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public AnnouncementAuthorization() {
        allow(EntityPermission.READ, OccupationType.SEURAN_YHDYSHENKILO, OccupationType.TOIMINNANOHJAAJA);
        allow(EntityPermission.UPDATE, OccupationType.SEURAN_YHDYSHENKILO, OccupationType.TOIMINNANOHJAAJA);
        allow(EntityPermission.DELETE, OccupationType.SEURAN_YHDYSHENKILO, OccupationType.TOIMINNANOHJAAJA);

        allow(EntityPermission.READ, SystemUser.Role.ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
        allow(EntityPermission.UPDATE, SystemUser.Role.ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
        allow(EntityPermission.DELETE, SystemUser.Role.ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Announcement announcement,
                                   @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            findAuthorizationRole(announcement, activePerson).ifPresent(collector::addAuthorizationRole);
        });
    }

    private Optional<Enum<?>> findAuthorizationRole(final Announcement announcement, final Person person) {
        final Organisation fromOrganisation = announcement.getFromOrganisation();

        switch (fromOrganisation.getOrganisationType()) {
            case CLUB:
                return userAuthorizationHelper.isClubContact(fromOrganisation, person)
                        ? Optional.of(OccupationType.SEURAN_YHDYSHENKILO)
                        : Optional.empty();
            case RHY:
                return userAuthorizationHelper.isCoordinator(fromOrganisation, person)
                        ? Optional.of(OccupationType.TOIMINNANOHJAAJA)
                        : Optional.empty();
            default:
                return Optional.empty();
        }
    }
}
