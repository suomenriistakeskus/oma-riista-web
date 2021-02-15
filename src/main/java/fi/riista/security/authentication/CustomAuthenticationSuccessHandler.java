package fi.riista.security.authentication;

import com.querydsl.sql.SQLQueryFactory;
import fi.riista.api.mobile.MobileAccountApiResource;
import fi.riista.api.personal.AccountApiResource;
import fi.riista.security.UserInfo;
import fi.riista.sql.SQMobileLoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    private static final String CLIENT_PARAM = "client";

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final Authentication authentication) throws IOException {

        if (authentication != null && authentication.isAuthenticated()) {
            // Always create session
            request.getSession(true);

            final UserInfo userInfo = UserInfo.extractFrom(authentication);
            logMobileLoginInfo(userInfo.getUsername(), request);

            // Redirect to Account API after sending new session cookie and optional remember-me cookie
            response.sendRedirect(getRedirectUrl(request));
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void logMobileLoginInfo(final String username, final HttpServletRequest request) {
        final String version = request.getHeader("mobileClientVersion");
        final String platform = request.getHeader("platform");
        final String device = request.getHeader("device");

        if (version != null || platform != null || device != null) {
            LOG.info("MobileLogin version:{} platform:{} device:{} username:{}", version, platform, device, username);

            final SQMobileLoginEvent EVENT = SQMobileLoginEvent.mobileLoginEvent;

            sqlQueryFactory.insert(EVENT)
                    .columns(EVENT.loginTime, EVENT.platform, EVENT.softwareVersion, EVENT.deviceName, EVENT.username)
                    .values(new Date(), platform, version, device, username)
                    .execute();
        }
    }

    private static String getRedirectUrl(final HttpServletRequest request) {
        final String clientParam = request.getParameter(CLIENT_PARAM);

        if (clientParam == null) {
            return AccountApiResource.ACCOUNT_RESOURCE_URL;
        }

        switch (clientParam) {
            case "web":
                return AccountApiResource.ACCOUNT_RESOURCE_URL;
            case "mobileapiv2":
                return MobileAccountApiResource.ACCOUNT_RESOURCE_URL;
            default:
                return clientParam.startsWith("mobile")
                        ? MobileAccountApiResource.ACCOUNT_RESOURCE_URL
                        : AccountApiResource.ACCOUNT_RESOURCE_URL;
        }
    }
}
