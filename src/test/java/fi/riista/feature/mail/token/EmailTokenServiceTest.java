package fi.riista.feature.mail.token;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.util.DateUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.annotation.Resource;
import java.util.UUID;

public class EmailTokenServiceTest extends EmbeddedDatabaseTest {
    @Resource
    private EmailTokenService emailTokenService;

    @Resource
    private EmailTokenRepository emailTokenRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static MockHttpServletRequest mockRequest() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        return request;
    }

    private String createEmailToken(final SystemUser user) {
        return emailTokenService.allocateToken(EmailTokenType.PASSWORD_RESET, user, user.getEmail(), mockRequest());
    }

    private void expire(final String token) {
        runInTransaction(() -> emailTokenRepository.getOne(token).setValidUntil(DateUtil.now()));
    }

    private void revoke(final String token) {
        runInTransaction(() -> emailTokenRepository.getOne(token).setRevokedAt(DateUtil.now()));
    }

    @Test
    public void testSmoke() {
        final SystemUser user = createNewUser();
        persistInNewTransaction();
        final String token = createEmailToken(user);

        emailTokenService.validate(token);
        emailTokenService.validateAndRevoke(token, mockRequest());
    }

    @Test
    public void testNotFound() {
        thrown.expect(EmailTokenException.class);
        thrown.expectMessage("not found");

        emailTokenService.validate(UUID.randomUUID().toString());
    }

    @Test
    public void testExpired() {
        thrown.expect(EmailTokenException.class);
        thrown.expectMessage("expired");

        final SystemUser user = createNewUser();
        persistInNewTransaction();
        final String token = createEmailToken(user);

        expire(token);

        emailTokenService.validate(token);
    }

    @Test
    public void testRevoked() {
        thrown.expect(EmailTokenException.class);
        thrown.expectMessage("revoked");

        final SystemUser user = createNewUser();
        persistInNewTransaction();
        final String token = createEmailToken(user);

        revoke(token);

        emailTokenService.validate(token);
    }

    @Test
    public void testValidationAfterRevoke() {
        thrown.expect(EmailTokenException.class);
        thrown.expectMessage("revoked");

        final SystemUser user = createNewUser();
        persistInNewTransaction();
        final String token = createEmailToken(user);

        emailTokenService.validateAndRevoke(token, mockRequest());
        emailTokenService.validate(token);
    }

    @Test
    public void testUserInactive() {
        thrown.expect(EmailTokenException.class);
        thrown.expectMessage("invalid");

        final SystemUser user = createNewUser();
        user.setActive(false);
        persistInNewTransaction();
        final String token = createEmailToken(user);

        emailTokenService.validate(token);
        emailTokenService.validateAndRevoke(token, mockRequest());
    }

    @Test
    public void testUserHasIntegrationRole() {
        thrown.expect(EmailTokenException.class);
        thrown.expectMessage("invalid");

        final SystemUser user = createNewApiUser();
        persistInNewTransaction();
        final String token = createEmailToken(user);

        emailTokenService.validate(token);
        emailTokenService.validateAndRevoke(token, mockRequest());
    }

    @Test
    public void testAllUserTokenRevoked() {
        final SystemUser user1 = createUserWithPerson("user1");
        user1.setEmail("user1@invalid");
        final SystemUser user2 = createUserWithPerson("user2");
        user2.setEmail("user2@invalid");
        persistInNewTransaction();

        final String user1_token1 = createEmailToken(user1);
        final String user1_token2 = createEmailToken(user1);
        final String user2_token1 = createEmailToken(user2);

        // All valid
        emailTokenService.validate(user1_token1);
        emailTokenService.validate(user1_token2);
        emailTokenService.validate(user2_token1);

        // All revoked for user 1
        emailTokenService.validateAndRevoke(user1_token1, mockRequest());

        // Check validity for user 2
        emailTokenService.validate(user2_token1);

        thrown.expect(EmailTokenException.class);
        thrown.expectMessage("revoked");

        // Token 2 for user 1 should be revoked
        emailTokenService.validate(user1_token2);
    }

    @Test
    public void testAllTokensRevokedWithSameEmail() {
        final SystemUser user1 = createUserWithPerson("user1");
        user1.setEmail("user@invalid");
        final SystemUser user2 = createUserWithPerson("user2");
        user2.setEmail("user@invalid");
        final SystemUser user3 = createUserWithPerson("user3");
        user3.setEmail("other@invalid");
        persistInNewTransaction();

        final String user1_token = createEmailToken(user1);
        final String user2_token = createEmailToken(user2);
        final String user3_token = createEmailToken(user3);

        // All valid
        emailTokenService.validate(user1_token);
        emailTokenService.validate(user2_token);
        emailTokenService.validate(user3_token);

        // All revoked for user 1
        emailTokenService.validateAndRevoke(user1_token, mockRequest());

        // Check validity for user 3
        emailTokenService.validate(user3_token);

        thrown.expect(EmailTokenException.class);
        thrown.expectMessage("revoked");

        // Token for user 2 should be revoked
        emailTokenService.validate(user2_token);
    }
}
