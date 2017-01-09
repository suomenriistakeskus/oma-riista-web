package fi.riista.security.audit;

import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LogoutAuditEventListener implements LogoutHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LogoutAuditEventListener.class);

    @Resource
    private AccountAuditService accountAuditService;

    @Resource
    private AuditService auditService;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        try {
            if (authentication != null) {
                accountAuditService.auditLogoutEvent(request, authentication);
                auditService.log("logout", authentication.getName());
            }
        } catch (Exception ex) {
            LOG.error("Could not audit logout event", ex);
        }
    }
}
