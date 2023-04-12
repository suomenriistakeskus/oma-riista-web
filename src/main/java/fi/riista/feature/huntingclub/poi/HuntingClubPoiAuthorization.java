package fi.riista.feature.huntingclub.poi;

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
public class HuntingClubPoiAuthorization extends AbstractEntityAuthorization<PoiIdAllocation> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public HuntingClubPoiAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(SEURAN_YHDYSHENKILO);
        allow(EntityPermission.READ, SEURAN_JASEN);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final PoiIdAllocation poi,
                                   @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            final HuntingClub club = poi.getClub();

            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO,
                    () -> userAuthorizationHelper.isClubContact(club, activePerson));

            collector.addAuthorizationRole(SEURAN_JASEN,
                    () -> userAuthorizationHelper.isClubMember(club, activePerson));
        });
    }
}
