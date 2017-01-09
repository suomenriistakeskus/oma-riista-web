package fi.riista.feature.huntingclub;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_YHDYSHENKILO;

@Component
public class HuntingClubAuthorization extends AbstractEntityAuthorization {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private OrganisationRepository organisationRepository;

    public HuntingClubAuthorization() {
        super("huntingClub");

        allow(READ,   ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, SEURAN_JASEN);
        allow(CREATE, ROLE_USER);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {

        final Organisation org = getOrganisation(target);
        final Person person = userAuthorizationHelper.getPerson(userInfo);

        if (person != null && isClub(org)) {
            collector.addAuthorizationRole(
                    SEURAN_YHDYSHENKILO, () -> userAuthorizationHelper.isClubContact(org, person));

            collector.addAuthorizationRole(SEURAN_JASEN, () -> userAuthorizationHelper.isClubMember(org, person));
        }
    }

    private static boolean isClub(final Organisation org) {
        return org != null && org.getOrganisationType() == OrganisationType.CLUB;
    }

    private Organisation getOrganisation(final EntityAuthorizationTarget target) {
        return target.getAuthorizationTargetId() != null
                ? organisationRepository.findOne((Long) target.getAuthorizationTargetId())
                : target.getAuthorizationTarget(Organisation.class);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[] { HuntingClub.class, HuntingClubDTO.class };
    }

}
