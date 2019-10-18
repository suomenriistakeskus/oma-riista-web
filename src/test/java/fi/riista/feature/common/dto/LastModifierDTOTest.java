package fi.riista.feature.common.dto;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LastModifierDTOTest {

    private DateTime timestamp;

    @Before
    public void setup() {
        timestamp = DateUtil.now();
    }

    @Test
    public void testCreateForAutomatedTask() {
        final LastModifierDTO dto = LastModifierDTO.createForAutomatedTask(timestamp);
        assertEquals(timestamp, dto.getTimestamp());
        assertTrue(dto.isAdminOrModerator());
        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
        assertNull(dto.getFullName());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateForAutomatedTask_failsWhenTimestampIsNull() {
        LastModifierDTO.createForAutomatedTask(null);
    }

    @Test
    public void testCreateForAdminOrModerator() {
        final SystemUser moderator = createModerator("abc", "xyz");

        final LastModifierDTO dto = LastModifierDTO.createForAdminOrModerator(moderator, timestamp);
        assertEquals(timestamp, dto.getTimestamp());
        assertTrue(dto.isAdminOrModerator());
        assertEquals(moderator.getFirstName(), dto.getFirstName());
        assertEquals(moderator.getLastName(), dto.getLastName());
        assertEquals(moderator.getFullName(), dto.getFullName());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateForAdminOrModerator_failsWhenTimestampIsNull() {
        LastModifierDTO.createForAdminOrModerator(createModerator("abc", "xyz"), null);
    }

    @Test
    public void testCreateForAdminOrModerator_whenFirstNameNotPresent() {
        final SystemUser moderator = createModerator(null, "xyz");

        final LastModifierDTO dto = LastModifierDTO.createForAdminOrModerator(moderator, timestamp);
        assertEquals(timestamp, dto.getTimestamp());
        assertTrue(dto.isAdminOrModerator());
        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
        assertNull(dto.getFullName());
    }

    @Test
    public void testCreateForAdminOrModerator_whenLastNameNotPresent() {
        final SystemUser moderator = createModerator("abc", null);

        final LastModifierDTO dto = LastModifierDTO.createForAdminOrModerator(moderator, timestamp);
        assertEquals(timestamp, dto.getTimestamp());
        assertTrue(dto.isAdminOrModerator());
        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
        assertNull(dto.getFullName());
    }

    @Test
    public void testCreateForPerson() {
        final Person person = createPerson();
        final LastModifierDTO dto = LastModifierDTO.createForPerson(person, timestamp);

        assertEquals(timestamp, dto.getTimestamp());
        assertFalse(dto.isAdminOrModerator());
        assertEquals(person.getFirstName(), dto.getFirstName());
        assertEquals(person.getLastName(), dto.getLastName());
        assertEquals(person.getFullName(), dto.getFullName());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateForPerson_failsWhenTimestampIsNull() {
        LastModifierDTO.createForPerson(createPerson(), null);
    }

    private static SystemUser createModerator(final String firstName, final String lastName) {
        final SystemUser moderator = new SystemUser();
        moderator.setRole(SystemUser.Role.ROLE_MODERATOR);
        moderator.setFirstName(firstName);
        moderator.setLastName(lastName);
        return moderator;
    }

    private static Person createPerson() {
        final Person person = new Person();
        person.setFirstName("abc");
        person.setLastName("xyz");
        return person;
    }
}
