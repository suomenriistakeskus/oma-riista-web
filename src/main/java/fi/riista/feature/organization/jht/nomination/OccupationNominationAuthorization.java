package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class OccupationNominationAuthorization extends AbstractEntityAuthorization<OccupationNomination> {

    public enum Permission {
        PROPOSE,
        CANCEL,
        REJECT,
        ACCEPT
    }

    public enum Role {
        NOMINATION_COORDINATOR,
        ANY_COORDINATOR
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public OccupationNominationAuthorization() {
        allow(EntityPermission.READ, Role.NOMINATION_COORDINATOR);
        allow(EntityPermission.READ, SystemUser.Role.ROLE_MODERATOR);
        allow(EntityPermission.READ, SystemUser.Role.ROLE_ADMIN);

        allow(EntityPermission.DELETE, Role.NOMINATION_COORDINATOR);
        allow(EntityPermission.DELETE, SystemUser.Role.ROLE_MODERATOR);
        allow(EntityPermission.DELETE, SystemUser.Role.ROLE_ADMIN);

        allow(Permission.PROPOSE, Role.NOMINATION_COORDINATOR);
        allow(Permission.CANCEL, Role.NOMINATION_COORDINATOR);

        allow(Permission.REJECT, SystemUser.Role.ROLE_MODERATOR);
        allow(Permission.REJECT, SystemUser.Role.ROLE_ADMIN);

        allow(Permission.ACCEPT, SystemUser.Role.ROLE_MODERATOR);
        allow(Permission.ACCEPT, SystemUser.Role.ROLE_ADMIN);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final OccupationNomination nomination,
                                   @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(Role.ANY_COORDINATOR,
                    () -> userAuthorizationHelper.isCoordinatorAnywhere(activePerson));

            collector.addAuthorizationRole(Role.NOMINATION_COORDINATOR,
                    () -> userAuthorizationHelper.isCoordinator(nomination.getRhy(), activePerson));
        });
    }
}
