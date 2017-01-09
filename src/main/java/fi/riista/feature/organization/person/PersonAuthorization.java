package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class PersonAuthorization extends AbstractEntityAuthorization {

    public enum Permission {
        DEACTIVATE
    }

    public enum Role {
        SELF
    }

    @Resource
    private UserRepository userRepository;

    @Resource
    private PersonRepository personRepository;

    public PersonAuthorization() {
        super("person");

        allow(Permission.DEACTIVATE, ROLE_ADMIN, ROLE_MODERATOR);

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, Role.SELF);
        allow(CREATE, ROLE_ADMIN);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, Role.SELF);
        allow(DELETE, ROLE_ADMIN);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {
        final SystemUser user = getSystemUser(userInfo);
        final Person targetPerson = getPerson(target);

        if (user == null || targetPerson == null || user.getPerson() == null) {
            return;
        }

        collector.addAuthorizationRole(Role.SELF, () -> Objects.equals(user.getPerson().getId(), targetPerson.getId()));
    }

    private Person getPerson(EntityAuthorizationTarget target) {
        if (target.getAuthorizationTargetId() != null) {
            return personRepository.findOne((Long) target.getAuthorizationTargetId());
        }
        return target.getAuthorizationTarget(Person.class);
    }

    private SystemUser getSystemUser(UserInfo userInfo) {
        if (userInfo.getUserId() != null) {
            return userRepository.findOne(userInfo.getUserId());
        }
        return null;
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[] { Person.class, PersonDTO.class };
    }
}
