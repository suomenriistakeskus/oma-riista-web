package fi.riista.security.authentication;

import fi.riista.api.AccountApiResource;
import fi.riista.api.mobile.MobileGameDiaryV1ApiResource;
import fi.riista.api.mobile.MobileGameDiaryV2ApiResource;
import fi.riista.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    private static final String CLIENT_PARAM = "client";

    public enum ClientType {
        WEB("web", AccountApiResource.ACCOUNT_RESOURCE_URL),
        MOBILE_V1("mobilegamediary", MobileGameDiaryV1ApiResource.ACCOUNT_RESOURCE_URL),
        MOBILE_V2("mobileapiv2", MobileGameDiaryV2ApiResource.ACCOUNT_RESOURCE_URL);

        private final String id;

        private final String accountResourceUrl;

        ClientType(final String id, final String accountResourceUrl) {
            this.id = id;
            this.accountResourceUrl = accountResourceUrl;
        }

        public static Optional<ClientType> fromId(final String id) {
            if (id != null) {
                for (final ClientType type : values()) {
                    if (type.getId().equalsIgnoreCase(id)) {
                        return Optional.of(type);
                    }
                }
            }
            return Optional.empty();
        }

        public String getId() {
            return id;
        }

        public String getAccountResourceUrl() {
            return accountResourceUrl;
        }
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

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

    private static void logMobileLoginInfo(String username, HttpServletRequest request) {
        String version = request.getHeader("mobileClientVersion");
        String platform = request.getHeader("platform");
        String device = request.getHeader("device");

        if (version != null || platform != null || device != null) {
            LOG.info("MobileLogin version:{} platform:{} device:{} username:{}", version, platform, device, username);
        }
    }

    private static String getRedirectUrl(HttpServletRequest request) {
        String clientParam = request.getParameter(CLIENT_PARAM);
        return ClientType.fromId(clientParam).orElse(ClientType.WEB).getAccountResourceUrl();
    }
}
