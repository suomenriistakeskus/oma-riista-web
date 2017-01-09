package fi.riista.security.authorization;

import fi.riista.feature.account.user.UserEntityAuthorization;
import fi.riista.feature.account.AccountDTO;
import fi.riista.feature.account.user.SystemUserDTO;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.EntityPermission;
import fi.riista.security.authentication.TestAuthenticationTokenUtil;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import fi.riista.security.authorization.spi.EntityAuthorizationStrategy;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;

public class UserEntityAuthorizationTest {

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
        UserEntityAuthorization strategy = new UserEntityAuthorization();

        Mockito.when(roleHierarchy.getReachableGrantedAuthorities(any()))
                .thenAnswer((Answer<Object>) invocationOnMock -> invocationOnMock.getArguments()[0]);

        ReflectionTestUtils.setField(strategy, "roleHierarchy", roleHierarchy);

        return strategy;
    }

    private Authentication createAuthentication(String username, Long userId, SystemUser.Role role) {
        return TestAuthenticationTokenUtil.createAuthentication(username, "null", userId, role, passwordEncoder);
    }

    @Test
    public void testSimple() {
        EntityAuthorizationStrategy strategy = create();

        Assert.assertEquals("SystemUser", strategy.getEntityName());
        Assert.assertTrue(ArrayUtils.contains(strategy.getSupportedTypes(), SystemUser.class));
        Assert.assertTrue(ArrayUtils.contains(strategy.getSupportedTypes(), SystemUserDTO.class));
        Assert.assertTrue(ArrayUtils.contains(strategy.getSupportedTypes(), AccountDTO.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testAuthorizeSelf() {
        EntityAuthorizationStrategy strategy = create();

        Mockito.when(target.getAuthorizationTargetClass()).thenReturn((Class) SystemUser.class);
        Mockito.when(target.getAuthorizationTargetName()).thenReturn("user");
        Mockito.when(target.getAuthorizationTargetId()).thenReturn(1L);

        // Allowed read on self
        Assert.assertTrue(strategy.hasPermission(target, EntityPermission.READ, createAuthentication("user", 1L, SystemUser.Role.ROLE_USER)));

        // Allow write, update on self
        Assert.assertTrue(strategy.hasPermission(target, EntityPermission.UPDATE, createAuthentication("user", 1L, SystemUser.Role.ROLE_USER)));

        // Deny delete on self
        Assert.assertFalse(strategy.hasPermission(target, EntityPermission.DELETE, createAuthentication("user", 1L, SystemUser.Role.ROLE_USER)));

        // Deny create
        Assert.assertFalse(strategy.hasPermission(target, EntityPermission.CREATE, createAuthentication("user", 1L, SystemUser.Role.ROLE_USER)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testDenyOperationOnOthersWhenNotAdmin() {
        EntityAuthorizationStrategy strategy = create();

        Mockito.when(target.getAuthorizationTargetClass()).thenReturn((Class) SystemUser.class);
        Mockito.when(target.getAuthorizationTargetName()).thenReturn("user");
        Mockito.when(target.getAuthorizationTargetId()).thenReturn(1L);

        // Deny everything on others
        Assert.assertFalse(strategy.hasPermission(target, EntityPermission.READ, createAuthentication("user", 2L, SystemUser.Role.ROLE_USER)));
        Assert.assertFalse(strategy.hasPermission(target, EntityPermission.UPDATE, createAuthentication("user", 2L, SystemUser.Role.ROLE_USER)));
        Assert.assertFalse(strategy.hasPermission(target, EntityPermission.DELETE, createAuthentication("user", 2L, SystemUser.Role.ROLE_USER)));
        Assert.assertFalse(strategy.hasPermission(target, EntityPermission.CREATE, createAuthentication("user", 2L, SystemUser.Role.ROLE_USER)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testAuthorizeAdmin() {
        EntityAuthorizationStrategy strategy = create();

        Mockito.when(target.getAuthorizationTargetClass()).thenReturn((Class) SystemUser.class);
        Mockito.when(target.getAuthorizationTargetName()).thenReturn("user");
        Mockito.when(target.getAuthorizationTargetId()).thenReturn(1L);

        // Allow everything for admin on others
        Assert.assertTrue(strategy.hasPermission(target, EntityPermission.READ, createAuthentication("admin", 2L, SystemUser.Role.ROLE_ADMIN)));
        Assert.assertTrue(strategy.hasPermission(target, EntityPermission.UPDATE, createAuthentication("admin", 2L, SystemUser.Role.ROLE_ADMIN)));
        Assert.assertTrue(strategy.hasPermission(target, EntityPermission.CREATE, createAuthentication("admin", 2L, SystemUser.Role.ROLE_ADMIN)));
        Assert.assertTrue(strategy.hasPermission(target, EntityPermission.DELETE, createAuthentication("admin", 2L, SystemUser.Role.ROLE_ADMIN)));
    }
}
