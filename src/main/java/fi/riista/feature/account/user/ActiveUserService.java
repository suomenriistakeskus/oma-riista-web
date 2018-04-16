package fi.riista.feature.account.user;

import com.google.common.base.Preconditions;
import fi.riista.config.properties.SecurityConfigurationProperties;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.UserInfo;
import fi.riista.security.audit.AuthorizationAuditListener;
import fi.riista.security.authorization.EntityPermissionEvaluator;
import fi.riista.security.jwt.JwtAuthenticationProvider;
import fi.riista.util.DateUtil;
import io.jsonwebtoken.Jwts;
import org.joda.time.DateTime;
import org.joda.time.ReadableDuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Optional;

@Service
public class ActiveUserService {
    public static final long SCHEDULED_TASK_USER_ID = -10L;

    @Resource
    private UserRepository userRepository;

    @Resource
    private EntityPermissionEvaluator permissionEvaluator;

    @Resource
    private AuthorizationAuditListener authorizationAuditListener;

    @Resource
    private SecurityConfigurationProperties securityConfigurationProperties;

    private static final AuthenticationTrustResolver authTrustResolver = new AuthenticationTrustResolverImpl();

    private static boolean isAuthenticated(final Authentication authentication) {
        return authentication != null && authentication.getPrincipal() != null
                && !authTrustResolver.isAnonymous(authentication) && authentication.isAuthenticated();
    }

    public boolean isAuthenticated() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(ActiveUserService::isAuthenticated)
                .orElse(false);
    }

    public Authentication getAuthentication() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .orElse(null);
    }

    private static Authentication getAuthenticationAuthenticated() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(ActiveUserService::isAuthenticated)
                .orElse(null);
    }

    public boolean isModeratorOrAdmin() {
        return Optional.ofNullable(getAuthenticationAuthenticated())
                .map(UserInfo::extractFrom)
                .map(UserInfo::isAdminOrModerator)
                .orElse(false);
    }

    public UserInfo getActiveUserInfo() {
        return Optional.ofNullable(getAuthenticationAuthenticated())
                .map(UserInfo::extractFrom)
                .orElse(null);
    }

    public Long getActiveUserId() {
        return Optional.ofNullable(getAuthenticationAuthenticated())
                .map(UserInfo::extractFrom)
                .map(UserInfo::getUserId)
                .orElseThrow(() -> new RuntimeException("User id not available in security context"));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public SystemUser getActiveUser() {
        return Optional.ofNullable(getActiveUserId())
                .map(userRepository::findOne)
                .orElseThrow(() -> new IllegalStateException("User for authenticated principal does not exist in repository"));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Person requireActivePerson() {
        final SystemUser activeUser = getActiveUser();
        Preconditions.checkState(activeUser.getRole() == SystemUser.Role.ROLE_USER, "Active user has incorrect role");

        return Optional.ofNullable(activeUser.getPerson())
                .orElseThrow(() -> new IllegalStateException("Active user is not associated with person"));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean checkHasPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        return permissionEvaluator.hasPermission(getAuthenticationAuthenticated(), entity, permission);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertHasPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        final Authentication authentication = getAuthenticationAuthenticated();
        final boolean hasPermission = permissionEvaluator.hasPermission(authentication, entity, permission);

        // Audit event
        authorizationAuditListener.onAccessDecision(hasPermission, permission, entity, authentication);

        if (!hasPermission) {
            final StringBuilder msgBuf = new StringBuilder(String.format(
                    "Denied '%s' %s", permission, formatTargetObject(entity)));

            Optional.ofNullable(getActiveUserInfo()).ifPresent(userInfo -> msgBuf.append(" for ").append(userInfo));

            throw new AccessDeniedException(msgBuf.toString());
        }
    }

    private static String formatTargetObject(final BaseEntity<?> targetObject) {
        if (targetObject == null) {
            return "<null>";
        }
        try {
            final String className = targetObject.getClass().getSimpleName();
            final Serializable id = targetObject.getId();
            return id == null ? className : String.format("%s[id=%s]", className, id);
        } catch (final RuntimeException e) {
            // for extra safety
        }
        return targetObject.toString();
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
