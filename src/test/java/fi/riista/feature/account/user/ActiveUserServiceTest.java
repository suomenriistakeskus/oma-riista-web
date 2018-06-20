package fi.riista.feature.account.user;

import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class ActiveUserServiceTest extends EmbeddedDatabaseTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testRequireActivePerson() {
        withPerson(person1 -> withPerson(person2 -> withPerson(person3 -> {
            onSavedAndAuthenticated(createUser(person2), tx(() -> {
                final Person authorizedPerson = activeUserService().requireActivePerson();
                assertEquals(person2.getId(), authorizedPerson.getId());
            }));
        })));
    }

    @Test
    public void testRequireActivePerson_failsWhenUserNotAuthenticated() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("User id not available in security context");

        runInTransaction(activeUserService()::requireActivePerson);
    }

    @Test
    public void testRequireActivePerson_failsWhenPersonIsNotAssociatedWithUser() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Active user is not associated with person");

        onSavedAndAuthenticated(createNewUser(), tx(activeUserService()::requireActivePerson));
    }

    @Test
    public void testRequireActivePerson_failsWhenUserDoesNotHaveExpectedRole() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Active user has incorrect role");

        withPerson(person -> {
            final SystemUser user = createUser(person);
            user.setRole(someOtherThan(SystemUser.Role.ROLE_USER, SystemUser.Role.class));

            onSavedAndAuthenticated(user, tx(activeUserService()::requireActivePerson));
        });
    }
}
