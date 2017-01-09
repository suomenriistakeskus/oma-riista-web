package fi.riista.security.authorization;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authentication.TestAuthenticationTokenUtil;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.spi.EntityAuthorizationStrategy;
import fi.riista.security.authorization.support.AuthorizationTokenCollector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

public class AbstractAuthorizationTest {
    public enum TestEntityRole {
        ROLE_ENTITY;
    }

    static class SimpleAuthorizationStrategy extends AbstractEntityAuthorization {
        protected SimpleAuthorizationStrategy() {
            super("simpleEntity");

            allow(EntityPermission.READ, SystemUser.Role.ROLE_USER);
            allow("write", SystemUser.Role.ROLE_ADMIN);
            allow("other", TestEntityRole.ROLE_ENTITY);
        }

        @Override
        public Class<?>[] getSupportedTypes() {
            return new Class[] { Void.class };
        }

        @Override
        protected void authorizeTarget(final AuthorizationTokenCollector collector,
                                       final EntityAuthorizationTarget target,
                                       final UserInfo userInfo) {
            collector.addAuthorizationRole(TestEntityRole.ROLE_ENTITY);
        }
    }

    @Mock
    private RoleHierarchy roleHierarchy;

    @Mock
    private EntityAuthorizationTarget target;

    private PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private EntityAuthorizationStrategy create() {
        SimpleAuthorizationStrategy strategy = new SimpleAuthorizationStrategy();

        Mockito.when(roleHierarchy.getReachableGrantedAuthorities(ArgumentMatchers.any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        ReflectionTestUtils.setField(strategy, "roleHierarchy", roleHierarchy);

        return strategy;
    }

    private Authentication createAuthentication(SystemUser.Role role) {
        return TestAuthenticationTokenUtil.createAuthentication("testUser", "testPassword", 1L, role, passwordEncoder);
    }

    @Test
    public void testSimple() {
        EntityAuthorizationStrategy strategy = create();

        Assert.assertEquals("simpleEntity", strategy.getEntityName());
        Assert.assertArrayEquals(new Class[] { Void.class }, strategy.getSupportedTypes());
    }

    @Test
    public void testPermission() {
        EntityAuthorizationStrategy strategy = create();

        Assert.assertFalse(strategy.hasPermission(target, "write", createAuthentication(SystemUser.Role.ROLE_USER)));
        Assert.assertFalse(strategy.hasPermission(target, "delete", createAuthentication(SystemUser.Role.ROLE_ADMIN)));

        Assert.assertTrue(strategy.hasPermission(target, "read", createAuthentication(SystemUser.Role.ROLE_USER)));
        Assert.assertTrue(strategy.hasPermission(target, "write", createAuthentication(SystemUser.Role.ROLE_ADMIN)));

        Assert.assertTrue(strategy.hasPermission(target, "other", createAuthentication(SystemUser.Role.ROLE_USER)));
    }

    @Test
    public void testUnknownPermission() {
        EntityAuthorizationStrategy strategy = create();

        Assert.assertFalse(strategy.hasPermission(target, "unknown", createAuthentication(SystemUser.Role.ROLE_USER)));
        Assert.assertFalse(strategy.hasPermission(target, "unknown", createAuthentication(SystemUser.Role.ROLE_ADMIN)));
    }

    @Test
    public void testNonStringPermission() {
        EntityAuthorizationStrategy strategy = create();

        Assert.assertFalse(strategy.hasPermission(target, new Object(), createAuthentication(SystemUser.Role.ROLE_USER)));
        Assert.assertTrue(strategy.hasPermission(target, EntityPermission.READ, createAuthentication(SystemUser.Role.ROLE_USER)));
    }

    @Test
    public void testRoleHierarchyApplied() {
        EntityAuthorizationStrategy strategy = create();

        Authentication authentication = createAuthentication(SystemUser.Role.ROLE_USER);

        // Invoke
        boolean hasPermission = strategy.hasPermission(target, "read", authentication);
        Assert.assertTrue(hasPermission);

        // Verify invocation
        Mockito.verify(roleHierarchy, Mockito.times(1)).getReachableGrantedAuthorities(
                ArgumentMatchers.same(authentication.getAuthorities()));
    }
}
