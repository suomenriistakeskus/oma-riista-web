package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class PersonAuthorization extends AbstractEntityAuthorization<Person> {

    public enum Permission {
        DEACTIVATE
    }

    public enum Role {
        SELF
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public PersonAuthorization() {
        allowCRUD(ROLE_ADMIN);

        allow(EntityPermission.READ, ROLE_MODERATOR, Role.SELF);
        allow(EntityPermission.UPDATE, ROLE_MODERATOR, Role.SELF);

        allow(Permission.DEACTIVATE, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Person person,
                                   @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(Role.SELF, () ->
                    Objects.equals(F.getId(activePerson), F.getId(person)));
        });
    }
}
