package fi.riista.feature.account.audit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.sso.support.ExternalAuthenticationDetails;
import fi.riista.security.UserInfo;
import fi.riista.util.ClassUtils;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class AccountAuditService {

    @Resource
    private AccountActivityMessageRepository logMessageRepository;

    @Transactional
    public void auditLoginFailureEvent(@Nonnull final AbstractAuthenticationFailureEvent event) {
        Objects.requireNonNull(event, "event is null");
        final AccountActivityMessage message = createAccountActivityMessage(
                event.getAuthentication(), AccountActivityMessage.ActivityType.LOGIN_FAILRE);

        if (event.getException() != null) {
            message.setExceptionMessage(event.getException().getMessage());
        }

        logMessageRepository.save(message);
    }

    @Transactional
    public void auditLoginSuccessEvent(@Nonnull final AuthenticationSuccessEvent event) {
        Objects.requireNonNull(event, "event is null");
        logMessageRepository.save(createAccountActivityMessage(event.getAuthentication(),
                AccountActivityMessage.ActivityType.LOGIN_SUCCESS));
    }

    @Transactional
    public void auditLogoutEvent(@Nonnull final Authentication authentication) {
        Objects.requireNonNull(authentication, "authentication is null");
        logMessageRepository.save(createAccountActivityMessage(
                authentication, AccountActivityMessage.ActivityType.LOGOUT));
    }

    @Transactional
    public void auditPasswordResetRequest(@Nonnull final SystemUser user,
                                          @Nonnull final HttpServletRequest request) {
        Objects.requireNonNull(user, "user is null");
        Objects.requireNonNull(request, "request is null");
        logMessageRepository.save(createAccountActivityMessage(
                user, request, AccountActivityMessage.ActivityType.PASSWORD_RESET_REQUESTED));
    }

    @Transactional
    public void auditPasswordResetDone(@Nonnull final SystemUser user,
                                       @Nonnull final HttpServletRequest request) {
        Objects.requireNonNull(user, "user is null");
        Objects.requireNonNull(request, "request is null");
        logMessageRepository.save(createAccountActivityMessage(
                user, request, AccountActivityMessage.ActivityType.PASSWORD_RESET));
    }

    @Transactional
    public void auditActiveUserEvent(@Nonnull final AccountActivityMessage.ActivityType type,
                                     final String additionalMessage) {
        final SecurityContext securityContext = Objects.requireNonNull(
                SecurityContextHolder.getContext(), "securityContext is null");
        final AccountActivityMessage activity = createAccountActivityMessage(
                securityContext.getAuthentication(), type);

        if (additionalMessage != null) {
            if (activity.getMessage() != null) {
                activity.setMessage(activity.getMessage() + "; " + additionalMessage);
            } else {
                activity.setMessage(additionalMessage);
            }
        }

        logMessageRepository.save(activity);
    }

    private static AccountActivityMessage createAccountActivityMessage(
            @Nonnull final Authentication authentication,
            @Nonnull final AccountActivityMessage.ActivityType activityType) {

        Objects.requireNonNull(authentication, "authentication is null");
        Objects.requireNonNull(activityType, "activityType is null");

        final AccountActivityMessage message = new AccountActivityMessage();

        message.setActivityType(activityType);
        message.setUsername(authentication.getName());
        message.setUserId(UserInfo.extractUserIdForEntity(authentication));

        final Optional<WebAuthenticationDetails> webAuthenticationDetails =
                ClassUtils.cast(authentication.getDetails(), WebAuthenticationDetails.class);

        webAuthenticationDetails.ifPresent(d -> message.setIpAddress(d.getRemoteAddress()));

        final Optional<ExternalAuthenticationDetails> externalAuthenticationDetails =
                ClassUtils.cast(authentication.getDetails(), ExternalAuthenticationDetails.class);

        if (externalAuthenticationDetails.isPresent()) {
            message.setIpAddress(externalAuthenticationDetails.get().getRemoteAddress());
            UserInfo apiUserInfo = externalAuthenticationDetails.get().getApiUserInfo();

            if (apiUserInfo != null) {
                message.setMessage("EXTERNAL, client=" + apiUserInfo.getUsername());
            }
        }

        return message;
    }

    private static AccountActivityMessage createAccountActivityMessage(
            final @Nonnull SystemUser user,
            final @Nonnull HttpServletRequest request,
            final @Nonnull AccountActivityMessage.ActivityType activityType) {

        final AccountActivityMessage message = new AccountActivityMessage();
        message.setActivityType(activityType);
        message.setUsername(user.getUsername());
        message.setUserId(user.getId());
        message.setIpAddress(request.getRemoteAddr());

        return message;
    }
}
