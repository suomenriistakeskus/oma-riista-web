package fi.riista.feature.permit.decision;

import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_DISABILITY_PERMIT_APPLICATION;
import static fi.riista.feature.permit.decision.PermitDecisionAuthorization.Role.PERMIT_DECISION_MODERATOR;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class PermitDecisionAuthorization extends AbstractEntityAuthorization<PermitDecision> {

    public enum Role {
        PERMIT_DECISION_MODERATOR
    }

    public PermitDecisionAuthorization() {
        allow(CREATE, PERMIT_DECISION_MODERATOR, ROLE_ADMIN);
        allow(READ, PERMIT_DECISION_MODERATOR, ROLE_ADMIN);
        allow(UPDATE, PERMIT_DECISION_MODERATOR, ROLE_ADMIN);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final PermitDecision decision,
                                   @Nonnull final UserInfo userInfo) {
        if (PermitTypeCode.isDisabilityPermitTypeCode(decision.getPermitTypeCode())) {
            collector.addAuthorizationRole(PERMIT_DECISION_MODERATOR, () ->
                    userInfo.hasPrivilege(MODERATE_DISABILITY_PERMIT_APPLICATION));
        } else {
            collector.addAuthorizationRole(PERMIT_DECISION_MODERATOR, () ->
                    userInfo.isModerator());
        }
    }
}
