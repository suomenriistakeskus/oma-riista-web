package fi.riista.feature.permit.decision.delivery;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class PermitDecisionDeliveryFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitDecisionDeliveryFeature feature;

    @Resource
    private PermitDecisionDeliveryRepository deliveryRepository;

    private Riistanhoitoyhdistys rhy;
    private SystemUser moderator;
    private PermitDecision decision;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        rhy.setEmail("rhy@invalid");

        moderator = createNewModerator();

        decision = model().newPermitDecision(rhy);
        decision.setHandler(moderator);
        decision.setStatus(DecisionStatus.DRAFT);
    }

    @Test
    public void testDeliveryGeneration_default() {


        onSavedAndAuthenticated(moderator, () -> {
            final PermitDecisionDeliveryUpdateDTO dto = new PermitDecisionDeliveryUpdateDTO();
            dto.setId(decision.getId());
            dto.setDeliveries(emptyList());

            feature.updateDelivery(decision.getId(), dto);
        });

        runInTransaction(() -> {
            final List<PermitDecisionDelivery> deliveries = deliveryRepository.findAll();

            assertThat(deliveries, hasSize(1));
            assertThat(deliveries.get(0).getEmail(), equalTo(rhy.getEmail()));
        });

    }

    @Test
    public void testDeliveryGeneration_enabled() {
        decision.setAutomaticDeliveryDeduction(true);

        onSavedAndAuthenticated(moderator, () -> {
            final PermitDecisionDeliveryUpdateDTO dto = new PermitDecisionDeliveryUpdateDTO();
            dto.setId(decision.getId());
            dto.setDeliveries(emptyList());

            feature.updateDelivery(decision.getId(), dto);
        });

        runInTransaction(() -> {
            final List<PermitDecisionDelivery> deliveries = deliveryRepository.findAll();

            assertThat(deliveries, hasSize(1));
            assertThat(deliveries.get(0).getEmail(), equalTo(rhy.getEmail()));
        });

    }

    @Test
    public void testDeliveryGeneration_disabled() {
        decision.setAutomaticDeliveryDeduction(false);

        onSavedAndAuthenticated(moderator, () -> {
            final PermitDecisionDeliveryUpdateDTO dto = new PermitDecisionDeliveryUpdateDTO();
            dto.setId(decision.getId());
            dto.setDeliveries(emptyList());

            feature.updateDelivery(decision.getId(), dto);
        });

        runInTransaction(() -> {
            final List<PermitDecisionDelivery> deliveries = deliveryRepository.findAll();

            assertThat(deliveries, Matchers.is(emptyList()));
        });

    }

}
