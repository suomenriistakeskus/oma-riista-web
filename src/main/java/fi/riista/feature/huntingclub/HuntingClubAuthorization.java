package fi.riista.feature.huntingclub;

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
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class HuntingClubAuthorization extends AbstractEntityAuthorization<HuntingClub> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public HuntingClubAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);

        allow(EntityPermission.CREATE, ROLE_USER);
        allow(EntityPermission.READ, SEURAN_JASEN, SEURAN_YHDYSHENKILO);
        allow(EntityPermission.UPDATE, SEURAN_YHDYSHENKILO);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HuntingClub club,
                                   @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                    userAuthorizationHelper.isClubContact(club, activePerson));

            collector.addAuthorizationRole(SEURAN_JASEN, () ->
                    userAuthorizationHelper.isClubMember(club, activePerson));
        });
    }
}
