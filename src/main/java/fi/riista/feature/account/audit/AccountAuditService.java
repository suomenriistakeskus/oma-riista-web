package fi.riista.feature.account.audit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.sso.support.ExternalAuthenticationDetails;
import fi.riista.security.UserInfo;
import fi.riista.util.ClassUtils;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

@Service
@Transactional
public class AccountAuditService {

    @Resource
    private AccountActivityMessageRepository logMessageRepository;

    @Resource
    private UserRepository userRepository;

    @Transactional
    public void auditLoginFailureEvent(AbstractAuthenticationFailureEvent failureEvent) {
        final AccountActivityMessage message = createLogMessage(
                null, failureEvent.getAuthentication(),
                AccountActivityMessage.ActivityType.LOGIN_FAILRE);

        if (failureEvent.getException() != null) {
            message.setExceptionMessage(failureEvent.getException().getMessage());
        }

        logMessageRepository.save(message);
    }

    @Transactional
    public void auditLoginSuccessEvent(InteractiveAuthenticationSuccessEvent successEvent) {
        final AccountActivityMessage message = createLogMessage(
                null, successEvent.getAuthentication(),
                AccountActivityMessage.ActivityType.LOGIN_SUCCESS);

        logMessageRepository.save(message);
    }

    @Transactional
    public void auditLoginSuccessEvent(AuthenticationSuccessEvent successEvent) {
        final AccountActivityMessage message = createLogMessage(
                null, successEvent.getAuthentication(),
                AccountActivityMessage.ActivityType.LOGIN_SUCCESS);

        logMessageRepository.save(message);
    }

    public void auditLogoutEvent(HttpServletRequest request, Authentication authentication) {
        final AccountActivityMessage message =
                createLogMessage(null, authentication, AccountActivityMessage.ActivityType.LOGOUT);

        logMessageRepository.save(message);
    }

    public void auditUserEvent(final SystemUser user, final Authentication authentication,
                               final AccountActivityMessage.ActivityType type, final String additionalMessage) {
        final AccountActivityMessage activity = createLogMessage(user, authentication, type);

        if (additionalMessage != null) {
            if (activity.getMessage() != null) {
                activity.setMessage(activity.getMessage() + "; " + additionalMessage);
            } else {
                activity.setMessage(additionalMessage);
            }
        }

        logMessageRepository.save(activity);
    }

    private AccountActivityMessage createLogMessage(final SystemUser user,
                                                    final Authentication authentication,
                                                    final AccountActivityMessage.ActivityType activityType) {
        final AccountActivityMessage message = new AccountActivityMessage();

        message.setActivityType(activityType);

        if (user != null) {
            message.setUsername(user.getUsername());
            message.setUserId(user.getId());
        } else {
            message.setUsername(authentication.getName());
            message.setUserId(getUserId(authentication));
        }

        final Optional<WebAuthenticationDetails> webAuthenticationDetails =
                ClassUtils.cast(authentication.getDetails(), WebAuthenticationDetails.class);

        if (webAuthenticationDetails.isPresent()) {
            message.setIpAddress(webAuthenticationDetails.get().getRemoteAddress());
        }

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

    private Long getUserId(final Authentication auth) {
        if (auth.isAuthenticated() && auth.getPrincipal() instanceof UserInfo) {
            final UserInfo userInfo = UserInfo.extractFrom(auth);
            if (userInfo != null) {
                return userInfo.getUserId();
            }
        } else {
            // Failure event do not have user information in principal
            final SystemUser systemUser = userRepository.findByUsernameIgnoreCase(auth.getName());
            if (systemUser != null) {
                return systemUser.getId();
            }
        }

        return null;
    }
}
