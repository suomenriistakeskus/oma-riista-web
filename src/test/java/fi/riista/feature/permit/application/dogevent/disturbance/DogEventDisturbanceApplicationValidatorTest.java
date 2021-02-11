package fi.riista.feature.permit.application.dogevent.disturbance;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.dogevent.fixture.DogEventDisturbanceFixtureMixin;
import fi.riista.feature.permit.application.dogevent.fixture.SpeciesFixtureMixin;
import fi.riista.feature.permit.application.validation.HarvestPermitApplicationValidationService;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class DogEventDisturbanceApplicationValidatorTest extends EmbeddedDatabaseTest
        implements DogEventDisturbanceFixtureMixin, SpeciesFixtureMixin {

    @Resource
    private HarvestPermitApplicationValidationService validationService;

    @Test
    public void bothEventsSkipped() {
        withDogDisturbanceSpecies(s -> {
            withSkippedEventFixture(s, true, true, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    assertThat(applicationContentValidationOk(f.application), is(false));
                });
            });
        });
    }

    @Test
    public void skippedTestEvent() {
        withDogDisturbanceSpecies(s -> {
            withSkippedEventFixture(s, false, true, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    assertThat(applicationContentValidationOk(f.application), is(true));

                });
            });
        });
    }

    @Test
    public void skippedTrainingEvent() {
        withDogDisturbanceSpecies(s -> {
            withSkippedEventFixture(s, true, false, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    assertThat(applicationContentValidationOk(f.application), is(true));
                });
            });
        });
    }

    @Test
    public void bothEventsExists() {
        withDogDisturbanceSpecies(s -> {
            withSkippedEventFixture(s, false, false, f -> {
                onSavedAndAuthenticated(createUser(f.applicant), () -> {
                    assertThat(applicationContentValidationOk(f.application), is(true));
                });
            });
        });
    }

    /**
     *  Helpers
     */

    private boolean applicationContentValidationOk(HarvestPermitApplication application) {
        try {
            runInTransaction(() -> validationService.validateContent(application));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
