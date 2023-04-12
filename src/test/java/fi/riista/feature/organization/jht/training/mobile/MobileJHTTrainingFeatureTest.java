package fi.riista.feature.organization.jht.training.mobile;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.jht.mobile.MobileJHTTrainingDTO;
import fi.riista.feature.organization.jht.training.JHTTraining;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.jht.mobile.MobileJHTTrainingFeature;
import fi.riista.test.EmbeddedDatabaseTest;
import java.util.List;
import javax.annotation.Resource;
import org.junit.Test;

public class MobileJHTTrainingFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileJHTTrainingFeature mobileJhtTrainingFeature;

    @Test
    public void listMine() {
        withPerson((person) -> {
            final OccupationType occupationType = AMPUMAKOKEEN_VASTAANOTTAJA;

            final JHTTraining last = model().newJHTTraining(occupationType, person);
            last.setTrainingDate(today().minusDays(1));
            final JHTTraining first = model().newJHTTraining(occupationType, person);
            first.setTrainingDate(last.getTrainingDate().minusDays(1)); // Before last training
            final JHTTraining other = model().newJHTTraining(METSASTYKSENVALVOJA, person);
            other.setTrainingDate(last.getTrainingDate().plusDays(1)); // After last training
            final SystemUser user = createUser(person);

            onSavedAndAuthenticated(user, () -> {
                final List<MobileJHTTrainingDTO> trainingDTOS = mobileJhtTrainingFeature.listMine();
                assertThat(trainingDTOS.size(), equalTo(2));
                trainingDTOS.get(0).getOccupationType().equals(METSASTYKSENVALVOJA);
                trainingDTOS.get(1).getOccupationType().equals(AMPUMAKOKEEN_VASTAANOTTAJA);
            });
        });
    }
}
