package fi.riista.feature.organization.person;

import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.SsnSequence;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PersonSearchFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PersonSearchFeature feature;

    private Person finnishPerson;
    private Person foreignPerson;
    private Person finnishHunter;
    private Person foreignHunter;

    @Before
    public void setup() {
        finnishPerson = model().newPerson("suomi", "henkilö", SsnSequence.nextArtificialSsn(), null);
        foreignPerson = model().newForeignPerson("ulkomaalainen", "henkilö", new LocalDate(1960, 2, 2), null);
        finnishHunter = model().newPerson("suomi", "henkilö", SsnSequence.nextArtificialSsn(), "11111111");
        foreignHunter = model().newForeignPerson("ulkomaalainen", "henkilö", new LocalDate(1960, 2, 2), "22222222");
    }

    @Test
    public void testFindsAllPersonsByHunterNumber() {
        assertFindsByHunterNumber(finnishHunter);
        assertFindsByHunterNumber(foreignHunter);
    }

    private void assertFindsByHunterNumber(final Person person) {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PersonWithHunterNumberDTO dto =
                    feature.findNameByHunterNumber(person.getHunterNumber());
            assertNotNull(dto);
            assertEquals(person.getHunterNumber(), dto.getHunterNumber());
            assertEquals(person.getByName(), dto.getByName());
            assertEquals(person.getLastName(), dto.getLastName());
        });
    }

}
