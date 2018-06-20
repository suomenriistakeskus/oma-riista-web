package fi.riista.feature.organization.jht.training;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.jht.nomination.OccupationNomination;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.feature.organization.jht.nomination.OccupationNominationRepository;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

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

    private void testProposeDoesNotCreateNominationIfAnotherIsPending(
            final OccupationNomination.NominationStatus existingNominationStatus, final Runnable task) {

        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person person = model().newPerson();

            final SystemUser moderator = createNewModerator();

            final OccupationType occupationType = METSASTYKSENVALVOJA;
            final JHTTraining training = model().newJHTTraining(occupationType, person);
            final OccupationNomination nomination = model().newOccupationNomination(rhy, occupationType, person, coordinator);
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
