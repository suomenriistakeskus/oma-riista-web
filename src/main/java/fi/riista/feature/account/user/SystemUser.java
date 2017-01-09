package fi.riista.feature.account.user;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.entity.PersistableEnumConverter;
import fi.riista.feature.organization.person.Person;
import fi.riista.validation.PhoneNumber;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

@Entity
@Access(value = AccessType.FIELD)
public class SystemUser extends LifecycleEntity<Long> {
    public enum Role {
        ROLE_USER,
        ROLE_REST,
        ROLE_MODERATOR,
        ROLE_ADMIN;

        public String includes(Role that) {
            return this + " > " + that;
        }

        public boolean isNormalUser() {
            return this == ROLE_USER;
        }

        public boolean isAdmin() {
            return this == ROLE_ADMIN;
        }

        public boolean isModerator() {
            return this == ROLE_MODERATOR;
        }

        public boolean isModeratorOrAdmin() {
            return isModerator() || isAdmin();
        }

        public boolean canAcceptEmailToken() {
            return isModeratorOrAdmin() || isNormalUser();
        }
    }

    public static class SystemUserPrivilegeConverter implements PersistableEnumConverter<SystemUserPrivilege> {
    }

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "name")
    @CollectionTable(name = "system_user_privilege", joinColumns = @JoinColumn(name = "user_id"))
    @Convert(converter = SystemUserPrivilegeConverter.class)
    private Set<SystemUserPrivilege> privileges = new HashSet<>();

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String password;

    // Comma-delimited list of valid CIDR patterns (eg. 192.168.1.1/32)
    @Column(columnDefinition = "text")
    private String ipWhiteList;

    @Email
    @Column
    private String email;

    @PhoneNumber
    @Size(max = 255)
    @Column
    private String phoneNumber;

    @Column(name = "locale_id")
    private Locale locale;

    @Column(name = "timezone_id")
    private TimeZone timeZone;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;

    private Long id;

    public void setPasswordAsPlaintext(final String plainTextPassword, final PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(plainTextPassword);
    }

    public boolean isModeratorOrAdmin() {
        if (role == null) {
            throw new IllegalStateException("User role not defined");
        }
        return role.isModeratorOrAdmin();
    }

    public String getFullName() {
        return String.format("%s %s", getFirstName(), getLastName());
    }

    public void addPrivilege(SystemUserPrivilege value) {
        this.privileges.add(value);
    }

    public void clearPrivileges() {
        this.privileges.clear();
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "user_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getHashedPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    public String getIpWhiteList() {
        return ipWhiteList;
    }

    public void setIpWhiteList(String ipWhiteList) {
        this.ipWhiteList = ipWhiteList;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<SystemUserPrivilege> getPrivileges() {
        return privileges;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
