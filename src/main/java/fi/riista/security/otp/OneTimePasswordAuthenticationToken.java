package fi.riista.security.otp;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.Serializable;

public class OneTimePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final Serializable oneTimePassword;
    private boolean enforceOneTimePassword;

    public OneTimePasswordAuthenticationToken(final Serializable principal,
                                              final Serializable credentials,
                                              final Serializable oneTimePassword) {
        super(principal, credentials);
        this.oneTimePassword = oneTimePassword;
        this.enforceOneTimePassword = false;
    }

    public Object getOneTimePassword() {
        return oneTimePassword;
    }

    public void enforceOneTimePassword(){
        this.enforceOneTimePassword = true;
    }

    public boolean isEnforceOneTimePassword() {
        return enforceOneTimePassword;
    }

    public boolean hasOneTimePassword() {
        return this.oneTimePassword != null;
    }
}
