package fi.riista.feature.account.user;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserRepositoryTest extends EmbeddedDatabaseTest {

    @Resource
    private UserRepository userRepository;

    @Test
    public void testIsModeratorOrAdmin() {
        final SystemUser user = createNewUser(SystemUser.Role.ROLE_USER);
        final SystemUser moderator = createNewUser(SystemUser.Role.ROLE_MODERATOR);
        final SystemUser admin = createNewUser(SystemUser.Role.ROLE_ADMIN);

        persistInNewTransaction();

        assertFalse(userRepository.isModeratorOrAdmin(user.getId()));
        assertTrue(userRepository.isModeratorOrAdmin(moderator.getId()));
        assertTrue(userRepository.isModeratorOrAdmin(admin.getId()));
    }

}
