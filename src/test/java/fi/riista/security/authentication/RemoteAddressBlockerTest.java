package fi.riista.security.authentication;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.UserInfo;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class RemoteAddressBlockerTest {

    @Test
    public void testSingleAddressWhiteListInRange() {
        UserDetails userDetails = createUserDetails("10.173.21.93/32", SystemUser.Role.ROLE_USER);
        Authentication authentication = createRemoteAuthentication("10.173.21.93");

        RemoteAddressBlocker.assertNotBlocked(userDetails, authentication);
    }

    @Test
    public void testMultipleAddressWhiteListInRange() {
        UserDetails userDetails = createUserDetails("127.0.0.1/8,10.173.21.93/24", SystemUser.Role.ROLE_USER);
        Authentication authentication = createRemoteAuthentication("10.173.21.93");

        RemoteAddressBlocker.assertNotBlocked(userDetails, authentication);
    }

    @Test(expected = RemoteAddressBlockedException.class)
    public void testSingleAddressWhiteListOutOfRange() {
        UserDetails userDetails = createUserDetails("10.173.21.93/32", SystemUser.Role.ROLE_USER);
        Authentication authentication = createRemoteAuthentication("10.173.21.94");

        RemoteAddressBlocker.assertNotBlocked(userDetails, authentication);
    }

    @Test(expected = RemoteAddressBlockedException.class)
    public void testMultipleAddressWhiteListOutOfRange() {
        UserDetails userDetails = createUserDetails("127.0.0.1/8,10.173.21.93/24", SystemUser.Role.ROLE_USER);
        Authentication authentication = createRemoteAuthentication("10.173.22.93");

        RemoteAddressBlocker.assertNotBlocked(userDetails, authentication);
    }

    @Test
    public void testNormalUserAllowedWithoutWhiteList() {
        UserDetails userDetails = createUserDetails(null, SystemUser.Role.ROLE_USER);
        Authentication authentication = createRemoteAuthentication("10.173.21.93");

        RemoteAddressBlocker.assertNotBlocked(userDetails, authentication);
    }

    @Test(expected = RemoteAddressBlockedException.class)
    public void testApiUserNotAllowedWithoutWhiteList() {
        UserDetails userDetails = createUserDetails(null, SystemUser.Role.ROLE_REST);
        Authentication authentication = createRemoteAuthentication("10.173.21.93");

        RemoteAddressBlocker.assertNotBlocked(userDetails, authentication);
    }

    private static Authentication createRemoteAuthentication(String remoteAddress) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("username", "password");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddress);
        token.setDetails(new WebAuthenticationDetails(request));
        return token;
    }

    private static UserDetails createUserDetails(String whiteList, SystemUser.Role role) {
        SystemUser systemUser = new SystemUser();
        systemUser.setId(1L);
        systemUser.setUsername("username");
        systemUser.setRole(role);
        systemUser.setPasswordAsPlaintext("password", new StandardPasswordEncoder());
        systemUser.setIpWhiteList(whiteList);

        return new UserInfo.UserInfoBuilder(systemUser).createUserInfo();
    }
}
