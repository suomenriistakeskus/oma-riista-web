package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoice;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoice;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO.byApplicationNumber;
import static fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO.byCreditorReferenceOf;
import static fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO.byInvoiceNumberOf;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.NumberUtils.bigDecimalEquals;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class InvoiceSearchFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private InvoiceSearchFeature feature;

    @Test
    public void testSearchInvoices_byInvoiceNumberOfPermitDecisionInvoice() {
        withRhy(rhy -> {
            final Invoice invoice = model().newPermitDecisionInvoice(rhy).getInvoice();

            // Create another invoice to verify result correctness.
            model().newPermitDecisionInvoice(rhy);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                verifyResultsOfSearching(byInvoiceNumberOf(invoice), asList(invoice));
            });
        });
    }

    @Test
    public void testSearchInvoices_byCreditorReferenceOfPermitDecisionInvoice() {
        withRhy(rhy -> {
            final Invoice invoice = model().newPermitDecisionInvoice(rhy).getInvoice();

            // Create another invoice to verify result correctness.
            model().newPermitDecisionInvoice(rhy);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                verifyResultsOfSearching(byInvoiceNumberOf(invoice), asList(invoice));
            });
        });
    }

    @Test
    public void testSearchInvoices_byInvoiceNumberOfPermitHarvestInvoice() {
        withRhy(rhy -> {
            final PermitHarvestInvoice harvestInvoice = model().newPermitHarvestInvoice(rhy);
            final Invoice invoice = harvestInvoice.getInvoice();

            // Create more invoices to verify correct filtering.
            model().newPermitDecisionInvoice(harvestInvoice.getSpeciesAmount().getHarvestPermit().getPermitDecision());
            model().newPermitHarvestInvoice(rhy);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                verifyResultsOfSearching(byInvoiceNumberOf(invoice), asList(invoice));
            });
        });
    }

    @Test
    public void testSearchInvoices_byCreditorReferenceOfPermitHarvestInvoice() {
        withRhy(rhy -> {
            final PermitHarvestInvoice harvestInvoice = model().newPermitHarvestInvoice(rhy);
            final Invoice invoice = harvestInvoice.getInvoice();

            // Create more invoices to verify correct filtering.
            model().newPermitDecisionInvoice(harvestInvoice.getSpeciesAmount().getHarvestPermit().getPermitDecision());
            model().newPermitHarvestInvoice(rhy);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                verifyResultsOfSearching(byCreditorReferenceOf(invoice), asList(invoice));
            });
        });
    }

    @Test
    public void testSearchInvoices_byApplicationNumber() {
        withRhy(rhy -> {
            final HarvestPermitApplication application =
                    model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.MOOSELIKE);
            final PermitDecision decision = model().newPermitDecision(application);
            final PermitDecisionInvoice decisionInvoice = model().newPermitDecisionInvoice(decision);

            final HarvestPermit permit = model().newMooselikePermit(decision);
            final HarvestPermitSpeciesAmount spa1 = model().newHarvestPermitSpeciesAmount(permit);
            final HarvestPermitSpeciesAmount spa2 = model().newHarvestPermitSpeciesAmount(permit);

            final PermitHarvestInvoice harvestInvoice1 = model().newPermitHarvestInvoice(spa1);
            final PermitHarvestInvoice harvestInvoice2 = model().newPermitHarvestInvoice(spa2);

            // Create more invoices to verify correct filtering.
            model().newPermitDecisionInvoice(model().newPermitDecision(rhy));
            model().newPermitHarvestInvoice(rhy, spa1.getGameSpecies());
            model().newPermitHarvestInvoice(rhy, spa2.getGameSpecies());

            onSavedAndAuthenticated(createNewModerator(), () -> {
                verifyResultsOfSearching(
                        byApplicationNumber(application.getApplicationNumber()),
                        asList(harvestInvoice2.getInvoice(), harvestInvoice1.getInvoice(),
                                decisionInvoice.getInvoice()));
            });
        });
    }

    @Test
    public void testSearchInvoices_byPaymentState() {
        withRhy(rhy -> {
            final LocalDate today = today();
            final BigDecimal correctedAmount = new BigDecimal("600.00");

            final Invoice unpaidDecisionInvoice = model().newPermitDecisionInvoice(rhy).getInvoice();

            final Invoice paidDecisionInvoice = model().newPermitDecisionInvoice(rhy).getInvoice();
            paidDecisionInvoice.setPaid(today);

            final Invoice unpaidHarvestInvoice = model().newPermitHarvestInvoice(rhy).getInvoice();

            final Invoice paidHarvestInvoice = model().newPermitHarvestInvoice(rhy).getInvoice();
            paidHarvestInvoice.setPaid(today);
            paidHarvestInvoice.setReceivedAmount(paidHarvestInvoice.getAmount());

            final Invoice harvestInvoiceWithPaymentInitiatedViaPaytrail =
                    model().newPermitHarvestInvoice(rhy).getInvoice();
            harvestInvoiceWithPaymentInitiatedViaPaytrail.setPaid(today);

            final Invoice partiallyPaidHarvestInvoice = model().newPermitHarvestInvoice(rhy).getInvoice();
            partiallyPaidHarvestInvoice.setPaid(today);
            partiallyPaidHarvestInvoice
                    .setReceivedAmount(partiallyPaidHarvestInvoice.getAmount().subtract(BigDecimal.ONE));

            final Invoice correctedHarvestInvoice = model().newPermitHarvestInvoice(rhy).getInvoice();
            correctedHarvestInvoice.setCorrectedAmount(correctedAmount);
            correctedHarvestInvoice.setPaid(today);
            correctedHarvestInvoice.setReceivedAmount(correctedHarvestInvoice.getAmount());

            final Invoice correctedAndFullyPaidHarvestInvoice = model().newPermitHarvestInvoice(rhy).getInvoice();
            correctedAndFullyPaidHarvestInvoice.setCorrectedAmount(correctedAmount);
            correctedAndFullyPaidHarvestInvoice.setPaid(today);
            correctedAndFullyPaidHarvestInvoice.setReceivedAmount(correctedAmount);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final InvoiceSearchFilterDTO paidPaymentState = new InvoiceSearchFilterDTO();
                paidPaymentState.setPaymentState(InvoicePaymentState.PAID);

                verifyResultsOfSearching(paidPaymentState, asList(
                        correctedAndFullyPaidHarvestInvoice, paidHarvestInvoice, paidDecisionInvoice));

                final InvoiceSearchFilterDTO paymentSumDiffers = new InvoiceSearchFilterDTO();
                paymentSumDiffers.setPaymentState(InvoicePaymentState.PAYMENT_SUM_DIFFERS);

                verifyResultsOfSearching(
                        paymentSumDiffers, asList(correctedHarvestInvoice, partiallyPaidHarvestInvoice));

                final InvoiceSearchFilterDTO otherPaymentState = new InvoiceSearchFilterDTO();
                otherPaymentState.setPaymentState(InvoicePaymentState.OTHER);

                verifyResultsOfSearching(otherPaymentState, asList(
                        harvestInvoiceWithPaymentInitiatedViaPaytrail, unpaidHarvestInvoice, unpaidDecisionInvoice));
            });
        });
    }

    private void verifyResultsOfSearching(final InvoiceSearchFilterDTO filters, final List<Invoice> expected) {
        final List<InvoiceSearchResultDTO> results = feature.searchInvoices(filters);

        assertEquals(expected.size(), results.size());

        for (int i = 0; i < results.size(); i++) {
            assertSearchResult(expected.get(i), results.get(i));
        }
    }

    private static void assertSearchResult(final Invoice source, final InvoiceSearchResultDTO result) {
        assertEquals(source.getId(), result.getId());
        assertEquals(source.getType(), result.getType());
        assertEquals(source.getInvoiceNumber(), result.getInvoiceNumber());
        assertEquals(source.getInvoiceDate(), result.getInvoiceDate());
        assertEquals(source.getDueDate(), result.getDueDate());
        assertEquals(source.getCreditorReference().getValue(), result.getCreditorReference());
        bigDecimalEquals(
                Optional.ofNullable(source.getCorrectedAmount()).orElse(source.getAmount()),
                result.getPaymentAmount());
        assertEquals(source.getRecipientName(), result.getInvoiceRecipientName());
    }
}
