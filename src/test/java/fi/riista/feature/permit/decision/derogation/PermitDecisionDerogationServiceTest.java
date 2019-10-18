package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Collections;

public class PermitDecisionDerogationServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitDecisionDerogationService permitDecisionDerogationService;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    private Riistanhoitoyhdistys rhy;
    private SystemUser handler;
    private PermitDecision decision;
    private HarvestPermitApplication application;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        rhy.setEmail("rhy@invalid");
        handler = createNewModerator();
        application = createBirdPermitApplication();
        decision = model().newPermitDecision(application);
        decision.setHandler(handler);
        decision.setStatusDraft();
        persistInNewTransaction();
    }

    @Test
    public void testValid() {
        onSavedAndAuthenticated(handler, () -> {
            permitDecisionDerogationService.requireDecisionDerogationEditable(decision.getId());
        });
    }


    @Test
    public void testCannotEditDerogationForLockedDecision() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(Matchers.startsWith("status should be"));

        updateDecision(decision::setStatusLocked);

        onSavedAndAuthenticated(handler, () -> {
            permitDecisionDerogationService.requireDecisionDerogationEditable(decision.getId());
        });
    }


    @Test
    public void testCannotEditDerogationWhenHandlerNotSet() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Handler is null");

        updateDecision(() -> decision.setHandler(null));

        onSavedAndAuthenticated(handler, () -> {
            permitDecisionDerogationService.requireDecisionDerogationEditable(decision.getId());
        });
    }

    @Test
    public void testCannotEditDerogationForDecisionHandledByOtherModerator() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Handler is not same as current user");

        onSavedAndAuthenticated(createNewModerator(), () -> {
            permitDecisionDerogationService.requireDecisionDerogationEditable(decision.getId());
        });
    }

    @Test
    public void basicUserCannotEditDerogationData() {
        thrown.expect(AccessDeniedException.class);
        thrown.expectMessage(Matchers.startsWith("Denied"));

        onSavedAndAuthenticated(createNewUser(), () -> {
            permitDecisionDerogationService.requireDecisionDerogationEditable(decision.getId());
        });
    }

    private void updateDecision(Runnable runnable) {
        runInTransaction(() -> {
            runnable.run();
            permitDecisionRepository.save(decision);
        });
    }

    private HarvestPermitApplication createBirdPermitApplication() {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        model().newBirdPermitApplication(application);

        application.setDeliveryAddress(DeliveryAddress.createFromPersonNullable(application.getContactPerson()));
        final HarvestPermitApplicationSpeciesAmount harvestPermitApplicationSpeciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, model().newGameSpecies(), 1.0f, 4);
        application.setSpeciesAmounts(Collections.singletonList(harvestPermitApplicationSpeciesAmount));
        return application;
    }
}
