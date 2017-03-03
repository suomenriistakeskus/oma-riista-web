package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class SrvaEventAuthorization extends AbstractEntityAuthorization<SrvaEvent> {

    private enum Role {
        AUTHOR,
        RHY_COORDINATOR,
        RHY_SRVA_CONTACT_PERSON
    }

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public SrvaEventAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR, Role.AUTHOR);

        allow(EntityPermission.READ, Role.RHY_COORDINATOR, Role.RHY_SRVA_CONTACT_PERSON);
        allow(EntityPermission.UPDATE, Role.RHY_COORDINATOR, Role.RHY_SRVA_CONTACT_PERSON);
        allow(EntityPermission.DELETE, Role.RHY_COORDINATOR, Role.RHY_SRVA_CONTACT_PERSON);
    }

    @Override
    protected void authorizeTarget(@Nonnull AuthorizationTokenCollector collector,
                                   @Nonnull SrvaEvent srvaEvent,
                                   @Nonnull UserInfo userInfo) {

        final Person authenticatedPerson = getAuthenticatedPerson(userInfo);

        collector.addAuthorizationRole(Role.AUTHOR,
                () -> Objects.equals(authenticatedPerson, srvaEvent.getAuthor()));

        collector.addAuthorizationRole(Role.RHY_COORDINATOR,
                () -> userAuthorizationHelper.isCoordinator(srvaEvent.getRhy(), userInfo) ||
                        srvaEvent.isAccident() && userAuthorizationHelper.isCoordinatorAnywhere(userInfo));

        collector.addAuthorizationRole(Role.RHY_SRVA_CONTACT_PERSON,
                () -> userAuthorizationHelper.isSrvaContactPerson(srvaEvent.getRhy(), userInfo) ||
                        srvaEvent.isAccident() && userAuthorizationHelper.isSrvaContactPersonAnywhere(userInfo));
    }

    private Person getAuthenticatedPerson(final UserInfo userInfo) {
        return userInfo.getUserId() != null ? userRepository.getOne(userInfo.getUserId()).getPerson() : null;
    }
}
