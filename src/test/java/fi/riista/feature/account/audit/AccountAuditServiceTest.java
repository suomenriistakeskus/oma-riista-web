package fi.riista.feature.account.audit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.security.UserInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountAuditServiceTest {

    @InjectMocks
    private AccountAuditService auditService;

    @Mock
    private AccountActivityMessageRepository accountActivityMessageRepository;

    @Mock
    private Authentication authMock;

    private String username;

    @Before
    public void setup() {
        username = "user";

        when(authMock.isAuthenticated()).thenReturn(true);
        when(authMock.getName()).thenReturn(username);
        when(authMock.getPrincipal()).thenReturn(
                new UserInfo.UserInfoBuilder(username, 123L, SystemUser.Role.ROLE_USER).createUserInfo());
        when(authMock.getDetails()).thenReturn(new WebAuthenticationDetails(new MockHttpServletRequest()));
    }

    @Test
    public void testAuditLogin_success() {
        final AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authMock);

        auditService.auditLoginSuccessEvent(event);

        verify(accountActivityMessageRepository, times(1))
                .save(argThat(matches(true, username, null)));

        verifyNoMoreInteractions(accountActivityMessageRepository);
    }

    @Test
    public void testAuditLogin_failure() {
        final String error = "exception message";
        final AbstractAuthenticationFailureEvent event = new AuthenticationFailureBadCredentialsEvent(
                authMock, new BadCredentialsException(error));

        auditService.auditLoginFailureEvent(event);

        verify(accountActivityMessageRepository, times(1))
                .save(argThat(matches(false, username, error)));

        verifyNoMoreInteractions(accountActivityMessageRepository);
    }

    private static ArgumentMatcher<AccountActivityMessage> matches(
            final boolean isSuccessfulLogin,
            final String username,
            final String errorMessage) {
        return item -> {
            boolean errorOk = (item.getExceptionMessage() == null && errorMessage == null)
                    || item.getExceptionMessage().equals(errorMessage);
            boolean usernameOk = (username == null && item.getUsername() == null)
                    || item.getUsername().equals(username);

            boolean typeOk = isSuccessfulLogin
                    ? item.getActivityType() == AccountActivityMessage.ActivityType.LOGIN_SUCCESS
                    : item.getActivityType() == AccountActivityMessage.ActivityType.LOGIN_FAILRE;

            return errorOk && usernameOk && typeOk;
        };
    }
}
