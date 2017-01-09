package fi.riista.security.authorization.support;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.EntityPermission;
import fi.riista.security.authentication.TestAuthenticationTokenUtil;
import fi.riista.security.authorization.ConditionalAuthorization;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class AuthorizationTokenCollectorTest {
    private static final RoleHierarchy ROLE_HIERARCHY = new NullRoleHierarchy();

    public enum TestAuthorisationRole {
        FIRST,
        SECOND
    }

    public static class TestContext {
        final Authentication authentication;

        final AuthorizationTokenHelper helper;

        final AuthorizationTokenCollector collector;

        public TestContext(SystemUser.Role role, EntityPermission permission) {
            this.authentication = createAuthentication(role);
            this.helper = new AuthorizationTokenHelper("");
            this.collector = new AuthorizationTokenCollector(authentication, ROLE_HIERARCHY, helper, permission);
        }

        public TestContext() {
            this(SystemUser.Role.ROLE_USER, EntityPermission.READ);
        }

        private static Authentication createAuthentication(SystemUser.Role role) {
            return TestAuthenticationTokenUtil.createAuthentication("testUser", "testPassword", 1l, role,
                    NoOpPasswordEncoder.getInstance());
        }
    }

    @Test
    public void testShouldFailWithNoGrants() {
        final TestContext testContext = new TestContext();

        assertFalse(testContext.collector.hasPermission());
    }

    @Test
    public void testShouldFailWithWrongToken() {
        final TestContext testContext = new TestContext();
        testContext.helper.grant(EntityPermission.READ, SystemUser.Role.ROLE_ADMIN);

        assertFalse(testContext.collector.hasPermission());
    }

    @Test
    public void testShouldWorkWithCorrectToken() {
        final TestContext testContext = new TestContext(SystemUser.Role.ROLE_ADMIN, EntityPermission.READ);
        testContext.helper.grant(EntityPermission.READ, SystemUser.Role.ROLE_ADMIN);

        assertTrue(testContext.collector.hasPermission());
    }

    @Test
    public void testShouldFailWithWrongCustomAuthorizationToken() {
        final TestContext testContext = new TestContext();
        testContext.helper.grant(EntityPermission.READ, TestAuthorisationRole.FIRST);

        assertFalse(testContext.collector.hasPermission());
    }

    @Test
    public void testShouldWorkWithCustomAuthorizationToken() {
        final TestContext testContext = new TestContext();
        testContext.helper.grant(EntityPermission.READ, TestAuthorisationRole.FIRST);
        testContext.collector.addAuthorizationRole(TestAuthorisationRole.FIRST);

        assertTrue(testContext.collector.hasPermission());
    }

    @Test
    public void testShouldEvaluateSuppliers() {
        final TestContext testContext = new TestContext();
        testContext.helper.grant(EntityPermission.READ, TestAuthorisationRole.FIRST);
        testContext.collector.addAuthorizationRole(TestAuthorisationRole.FIRST, new ConditionalAuthorization() {
            @Override
            public boolean applies() {
                return true;
            }
        });

        assertTrue(testContext.collector.hasPermission());
    }

    @Test
    public void testShouldNotEvaluateSupplierIfNotRelevant() {
        final ConditionalAuthorization condition = mock(ConditionalAuthorization.class);
        Mockito.when(condition.applies()).thenReturn(Boolean.TRUE);

        final TestContext testContext = new TestContext();
        testContext.helper.grant(EntityPermission.READ, TestAuthorisationRole.FIRST);
        testContext.collector.addAuthorizationRole(TestAuthorisationRole.SECOND, condition);

        assertFalse(testContext.collector.hasPermission());

        verify(condition, never()).applies();
        verifyNoMoreInteractions(condition);
    }

    @Test
    public void testShouldIgnoreSecondSupplierWhenAlreadyGranted() {
        final ConditionalAuthorization c1 = mock(ConditionalAuthorization.class);
        Mockito.when(c1.applies()).thenReturn(Boolean.TRUE);

        final ConditionalAuthorization c2 = mock(ConditionalAuthorization.class);
        Mockito.when(c2.applies()).thenReturn(Boolean.TRUE);

        final TestContext testContext = new TestContext();
        testContext.helper.grant(EntityPermission.READ, TestAuthorisationRole.FIRST);
        testContext.collector.addAuthorizationRole(TestAuthorisationRole.FIRST, c1);
        testContext.collector.addAuthorizationRole(TestAuthorisationRole.FIRST, c2);

        assertTrue(testContext.collector.hasPermission());

        verify(c1, atLeast(1)).applies();
        verify(c2, never()).applies();
        verifyNoMoreInteractions(c1, c2);
    }
}
