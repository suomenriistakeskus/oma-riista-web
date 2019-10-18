package fi.riista.feature.organization.jht.expiry;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class JHTOccupationExpiryResolverTest extends EmbeddedDatabaseTest {

    @Resource
    private JHTOccupationExpiryResolver jhtOccupationExpiryResolver;

    @Test
    public void testSmoke() {
        final LocalDate occupationEndDate = new LocalDate(2018, 11, 3);

        final Person person = model().newPerson();
        person.setLanguageCode("sv");
        person.setEmail("person@invalid");
        person.setFirstName("First");
        person.setLastName("Last");

        final Person personWithoutEmail = model().newPerson();
        personWithoutEmail.setEmail(null);

        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setEmail("rhy@invalid");

        final Occupation occupation = model().newOccupation(rhy, person, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
        occupation.setEndDate(occupationEndDate);

        final Occupation notExpiring = model().newOccupation(rhy, person, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
        notExpiring.setEndDate(occupationEndDate.plusDays(1));

        final Occupation wrongType = model().newOccupation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO);
        wrongType.setEndDate(occupationEndDate);

        final Occupation missingEmail = model().newOccupation(rhy, personWithoutEmail, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA);
        missingEmail.setEndDate(occupationEndDate);

        persistInNewTransaction();

        runInTransaction(() -> {
            final List<JHTOccupationExpiryDTO> dtoList = jhtOccupationExpiryResolver.resolve(occupationEndDate);

            assertEquals(1, dtoList.size());

            final JHTOccupationExpiryDTO dto = dtoList.get(0);
            assertEquals(Locales.SV, dto.getLocale());
            assertEquals("First Last", dto.getPersonName());
            assertEquals((long) rhy.getId(), dto.getRhyId());
            assertEquals(person.getEmail(), dto.getOccupationEmail());
            assertEquals("3.11.2018", dto.getExpiryDate());
            assertEquals("Examinator för skjutprov", dto.getOccupationName());
            assertEquals(rhy.getNameSwedish(), dto.getRhyName());

            assertThat(jhtOccupationExpiryResolver.resolve(occupationEndDate.minusDays(1)), hasSize(0));
            assertThat(jhtOccupationExpiryResolver.resolve(occupationEndDate.plusDays(1)), hasSize(1));
        });
    }

}
