package fi.riista.security;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
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
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static java.util.Objects.requireNonNull;

/**
 * UserInfo is a serializable version of User domain object stored
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
        requireNonNull(authentication, "No authentication available");
        checkState(authentication.isAuthenticated(), "User is not authenticated");

        final Object principal = authentication.getPrincipal(); 
        requireNonNull(principal, "No principal for authentication");

        if (principal instanceof UserInfo) {
            return UserInfo.class.cast(principal);
        }
        throw new IllegalStateException("Authenticate user principal type is unknown: "
                + principal.getClass().getSimpleName());
    }

    public static UserInfo extractFrom(final UserDetails userDetails) {
        requireNonNull(userDetails, "No userDetails");
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
                final Long userId = extractFrom(authentication).getUserId();

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

        public UserInfoBuilder(final String username, final Long userId, final SystemUser.Role role) {
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
            this.phoneNumber = user.getPerson() != null ? user.getPerson().getPhoneNumber() : user.getPhoneNumber();
            this.ipWhiteList = user.getIpWhiteList();
            this.active = user.isActive();
            this.role = user.getRole();
            this.privileges = ImmutableSet.copyOf(user.getPrivileges());
            this.coordinator = false;
            this.twoFactorAuthentication = user.getTwoFactorAuthentication();
        }

        public UserInfoBuilder withOccupations(final SystemUser user, final OccupationRepository occupationRepository) {
            final Person person = user.getPerson(); 
            if (person != null) {
                this.coordinator =
                        occupationRepository.countActiveByTypeAndPerson(person, EnumSet.of(TOIMINNANOHJAAJA)) > 0;
            }
            return this;
        }

        private List<GrantedAuthority> getAuthorities() {
            return role == null
                    ? Collections.emptyList()
                    : coordinator
                            ? AuthorityUtils.createAuthorityList(role.name(), ROLE_COORDINATOR)
                            : AuthorityUtils.createAuthorityList(role.name());
        }

        public Authentication createAuthentication() {
            return new PreAuthenticatedAuthenticationToken(new UserInfo(this), null, getAuthorities());
        }

        public UserInfo createUserInfo() {
            Assert.hasText(this.username, "username is empty");
            return new UserInfo(this);
        }
    }

    private UserInfo(final UserInfoBuilder builder) {
        super(builder.username, builder.password, builder.active, builder.active, builder.active,
                builder.active, builder.getAuthorities());

        this.role = requireNonNull(builder.role);
        this.privileges = requireNonNull(builder.privileges);
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
