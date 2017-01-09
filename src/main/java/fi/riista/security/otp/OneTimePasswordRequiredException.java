package fi.riista.security.otp;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

public class OneTimePasswordRequiredException extends InsufficientAuthenticationException {
    private final String expectedCode;
    private final UserDetails userDetails;

    public OneTimePasswordRequiredException(UserDetails userDetails, String expectedCode) {
        super("One time password was not provided");

        this.userDetails = Objects.requireNonNull(userDetails);
        this.expectedCode = Objects.requireNonNull(expectedCode);
    }

    public String getExpectedCode() {
        return expectedCode;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }
}
