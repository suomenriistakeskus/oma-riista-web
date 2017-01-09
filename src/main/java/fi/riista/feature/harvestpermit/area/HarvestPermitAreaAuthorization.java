package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_USER;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_JASEN;
import static fi.riista.feature.huntingclub.members.ClubRole.SEURAN_YHDYSHENKILO;

@Component
public class HarvestPermitAreaAuthorization extends AbstractEntityAuthorization {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    public HarvestPermitAreaAuthorization() {
        super("harvestPermitArea");

        allow(CREATE, ROLE_USER);
        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, SEURAN_YHDYSHENKILO, SEURAN_JASEN);
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

        getArea(target).ifPresent(area -> {
            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO,
                    () -> userAuthorizationHelper.isClubContact(area.getClub(), person));

            collector.addAuthorizationRole(SEURAN_JASEN,
                    () -> userAuthorizationHelper.isClubMember(area.getClub(), person));
        });
    }

    private Optional<HarvestPermitArea> getArea(final EntityAuthorizationTarget target) {
        return target.getAuthorizationTargetId() != null
                ? Optional.of(harvestPermitAreaRepository.getOne((Long) target.getAuthorizationTargetId()))
                : Optional.ofNullable(target.getAuthorizationTarget(HarvestPermitArea.class));
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class<?>[]{HarvestPermitArea.class, HarvestPermitAreaDTO.class};
    }
}
