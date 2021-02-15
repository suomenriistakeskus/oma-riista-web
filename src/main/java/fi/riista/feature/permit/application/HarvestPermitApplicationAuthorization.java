package fi.riista.feature.permit.application;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_DISABILITY_PERMIT_APPLICATION;
import static fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization.Role.CONTACT_PERSON;
import static fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization.Role.COORDINATOR_ANYWHERE;
import static fi.riista.feature.permit.application.HarvestPermitApplicationAuthorization.Role.HARVEST_PERMIT_APPLICATION_MODERATOR;

@Component
public class HarvestPermitApplicationAuthorization extends AbstractEntityAuthorization<HarvestPermitApplication> {
    public enum Permission {
        LIST_CONFLICTS,
        AMEND,
        CANCEL
    }

    public enum Role {
        COORDINATOR_ANYWHERE,
        CONTACT_PERSON,
        HARVEST_PERMIT_APPLICATION_MODERATOR
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public HarvestPermitApplicationAuthorization() {
        allowCRUD(ROLE_ADMIN, HARVEST_PERMIT_APPLICATION_MODERATOR, CONTACT_PERSON);
        allow(EntityPermission.READ, COORDINATOR_ANYWHERE);
        allow(Permission.LIST_CONFLICTS, ROLE_ADMIN, HARVEST_PERMIT_APPLICATION_MODERATOR);
        allow(Permission.AMEND, ROLE_ADMIN, HARVEST_PERMIT_APPLICATION_MODERATOR);
        allow(Permission.CANCEL, ROLE_ADMIN, HARVEST_PERMIT_APPLICATION_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HarvestPermitApplication application,
                                   @Nonnull final UserInfo userInfo) {

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson ->
            collector.addAuthorizationRole(CONTACT_PERSON, () ->
                    activePerson.equals(application.getContactPerson())));

        if (application.getHarvestPermitCategory() == HarvestPermitCategory.DISABILITY) {
            collector.addAuthorizationRole(HARVEST_PERMIT_APPLICATION_MODERATOR, () ->
                    userInfo.hasPrivilege(MODERATE_DISABILITY_PERMIT_APPLICATION));
        } else {
            collector.addAuthorizationRole(HARVEST_PERMIT_APPLICATION_MODERATOR, () ->
                    userInfo.isModerator());
            userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson ->
                    collector.addAuthorizationRole(COORDINATOR_ANYWHERE, () ->
                            userAuthorizationHelper.isCoordinatorAnywhere(activePerson)));
        }
    }
}
