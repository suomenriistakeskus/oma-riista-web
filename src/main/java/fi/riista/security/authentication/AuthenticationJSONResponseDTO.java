package fi.riista.security.authentication;

import java.io.Serializable;
import java.util.Objects;

public class AuthenticationJSONResponseDTO implements Serializable {
    public enum StatusCode {
        INVALID_CREDENTIALS,
        OTP_REQUIRED,
        OTP_FAILURE
    }

    public static AuthenticationJSONResponseDTO create(final StatusCode status) {
        return new AuthenticationJSONResponseDTO(status, null);
    }

    public static AuthenticationJSONResponseDTO create(final StatusCode status, final String message) {
        return new AuthenticationJSONResponseDTO(status, message);
    }

    private final StatusCode status;
    private final String message;

    public AuthenticationJSONResponseDTO(StatusCode status, String message) {
        this.status = Objects.requireNonNull(status);
        this.message = message;
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
