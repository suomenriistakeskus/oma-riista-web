package fi.riista.feature.account.user;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class ActiveUserServiceTest extends EmbeddedDatabaseTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAuthorizeAndGetPerson() {
        withPerson(person -> onSavedAndAuthenticated(createUser(person), () -> {
            final Person authorizedPerson = activeUserService().requireActivePerson();
            assertEquals(person.getId(), authorizedPerson.getId());
        }));
    }

    @Test
    public void testAuthorizeAndGetPerson_failsWhenUserNotAuthenticated() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("User id not available in security context");

        runInTransaction(activeUserService()::requireActivePerson);
    }

    @Test
    public void testAuthorizeAndGetPerson_failsWhenPersonNotAssociatedToUser() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Active user is not associated with person");

        onSavedAndAuthenticated(createNewUser(), activeUserService()::requireActivePerson);
    }

    @Test
    public void testAuthorizeAndGetPerson_failsWhenUserDoesNotHaveExpectedRole() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Active user has incorrect role");

        final Person person = model().newPerson();
        final SystemUser user = createUser(person);
        user.setRole(someOtherThan(SystemUser.Role.ROLE_USER, SystemUser.Role.class));

        onSavedAndAuthenticated(user, activeUserService()::requireActivePerson);
    }
}
