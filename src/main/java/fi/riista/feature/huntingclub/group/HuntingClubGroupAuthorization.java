package fi.riista.feature.huntingclub.group;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.huntingclub.members.ClubRole.RYHMAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_YHDYSHENKILO;

@Component
public class HuntingClubGroupAuthorization extends AbstractEntityAuthorization {

    public enum HuntingGroupPermission {
        LINK_DIARY_ENTRY_TO_HUNTING_DAY
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubRepository clubRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    public HuntingClubGroupAuthorization() {
        super("huntingClubGroup");

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, SEURAN_JASEN, RYHMAN_METSASTYKSENJOHTAJA, RYHMAN_JASEN);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO);

        allow(HuntingGroupPermission.LINK_DIARY_ENTRY_TO_HUNTING_DAY,
                ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Override
    protected void authorizeTarget(
            final AuthorizationTokenCollector collector,
            final EntityAuthorizationTarget target,
            final UserInfo userInfo) {

        final Person person = userAuthorizationHelper.getPerson(userInfo);

        if (person == null) {
            return;
        }

        final Organisation org = getOrganisation(target);

        if (org != null) {
            if (org.getOrganisationType() == OrganisationType.CLUBGROUP) {
                final Set<OccupationType> groupRoles =
                        userAuthorizationHelper.findValidRolesInOrganisation(org, person);

                if (groupRoles.contains(OccupationType.RYHMAN_JASEN)) {
                    collector.addAuthorizationRole(RYHMAN_JASEN);
                }
                if (groupRoles.contains(OccupationType.RYHMAN_METSASTYKSENJOHTAJA)) {
                    collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA);
                }

                final Organisation club = org.getParentOrganisation();
                if (userAuthorizationHelper.isClubContact(club, person)) {
                    collector.addAuthorizationRole(SEURAN_YHDYSHENKILO);
                }
                if (userAuthorizationHelper.isClubMember(club, person)) {
                    collector.addAuthorizationRole(SEURAN_JASEN);
                }
            }
        } else { // org == null
            Optional.ofNullable(target.getAuthorizationTarget(HuntingClubGroupDTO.class))
                    .filter(Objects::nonNull)
                    .ifPresent(dto -> {
                        final HuntingClub club = clubRepository.getOne(dto.getClubId());

                        if (userAuthorizationHelper.isClubContact(club, person)) {
                            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO);
                        }
                    });
        }
    }

    private Organisation getOrganisation(final EntityAuthorizationTarget target) {
        return target.getAuthorizationTargetId() != null
                ? organisationRepository.findOne((Long) target.getAuthorizationTargetId())
                : target.getAuthorizationTarget(Organisation.class);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[] { HuntingClubGroup.class, HuntingClubGroupDTO.class };
    }

}
