package fi.riista.security.otp;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class OneTimePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final Object oneTimePassword;
    private boolean enforceOneTimePassword;

    public OneTimePasswordAuthenticationToken(Object principal, Object credentials, Object oneTimePassword) {
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
