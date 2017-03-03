package fi.riista.security;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.OccupationRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * User Info is serializable version of User domain object stored
 * in SecurityContextHolder during request processing.
 */
public class UserInfo extends org.springframework.security.core.userdetails.User {
    private static final long serialVersionUID = -1;

    public static final String ROLE_COORDINATOR = "ROLE_COORDINATOR";

    private final Long userId;
    private final SystemUser.Role role;
    private final String phoneNumber;
    private final String ipWhiteList;
    private final Set<SystemUserPrivilege> privileges;
    private final SystemUser.TwoFactorAuthenticationMode twoFactorAuthentication;

    public static UserInfo extractFrom(final Authentication authentication) {
        Objects.requireNonNull(authentication, "No authentication available");
        Preconditions.checkState(authentication.isAuthenticated(), "User is not authenticated");
        Objects.requireNonNull(authentication.getPrincipal(), "No principal for authentication");

        if (authentication.getPrincipal() instanceof UserInfo) {
            return UserInfo.class.cast(authentication.getPrincipal());
        }
        throw new IllegalStateException("Authenticate user principal type is unknown: "
                + authentication.getPrincipal().getClass().getSimpleName());
    }

    public static UserInfo extractFrom(final UserDetails userDetails) {
        Objects.requireNonNull(userDetails, "No userDetails");
        if (userDetails instanceof UserInfo) {
            return UserInfo.class.cast(userDetails);
        }
        throw new IllegalStateException("Authenticate user principal type is unknown: "
                + userDetails.getClass().getSimpleName());
    }

    public static long extractUserIdForEntity(final Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() == null) {
                return -2L;

            } else if (authentication.getPrincipal() instanceof UserInfo) {
                final Long userId = UserInfo.extractFrom(authentication).getUserId();

                // This check is needed when database is empty and there exists no
                // users at all initially.
                return userId != null ? userId : -3L;
            }

            return -4L;
        }

        return -1L;
    }

    public static class UserInfoBuilder {
        private String username;
        private String password;
        private Long userId;
        private String phoneNumber;
        private String ipWhiteList;
        private boolean active;
        private SystemUser.Role role;
        private Set<SystemUserPrivilege> privileges;
        private boolean coordinator;
        private SystemUser.TwoFactorAuthenticationMode twoFactorAuthentication;

        public UserInfoBuilder(final String username,
                               final Long userId,
                               final SystemUser.Role role) {
            this.username = username;
            this.userId = userId;
            this.role = role;
            this.password = RandomStringUtils.random(32);
            this.privileges = Collections.emptySet();
            this.coordinator = false;
        }

        public UserInfoBuilder(final SystemUser user) {
            this.username = user.getUsername();
            this.password = user.getHashedPassword();
            this.userId = user.getId();
            this.phoneNumber = user.getPerson() != null
                    ? user.getPerson().getPhoneNumber()
                    : user.getPhoneNumber();
            this.ipWhiteList = user.getIpWhiteList();
            this.active = user.isActive();
            this.role = user.getRole();
            this.privileges = ImmutableSet.copyOf(user.getPrivileges());
            this.coordinator = false;
            this.twoFactorAuthentication = user.getTwoFactorAuthentication();
        }

        public UserInfoBuilder withOccupations(SystemUser user, OccupationRepository occupationRepository) {
            if (user.getPerson() != null) {
                this.coordinator = occupationRepository.countActiveOccupationByTypeAndPerson(
                        user.getPerson(), EnumSet.of(OccupationType.TOIMINNANOHJAAJA)) > 0;
            }

            return this;
        }

        private List<GrantedAuthority> getAuthorities() {
            return role == null ? Collections.emptyList()
                    : coordinator ? AuthorityUtils.createAuthorityList(role.name(), ROLE_COORDINATOR)
                    : AuthorityUtils.createAuthorityList(role.name());
        }

        public Authentication createAuthentication() {
            return new PreAuthenticatedAuthenticationToken(new UserInfo(this), null, getAuthorities());
        }

        public UserInfo createUserInfo() {
            Assert.hasText(this.username);
            return new UserInfo(this);
        }
    }

    private UserInfo(final UserInfoBuilder builder) {
        super(builder.username, builder.password, builder.active, builder.active, builder.active,
                builder.active, builder.getAuthorities());
        this.role = Objects.requireNonNull(builder.role);
        this.privileges = Objects.requireNonNull(builder.privileges);
        this.userId = builder.userId;
        this.phoneNumber = builder.phoneNumber;
        this.ipWhiteList = builder.ipWhiteList;
        this.twoFactorAuthentication = builder.twoFactorAuthentication;
    }

    public boolean isAdmin() {
        return this.role.isAdmin();
    }

    public boolean isModerator() {
        return this.role.isModerator();
    }

    public boolean isAdminOrModerator() {
        return this.role.isModeratorOrAdmin();
    }

    public Long getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getIpWhiteList() {
        return ipWhiteList;
    }

    public boolean hasPrivilege(final SystemUserPrivilege privilege) {
        return privilege != null && this.privileges.contains(privilege);
    }

    public SystemUser.TwoFactorAuthenticationMode getTwoFactorAuthentication() {
        return twoFactorAuthentication;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("username", getUsername())
                .append("userId", userId)
                .append("role", role)
                .toString();
    }
}
