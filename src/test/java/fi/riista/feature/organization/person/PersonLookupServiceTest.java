package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.ActiveUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonLookupServiceTest {
    private static final String OK_SSN = "111111-1012";
    private static final String ARTIFICIAL_SSN = "111111-9012";

    @InjectMocks
    private PersonLookupService personLookupService;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ActiveUserService activeUserService;

    @Test
    public void testFindByHunterNumber() {
        Person person = createPerson("11111111", OK_SSN);
        mockPersonRepository(person);
        assertFalse(person.isArtificialPerson());

        final Optional<Person> result = personLookupService.findByHunterNumber(person.getHunterNumber());
        assertTrue(result.isPresent());
        assertEquals(person.getHunterNumber(), result.get().getHunterNumber());
    }

    @Test
    public void testFindByHunterNumberArtificialPersonForUser() {
        Person person = createPerson("11111111", ARTIFICIAL_SSN);
        mockPersonRepository(person);
        assertTrue(person.isArtificialPerson());

        final Optional<Person> result = personLookupService.findByHunterNumber(person.getHunterNumber());
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByHunterNumberArtificialPersonForAdmin() {
        Person person = createPerson("11111111", ARTIFICIAL_SSN);
        mockPersonRepository(person);
        assertTrue(person.isArtificialPerson());
        when(activeUserService.isModeratorOrAdmin()).thenReturn(true);

        final Optional<Person> result = personLookupService.findByHunterNumber(person.getHunterNumber());
        assertTrue(result.isPresent());
        assertEquals(person.getHunterNumber(), result.get().getHunterNumber());
    }

    private void mockPersonRepository(Person person) {
        when(personRepository.findByHunterNumber(eq(person.getHunterNumber()))).thenReturn(Optional.of(person));
    }

    private static Person createPerson(String hunterNumber, String ssn) {
        Person person = new Person();
        person.setHunterNumber(hunterNumber);
        person.setSsn(ssn);
        return person;
    }
}
