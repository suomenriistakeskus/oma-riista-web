package fi.riista.feature.huntingclub.hunting.day;

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
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class GroupHuntingDayAuthorization extends AbstractEntityAuthorization<GroupHuntingDay> {

    public enum Permission {
        LINK_DIARY_ENTRY_TO_HUNTING_DAY,
        CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT,
        UPDATE_MOOSE_DATA_CARD_ORIGINATED,
        DELETE_MOOSE_DATA_CARD_ORIGINATED,
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public GroupHuntingDayAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);

        allow(EntityPermission.READ, RYHMAN_JASEN);

        allow(Permission.LINK_DIARY_ENTRY_TO_HUNTING_DAY,
                ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);

        allow(Permission.CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.UPDATE_MOOSE_DATA_CARD_ORIGINATED, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.DELETE_MOOSE_DATA_CARD_ORIGINATED, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final GroupHuntingDay groupHuntingDay,
                                   @Nonnull final UserInfo userInfo) {
        if (groupHuntingDay.getGroup() == null) {
            return;
        }

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                    userAuthorizationHelper.isClubContact(groupHuntingDay.getGroup().getParentOrganisation(), activePerson));

            collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA, () ->
                    userAuthorizationHelper.isGroupLeader(groupHuntingDay.getGroup(), activePerson));

            collector.addAuthorizationRole(RYHMAN_JASEN, () ->
                    userAuthorizationHelper.isGroupMember(groupHuntingDay.getGroup(), activePerson));
        });
    }
}
