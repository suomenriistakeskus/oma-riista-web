package fi.riista.feature.permit.application;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization.Role.CONTACT_PERSON;
import static fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization.Role.COORDINATOR_ANYWHERE;

@Component
public class HarvestPermitApplicationAuthorization extends AbstractEntityAuthorization<HarvestPermitApplication> {
    public enum Permission {
        LIST_CONFLICTS,
        AMEND
    }

    public enum Role {
        COORDINATOR_ANYWHERE,
        CONTACT_PERSON
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public HarvestPermitApplicationAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR, COORDINATOR_ANYWHERE);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.LIST_CONFLICTS, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.AMEND, ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(CONTACT_PERSON);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HarvestPermitApplication application,
                                   @Nonnull final UserInfo userInfo) {

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(CONTACT_PERSON, () ->
                    activePerson.equals(application.getContactPerson()));
            collector.addAuthorizationRole(COORDINATOR_ANYWHERE, () ->
                    userAuthorizationHelper.isCoordinatorAnywhere(activePerson));
        });
    }
}
