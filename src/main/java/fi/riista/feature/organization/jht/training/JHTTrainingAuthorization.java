package fi.riista.feature.organization.jht.training;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class JHTTrainingAuthorization extends AbstractEntityAuthorization<JHTTraining> {

    public enum Permission {
        PROPOSE
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public JHTTrainingAuthorization() {
        allow(EntityPermission.CREATE, ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
        allow(EntityPermission.DELETE, ROLE_MODERATOR, SystemUser.Role.ROLE_ADMIN);
        allow(Permission.PROPOSE, TOIMINNANOHJAAJA);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final JHTTraining training,
                                   @Nonnull final UserInfo userInfo) {

        collector.addAuthorizationRole(TOIMINNANOHJAAJA, () -> userAuthorizationHelper.isCoordinatorAnywhere(userInfo));
    }
}
