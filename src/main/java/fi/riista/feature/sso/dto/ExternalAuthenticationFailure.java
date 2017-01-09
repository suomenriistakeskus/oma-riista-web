package fi.riista.feature.sso.dto;

import org.springframework.security.core.AuthenticationException;

import java.util.Objects;

public class ExternalAuthenticationFailure {
    public static ExternalAuthenticationFailure invalidCredentials(AuthenticationException e) {
        return create(ExternalAuthenticationStatusCode.INVALID_CREDENTIALS, e.getMessage());
    }

    public static ExternalAuthenticationFailure smsSentFailed() {
        return create(ExternalAuthenticationStatusCode.SMS_SEND_FAILURE);
    }

    public static ExternalAuthenticationFailure twoFactorAuthenticationRequired() {
        return create(ExternalAuthenticationStatusCode.TWO_FACTOR_AUTHENTICATION_REQUIRED);
    }

    public static ExternalAuthenticationFailure remoteAddressBlocked() {
        return create(ExternalAuthenticationStatusCode.REMOTE_ADDRESS_BLOCKED);
    }

    public static ExternalAuthenticationFailure unknownError() {
        return create(ExternalAuthenticationStatusCode.UNKNOWN_ERROR, "See server log for error message.");
    }

    public static ExternalAuthenticationFailure create(
            final ExternalAuthenticationStatusCode status) {
        return new ExternalAuthenticationFailure(status, null);
    }

    public static ExternalAuthenticationFailure create(
            final ExternalAuthenticationStatusCode status, final String message) {
        return new ExternalAuthenticationFailure(status, message);
    }

    private final ExternalAuthenticationStatusCode status;
    private final String message;

    ExternalAuthenticationFailure(
            final ExternalAuthenticationStatusCode status,
            final String message) {
        this.status = Objects.requireNonNull(status);
        this.message = message;
    }

    public ExternalAuthenticationStatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
