package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NominationDecisionFeatureTest extends EmbeddedDatabaseTest {

    private NominationDecision decision;
    private Riistanhoitoyhdistys rhy;
    private Person coordinator;

    @Resource
    private NominationDecisionFeature feature;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        coordinator = model().newPersonWithAddress();
        model().newOccupation(rhy, coordinator, OccupationType.TOIMINNANOHJAAJA);
        final DeliveryAddress deliveryAddress = DeliveryAddress.create(rhy.getNameFinnish(), coordinator.getAddress());

        decision = model().newNominationDecision(rhy, OccupationType.METSASTYKSENVALVOJA, coordinator, deliveryAddress);
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthentication_unauthorized() {
        persistInNewTransaction();

        feature.getDecision(decision.getId());
        fail("Should have thrown an exception");
    }

    @Test
    public void testGetSmoke() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final NominationDecisionDTO dto = feature.getDecision(feature.getDecision(decision.getId()).getId());
            assertEquals(decision.getDecisionType(), dto.getDecisionType());
            assertEquals(rhy.getId(), dto.getRhy().getId());
            assertEquals(decision.getOccupationType(), dto.getOccupationType());
            assertEquals(decision.getDeliveryAddress().getStreetAddress(), dto.getDeliveryAddress().getStreetAddress());
            assertEquals(decision.getDeliveryAddress().getPostalCode(), dto.getDeliveryAddress().getPostalCode());
            assertEquals(decision.getDeliveryAddress().getCity(), dto.getDeliveryAddress().getCity());
            assertEquals(decision.getDeliveryAddress().getCountry(), dto.getDeliveryAddress().getCountry());
        });
    }
}
