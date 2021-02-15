package fi.riista.feature.permit.application.gamemanagement.justification;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class GameManagementJustificationFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GameManagementJustificationFeature feature;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;

    private Riistanhoitoyhdistys rhy;
    private Person contactPerson;
    private HarvestPermitApplication application;
    private GameManagementPermitApplication gameManagementPermitApplication;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        contactPerson = model().newPerson();
        application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.GAME_MANAGEMENT);
        application.setContactPerson(contactPerson);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        gameManagementPermitApplication = model().newGameManagementPermitApplication(application);
        gameManagementPermitApplication.setJustification("Game management justification");
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthentication_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getJustification(application.getId());
        });
    }

    @Test
    public void testGetJustification() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final GameManagementJustificationDTO justificationDto = feature.getJustification(application.getId());
            assertThat(justificationDto, is(notNullValue()));
            assertThat(justificationDto.getJustification(), is(equalTo(gameManagementPermitApplication.getJustification())));
        });
    }

    @Test
    public void testUpdateJustification() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final GameManagementJustificationDTO updateDto = new GameManagementJustificationDTO();
            updateDto.setJustification("Updated game management justification");
            feature.updateJustification(application.getId(), updateDto);

            runInTransaction(() -> {
                final GameManagementPermitApplication updatedApplication =
                        gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
                assertThat(updatedApplication, is(notNullValue()));
                assertThat(updatedApplication.getJustification(), is(equalTo(updateDto.getJustification())));
            });
        });
    }
}
