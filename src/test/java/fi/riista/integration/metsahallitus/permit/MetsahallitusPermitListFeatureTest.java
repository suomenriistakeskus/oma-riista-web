package fi.riista.integration.metsahallitus.permit;

import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class MetsahallitusPermitListFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MetsahallitusPermitListFeature metsahallitusPermitListFeature;


    @Test
    public void testDoesNotReturnFalsePermits() {
        final Person person = model().newPerson();
        model().newMetsahallitusPermit("Karhulupa", "Karhualue", hunterNumber());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<MetsahallitusPermitListDTO> metsahallitusPermitListDTOS =
                    metsahallitusPermitListFeature.listAll(person.getId());
            assertThat(metsahallitusPermitListDTOS, hasSize(0));
        });
    }

    @Test
    public void testDoesNotReturnFalsePermits_foreignPerson() {
        final Person person = model().newForeignPerson();
        model().newMetsahallitusPermit("Karhulupa", "Karhualue", hunterNumber());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<MetsahallitusPermitListDTO> metsahallitusPermitListDTOS =
                    metsahallitusPermitListFeature.listAll(person.getId());
            assertThat(metsahallitusPermitListDTOS, hasSize(0));
        });
    }

    @Test
    public void testFindsBySsn() {
        final Person person = model().newPerson();
        person.setHunterNumber(null);
        final MetsahallitusPermit mhPermit = model().newMetsahallitusPermit("Karhulupa", "Karhualue", null);
        mhPermit.setSsn(person.getSsn());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<MetsahallitusPermitListDTO> metsahallitusPermitListDTOS =
                    metsahallitusPermitListFeature.listAll(person.getId());

            assertThat(metsahallitusPermitListDTOS, hasSize(1));
            final MetsahallitusPermitListDTO dto = metsahallitusPermitListDTOS.get(0);
            assertEquals(mhPermit.getPermitIdentifier(), dto.getPermitIdentifier());
        });
    }

    @Test
    public void testFindsByHunterNumber() {
        final Person person = model().newPerson();
        final MetsahallitusPermit mhPermit = model().newMetsahallitusPermit("Karhulupa", "Karhualue",
                                                                            person.getHunterNumber());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<MetsahallitusPermitListDTO> metsahallitusPermitListDTOS =
                    metsahallitusPermitListFeature.listAll(person.getId());

            assertThat(metsahallitusPermitListDTOS, hasSize(1));
            final MetsahallitusPermitListDTO dto = metsahallitusPermitListDTOS.get(0);
            assertEquals(mhPermit.getPermitIdentifier(), dto.getPermitIdentifier());
        });
    }

    @Test
    public void testFindsForeignPersonsPermitByHunterNumber() {
        final Person person = model().newForeignPerson("Foreign", "Person", new LocalDate(1980, 1, 1), "11111111");
        final MetsahallitusPermit mhPermit = model().newMetsahallitusPermit("Karhulupa", "Karhualue",
                                                                            person.getHunterNumber());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<MetsahallitusPermitListDTO> metsahallitusPermitListDTOS =
                    metsahallitusPermitListFeature.listAll(person.getId());

            assertThat(metsahallitusPermitListDTOS, hasSize(1));
            final MetsahallitusPermitListDTO dto = metsahallitusPermitListDTOS.get(0);
            assertEquals(mhPermit.getPermitIdentifier(), dto.getPermitIdentifier());
        });
    }


    @Test
    public void testEmptyListForForeignPersonWithNoHunterNumber() {
        final Person person = model().newForeignPerson();
        person.setHunterNumber(null);

        // Neither permit should be found
        model().newMetsahallitusPermit("Karhulupa", "Karhualue", hunterNumber());
        final MetsahallitusPermit permitWithSsn = model().newMetsahallitusPermit("Karhulupa",
                                                                                 "Karhualue",
                                                                                 null);
        permitWithSsn.setSsn(ssn());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<MetsahallitusPermitListDTO> metsahallitusPermitListDTOS =
                    metsahallitusPermitListFeature.listAll(person.getId());

            assertThat(metsahallitusPermitListDTOS, hasSize(0));
        });
    }
}
