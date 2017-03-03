package fi.riista.feature.huntingclub.group;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class HuntingClubGroupAuthorization extends AbstractEntityAuthorization<HuntingClubGroup> {

    public enum Permission {
        LINK_DIARY_ENTRY_TO_HUNTING_DAY
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public HuntingClubGroupAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(SEURAN_YHDYSHENKILO);

        allow(EntityPermission.READ, SEURAN_JASEN, RYHMAN_METSASTYKSENJOHTAJA, RYHMAN_JASEN);
        allow(EntityPermission.UPDATE, RYHMAN_METSASTYKSENJOHTAJA);

        allow(Permission.LINK_DIARY_ENTRY_TO_HUNTING_DAY,
                ROLE_ADMIN, ROLE_MODERATOR,
                SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Override
    protected void authorizeTarget(
            @Nonnull final AuthorizationTokenCollector collector,
            @Nonnull final HuntingClubGroup group,
            @Nonnull final UserInfo userInfo) {
        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            final Organisation club = group.getParentOrganisation();

            if (userAuthorizationHelper.isClubContact(club, activePerson)) {
                collector.addAuthorizationRole(SEURAN_YHDYSHENKILO);
            }
            if (userAuthorizationHelper.isClubMember(club, activePerson)) {
                collector.addAuthorizationRole(SEURAN_JASEN);
            }

            if (!group.isNew()) {
                userAuthorizationHelper.findValidRolesInOrganisation(group, activePerson)
                        .forEach(collector::addAuthorizationRole);
            }
        });
    }
}
