package fi.riista.security.authentication;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.UserInfo;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.util.LinkedList;
import java.util.List;

public class TestAuthenticationTokenUtil {
    private TestAuthenticationTokenUtil() {
    }

    public static Authentication createNotAuthenticated() {
        return TestAuthenticationTokenUtil.createAuthentication(
                "anonymous", "", -1L, SystemUser.Role.ROLE_USER, false);
    }

    public static Authentication createUserAuthentication() {
        return TestAuthenticationTokenUtil.createAuthentication(SystemUser.Role.ROLE_USER, 1L);
    }

    public static Authentication createUserAuthentication(Long userId) {
        return TestAuthenticationTokenUtil.createAuthentication(SystemUser.Role.ROLE_USER, userId);
    }

    public static Authentication createAdminAuthentication() {
        return TestAuthenticationTokenUtil.createAuthentication(SystemUser.Role.ROLE_ADMIN, 2L);
    }

    public static Authentication createAuthentication(final SystemUser.Role role,
                                                      final Long userId) {
        return TestAuthenticationTokenUtil.createAuthentication(
                role.name(), role.name(), userId, role, true);
    }

    public static Authentication createAuthentication(final String username,
                                                      final String plaintextPassword,
                                                      final Long userId,
                                                      final SystemUser.Role role,
                                                      final boolean authenticated) {
        final SystemUser user = new SystemUser();
        user.setUsername(username);
        user.setId(userId);
        user.setRole(role);
        user.setPasswordAsPlaintext(plaintextPassword, NoOpPasswordEncoder.getInstance());

        final List<GrantedAuthority> grantedAuthorities = (role != null)
                ? AuthorityUtils.createAuthorityList(role.name())
                : new LinkedList<>();

        final UserInfo.UserInfoBuilder builder =
                new UserInfo.UserInfoBuilder(user);

        final UserInfo principal = builder.createUserInfo();

        final TestingAuthenticationToken authenticationToken =
                new TestingAuthenticationToken(principal, plaintextPassword, grantedAuthorities);

        authenticationToken.setAuthenticated(authenticated);

        return authenticationToken;
    }
}
