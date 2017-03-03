package fi.riista.security.audit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class AuthorizationAuditListenerTest {
    private static class SimpleEntity extends BaseEntity<Long> {
        private Long id = 1L;

        SimpleEntity(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }
    }

    private AuthorizationAuditListener auditListener = new AuthorizationAuditListener();

    private static Authentication wrap(UserInfo userInfo) {
        return new PreAuthenticatedAuthenticationToken(userInfo, null, userInfo.getAuthorities());
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBuildLogMessage() {
        final UserInfo testUser = new UserInfo.UserInfoBuilder(
                "testUser", 256L, SystemUser.Role.ROLE_USER).createUserInfo();

        final SimpleEntity target = new SimpleEntity(312L);

        String logMessage = auditListener.createLogMessage(true, EntityPermission.UPDATE, target, wrap(testUser));

        Assert.assertEquals("Wrong message",
                "Granted 'UPDATE' permission for user [id=256, username='testUser'] for target object [type='SimpleEntity', id=312]", logMessage);
    }

    @Test
    public void testBuildGrantedPermissionPrefix() {
        StringBuilder sb = new StringBuilder();
        auditListener.buildPermissionPrefix(true, EntityPermission.UPDATE, sb);

        Assert.assertEquals("Wrong message", "Granted 'UPDATE' permission", sb.toString());
    }

    @Test
    public void testBuildDeniedPermissionPrefix() {
        StringBuilder sb = new StringBuilder();
        auditListener.buildPermissionPrefix(false, EntityPermission.READ, sb);

        Assert.assertEquals("Wrong message", "Denied 'READ' permission", sb.toString());
    }

    @Test
    public void testBuidNormalUserIdentifier() {
        final UserInfo testUser = new UserInfo.UserInfoBuilder(
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

    @Test
    public void testTargetIdentifier() {
        final SimpleEntity target = new SimpleEntity(312L);
        final StringBuilder sb = new StringBuilder();
        auditListener.buildTargetIdentifier(target, sb);

        Assert.assertEquals("Wrong message", "for target object [type='SimpleEntity', id=312]", sb.toString());
    }

    @Test
    public void testForUnknownTarget() {
        StringBuilder sb = new StringBuilder();
        auditListener.buildTargetIdentifier(null, sb);

        Assert.assertEquals("Wrong message", "for unknown target object", sb.toString());
    }
}
