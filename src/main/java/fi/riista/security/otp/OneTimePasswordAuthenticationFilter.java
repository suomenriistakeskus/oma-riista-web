package fi.riista.security.otp;

import com.google.common.base.Strings;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OneTimePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final static String REQUEST_PARAM_USERNAME = "username";
    private final static String REQUEST_PARAM_PASSWORD = "password";
    private final static String REQUEST_PARAM_OTP = "otp";

    public OneTimePasswordAuthenticationFilter(final String actionUrl) {
        super(actionUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        final String username = obtainUsername(request);
        final String password = obtainPassword(request);
        final String receivedOtp = obtainOneTimeToken(request);

        final OneTimePasswordAuthenticationToken authRequest =
                new OneTimePasswordAuthenticationToken(username, password, receivedOtp);

        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private static String obtainUsername(final HttpServletRequest request) {
        final String username = request.getParameter(REQUEST_PARAM_USERNAME);
        return Strings.nullToEmpty(username).trim();
    }

    private static String obtainPassword(final HttpServletRequest request) {
        final String password = request.getParameter(REQUEST_PARAM_PASSWORD);
        return Strings.nullToEmpty(password).trim();
    }

    private static String obtainOneTimeToken(final HttpServletRequest request) {
        return request.getParameter(REQUEST_PARAM_OTP);
    }
}
