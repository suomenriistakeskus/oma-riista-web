package fi.riista.feature.huntingclub.moosedatacard;

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
public class MooseDataCardImportAuthorization
        extends SimpleEntityDTOAuthorization<MooseDataCardImport, MooseDataCardImportDTO, Long> {

    @Resource
    private MooseDataCardImportRepository importRepo;

    @Resource
    private HuntingClubGroupRepository groupRepo;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public MooseDataCardImportAuthorization() {
        super("moosedatacardimport");

        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR);
        allow(READ,   ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA, RYHMAN_JASEN);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected JpaRepository<MooseDataCardImport, Long> getRepository() {
        return importRepo;
    }

    @Override
    protected void authorizeTarget(
            final AuthorizationTokenCollector collector,
            final EntityAuthorizationTarget target,
            final UserInfo userInfo) {

        Optional.ofNullable(userAuthorizationHelper.getPerson(userInfo))
                .ifPresent(person -> findHuntingGroup(target).ifPresent(group -> {

                    collector.addAuthorizationRole(
                            SEURAN_YHDYSHENKILO,
                            () -> userAuthorizationHelper.isClubContact(group.getParentOrganisation(), person));

                    collector.addAuthorizationRole(
                            RYHMAN_METSASTYKSENJOHTAJA, () -> userAuthorizationHelper.isGroupLeader(group, person));

                    collector.addAuthorizationRole(
                            RYHMAN_JASEN, () -> userAuthorizationHelper.isGroupMember(group, person));

                }));
    }

    private Optional<HuntingClubGroup> findHuntingGroup(final EntityAuthorizationTarget target) {
        final Optional<MooseDataCardImportDTO> dtoOpt = findDto(target);

        return dtoOpt.isPresent()
                ? dtoOpt.map(MooseDataCardImportDTO::getHuntingGroupId).map(groupRepo::findOne)
                : findEntity(target).map(MooseDataCardImport::getGroup);
    }

}
