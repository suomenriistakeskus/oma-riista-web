package fi.riista.feature.account.user;

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

    private static final AuthenticationTrustResolver AUTH_TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

    @Resource
    private UserRepository userRepository;

    @Resource
    private EntityPermissionEvaluator permissionEvaluator;

    @Resource
    private AuthorizationAuditListener authorizationAuditListener;

    @Resource
    private SecurityConfigurationProperties securityConfigurationProperties;

    private static boolean isAuthenticated(final Authentication authentication) {
        return authentication != null
                && authentication.getPrincipal() != null
                && !AUTH_TRUST_RESOLVER.isAnonymous(authentication)
                && authentication.isAuthenticated();
    }

    private static Optional<Authentication> findAuthenticationAuthenticated() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(ActiveUserService::isAuthenticated);
    }

    public boolean isAuthenticated() {
        return findAuthenticationAuthenticated().isPresent();
    }

    public Optional<UserInfo> findActiveUserInfo() {
        return findAuthenticationAuthenticated().map(UserInfo::extractFrom);
    }

    public boolean isModeratorOrAdmin() {
        return findActiveUserInfo().map(UserInfo::isAdminOrModerator).orElse(false);
    }

    public UserInfo getActiveUserInfoOrNull() {
        return findActiveUserInfo().orElse(null);
    }

    public String getActiveUsernameOrNull() {
        return findActiveUserInfo().map(UserInfo::getUsername).orElse(null);
    }

    public Optional<Long> findActiveUserId() {
        return findActiveUserInfo().map(UserInfo::getUserId);
    }

    public Long getActiveUserIdOrNull() {
        return findActiveUserId().orElse(null);
    }

    public Long requireActiveUserId() {
        return findActiveUserId().orElseThrow(() -> {
            return new AccessDeniedException("User id not available in security context");
        });
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<SystemUser> findActiveUser() {
        return findActiveUserId().flatMap(userRepository::findById);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public SystemUser requireActiveUser() {
        return Optional.of(requireActiveUserId())
                .flatMap(userRepository::findById)
                .orElseThrow(() -> {
                    return new AccessDeniedException("User for authenticated principal does not exist in repository");
                });
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Person requireActivePerson() {
        return requireActiveUser().requirePerson();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean checkHasPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        return findAuthenticationAuthenticated()
                .map(auth -> permissionEvaluator.hasPermission(auth, entity, permission))
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertHasPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        final Authentication authentication =
                findAuthenticationAuthenticated().orElseThrow(() -> new AccessDeniedException("Not authenticated"));
        final boolean hasPermission = permissionEvaluator.hasPermission(authentication, entity, permission);

        // Audit event
        authorizationAuditListener.onAccessDecision(hasPermission, permission, entity, authentication);

        if (!hasPermission) {
            final StringBuilder msgBuf = new StringBuilder(String.format(
                    "Denied '%s' %s", permission, formatTargetObject(entity)));

            findActiveUserInfo().ifPresent(userInfo -> msgBuf.append(" for ").append(userInfo));

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
        final Authentication authentication = new UserInfo.UserInfoBuilder(systemUser).createAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public String createLoginTokenForActiveUser(final ReadableDuration timeToLive) {
        final DateTime now = DateUtil.now();

        return Jwts.builder()
                .setSubject(getActiveUsernameOrNull())
                .setIssuedAt(now.toDate())
                .setExpiration(now.plus(timeToLive).toDate())
                .setAudience(JwtAuthenticationProvider.AUD_LOGIN)
                .signWith(JwtAuthenticationProvider.JWT_SIGNATURE_ALG, securityConfigurationProperties.getJwtSecret())
                .compact();
    }
}
