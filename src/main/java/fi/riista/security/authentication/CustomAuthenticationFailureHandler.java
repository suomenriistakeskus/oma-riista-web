package fi.riista.security.authentication;

import static fi.riista.security.authentication.AuthenticationJSONResponseDTO.StatusCode.INVALID_CREDENTIALS;
import static fi.riista.security.authentication.AuthenticationJSONResponseDTO.StatusCode.OTP_FAILURE;
import static fi.riista.security.authentication.AuthenticationJSONResponseDTO.StatusCode.OTP_REQUIRED;

import fi.riista.security.otp.OneTimePasswordRequiredException;
import fi.riista.security.otp.OneTimePasswordSMSService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

    @Resource
    private OneTimePasswordSMSService oneTimePasswordSMSService;

    @Override
    public void onAuthenticationFailure(
            final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception)
                    throws IOException {

        try (ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response)) {
            httpResponse.setStatusCode(HttpStatus.FORBIDDEN);

            messageConverter.write(createResponse(exception), MediaType.APPLICATION_JSON, httpResponse);
        }
    }

    private AuthenticationJSONResponseDTO createResponse(final AuthenticationException exception) {
        if (exception instanceof OneTimePasswordRequiredException) {
            boolean smsSentSuccessfully = oneTimePasswordSMSService.sendCodeUsingSMS(
                    (OneTimePasswordRequiredException) exception);

            return smsSentSuccessfully
                    ? AuthenticationJSONResponseDTO.create(OTP_REQUIRED, "OTP is required")
                    : AuthenticationJSONResponseDTO.create(OTP_FAILURE, "Could not send OTP");
        }

        return AuthenticationJSONResponseDTO.create(INVALID_CREDENTIALS, "Invalid username or password");
    }
}
