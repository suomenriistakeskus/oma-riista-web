package fi.riista.feature.organization;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.OrganisationAuthorization.OrganisationPermission.LIST_SRVA;
import static fi.riista.feature.organization.rhy.RhyRole.COORDINATOR;
import static fi.riista.feature.organization.rhy.RhyRole.SRVA_CONTACT_PERSON;

@Component
public class OrganisationAuthorization extends AbstractEntityAuthorization {

    public enum OrganisationPermission {
        LIST_SRVA
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private OrganisationRepository organisationRepository;

    public OrganisationAuthorization() {
        super("organisation");

        allow(READ,      ROLE_ADMIN, ROLE_MODERATOR, COORDINATOR);
        allow(UPDATE,    ROLE_ADMIN, ROLE_MODERATOR, COORDINATOR);
        allow(LIST_SRVA, ROLE_ADMIN, ROLE_MODERATOR, COORDINATOR, SRVA_CONTACT_PERSON);
    }

    @Override
    protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                   final EntityAuthorizationTarget target,
                                   final UserInfo userInfo) {

        Optional.ofNullable(getOrganisation(target))
                .filter(org -> org.getOrganisationType() == OrganisationType.RHY)
                .ifPresent(rhy -> {
                    collector.addAuthorizationRole(
                            COORDINATOR, () -> userAuthorizationHelper.isCoordinator(rhy, userInfo));

                    collector.addAuthorizationRole(
                            SRVA_CONTACT_PERSON, () -> userAuthorizationHelper.isSrvaContactPerson(rhy, userInfo));
                });
    }

    private Organisation getOrganisation(final EntityAuthorizationTarget target) {
        return target.getAuthorizationTargetId() != null
                ? organisationRepository.findOne((Long) target.getAuthorizationTargetId())
                : target.getAuthorizationTarget(Organisation.class);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[]{
                Organisation.class, OrganisationDTO.class,
                Riistakeskus.class,
                ValtakunnallinenRiistaneuvosto.class,
                AlueellinenRiistaneuvosto.class,
                RiistakeskuksenAlue.class,
                Riistanhoitoyhdistys.class, RiistanhoitoyhdistysDTO.class
        };
    }
}
