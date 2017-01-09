package fi.riista.feature.account.user;

import com.google.common.base.Preconditions;
import fi.riista.config.properties.SecurityConfigurationProperties;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.jwt.JwtAuthenticationProvider;
import fi.riista.util.DateUtil;
import io.jsonwebtoken.Jwts;
import org.joda.time.DateTime;
import org.joda.time.ReadableDuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class ActiveUserService {
    public static final long SCHEDULED_TASK_USER_ID = -10L;

    @Resource
    private UserRepository userRepository;

    @Resource
    private PermissionEvaluator permissionEvaluator;

    @Resource
    private SecurityConfigurationProperties securityConfigurationProperties;

    public boolean isAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserInfo;
    }

    public boolean isModeratorOrAdmin() {
        return isAuthenticatedUser() && getActiveUserInfo().isAdminOrModerator();
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Person requireActivePerson() {
        final SystemUser activeUser = getActiveUser();
        Preconditions.checkState(activeUser.getRole() == SystemUser.Role.ROLE_USER, "Active user has incorrect role");

        return Optional.ofNullable(activeUser.getPerson())
                .orElseThrow(() -> new IllegalStateException("Active user is not associated with person"));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public SystemUser getActiveUser() {
        return Optional.ofNullable(userRepository.findOne(getActiveUserId())).orElseThrow(() -> {
            return new IllegalStateException("User for authenticated principal does not exist in repository");
        });
    }

    public Long getActiveUserId() {
        return Optional.ofNullable(getActiveUserInfo())
                .map(UserInfo::getUserId)
                .orElseThrow(() -> new RuntimeException("User id not available in security context"));
    }

    public UserInfo getActiveUserInfo() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(ActiveUserService::isAuthenticated)
                .map(UserInfo::extractFrom)
                .orElse(null);
    }

    private static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() != null && authentication.isAuthenticated();
    }

    public void assertHasPermission(final Object targetObject, final Object permission) {
        if (!checkHasPermission(targetObject, permission)) {
            final StringBuilder msgBuf = new StringBuilder(String.format(
                    "Denied '%s' %s", permission, formatTargetObject(targetObject)));

            Optional.ofNullable(getActiveUserInfo()).ifPresent(userInfo -> msgBuf.append(" for " + userInfo));

            throw new AccessDeniedException(msgBuf.toString());
        }
    }

    private static String formatTargetObject(final Object targetObject) {
        if (targetObject == null) {
            return "<null>";
        }
        try {
            if (targetObject instanceof HasID) {
                final String className = targetObject.getClass().getSimpleName();
                final Object id = HasID.class.cast(targetObject).getId();
                return id == null ? className : String.format("%s[id=%s]", className, id);
            }
        } catch (final RuntimeException e) {
            // for extra safety
        }
        return targetObject.toString();
    }

    public boolean checkHasPermission(final Object targetObject, final Object permission) {
        final SecurityContext securityContext = SecurityContextHolder.getContext();

        if (securityContext == null) {
            throw new IllegalStateException("SecurityContext is not available");
        }

        final Authentication authentication = securityContext.getAuthentication();

        return permissionEvaluator.hasPermission(authentication, targetObject, permission);
    }

    public void loginWithoutCheck(final SystemUser systemUser) {
        final Authentication authentication = new UserInfo.UserInfoBuilder(systemUser)
                .createAuthentication();

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public String createLoginTokenForActiveUser(final ReadableDuration timeToLive) {
        final UserInfo activeUserInfo = getActiveUserInfo();
        final DateTime now = DateUtil.now();

        return Jwts.builder()
                .setSubject(activeUserInfo.getUsername())
                .setIssuedAt(now.toDate())
                .setExpiration(now.plus(timeToLive).toDate())
                .setAudience(JwtAuthenticationProvider.AUD_LOGIN)
                .signWith(JwtAuthenticationProvider.JWT_SIGNATURE_ALG, securityConfigurationProperties.getJwtSecret())
                .compact();
    }
}
