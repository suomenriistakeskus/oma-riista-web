package fi.riista.feature.sso.service;

import fi.riista.feature.sso.dto.ExternalAuthenticationRequest;
import fi.riista.feature.sso.support.ExternalAuthenticationDetails;
import fi.riista.security.UserInfo;
import fi.riista.security.otp.OneTimePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalAuthenticationService {
    private AuthenticationManager authenticationManager;

    public ExternalAuthenticationService(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public UserInfo authenticate(final ExternalAuthenticationRequest externalAuthRequest,
                                 final UserInfo apiUserInfo) {
        final Authentication authenticationToken = buildAuthentication(externalAuthRequest, apiUserInfo);

        return UserInfo.extractFrom(attemptAuthentication(authenticationToken));
    }

    // Spring Security {@link AuthenticationManager} is guaranteed to throw exception if authentication fails.
    private Authentication attemptAuthentication(final Authentication authentication) {
        return authenticationManager.authenticate(authentication);
    }

    // Create Spring Security compatible authentication request
    private static OneTimePasswordAuthenticationToken buildAuthentication(final ExternalAuthenticationRequest request,
                                                                          final UserInfo apiUserInfo) {
        final OneTimePasswordAuthenticationToken authRequest = new OneTimePasswordAuthenticationToken(
                request.getUsername(), request.getPassword(), request.getOtp());

        authRequest.setDetails(new ExternalAuthenticationDetails(request, apiUserInfo));

        if (request.isRequireOtp()) {
            authRequest.enforceOneTimePassword();
        }

        return authRequest;
    }
}
