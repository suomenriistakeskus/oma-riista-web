package fi.riista.security.authentication;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.UserInfo;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedList;
import java.util.List;

public class TestAuthenticationTokenUtil {
    public static Authentication createAuthentication(final String username,
                                                      final String plaintextPassword,
                                                      final Long userId,
                                                      final SystemUser.Role role,
                                                      final PasswordEncoder passwordEncoder) {
        final SystemUser user = new SystemUser();
        user.setUsername(username);
        user.setId(userId);
        user.setRole(role);
        user.setPasswordAsPlaintext(plaintextPassword, passwordEncoder);

        final List<GrantedAuthority> grantedAuthorities = (role != null)
                ? AuthorityUtils.createAuthorityList(role.name())
                : new LinkedList<>();

        final UserInfo.UserInfoBuilder builder =
                new UserInfo.UserInfoBuilder(user);

        final UserInfo principal = builder.createUserInfo();

        final TestingAuthenticationToken authenticationToken =
                new TestingAuthenticationToken(principal, passwordEncoder.encode(plaintextPassword), grantedAuthorities);
        authenticationToken.setAuthenticated(true);

        return authenticationToken;
    }

    private TestAuthenticationTokenUtil() {
    }
}
