package fi.riista.feature.huntingclub.area;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_YHDYSHENKILO;

@Component
public class HuntingClubAreaAuthorization extends AbstractEntityAuthorization {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    public HuntingClubAreaAuthorization() {
        super("huntingClubArea");

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, SEURAN_JASEN);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {

        final Person person = userAuthorizationHelper.getPerson(userInfo);

        if (person == null) {
            return;
        }

        final HuntingClubArea area = getArea(target);

        if (area == null) {
            collector.addAuthorizationRole(
                    SEURAN_YHDYSHENKILO,
                    () -> userAuthorizationHelper.hasRoleAnywhere(person, OccupationType.SEURAN_YHDYSHENKILO));

        } else {
            final HuntingClub org = area.getClub();

            collector.addAuthorizationRole(
                    SEURAN_YHDYSHENKILO, () -> userAuthorizationHelper.isClubContact(org, person));

            collector.addAuthorizationRole(SEURAN_JASEN, () -> userAuthorizationHelper.isClubMember(org, person));
        }
    }

    private HuntingClubArea getArea(final EntityAuthorizationTarget target) {
        return target.getAuthorizationTargetId() != null
                ? huntingClubAreaRepository.findOne((Long) target.getAuthorizationTargetId())
                : target.getAuthorizationTarget(HuntingClubArea.class);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[] { HuntingClubArea.class, HuntingClubAreaDTO.class };
    }

}
