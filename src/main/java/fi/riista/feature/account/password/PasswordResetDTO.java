package fi.riista.feature.account.password;

import fi.riista.validation.XssSafe;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.Size;

public class PasswordResetDTO {

    // Password reset token from email
    @XssSafe
    @NotBlank
    private String token;

    // New password
    @XssSafe
    @NotBlank
    @Size(min = 8)
    private String password;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
