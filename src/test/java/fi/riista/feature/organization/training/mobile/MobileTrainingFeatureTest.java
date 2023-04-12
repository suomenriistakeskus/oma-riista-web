package fi.riista.feature.organization.training.mobile;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.organization.jht.mobile.MobileJHTTrainingDTO;
import fi.riista.feature.organization.jht.training.JHTTraining;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.mobile.MobileOccupationTrainingDTO;
import fi.riista.feature.organization.rhy.training.OccupationTraining;
import fi.riista.feature.training.mobile.MobileTrainingFeature;
import fi.riista.feature.training.mobile.MobileTrainingsDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import javax.annotation.Resource;
import org.junit.Before;
import org.junit.Test;

public class MobileTrainingFeatureTest extends EmbeddedDatabaseTest {

    private Person trainingPerson;
    private Person otherPerson;

    @Resource
    private MobileTrainingFeature mobileTrainingFeature;

    @Before
    public void setup() {
        trainingPerson = model().newPerson();
        otherPerson = model().newPerson();
    }

    @Test
    public void testListMine() {
        final OccupationTraining occupationTraining = model().newOccupationTraining(trainingPerson);
        model().newOccupationTraining(otherPerson);
        final JHTTraining jhtTraining = model().newJHTTraining(OccupationType.METSASTYKSENVALVOJA, trainingPerson);
        model().newJHTTraining(OccupationType.METSASTYKSENVALVOJA, trainingPerson);

        onSavedAndAuthenticated(createNewUser("person", trainingPerson), () -> {
            final MobileTrainingsDTO dto = mobileTrainingFeature.listMine();
            assertEquality(occupationTraining, jhtTraining, dto);
        });
    }

    private void assertEquality(final OccupationTraining occupationTraining, final JHTTraining jhtTraining, MobileTrainingsDTO dto) {
        assertThat(dto.getOccupationTrainings(), hasSize(1));
        final MobileOccupationTrainingDTO occupationTrainingDto = dto.getOccupationTrainings().get(0);
        assertThat(occupationTrainingDto.getId(), equalTo(occupationTraining.getId()));
        assertThat(occupationTrainingDto.getOccupationType(), equalTo(OccupationType.PETOYHDYSHENKILO));
        assertThat(occupationTrainingDto.getDate(), equalTo(occupationTraining.getTrainingDate()));
        assertThat(occupationTrainingDto.getTrainingType(), equalTo(TrainingType.SAHKOINEN));

        assertThat(dto.getJhtTrainings(), hasSize(1));
        final MobileJHTTrainingDTO jhtTrainingDto = dto.getJhtTrainings().get(0);
        assertThat(jhtTrainingDto.getId(), equalTo(jhtTraining.getId()));
        assertThat(jhtTrainingDto.getOccupationType(), equalTo(OccupationType.METSASTYKSENVALVOJA));
        assertThat(jhtTrainingDto.getDate(), equalTo(jhtTraining.getTrainingDate()));
        assertThat(jhtTrainingDto.getTrainingType(), equalTo(TrainingType.SAHKOINEN));
    }
}
