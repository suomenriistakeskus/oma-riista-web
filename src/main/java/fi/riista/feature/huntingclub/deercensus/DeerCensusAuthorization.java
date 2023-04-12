package fi.riista.feature.huntingclub.deercensus;

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
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class DeerCensusAuthorization extends AbstractEntityAuthorization<DeerCensus> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public DeerCensusAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
        allow(EntityPermission.READ, SEURAN_JASEN);
        allow(EntityPermission.CREATE, ROLE_USER);
    }

    @Override
    protected void authorizeTarget(
            @Nonnull final AuthorizationTokenCollector collector,
            @Nonnull final DeerCensus deerCensus,
            @Nonnull final UserInfo userInfo) {
        final HuntingClub huntingClub = deerCensus.getHuntingClub();
        if (huntingClub == null) {
            return;
        }

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(SEURAN_JASEN, () ->
                    userAuthorizationHelper.isClubMember(huntingClub, activePerson));

            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                    userAuthorizationHelper.isClubContact(huntingClub, activePerson));

            collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA, () ->
                    userAuthorizationHelper.isLeaderOfSomeClubHuntingGroup(huntingClub, activePerson));
        });
    }
}
