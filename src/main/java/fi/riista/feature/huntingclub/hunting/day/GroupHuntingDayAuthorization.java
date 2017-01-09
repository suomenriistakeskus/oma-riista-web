package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.SimpleEntityDTOAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.huntingclub.members.ClubRole.RYHMAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_YHDYSHENKILO;

@Component
public class GroupHuntingDayAuthorization
        extends SimpleEntityDTOAuthorization<GroupHuntingDay, GroupHuntingDayDTO, Long> {

    public enum Permission {
        LINK_DIARY_ENTRY_TO_HUNTING_DAY,
        CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT,
        UPDATE_MOOSE_DATA_CARD_ORIGINATED,
        DELETE_MOOSE_DATA_CARD_ORIGINATED,
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    public GroupHuntingDayAuthorization() {
        super("groupHuntingDay");

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, RYHMAN_JASEN);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);

        allow(Permission.LINK_DIARY_ENTRY_TO_HUNTING_DAY,
                ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);

        allow(Permission.CREATE_WITHIN_MOOSE_DATA_CARD_IMPORT, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.UPDATE_MOOSE_DATA_CARD_ORIGINATED, ROLE_ADMIN, ROLE_MODERATOR);
        allow(Permission.DELETE_MOOSE_DATA_CARD_ORIGINATED, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected JpaRepository<GroupHuntingDay, Long> getRepository() {
        return groupHuntingDayRepository;
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {

        Optional.ofNullable(userAuthorizationHelper.getPerson(userInfo))
                .ifPresent(person -> findHuntingGroup(target).ifPresent(group -> {

                    collector.addAuthorizationRole(
                            SEURAN_YHDYSHENKILO,
                            () -> userAuthorizationHelper.isClubContact(group.getParentOrganisation(), person));

                    collector.addAuthorizationRole(
                            RYHMAN_METSASTYKSENJOHTAJA,
                            () -> userAuthorizationHelper.isGroupLeader(group, person));

                    collector.addAuthorizationRole(
                            RYHMAN_JASEN, () -> userAuthorizationHelper.isGroupMember(group, person));
                }));
    }

    private Optional<HuntingClubGroup> findHuntingGroup(final EntityAuthorizationTarget target) {
        final Optional<GroupHuntingDayDTO> dtoOpt = findDto(target);

        return dtoOpt.isPresent()
                ? dtoOpt.map(GroupHuntingDayDTO::getHuntingGroupId).map(huntingClubGroupRepository::getOne)
                : findEntity(target).map(GroupHuntingDay::getGroup);
    }

}
