package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.SimpleEntityDTOAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

@Component
public class OccupationNominationAuthorization
        extends SimpleEntityDTOAuthorization<OccupationNomination, OccupationNominationDTO, Long> {

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
    private OccupationNominationRepository occupationNominationRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public OccupationNominationAuthorization() {
        super("occupationNomination");

        allow(READ, Role.NOMINATION_COORDINATOR);
        allow(READ, SystemUser.Role.ROLE_MODERATOR);
        allow(READ, SystemUser.Role.ROLE_ADMIN);

        allow(DELETE, Role.NOMINATION_COORDINATOR);
        allow(DELETE, SystemUser.Role.ROLE_MODERATOR);
        allow(DELETE, SystemUser.Role.ROLE_ADMIN);

        allow(Permission.PROPOSE, Role.NOMINATION_COORDINATOR);
        allow(Permission.CANCEL, Role.NOMINATION_COORDINATOR);

        allow(Permission.REJECT, SystemUser.Role.ROLE_MODERATOR);
        allow(Permission.REJECT, SystemUser.Role.ROLE_ADMIN);

        allow(Permission.ACCEPT, SystemUser.Role.ROLE_MODERATOR);
        allow(Permission.ACCEPT, SystemUser.Role.ROLE_ADMIN);
    }

    @Override
    protected JpaRepository<OccupationNomination, Long> getRepository() {
        return occupationNominationRepository;
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {
        Optional.ofNullable(userAuthorizationHelper.getPerson(userInfo)).ifPresent(person -> {
            collector.addAuthorizationRole(Role.ANY_COORDINATOR,
                    () -> userAuthorizationHelper.isCoordinatorAnywhere(person));

            findEntity(target).ifPresent(nomination -> {
                collector.addAuthorizationRole(Role.NOMINATION_COORDINATOR,
                        () -> userAuthorizationHelper.isCoordinator(nomination.getRhy(), person));
            });
        });
    }
}
