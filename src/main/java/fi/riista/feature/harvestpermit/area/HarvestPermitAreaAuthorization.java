package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class HarvestPermitAreaAuthorization extends AbstractEntityAuthorization<HarvestPermitArea> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public HarvestPermitAreaAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);

        allow(EntityPermission.CREATE, SEURAN_YHDYSHENKILO);
        allow(EntityPermission.READ, SEURAN_YHDYSHENKILO, SEURAN_JASEN);
        allow(EntityPermission.UPDATE, SEURAN_YHDYSHENKILO);
        allow(EntityPermission.DELETE, SEURAN_YHDYSHENKILO);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HarvestPermitArea harvestPermitArea,
                                   @Nonnull final UserInfo userInfo) {
        final HuntingClub club = harvestPermitArea.getClub();

        if (club == null) {
            return;
        }

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                    userAuthorizationHelper.isClubContact(club, activePerson));

            collector.addAuthorizationRole(SEURAN_JASEN, () ->
                    userAuthorizationHelper.isClubMember(club, activePerson));
        });
    }
}
