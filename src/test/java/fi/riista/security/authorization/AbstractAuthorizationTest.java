package fi.riista.security.authorization;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authentication.TestAuthenticationTokenUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;

public class AbstractAuthorizationTest {
    public enum Permission {
        OTHER,
        UNKNOWN;
    }

    public enum AuthorizationRole {
        ROLE_ENTITY;
    }

    private static class SimpleEntity extends BaseEntity<Long> {
        private Long id = 1L;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }
    }

    static class SimpleAuthorizationStrategy extends AbstractEntityAuthorization<SimpleEntity> {
        SimpleAuthorizationStrategy() {
            allow(EntityPermission.READ, SystemUser.Role.ROLE_USER);
            allow(EntityPermission.UPDATE, SystemUser.Role.ROLE_ADMIN);
            allow(Permission.OTHER, AuthorizationRole.ROLE_ENTITY);
        }

        @Override
        protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                       @Nonnull final SimpleEntity entity,
                                       @Nonnull final UserInfo userInfo) {
            collector.addAuthorizationRole(AuthorizationRole.ROLE_ENTITY);
        }
    }

    @Mock
    private RoleHierarchy roleHierarchy;

    private EntityAuthorizationStrategy<SimpleEntity> strategy;

    private SimpleEntity entity;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.entity = new SimpleEntity();
        this.strategy = new SimpleAuthorizationStrategy();

        Mockito.when(roleHierarchy.getReachableGrantedAuthorities(anyCollection()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        ReflectionTestUtils.setField(strategy, "roleHierarchy", roleHierarchy);
    }

    @Test
    public void testSimple() {
        assertEquals(SimpleEntity.class, strategy.getEntityClass());
    }

    @Test
    public void testPermission() {
        final Authentication anonymous = TestAuthenticationTokenUtil.createNotAuthenticated();
        final Authentication user = TestAuthenticationTokenUtil.createUserAuthentication();
        final Authentication admin = TestAuthenticationTokenUtil.createAdminAuthentication();

        assertFalse(strategy.hasPermission(entity, EntityPermission.READ, anonymous));
        assertTrue(strategy.hasPermission(entity, EntityPermission.READ, user));
        assertFalse(strategy.hasPermission(entity, EntityPermission.UPDATE, anonymous));
        assertFalse(strategy.hasPermission(entity, EntityPermission.UPDATE, user));
        assertTrue(strategy.hasPermission(entity, EntityPermission.UPDATE, admin));
        assertFalse(strategy.hasPermission(entity, EntityPermission.DELETE, admin));
        assertFalse(strategy.hasPermission(entity, Permission.OTHER, anonymous));
        assertTrue(strategy.hasPermission(entity, Permission.OTHER, user));
    }

    @Test
    public void testUnknownPermission() {
        final Authentication anonymous = TestAuthenticationTokenUtil.createNotAuthenticated();
        final Authentication user = TestAuthenticationTokenUtil.createUserAuthentication();
        final Authentication admin = TestAuthenticationTokenUtil.createAdminAuthentication();

        assertFalse(strategy.hasPermission(entity, Permission.UNKNOWN, anonymous));
        assertFalse(strategy.hasPermission(entity, Permission.UNKNOWN, user));
        assertFalse(strategy.hasPermission(entity, Permission.UNKNOWN, admin));
    }

    @Test
    public void testRoleHierarchyApplied() {
        final Authentication userAuthentication = TestAuthenticationTokenUtil.createUserAuthentication();

        // Invoke
        assertTrue(strategy.hasPermission(entity, EntityPermission.READ, userAuthentication));

        // Verify invocation
        Mockito.verify(roleHierarchy, Mockito.times(1)).getReachableGrantedAuthorities(
                Mockito.same(userAuthentication.getAuthorities()));
    }
}
