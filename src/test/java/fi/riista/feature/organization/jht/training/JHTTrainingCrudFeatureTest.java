package fi.riista.feature.organization.jht.training;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.jht.nomination.OccupationNomination;
import fi.riista.feature.organization.jht.nomination.OccupationNominationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.jht.training.JHTTraining.TrainingType.LAHI;
import static fi.riista.feature.organization.jht.training.JHTTrainingSearchDTO.SearchType.HOME_RHY;
import static fi.riista.feature.organization.jht.training.JHTTrainingSearchDTO.SearchType.PERSON;
import static fi.riista.feature.organization.jht.training.JHTTrainingSearchDTO.SearchType.PREVIOUS_OCCUPATION;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.util.DateUtil.today;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JHTTrainingCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private JHTTrainingCrudFeature jhtTrainingCrudFeature;

    @Resource
    private OccupationNominationRepository occupationNominationRepository;

    @Test
    public void testProposeDoesNotCreateNominationIfAnotherIsPending_EHDOLLA() {
        testProposeDoesNotCreateNominationIfAnotherIsPending(OccupationNomination.NominationStatus.EHDOLLA, () -> {
            assertEquals(1, occupationNominationRepository.count());
        });
    }

    @Test
    public void testProposeDoesNotCreateNominationIfAnotherIsPending_ESITETTY() {
        testProposeDoesNotCreateNominationIfAnotherIsPending(OccupationNomination.NominationStatus.ESITETTY, () -> {
            assertEquals(1, occupationNominationRepository.count());
        });
    }

    @Test
    public void testProposeDoesNotCreateNominationIfAnotherIsPending_HYLATTY() {
        testProposeDoesNotCreateNominationIfAnotherIsPending(OccupationNomination.NominationStatus.HYLATTY, () -> {
            assertEquals(2, occupationNominationRepository.count());
        });
    }

    @Test
    public void testProposeDoesNotCreateNominationIfAnotherIsPending_NIMITETTY() {
        testProposeDoesNotCreateNominationIfAnotherIsPending(OccupationNomination.NominationStatus.NIMITETTY, () -> {
            assertEquals(2, occupationNominationRepository.count());
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void coordinatorCannotSearchByRka() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            createOccupationAndTraining(rhy, AMPUMAKOKEEN_VASTAANOTTAJA);

            onSavedAndAuthenticated(createNewUser("coordinator", coordinator), () -> {
                final JHTTrainingSearchDTO searchDTO = new JHTTrainingSearchDTO();
                searchDTO.setAreaCode(rhy.getParentOrganisation().getOfficialCode());
                searchDTO.setSearchType(PREVIOUS_OCCUPATION);
                searchDTO.setOccupationType(AMPUMAKOKEEN_VASTAANOTTAJA);
                jhtTrainingCrudFeature.search(searchDTO);
                fail("Should have thrown an exception");
            });
        });
    }

    @Test
    public void coordinatorCanSearchByRhy_previousOccupation() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            createOccupationAndTraining(rhy, AMPUMAKOKEEN_VASTAANOTTAJA);

            onSavedAndAuthenticated(createNewUser("coordinator", coordinator), () -> {
                final JHTTrainingSearchDTO searchDTO = new JHTTrainingSearchDTO();
                searchDTO.setRhyCode(rhy.getOfficialCode());
                searchDTO.setSearchType(PREVIOUS_OCCUPATION);
                searchDTO.setOccupationType(AMPUMAKOKEEN_VASTAANOTTAJA);

                final Page<JHTTrainingDTO> dtos = jhtTrainingCrudFeature.search(searchDTO);
                assertThat(dtos.getContent(), hasSize(1));
            });
        });
    }

    @Test
    public void coordinatorCanSearchByRhy_homeRhy() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();
            person.setRhyMembership(rhy);
            final JHTTraining training = model().newJHTTraining(AMPUMAKOKEEN_VASTAANOTTAJA, person);
            training.setTrainingType(LAHI);

            onSavedAndAuthenticated(createNewUser("coordinator", coordinator), () -> {
                final JHTTrainingSearchDTO searchDTO = new JHTTrainingSearchDTO();
                searchDTO.setRhyCode(rhy.getOfficialCode());
                searchDTO.setSearchType(HOME_RHY);
                searchDTO.setOccupationType(AMPUMAKOKEEN_VASTAANOTTAJA);

                final Page<JHTTrainingDTO> dtos = jhtTrainingCrudFeature.search(searchDTO);
                assertThat(dtos.getContent(), hasSize(1));
            });
        });
    }

    @Test
    public void coordinatorCanSearchByPerson() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = createOccupationAndTraining(rhy, AMPUMAKOKEEN_VASTAANOTTAJA);

            onSavedAndAuthenticated(createNewUser("coordinator", coordinator), () -> {
                final JHTTrainingSearchDTO searchDTO = new JHTTrainingSearchDTO();
                searchDTO.setRhyCode(rhy.getOfficialCode());
                searchDTO.setSearchType(PERSON);
                searchDTO.setOccupationType(AMPUMAKOKEEN_VASTAANOTTAJA);
                searchDTO.setSsn(person.getSsn());

                final Page<JHTTrainingDTO> dtos = jhtTrainingCrudFeature.search(searchDTO);
                assertThat(dtos.getContent(), hasSize(1));
            });
        });
    }

    @Test
    public void moderatorCanSearchByRka() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            createOccupationAndTraining(rhy, AMPUMAKOKEEN_VASTAANOTTAJA);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final JHTTrainingSearchDTO searchDTO = new JHTTrainingSearchDTO();
                searchDTO.setAreaCode(rhy.getParentOrganisation().getOfficialCode());
                searchDTO.setSearchType(PREVIOUS_OCCUPATION);
                searchDTO.setOccupationType(AMPUMAKOKEEN_VASTAANOTTAJA);

                final Page<JHTTrainingDTO> dtos = jhtTrainingCrudFeature.search(searchDTO);
                assertThat(dtos.getContent(), hasSize(1));
            });
        });
    }

    @Test
    public void lastTrainingsShowsTheLatestDate() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person trainingPerson = model().newPerson();
            final OccupationType occupationType = AMPUMAKOKEEN_VASTAANOTTAJA;

            final JHTTraining last = model().newJHTTraining(occupationType, trainingPerson);
            last.setTrainingDate(today().minusDays(1));
            final JHTTraining first = model().newJHTTraining(occupationType, trainingPerson);
            first.setTrainingDate(last.getTrainingDate().minusDays(1)); // Before last training
            final JHTTraining other = model().newJHTTraining(METSASTYKSENVALVOJA, trainingPerson);
            other.setTrainingDate(last.getTrainingDate().plusDays(1)); // After last training

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<Long> personIds = Arrays.asList(trainingPerson.getId());

                final Map<Long, LocalDate> trainingDates = jhtTrainingCrudFeature.lastTrainings(personIds, occupationType);

                assertThat(trainingDates.size(), equalTo(1));
                assertThat(trainingDates.get(trainingPerson.getId()), equalTo(last.getTrainingDate()));
            });
        });
    }

    @Test
    public void lastTrainingsShowsCorrectTrainingType() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person trainingPerson = model().newPerson();
            model().newJHTTraining(AMPUMAKOKEEN_VASTAANOTTAJA, trainingPerson);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<Long> personIds = Arrays.asList(trainingPerson.getId());
                final Map<Long, LocalDate> trainingDates = jhtTrainingCrudFeature.lastTrainings(personIds, METSASTYKSENVALVOJA);
                assertThat(trainingDates.size(), equalTo(0));
            });
        });
    }

    @Test
    public void lastTrainingsShowsCorrectPerson() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person nonTrainingPerson = model().newPerson();
            model().newJHTTraining(AMPUMAKOKEEN_VASTAANOTTAJA, model().newPerson());

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<Long> personIds = Arrays.asList(nonTrainingPerson.getId());
                final Map<Long, LocalDate> trainingDates = jhtTrainingCrudFeature.lastTrainings(personIds, AMPUMAKOKEEN_VASTAANOTTAJA);
                assertThat(trainingDates.size(), equalTo(0));
            });
        });
    }

    @Test
    public void coordinatorCanSearchLastTrainings() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                jhtTrainingCrudFeature.lastTrainings(emptyList(), AMPUMAKOKEEN_VASTAANOTTAJA);
            });
        });
    }

    @Test
    public void moderatorCanSearchLastTrainings() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createNewModerator(), () -> {
                jhtTrainingCrudFeature.lastTrainings(emptyList(), AMPUMAKOKEEN_VASTAANOTTAJA);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void userCanNotSearchLastTrainings() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createNewUser(), () -> {
                jhtTrainingCrudFeature.lastTrainings(emptyList(), AMPUMAKOKEEN_VASTAANOTTAJA);
            });
        });
    }

    private Person createOccupationAndTraining(final Riistanhoitoyhdistys rhy, final OccupationType type) {
        final Person trainingPerson = model().newPerson();
        model().newOccupation(rhy, trainingPerson, type);
        model().newJHTTraining(type, trainingPerson);
        return trainingPerson;
    }

    private void testProposeDoesNotCreateNominationIfAnotherIsPending(
            final OccupationNomination.NominationStatus existingNominationStatus, final Runnable task) {

        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();

            final SystemUser moderator = createNewModerator();

            final OccupationType occupationType = METSASTYKSENVALVOJA;
            final JHTTraining training = model().newJHTTraining(occupationType, person);
            final OccupationNomination nomination = model().newOccupationNomination(rhy, occupationType, person,
                    coordinator);
            nomination.setNominationStatus(existingNominationStatus);

            if (existingNominationStatus != OccupationNomination.NominationStatus.EHDOLLA) {
                nomination.setRhyPerson(coordinator);
                nomination.setNominationDate(today());
            }
            if (existingNominationStatus == OccupationNomination.NominationStatus.HYLATTY ||
                    existingNominationStatus == OccupationNomination.NominationStatus.NIMITETTY) {

                nomination.setModeratorUser(moderator);
                nomination.setDecisionDate(today());
            }

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                jhtTrainingCrudFeature.propose(training.getId(), rhy.getId());
                task.run();
            });
        });
    }

}
