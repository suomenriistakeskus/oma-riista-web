package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
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
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class MooseDataCardImportAuthorization extends AbstractEntityAuthorization<MooseDataCardImport> {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public MooseDataCardImportAuthorization() {
        allow(EntityPermission.CREATE, ROLE_ADMIN, ROLE_MODERATOR);
        allow(EntityPermission.READ, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, RYHMAN_JASEN);
        allow(EntityPermission.DELETE, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(
            @Nonnull final AuthorizationTokenCollector collector,
            @Nonnull final MooseDataCardImport mooseDataCardImport,
            @Nonnull final UserInfo userInfo) {
        final HuntingClubGroup group = mooseDataCardImport.getGroup();

        if (group == null) {
            return;
        }

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                    userAuthorizationHelper.isClubContact(group.getParentOrganisation(), activePerson));

            collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA, () ->
                    userAuthorizationHelper.isGroupLeader(group, activePerson));

            collector.addAuthorizationRole(RYHMAN_JASEN, () ->
                    userAuthorizationHelper.isGroupMember(group, activePerson));
        });
    }
}
