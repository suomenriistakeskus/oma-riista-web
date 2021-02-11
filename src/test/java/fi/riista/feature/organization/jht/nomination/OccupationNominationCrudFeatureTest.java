package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.jht.JHTPeriod;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.ESITETTY;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OccupationNominationCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private OccupationNominationCrudFeature occupationNominationCrudFeature;

    @Resource
    private OccupationRepository occupationRepository;

    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;
    private Person person;
    private Person rhyPerson;

    @Before
    public void setup() {
        rka = model().newRiistakeskuksenAlue("500");
        rhy = model().newRiistanhoitoyhdistys(rka, "550");
        person = model().newPerson();
        rhyPerson = model().newPerson();

    }

    @Test(expected = AccessDeniedException.class)
    public void testAutorization_normalUser() {
        onSavedAndAuthenticated(createNewUser("user"),
                () -> occupationNominationCrudFeature.search(new OccupationNominationSearchDTO()));
    }

    @Test
    public void testReturnsEmptyListWhenRkaIsNotFound() {
        final OccupationNominationSearchDTO searchDTO = new OccupationNominationSearchDTO();
        searchDTO.setAreaCode("600");
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final Page<OccupationNominationDTO> resultDTO = occupationNominationCrudFeature.search(searchDTO);
            assertThat(resultDTO.getContent(), hasSize(0));
        });
    }

    @Test
    public void testReturnsEmptyListWhenRhyIsNotFound() {
        final OccupationNominationSearchDTO searchDTO = new OccupationNominationSearchDTO();
        searchDTO.setRhyCode("650");
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final Page<OccupationNominationDTO> resultDTO = occupationNominationCrudFeature.search(searchDTO);
            assertThat(resultDTO.getContent(), hasSize(0));
        });
    }

    @Test
    public void testReturnsEmptyListWhenPersonIsNotFound() {
        final OccupationNominationSearchDTO searchDTO = new OccupationNominationSearchDTO();
        searchDTO.setHunterNumber("55555555");
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final Page<OccupationNominationDTO> resultDTO = occupationNominationCrudFeature.search(searchDTO);
            assertThat(resultDTO.getContent(), hasSize(0));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCoordinatorNotAllowedToSearchByArea() {
        final Person coordinator = model().newPerson();
        model().newOccupation(rhy, coordinator, TOIMINNANOHJAAJA);

        final OccupationNominationSearchDTO searchDTO = new OccupationNominationSearchDTO();
        searchDTO.setAreaCode(rka.getOfficialCode());
        searchDTO.setNominationStatus(ESITETTY);

        onSavedAndAuthenticated(createNewUser("coordinator", coordinator), () -> {
            occupationNominationCrudFeature.search(searchDTO);
            fail("Should throw an exception");
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCoordinatorNotAllowedToSearchFromDifferentRhy() {
        final Riistanhoitoyhdistys anotherRhy = model().newRiistanhoitoyhdistys();

        final OccupationNominationSearchDTO searchDTO = new OccupationNominationSearchDTO();
        searchDTO.setRhyCode(anotherRhy.getOfficialCode());
        searchDTO.setNominationStatus(ESITETTY);

        final Person coordinator = model().newPerson();
        model().newOccupation(rhy, coordinator, TOIMINNANOHJAAJA);
        onSavedAndAuthenticated(createNewUser("coordinator", coordinator), () -> {
            occupationNominationCrudFeature.search(searchDTO);
            fail("Should throw an exception");
        });
    }


    @Test
    public void testCurrentOccupationSetToEndBeforeNewOccupation() {
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
            final List<Occupation> occupationList = occupationRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
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
            final List<Occupation> occupationList = occupationRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
            assertEquals(1, occupationList.size());

            final Occupation first = occupationList.get(0);

            assertEquals("new beginDate equals JHT period", jhtPeriod.getBeginDate(), first.getBeginDate());
            assertEquals("new endDate equals JHT period", jhtPeriod.getEndDate(), first.getEndDate());
        });
    }

}
