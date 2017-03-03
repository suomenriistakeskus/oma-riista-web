package fi.riista.feature.account.user;

import fi.riista.security.authentication.TestAuthenticationTokenUtil;
import fi.riista.security.authorization.EntityAuthorizationStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserEntityAuthorizationTest {
    private static final long USER_ID = 1;
    private static final long OTHER_ID = 2;

    @Mock
    private RoleHierarchy roleHierarchy;

    private EntityAuthorizationStrategy<SystemUser> strategy;
    private SystemUser user;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.strategy = create();
        this.user = new SystemUser();
        this.user.setId(USER_ID);
    }

    private EntityAuthorizationStrategy<SystemUser> create() {
        final UserEntityAuthorization strategy = new UserEntityAuthorization();

        Mockito.when(roleHierarchy.getReachableGrantedAuthorities(ArgumentMatchers.anyCollection()))
                .thenAnswer((Answer<Object>) invocationOnMock -> invocationOnMock.getArguments()[0]);

        ReflectionTestUtils.setField(strategy, "roleHierarchy", roleHierarchy);

        return strategy;
    }

    private void assertDenyAll(final Authentication authentication) {
        assertFalse(strategy.hasPermission(user, READ, authentication));
        assertFalse(strategy.hasPermission(user, UPDATE, authentication));
        assertFalse(strategy.hasPermission(user, DELETE, authentication));
        assertFalse(strategy.hasPermission(user, CREATE, authentication));
    }

    @Test
    public void testSimple() {
        assertEquals(SystemUser.class, strategy.getEntityClass());
    }

    @Test
    public void testAuthorizeSelf() {
        final Authentication authentication = TestAuthenticationTokenUtil.createUserAuthentication(USER_ID);

        assertDenyAll(authentication);
    }

    @Test
    public void testDenyOperationOnOthersWhenNotAdmin() {
        final Authentication authentication = TestAuthenticationTokenUtil.createUserAuthentication(OTHER_ID);

        // Deny everything on others
        assertDenyAll(authentication);
    }

    @Test
    public void testAuthorizeAdmin() {
        final Authentication authentication = TestAuthenticationTokenUtil.createAdminAuthentication();

        // Allow everything for admin on others
        assertTrue(strategy.hasPermission(user, READ, authentication));
        assertTrue(strategy.hasPermission(user, UPDATE, authentication));
        assertTrue(strategy.hasPermission(user, CREATE, authentication));
        assertTrue(strategy.hasPermission(user, DELETE, authentication));
    }
}
