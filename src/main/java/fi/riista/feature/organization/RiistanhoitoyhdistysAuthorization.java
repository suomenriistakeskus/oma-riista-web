package fi.riista.feature.organization;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class RiistanhoitoyhdistysAuthorization extends AbstractEntityAuthorization<Riistanhoitoyhdistys> {

    public enum Permission {
        LIST_SRVA
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public RiistanhoitoyhdistysAuthorization() {
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(EntityPermission.UPDATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA);
        allow(Permission.LIST_SRVA, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, SRVA_YHTEYSHENKILO);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final Riistanhoitoyhdistys rhy,
                                   @Nonnull final UserInfo userInfo) {
        collector.addAuthorizationRole(TOIMINNANOHJAAJA, () ->
                userAuthorizationHelper.isCoordinator(rhy, userInfo));

        collector.addAuthorizationRole(SRVA_YHTEYSHENKILO, () ->
                userAuthorizationHelper.isSrvaContactPerson(rhy, userInfo));
    }
}
