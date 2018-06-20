package fi.riista.feature.shootingtest;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.organization.rhy.RhyAuthorizationHelper;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.shootingtest.AbstractShootingTestEntityAuthorization.Role.ASSIGNED_SHOOTING_TEST_OFFICIAL;

public abstract class AbstractShootingTestEntityAuthorization<T extends BaseEntity<?>>
        extends AbstractEntityAuthorization<T> {

    public enum Role {
        ASSIGNED_SHOOTING_TEST_OFFICIAL
    }

    @Resource
    private RhyAuthorizationHelper helper;

    protected abstract ShootingTestEvent getShootingTestEvent(T entity);

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final T entity,
                                   @Nonnull final UserInfo userInfo) {

        final ShootingTestEvent event = getShootingTestEvent(entity);

        helper.collectAllRhyRoles(event.getCalendarEvent().getOrganisation(), collector, userInfo);

        if (!collector.hasPermission()) {
            collector.addAuthorizationRole(ASSIGNED_SHOOTING_TEST_OFFICIAL,
                    () -> helper.isPermittedAsAssignedOfficial(event, userInfo));
        }
    }
}
