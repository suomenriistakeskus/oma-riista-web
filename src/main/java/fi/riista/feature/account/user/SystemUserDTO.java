package fi.riista.feature.account.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.common.dto.XssSafe;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.validation.PhoneNumber;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class SystemUserDTO extends BaseEntityDTO<Long> {

    public interface Create {}
    public interface Edit {}

    private Long id;

    private Integer rev;

    private boolean active = true;

    @NotNull(groups = { Create.class })
    private SystemUser.Role role;

    @Size(min = 2, max = 63, groups = { Edit.class, Create.class })
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String username;

    @XssSafe
    @NotEmpty(groups = Create.class)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Size(min = 8, groups = Create.class)
    private String password;

    @XssSafe
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @NotEmpty(groups = Create.class)
    private String passwordConfirm;

    @Email(groups = { Edit.class, Create.class })
    private String email;

    @PhoneNumber(groups = { Edit.class, Create.class })
    private String phoneNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE, groups = { Edit.class, Create.class })
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE, groups = { Edit.class, Create.class })
    private String lastName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE, groups = { Edit.class, Create.class })
    private String ipWhiteList;

    private SystemUser.TwoFactorAuthenticationMode twoFactorAuthentication;

    private TimeZone timeZone;

    private Locale locale;

    private boolean nameEditable;

    private Set<SystemUserPrivilege> privileges;

    @AssertTrue(message = "password confirmation must match", groups = { Create.class, Edit.class })
    @JsonIgnore
    public boolean isPasswordConfirmMatch() {
        if (this.id == null) {
            // Password is only required when creating new user
            return this.password != null && this.password.equals(this.passwordConfirm);
        }
        return this.password == null || this.password.equals(this.passwordConfirm);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public SystemUser.Role getRole() {
        return role;
    }

    public void setRole(final SystemUser.Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(final String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public void setNameEditable(boolean nameEditable) {
        this.nameEditable = nameEditable;
    }

    public boolean isNameEditable() {
        return nameEditable;
    }

    public String getIpWhiteList() {
        return ipWhiteList;
    }

    public void setIpWhiteList(String ipWhiteList) {
        this.ipWhiteList = ipWhiteList;
    }

    public SystemUser.TwoFactorAuthenticationMode getTwoFactorAuthentication() {
        return twoFactorAuthentication;
    }

    public void setTwoFactorAuthentication(final SystemUser.TwoFactorAuthenticationMode twoFactorAuthentication) {
        this.twoFactorAuthentication = twoFactorAuthentication;
    }

    public Set<SystemUserPrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Set<SystemUserPrivilege> privileges) {
        this.privileges = privileges;
    }
}
