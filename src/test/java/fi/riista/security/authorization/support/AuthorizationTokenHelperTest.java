package fi.riista.security.authorization.support;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserEntityAuthorization;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class AuthorizationTokenHelperTest {
    @Test
    public void testDeniedWithEmptyAcquiredTokensAndNoGrants() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        Assert.assertFalse("No access should be granted", helper.hasPermission("read",
                Collections.<String>emptySet()));
    }

    @Test
    public void testDeniedWithNonEmptyAcquiredTokensAndNoGrants() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        Assert.assertFalse("No access should be granted", helper.hasPermission("read",
                Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testDeniedWithAcquiredTokenNotGranted() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant("read", SystemUser.Role.ROLE_ADMIN);

        Assert.assertFalse("No access should be granted", helper.hasPermission("read",
                Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testDeniedWithAcquiredTokenNotGrantedForGivenPermission() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant("write", SystemUser.Role.ROLE_USER);

        Assert.assertFalse("No access should be granted", helper.hasPermission("read",
                Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testAccessGranted() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant("read", SystemUser.Role.ROLE_USER);

        Assert.assertTrue("Access should be granted", helper.hasPermission("read",
                Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testAccessGrantedForMultipleRoles() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant("read", SystemUser.Role.ROLE_USER);
        helper.grant("read", SystemUser.Role.ROLE_ADMIN);

        Assert.assertTrue("Access should be granted", helper.hasPermission("read",
                Collections.singleton("ROLE_USER")));
    }

    @Test
    public void testCanonicalTokenNameForUserRole() {
        Assert.assertEquals("Should not add class prefix", "ROLE_ADMIN",
                AuthorizationTokenHelper.getCanonicalAuthorizationToken(SystemUser.Role.ROLE_ADMIN));
    }

    @Test
    public void testCanonicalTokenNameForEntityRole() {
        Assert.assertEquals("Should add class prefix", "Role.SELF",
                AuthorizationTokenHelper.getCanonicalAuthorizationToken(UserEntityAuthorization.Role.SELF));
    }

    @Test
    public void testCanAcceptRole() {
        AuthorizationTokenHelper helper = new AuthorizationTokenHelper("");
        helper.grant("read", SystemUser.Role.ROLE_USER);

        Assert.assertTrue("Access should be granted", helper.canAcceptRoleForPermission("read", SystemUser.Role.ROLE_USER));
        Assert.assertFalse("Access should be granted", helper.canAcceptRoleForPermission("read", SystemUser.Role.ROLE_ADMIN));
    }
}
