package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
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
public class PersonLookupServiceTest implements ValueGeneratorMixin {

    private static final String OK_SSN = "111111-1012";
    private static final String ARTIFICIAL_SSN = "111111-9012";

    @InjectMocks
    private PersonLookupService personLookupService;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ActiveUserService activeUserService;

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Test
    public void testFindByHunterNumber_ofFinnishPerson() {
        final Person person = createPerson(OK_SSN);
        mockPersonRepository(person);
        assertFalse(person.isArtificialPerson());

        final Optional<Person> result = personLookupService.findByHunterNumber(person.getHunterNumber(), false);
        assertTrue(result.isPresent());
        assertEquals(person.getHunterNumber(), result.get().getHunterNumber());
    }

    @Test
    public void testFindByHunterNumber_ofForeignPerson() {
        final Person person = createForeignPerson();
        mockPersonRepository(person);
        assertFalse(person.isArtificialPerson());

        final Optional<Person> result = personLookupService.findByHunterNumber(person.getHunterNumber(), true);
        assertTrue(result.isPresent());
        assertEquals(person.getHunterNumber(), result.get().getHunterNumber());
    }

    @Test(expected = PersonNotFoundException.class)
    public void testFindByHunterNumber_whenForeignPersonPresentWhileExpectingFinnishPerson() {
        final Person person = createForeignPerson();
        mockPersonRepository(person);
        assertFalse(person.isArtificialPerson());

        personLookupService.findByHunterNumber(person.getHunterNumber(), false);
    }

    @Test
    public void testFindByHunterNumberArtificialPersonForUser() {
        final Person person = createPerson(ARTIFICIAL_SSN);
        mockPersonRepository(person);
        assertTrue(person.isArtificialPerson());

        final Optional<Person> result = personLookupService.findByHunterNumber(person.getHunterNumber(), false);
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByHunterNumberArtificialPersonForAdmin() {
        final Person person = createPerson(ARTIFICIAL_SSN);
        mockPersonRepository(person);
        assertTrue(person.isArtificialPerson());
        when(activeUserService.isModeratorOrAdmin()).thenReturn(true);

        final Optional<Person> result = personLookupService.findByHunterNumber(person.getHunterNumber(), false);
        assertTrue(result.isPresent());
        assertEquals(person.getHunterNumber(), result.get().getHunterNumber());
    }

    private void mockPersonRepository(final Person person) {
        when(personRepository.findByHunterNumber(eq(person.getHunterNumber()))).thenReturn(Optional.of(person));
    }

    private Person createPerson(final String ssn) {
        final Person person = new Person();
        person.setId(nextLong());
        person.setHunterNumber(hunterNumber());
        person.setSsn(ssn);
        return person;
    }

    private Person createForeignPerson() {
        final Person person = new Person();
        person.setId(nextLong());
        person.setHunterNumber(hunterNumber());
        return person;
    }
}
