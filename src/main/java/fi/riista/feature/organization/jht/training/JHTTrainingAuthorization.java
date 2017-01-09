package fi.riista.feature.organization.jht.training;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.rhy.RhyRole;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.SimpleEntityDTOAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class JHTTrainingAuthorization
        extends SimpleEntityDTOAuthorization<JHTTraining, JHTTrainingDTO, Long> {

    public enum Permission {
        PROPOSE
    }

    @Resource
    private JHTTrainingRepository jhtTrainingRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public JHTTrainingAuthorization() {
        super("jhtTraining");

        allow(CREATE, RhyRole.COORDINATOR, SystemUser.Role.ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
        allow(DELETE, SystemUser.Role.ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
        allow(Permission.PROPOSE, RhyRole.COORDINATOR);
    }

    @Override
    protected JpaRepository<JHTTraining, Long> getRepository() {
        return jhtTrainingRepository;
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {
        final Person activePerson = userAuthorizationHelper.getPerson(userInfo);

        collector.addAuthorizationRole(RhyRole.COORDINATOR,
                () -> activePerson != null && userAuthorizationHelper.isCoordinatorAnywhere(activePerson));
    }
}
