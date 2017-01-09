package fi.riista.security.audit;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.sso.support.ExternalAuthenticationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Component
public class AuthenticationAuditEventListener implements ApplicationListener<AbstractAuthenticationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationAuditEventListener.class);

    @Resource
    private AccountAuditService accountAuditService;

    @Resource
    private AuditService auditService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        logToAuditService(event);

        emitLogMessage(event);
        storeLogMessage(event);
    }

    private static void emitLogMessage(AbstractAuthenticationEvent event) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Authentication event ");
        builder.append(ClassUtils.getShortName(event.getClass()));
        builder.append(": ");
        builder.append(event.getAuthentication().getName());

        if (event instanceof AbstractAuthenticationFailureEvent) {
            builder.append("; exception: ");
            builder.append(((AbstractAuthenticationFailureEvent) event).getException().getMessage());
        }

        LOG.warn(builder.toString());
    }

    private void storeLogMessage(final AbstractAuthenticationEvent event) {
        try {
            if (event instanceof InteractiveAuthenticationSuccessEvent) {
                accountAuditService.auditLoginSuccessEvent(InteractiveAuthenticationSuccessEvent.class.cast(event));
            } else if (event instanceof AuthenticationSuccessEvent) {
                accountAuditService.auditLoginSuccessEvent(AuthenticationSuccessEvent.class.cast(event));
            } else if (event instanceof AbstractAuthenticationFailureEvent) {
                accountAuditService.auditLoginFailureEvent(AbstractAuthenticationFailureEvent.class.cast(event));
            }
        } catch (Exception ex) {
            LOG.error("Failed to audit authentication event in database", ex);
        }
    }

    private void logToAuditService(AbstractAuthenticationEvent event) {
        if (event instanceof AuthenticationSuccessEvent) {
            final Authentication authentication = event.getAuthentication();

            final ImmutableMap.Builder<String, Object> extra = auditService.extra("remoteAddress",
                    getRemoteAddress(authentication));
            addGrantedAuthorities(authentication, extra);
            addSource(event, extra);
            auditService.log("loginSuccess", authentication.getName(), extra.build());
        }
    }

    private static String getRemoteAddress(Authentication authentication) {
        if (authentication.getDetails() instanceof WebAuthenticationDetails) {
            final WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
            return details.getRemoteAddress();
        } else if (authentication.getDetails() instanceof ExternalAuthenticationDetails) {
            ExternalAuthenticationDetails details = (ExternalAuthenticationDetails) authentication.getDetails();
            return details.getRemoteAddress();
        }
        LOG.warn("Unknown authentication details:" + authentication.getDetails());
        return "unknown";
    }

    private static void addGrantedAuthorities(
            Authentication authentication, ImmutableMap.Builder<String, Object> extra) {

        final String grantedAuthorities = StringUtils.collectionToCommaDelimitedString(authentication.getAuthorities());
        extra.put("grantedAuthorities", grantedAuthorities);
    }

    private static void addSource(AbstractAuthenticationEvent event, ImmutableMap.Builder<String, Object> extra) {
        extra.put("source", event.getSource().getClass().getSimpleName());
    }

}
