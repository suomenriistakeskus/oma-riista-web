package fi.riista.feature.organization.rhy.training.mobile;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.mobile.MobileOccupationTrainingDTO;
import fi.riista.feature.organization.rhy.mobile.MobileOccupationTrainingFeature;
import fi.riista.feature.organization.rhy.training.OccupationTraining;
import fi.riista.test.EmbeddedDatabaseTest;
import java.util.List;
import javax.annotation.Resource;
import org.junit.Before;
import org.junit.Test;

public class MobileOccupationTrainingFeatureTest extends EmbeddedDatabaseTest {

    private Person trainingPerson;
    private Person otherPerson;

    @Resource
    private MobileOccupationTrainingFeature feature;

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
            final List<MobileOccupationTrainingDTO> dtos = feature.listMine();
            assertEquality(training, dtos);
        });
    }

    private void assertEquality(final OccupationTraining training, final List<MobileOccupationTrainingDTO> dtos) {
        assertThat(dtos, hasSize(1));
        final MobileOccupationTrainingDTO dto = dtos.get(0);
        assertThat(dto.getId(), equalTo(training.getId()));
        assertThat(dto.getOccupationType(), equalTo(OccupationType.PETOYHDYSHENKILO));
        assertThat(dto.getDate(), equalTo(training.getTrainingDate()));
        assertThat(dto.getTrainingType(), equalTo(TrainingType.SAHKOINEN));
    }
}
