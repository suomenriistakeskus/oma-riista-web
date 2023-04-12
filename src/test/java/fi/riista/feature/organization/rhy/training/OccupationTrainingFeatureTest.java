package fi.riista.feature.organization.rhy.training;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class OccupationTrainingFeatureTest extends EmbeddedDatabaseTest {

    private Person trainingPerson;
    private Person otherPerson;

    @Resource
    private OccupationTrainingFeature feature;

    @Before
    public void setup() {
        trainingPerson = model().newPerson();
        otherPerson = model().newPerson();
    }

    @Test
    public void testListMine() {
        final OccupationTraining training = model().newOccupationTraining(trainingPerson);
        model().newOccupationTraining(otherPerson);

        onSavedAndAuthenticated(createNewUser("person", trainingPerson), () -> {
            final List<OccupationTrainingDTO> dtos = feature.listMine();
            assertEquality(training, dtos);
        });
    }

    @Test
    public void testList_moderator() {
        final OccupationTraining training = model().newOccupationTraining(trainingPerson);
        model().newOccupationTraining(otherPerson);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<OccupationTrainingDTO> dtos = feature.listForPerson(trainingPerson.getId());
            assertEquality(training, dtos);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testListMine_unauthorized() {
        final OccupationTraining training = model().newOccupationTraining(trainingPerson);
        model().newOccupationTraining(otherPerson);
        persistInNewTransaction();

        final List<OccupationTrainingDTO> dtos = feature.listMine();
        assertEquality(training, dtos);

    }

    @Test(expected = AccessDeniedException.class)
    public void testList_moderator_normalUser() {
        final OccupationTraining training = model().newOccupationTraining(trainingPerson);
        model().newOccupationTraining(otherPerson);

        onSavedAndAuthenticated(createNewUser(SystemUser.Role.ROLE_USER), () -> {
            final List<OccupationTrainingDTO> dtos = feature.listForPerson(trainingPerson.getId());
            assertEquality(training, dtos);
        });
    }

    private void assertEquality(final OccupationTraining training, final List<OccupationTrainingDTO> dtos) {
        assertThat(dtos, hasSize(1));
        final OccupationTrainingDTO dto = dtos.get(0);
        assertThat(dto.getId(), equalTo(training.getId()));
        assertThat(dto.getOccupationType(), equalTo(OccupationType.PETOYHDYSHENKILO));
        assertThat(dto.getTrainingDate(), equalTo(training.getTrainingDate()));
        assertThat(dto.getTrainingType(), equalTo(TrainingType.SAHKOINEN));
    }
}
