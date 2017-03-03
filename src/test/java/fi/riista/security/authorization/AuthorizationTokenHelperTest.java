package fi.riista.security.authorization;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.EntityPermission;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthorizationTokenHelperTest {
    @Test
    public void testDeniedWithEmptyAcquiredTokensAndNoGrants() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        assertFalse(helper.hasPermission(EntityPermission.READ, Collections.emptySet()));
    }

    @Test
    public void testDeniedWithNonEmptyAcquiredTokensAndNoGrants() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        assertFalse(helper.hasPermission(EntityPermission.READ, Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testDeniedWithAcquiredTokenNotGranted() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant(EntityPermission.READ, SystemUser.Role.ROLE_ADMIN);

        assertFalse(helper.hasPermission(EntityPermission.READ, Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testDeniedWithAcquiredTokenNotGrantedForGivenPermission() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant(EntityPermission.UPDATE, SystemUser.Role.ROLE_USER);

        assertFalse(helper.hasPermission(EntityPermission.READ, Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testAccessGranted() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant(EntityPermission.READ, SystemUser.Role.ROLE_USER);

        assertTrue(helper.hasPermission(EntityPermission.READ, Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testAccessGrantedForMultipleRoles() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant(EntityPermission.READ, SystemUser.Role.ROLE_USER);
        helper.grant(EntityPermission.READ, SystemUser.Role.ROLE_ADMIN);

        assertTrue(helper.hasPermission(EntityPermission.READ, Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testCanonicalTokenNameForUserRole() {
        assertEquals("ROLE_ADMIN", AuthorizationTokenHelper.getAuthorizationRoleName(SystemUser.Role.ROLE_ADMIN));
    }

    @Test
    public void testCanonicalTokenNameForEntityRole() {
        assertEquals("fi.riista.security.authorization.AuthorizationTokenCollectorTest.TestAuthorisationRole.FIRST",
                AuthorizationTokenHelper.getAuthorizationRoleName(AuthorizationTokenCollectorTest.TestAuthorisationRole.FIRST));
    }

    @Test
    public void testCanAcceptRole() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant(EntityPermission.READ, SystemUser.Role.ROLE_USER);

        Assert.assertTrue("Access should be granted", helper.canAcceptRoleForPermission(EntityPermission.READ, SystemUser.Role.ROLE_USER));
        Assert.assertFalse("Access should be granted", helper.canAcceptRoleForPermission(EntityPermission.READ, SystemUser.Role.ROLE_ADMIN));
    }
}
