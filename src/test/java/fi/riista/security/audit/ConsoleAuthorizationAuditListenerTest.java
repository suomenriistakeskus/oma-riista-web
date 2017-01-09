package fi.riista.security.audit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.api.EntityAuthorizationTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class ConsoleAuthorizationAuditListenerTest {

    @Mock
    private EntityAuthorizationTarget target;

    private ConsoleAuthorizationAuditListener auditListener = new ConsoleAuthorizationAuditListener();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private static Authentication wrap(UserInfo userInfo) {
        return new PreAuthenticatedAuthenticationToken(userInfo, null, userInfo.getAuthorities());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testBuildLogMessage() {
        UserInfo testUser = new UserInfo.UserInfoBuilder(
                "testUser", 256L, SystemUser.Role.ROLE_USER).createUserInfo();
        Mockito.when(target.getAuthorizationTargetClass()).thenReturn((Class) Double.class);
        Mockito.when(target.getAuthorizationTargetId()).thenReturn(312L);
        Mockito.when(target.getAuthorizationTargetName()).thenReturn("testTarget");

        String logMessage = auditListener.createLogMessage(true, "write", target, wrap(testUser));

        Assert.assertEquals("Wrong message",
                "Granted 'write' permission for user [id=256, username='testUser'] for target object [type='testTarget', id=312]", logMessage);
    }

    @Test
    public void testBuildGrantedPermissionPrefix() {
        StringBuilder sb = new StringBuilder();
        auditListener.buildPermissionPrefix(true, "write", sb);

        Assert.assertEquals("Wrong message", "Granted 'write' permission", sb.toString());
    }

    @Test
    public void testBuildDeniedPermissionPrefix() {
        StringBuilder sb = new StringBuilder();
        auditListener.buildPermissionPrefix(false, "read", sb);

        Assert.assertEquals("Wrong message", "Denied 'read' permission", sb.toString());
    }

    @Test
    public void testBuidNormalUserIdentifier() {
        UserInfo testUser = new UserInfo.UserInfoBuilder(
                "testUser", 256L, SystemUser.Role.ROLE_USER).createUserInfo();

        StringBuilder sb = new StringBuilder();
        auditListener.buildUserIdentifier(wrap(testUser), sb);

        Assert.assertEquals("Wrong message", "for user [id=256, username='testUser']", sb.toString());
    }

    @Test
    public void testBuildAnonymousUserIdentifier() {
        StringBuilder sb = new StringBuilder();
        auditListener.buildUserIdentifier(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")), sb);

        Assert.assertEquals("Wrong message", "for user <anonymous>", sb.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testTargetIdentifier() {
        Mockito.when(target.getAuthorizationTargetClass()).thenReturn((Class) Double.class);
        Mockito.when(target.getAuthorizationTargetId()).thenReturn(312L);
        Mockito.when(target.getAuthorizationTargetName()).thenReturn("testTarget");

        StringBuilder sb = new StringBuilder();
        auditListener.buildTargetIdentifier(target, sb);

        Assert.assertEquals("Wrong message", "for target object [type='testTarget', id=312]", sb.toString());
    }

    @Test
    public void testForUnknownTarget() {
        StringBuilder sb = new StringBuilder();
        auditListener.buildTargetIdentifier(null, sb);

        Assert.assertEquals("Wrong message", "for unknown target object", sb.toString());
    }
}
