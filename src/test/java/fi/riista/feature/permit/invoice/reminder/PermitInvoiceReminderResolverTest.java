package fi.riista.feature.permit.invoice.reminder;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.permit.PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.now;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class PermitInvoiceReminderResolverTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitInvoiceReminderResolver resolver;

    private Riistanhoitoyhdistys rhy;
    private PermitDecision decision;


    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        decision = model().newPermitDecision(rhy);
        decision.setPublishDate(now().minusDays(14));
        model().newPermitDecisionInvoice(decision);
    }

    @Test
    public void testSendEmail() {

        final HarvestPermit harvestPermit = model().newHarvestPermit(rhy, decision.createPermitNumber(), FOWL_AND_UNPROTECTED_BIRD, decision);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<PermitInvoiceReminderDTO> resolve = resolver.resolve();
            assertThat(resolve, hasSize(1));
            final PermitInvoiceReminderDTO dto = resolve.get(0);
            assertThat(dto.getHarvestPermitId(), equalTo(harvestPermit.getId()));
        });

    }

    @Test
    public void testSendEmail_multiYearPermit() {
        final HarvestPermit harvestPermit =
                model().newHarvestPermit(rhy, decision.createPermitNumber(), FOWL_AND_UNPROTECTED_BIRD, decision);

        final String secondYearPermitNumber = decision.createPermitNumber(harvestPermit.getPermitYear() + 1);
        model().newHarvestPermit(rhy, secondYearPermitNumber, FOWL_AND_UNPROTECTED_BIRD, decision);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<PermitInvoiceReminderDTO> resolve = resolver.resolve();
            assertThat(resolve, hasSize(1));
            final PermitInvoiceReminderDTO dto = resolve.get(0);
            assertThat(dto.getHarvestPermitId(), equalTo(harvestPermit.getId()));
        });
    }

    @Test
    public void testSendEmail_noPermits() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final List<PermitInvoiceReminderDTO> resolve = resolver.resolve();
            assertThat(resolve, is(empty()));
        });
    }
}
