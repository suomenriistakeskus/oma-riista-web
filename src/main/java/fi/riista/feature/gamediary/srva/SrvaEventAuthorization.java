package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.gamediary.mobile.MobileSrvaEventDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Objects;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;

@Component
public class SrvaEventAuthorization extends AbstractEntityAuthorization {

    private enum Role {
        AUTHOR,
        RHY_COORDINATOR,
        RHY_SRVA_CONTACT_PERSON
    }

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    public SrvaEventAuthorization() {
        super(SrvaEvent.class.getSimpleName());

        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, Role.AUTHOR, Role.RHY_COORDINATOR, Role.RHY_SRVA_CONTACT_PERSON);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, Role.AUTHOR);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, Role.AUTHOR, Role.RHY_COORDINATOR, Role.RHY_SRVA_CONTACT_PERSON);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, Role.AUTHOR, Role.RHY_COORDINATOR, Role.RHY_SRVA_CONTACT_PERSON);
    }

    @Override
    public Class<?>[] getSupportedTypes() {
        return new Class[] { SrvaEvent.class, SrvaEventDTO.class, MobileSrvaEventDTO.class };
    }

    @Override
    protected void authorizeTarget(AuthorizationTokenCollector collector,
                                   EntityAuthorizationTarget target,
                                   UserInfo userInfo) {

        final Person authenticatedPerson = getAuthenticatedPerson(userInfo);
        final SrvaEvent srvaEvent = resolveEntity(target);

        if (srvaEvent != null) {
            collector.addAuthorizationRole(Role.AUTHOR,
                    () -> Objects.equals(authenticatedPerson, srvaEvent.getAuthor()));

            collector.addAuthorizationRole(Role.RHY_COORDINATOR,
                    () -> userAuthorizationHelper.isCoordinator(srvaEvent.getRhy(), userInfo) ||
                            srvaEvent.isAccident() && userAuthorizationHelper.isCoordinatorAnywhere(userInfo));

            collector.addAuthorizationRole(Role.RHY_SRVA_CONTACT_PERSON,
                    () -> userAuthorizationHelper.isSrvaContactPerson(srvaEvent.getRhy(), userInfo) ||
                            srvaEvent.isAccident() && userAuthorizationHelper.isSrvaContactPersonAnywhere(userInfo));
        } else {
            // Entity has null-ID and is transient (not persisted yet).
            // Entity will be directly associated (via author property) with the
            // currently authenticated Person.
            collector.addAuthorizationRole(Role.AUTHOR);
        }
    }

    private SrvaEvent resolveEntity(final EntityAuthorizationTarget target) {
        final SrvaEvent targetEntity = target.getAuthorizationTarget(SrvaEvent.class);

        if (targetEntity != null) {
            return targetEntity;
        }

        final Serializable srvaEventId = target.getAuthorizationTargetId();

        return srvaEventId != null ? srvaEventRepository.getOne((Long) srvaEventId) : null;
    }

    private Person getAuthenticatedPerson(final UserInfo userInfo) {
        return userInfo.getUserId() != null ? userRepository.getOne(userInfo.getUserId()).getPerson() : null;
    }
}
