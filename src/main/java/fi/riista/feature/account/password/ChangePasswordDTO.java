package fi.riista.feature.account.password;

import fi.riista.feature.common.dto.XssSafe;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

public class ChangePasswordDTO {

    @XssSafe
    @NotBlank
    @Size(min = 8)
    private String password;

    @XssSafe
    private String passwordConfirm;

    @XssSafe
    private String passwordCurrent;

    @AssertTrue(message = "password confirmation must match")
    public boolean isPasswordConfirmMatch() {
        // These need to match only if we are changing the password
        return this.password == null || this.password.equals(this.passwordConfirm);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getPasswordCurrent() {
        return passwordCurrent;
    }

    public void setPasswordCurrent(String passwordCurrent) {
        this.passwordCurrent = passwordCurrent;
    }
}
