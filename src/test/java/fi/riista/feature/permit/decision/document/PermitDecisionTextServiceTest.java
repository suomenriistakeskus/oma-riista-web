package fi.riista.feature.permit.decision.document;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDelivery;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;

import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PermitDecisionTextServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Test
    public void testGenerateDeliveryText_addsAllValues() {

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PermitDecision permitDecision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
            final PermitDecisionDelivery permitDecisionDelivery = model().newPermitDecisionDelivery("rhy1", "email1@invalid");
            final PermitDecisionDelivery permitDecisionDelivery2 = model().newPermitDecisionDelivery("rhy2", "email2@invalid");
            final PermitDecisionDelivery permitDecisionDelivery3 = model().newPermitDecisionDelivery("poliisi", "email3@invalid");
            permitDecision.setDelivery(ImmutableList.of(permitDecisionDelivery, permitDecisionDelivery2, permitDecisionDelivery3));

            final String delivery = permitDecisionTextService.generateDelivery(permitDecision);

            assertTrue(containsIgnoreCase(delivery, "rhy1"));
            assertTrue(containsIgnoreCase(delivery, "rhy2"));
            assertTrue(containsIgnoreCase(delivery, "poliisi"));

        });
    }

    @Test
    public void testGenerateDeliveryText_doesNotAddDuplicates() {

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PermitDecision permitDecision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
            final PermitDecisionDelivery permitDecisionDelivery = model().newPermitDecisionDelivery("poliisi", "email1@invalid");
            final PermitDecisionDelivery permitDecisionDelivery2 = model().newPermitDecisionDelivery("poliisi", "email2@invalid");
            permitDecision.setDelivery(ImmutableList.of(permitDecisionDelivery, permitDecisionDelivery2));

            final String delivery = permitDecisionTextService.generateDelivery(permitDecision);

            long count = Arrays.stream(delivery.split("\\n"))
                    .filter(s -> s.equalsIgnoreCase("poliisi"))
                    .count();

            assertEquals(1L, count);

        });
    }

    @Test
    public void testGenerateDeliveryText_skipsNulls() {

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PermitDecision permitDecision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
            final PermitDecisionDelivery permitDecisionDelivery = model().newPermitDecisionDelivery("rhy1", "email1@invalid");
            final PermitDecisionDelivery permitDecisionDelivery2 = model().newPermitDecisionDelivery(null, "email2@invalid");
            final PermitDecisionDelivery permitDecisionDelivery3 = model().newPermitDecisionDelivery("poliisi", "email3@invalid");
            permitDecision.setDelivery(ImmutableList.of(permitDecisionDelivery, permitDecisionDelivery2, permitDecisionDelivery3));

            final String delivery = permitDecisionTextService.generateDelivery(permitDecision);

            // Header and 2 values
            assertEquals(1 + 2, delivery.split("\\n").length);

            assertTrue(containsIgnoreCase(delivery, "rhy1"));
            assertTrue(containsIgnoreCase(delivery, "poliisi"));

        });
    }

    @Test
    public void testGenerateDeliveryText_skipsEmptyNames() {

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PermitDecision permitDecision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
            final PermitDecisionDelivery permitDecisionDelivery = model().newPermitDecisionDelivery("rhy1", "email1@invalid");
            final PermitDecisionDelivery permitDecisionDelivery2 = model().newPermitDecisionDelivery("", "email2@invalid");
            final PermitDecisionDelivery permitDecisionDelivery3 = model().newPermitDecisionDelivery("poliisi", "email3@invalid");
            permitDecision.setDelivery(ImmutableList.of(permitDecisionDelivery, permitDecisionDelivery2, permitDecisionDelivery3));

            final String delivery = permitDecisionTextService.generateDelivery(permitDecision);

            // Header and 2 values
            assertEquals(1 + 2, delivery.split("\\n").length);

            assertTrue(containsIgnoreCase(delivery, "rhy1"));
            assertTrue(containsIgnoreCase(delivery, "poliisi"));

        });
    }
}
