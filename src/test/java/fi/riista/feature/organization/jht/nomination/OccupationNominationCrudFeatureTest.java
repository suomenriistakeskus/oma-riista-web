package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.feature.organization.jht.JHTPeriod;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.ESITETTY;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

public class OccupationNominationCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private OccupationNominationCrudFeature occupationNominationCrudFeature;

    @Resource
    private OccupationRepository occupationRepository;

    @Test
    public void testCurrentOccupationSetToEndBeforeNewOccupation() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person person = model().newPerson();
        final Person rhyPerson = model().newPerson();
        final JHTPeriod jhtPeriod = new JHTPeriod(today());
        final OccupationNomination occupationNomination = model().newOccupationNomination(
                rhy, AMPUMAKOKEEN_VASTAANOTTAJA, person, rhyPerson);
        occupationNomination.setNominationStatus(ESITETTY);
        occupationNomination.setNominationDate(today());

        // Previous occupation with active now extending beyond start of new JHT period
        final LocalDate activeBeginDate = today().minusDays(1);
        final LocalDate activeEndDate = jhtPeriod.getBeginDate();

        model().newOccupation(rhy, person, AMPUMAKOKEEN_VASTAANOTTAJA, activeBeginDate, activeEndDate);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            occupationNominationCrudFeature.accept(occupationNomination.getId(), jhtPeriod);

            // Sort occupations by creation order
            final List<Occupation> occupationList = occupationRepository.findAll(new Sort(Sort.Direction.ASC, "id"));
            assertEquals(2, occupationList.size());

            final Occupation first = occupationList.get(0);
            final Occupation second = occupationList.get(1);

            assertEquals("active beginDate not modified", activeBeginDate, first.getBeginDate());
            assertEquals("active endDate set before new", jhtPeriod.getBeginDate().minusDays(1), first.getEndDate());
            assertEquals("new beginDate equals JHT period", jhtPeriod.getBeginDate(), second.getBeginDate());
            assertEquals("new endDate equals JHT period", jhtPeriod.getEndDate(), second.getEndDate());
        });
    }

    @Test
    public void testOccupationsInFutureRemoved() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person person = model().newPerson();
        final Person rhyPerson = model().newPerson();
        final JHTPeriod jhtPeriod = new JHTPeriod(today());
        final OccupationNomination occupationNomination = model().newOccupationNomination(
                rhy, METSASTYKSENVALVOJA, person, rhyPerson);
        occupationNomination.setNominationStatus(ESITETTY);
        occupationNomination.setNominationDate(today());

        // Previous occupation with active now extending beyond start of new JHT period
        final LocalDate futureBeginDate = today().plusDays(1);
        final LocalDate futureEndDate = today().plusDays(2);

        model().newOccupation(rhy, person, METSASTYKSENVALVOJA, futureBeginDate, futureEndDate);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            occupationNominationCrudFeature.accept(occupationNomination.getId(), jhtPeriod);

            // Sort occupations by creation order
            final List<Occupation> occupationList = occupationRepository.findAll(new Sort(Sort.Direction.ASC, "id"));
            assertEquals(1, occupationList.size());

            final Occupation first = occupationList.get(0);

            assertEquals("new beginDate equals JHT period", jhtPeriod.getBeginDate(), first.getBeginDate());
            assertEquals("new endDate equals JHT period", jhtPeriod.getEndDate(), first.getEndDate());
        });
    }
}
