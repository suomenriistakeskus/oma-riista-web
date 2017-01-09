package fi.riista.security.authentication;

import org.springframework.security.core.AuthenticationException;

public class RemoteAddressBlockedException extends AuthenticationException {
    public RemoteAddressBlockedException(String msg) {
        super(msg);
    }
}
