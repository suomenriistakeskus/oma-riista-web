package fi.riista.feature.organization.occupation;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;

@Component
public class OccupationAuthorization extends AbstractEntityAuthorization<Occupation> {
    private enum Role {
        PERSONAL_CLUB_MEMBERSHIP,
        PERSONAL_OCCUPATION
    }

    public enum OccupationPermission {
        UPDATE_CONTACT_INFO_VISIBILITY
    }


    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public OccupationAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);
        allowCRUD(TOIMINNANOHJAAJA);
        allowCRUD(SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);

        allow(EntityPermission.CREATE, Role.PERSONAL_CLUB_MEMBERSHIP);
        allow(EntityPermission.READ, SEURAN_JASEN, RYHMAN_JASEN);
        allow(EntityPermission.DELETE, SEURAN_JASEN, RYHMAN_JASEN, Role.PERSONAL_CLUB_MEMBERSHIP);

        allow(OccupationPermission.UPDATE_CONTACT_INFO_VISIBILITY, Role.PERSONAL_OCCUPATION, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(
            @Nonnull final AuthorizationTokenCollector collector,
            @Nonnull final Occupation occupation,
            @Nonnull final UserInfo userInfo) {
        final Organisation occupationOrganisation = occupation.getOrganisation();
        final Person occupationPerson = occupation.getPerson();

        if (occupationOrganisation == null || occupationPerson == null) {
            return;
        }

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(Role.PERSONAL_OCCUPATION, () ->
                    Objects.equals(activePerson, occupationPerson));

            final OrganisationType organisationType = occupationOrganisation.getOrganisationType();

            if (organisationType == OrganisationType.RHY) {
                collector.addAuthorizationRole(TOIMINNANOHJAAJA, () ->
                        userAuthorizationHelper.isCoordinator(occupationOrganisation, activePerson));

            } else if (organisationType == OrganisationType.CLUB) {
                collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                        userAuthorizationHelper.isClubContact(occupationOrganisation, activePerson));

                collector.addAuthorizationRole(SEURAN_JASEN, () ->
                        Objects.equals(activePerson, occupationPerson)
                                && userAuthorizationHelper.isClubMember(occupationOrganisation, activePerson));

                // user accepting invitation and creating occupation
                collector.addAuthorizationRole(Role.PERSONAL_CLUB_MEMBERSHIP, () ->
                        Objects.equals(activePerson, occupationPerson));

            } else if (organisationType == OrganisationType.CLUBGROUP) {
                collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                        userAuthorizationHelper.isClubContact(occupationOrganisation.getParentOrganisation(), activePerson));

                collector.addAuthorizationRole(RYHMAN_METSASTYKSENJOHTAJA, () ->
                        userAuthorizationHelper.isGroupLeader(occupationOrganisation, activePerson));

                collector.addAuthorizationRole(RYHMAN_JASEN, () ->
                        Objects.equals(activePerson, occupationPerson)
                                && userAuthorizationHelper.isGroupMember(occupationOrganisation, activePerson));
            }
        });
    }
}
