package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.search.InvoiceModeratorDTO;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static fi.riista.feature.account.user.SystemUserPrivilege.ALTER_INVOICE_PAYMENT;
import static fi.riista.feature.permit.invoice.InvoiceState.DELIVERED;
import static fi.riista.feature.permit.invoice.InvoiceState.PAID;
import static fi.riista.feature.permit.invoice.payment.InvoicePaymentUpdateErrorType.CANNOT_REMOVE_ACCOUNT_TRANSFER_BASED_INVOICE_PAYMENT_LINE;
import static fi.riista.feature.permit.invoice.payment.InvoicePaymentUpdateErrorType.CAN_ADD_PAYMENT_LINES_ONLY_FOR_PERMIT_HARVEST_INVOICES;
import static fi.riista.feature.permit.invoice.payment.InvoicePaymentUpdateErrorType.SUM_OF_PAYMENT_LINES_MUST_NOT_BE_NEGATIVE;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.NumberUtils.bigDecimalEquals;
import static org.junit.Assert.assertEquals;

public class InvoicePaymentLineFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private InvoicePaymentLineFeature feature;

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private InvoicePaymentLineRepository paymentRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Resource
    private EnumLocaliser localiser;

    @Test
    public void testAddInvoicePaymentLine_accessDeniedWithoutSpecialPrivilege() {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();

        thrown.expect(AccessDeniedException.class);

        onSavedAndAuthenticated(createNewModerator(), () -> invokeAddInvoicePaymentLine(invoice));
    }

    @Test
    public void testAddInvoicePaymentLine_smokeTest() {
        final LocalDate invoiceDate = today().minusDays(2);
        final Invoice invoice = model().newPermitHarvestInvoice(invoiceDate).getInvoice();

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {

            final AddInvoicePaymentLineDTO inputDTO = newDTO(invoiceDate.plusDays(1), invoice.getAmount());

            final InvoiceModeratorDTO outputDTO = feature.addInvoicePaymentLine(invoice.getId(), inputDTO);

            runInTransaction(() -> {
                final List<InvoicePaymentLine> payments = paymentRepository.findAll();
                assertEquals(1, payments.size());

                final InvoicePaymentLine payment = payments.iterator().next();
                assertEquals(inputDTO.getPaymentDate(), payment.getPaymentDate());
                bigDecimalEquals(inputDTO.getAmount(), payment.getAmount());

                final Invoice updatedInvoice = invoiceRepository.getOne(invoice.getId());
                assertPaymentState(updatedInvoice, PAID, inputDTO.getPaymentDate(), inputDTO.getAmount());

                verifyOutputDTO(outputDTO, updatedInvoice, payments);
            });
        });
    }

    @Test
    public void testAddInvoicePaymentLine_whenAddingAnotherPayment() {
        final LocalDate today = today();
        final LocalDate invoiceDate = today.minusWeeks(1);
        final Invoice invoice = model().newPermitHarvestInvoice(invoiceDate).getInvoice();
        final InvoicePaymentLine firstPayment = newInvoicePaymentLine(invoice, 130, invoiceDate.plusDays(1));

        invoice.setPaid(firstPayment.getPaymentDate());
        invoice.setReceivedAmount(firstPayment.getAmount());

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {

            final InvoiceModeratorDTO outputDTO = invokeAddInvoicePaymentLine(invoice, new BigDecimal(-10));

            runInTransaction(() -> {
                final List<InvoicePaymentLine> payments = paymentRepository.findAll();
                assertEquals(2, payments.size());

                final Invoice updatedInvoice = invoiceRepository.getOne(invoice.getId());

                // Payment date is expected to change.
                assertPaymentState(updatedInvoice, PAID, today, new BigDecimal(130 - 10));

                verifyOutputDTO(outputDTO, updatedInvoice, payments);
            });
        });

    }

    @Test
    public void testAddInvoicePaymentLine_shouldFailForSingleNegativeAmount() {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();

        expectException(SUM_OF_PAYMENT_LINES_MUST_NOT_BE_NEGATIVE);

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {
            invokeAddInvoicePaymentLine(invoice, BigDecimal.ONE.negate());
        });
    }

    @Test
    public void testAddInvoicePaymentLine_shouldFailWhenSumOfMultiplePaymentsIsNegative() {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();
        model().newInvoicePaymentLine(invoice, invoice.getAmount());

        expectException(SUM_OF_PAYMENT_LINES_MUST_NOT_BE_NEGATIVE);

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {
            invokeAddInvoicePaymentLine(invoice, invoice.getAmount().add(BigDecimal.ONE).negate());
        });
    }

    @Test
    public void testAddInvoicePaymentLine_shouldFailForProcessingInvoice() {
        final Invoice invoice = model().newPermitDecisionInvoice().getInvoice();

        expectException(CAN_ADD_PAYMENT_LINES_ONLY_FOR_PERMIT_HARVEST_INVOICES);

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> invokeAddInvoicePaymentLine(invoice));
    }

    @Test
    public void testRemoveInvoicePaymentLine_accessDeniedWithoutSpecialPrivilege() {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();
        final InvoicePaymentLine payment = model().newInvoicePaymentLine(invoice);

        thrown.expect(AccessDeniedException.class);

        onSavedAndAuthenticated(createNewModerator(), () -> feature.removeInvoicePaymentLine(payment.getId()));
    }

    @Test
    public void testRemoveInvoicePaymentLine_whenRemovingOnlyRemainingPayment_whenPaytrailPaymentInitiated() {
        testRemoveInvoicePaymentLine_whenRemovingOnlyRemainingPayment(true);
    }

    @Test
    public void testRemoveInvoicePaymentLine_whenRemovingOnlyRemainingPayment_whenPaytrailPaymentNotInitiated() {
        testRemoveInvoicePaymentLine_whenRemovingOnlyRemainingPayment(false);
    }

    private void testRemoveInvoicePaymentLine_whenRemovingOnlyRemainingPayment(final boolean paytrailPaymentInitiated) {
        final Invoice invoice = model().newPermitHarvestInvoice().getInvoice();
        final InvoicePaymentLine payment = model().newInvoicePaymentLine(invoice);

        invoice.setPaid(payment.getPaymentDate());
        invoice.setReceivedAmount(payment.getAmount());

        if (paytrailPaymentInitiated) {
            invoice.setPaytrailPaymentId("1");
        }

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {

            final InvoiceModeratorDTO outputDTO = feature.removeInvoicePaymentLine(payment.getId());

            runInTransaction(() -> {
                final List<InvoicePaymentLine> payments = paymentRepository.findAll();
                assertEmpty(payments);

                final Invoice updatedInvoice = invoiceRepository.getOne(invoice.getId());

                if (paytrailPaymentInitiated) {
                    assertPaymentState(updatedInvoice, PAID, payment.getPaymentDate(), null);
                } else {
                    assertPaymentState(updatedInvoice, DELIVERED, null, null);
                }

                verifyOutputDTO(outputDTO, updatedInvoice, payments);
            });
        });
    }

    @Test
    public void testRemoveInvoicePaymentLine_whenRemovingFirstOfTwoPayments() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final Invoice invoice = model().newPermitHarvestInvoice(invoiceDate).getInvoice();

        final InvoicePaymentLine payment1 = newInvoicePaymentLine(invoice, 70, invoiceDate.plusDays(1));
        final InvoicePaymentLine payment2 = newInvoicePaymentLine(invoice, 50, invoiceDate.plusDays(2));

        final LocalDate latestPaymentDate = payment2.getPaymentDate();
        invoice.setPaid(latestPaymentDate);
        invoice.setReceivedAmount(new BigDecimal(70 + 50));

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {

            final InvoiceModeratorDTO outputDTO = feature.removeInvoicePaymentLine(payment1.getId());

            runInTransaction(() -> {
                final List<InvoicePaymentLine> payments = paymentRepository.findAll();
                assertEquals(F.getUniqueIds(payment2), F.getUniqueIds(payments));

                final Invoice updatedInvoice = invoiceRepository.getOne(invoice.getId());
                assertPaymentState(updatedInvoice, PAID, latestPaymentDate, new BigDecimal(50));

                verifyOutputDTO(outputDTO, updatedInvoice, payments);
            });
        });
    }

    @Test
    public void testRemoveInvoicePaymentLine_whenRemovingLatterOfTwoPayments() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final Invoice invoice = model().newPermitHarvestInvoice(invoiceDate).getInvoice();

        final InvoicePaymentLine payment1 = newInvoicePaymentLine(invoice, 70, invoiceDate.plusDays(1));
        final InvoicePaymentLine payment2 = newInvoicePaymentLine(invoice, 50, invoiceDate.plusDays(2));

        invoice.setPaid(payment2.getPaymentDate());
        invoice.setReceivedAmount(new BigDecimal(70 + 50));

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {

            final InvoiceModeratorDTO outputDTO = feature.removeInvoicePaymentLine(payment2.getId());

            runInTransaction(() -> {
                final List<InvoicePaymentLine> payments = paymentRepository.findAll();
                assertEquals(F.getUniqueIds(payment1), F.getUniqueIds(payments));

                final Invoice updatedInvoice = invoiceRepository.getOne(invoice.getId());
                assertPaymentState(updatedInvoice, PAID, payment1.getPaymentDate(), new BigDecimal(70));

                verifyOutputDTO(outputDTO, updatedInvoice, payments);
            });
        });
    }

    @Test
    public void testRemoveInvoicePaymentLine_whenRemovingMidstOfThreePayments() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final Invoice invoice = model().newPermitHarvestInvoice(invoiceDate).getInvoice();

        final InvoicePaymentLine payment1 = newInvoicePaymentLine(invoice, 30, invoiceDate.plusDays(1));
        final InvoicePaymentLine payment2 = newInvoicePaymentLine(invoice, 40, invoiceDate.plusDays(2));
        final InvoicePaymentLine payment3 = newInvoicePaymentLine(invoice, 50, invoiceDate.plusDays(3));

        final LocalDate latestPaymentDate = payment3.getPaymentDate();
        invoice.setPaid(latestPaymentDate);
        invoice.setReceivedAmount(new BigDecimal(30 + 40 + 50));

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {

            final InvoiceModeratorDTO outputDTO = feature.removeInvoicePaymentLine(payment2.getId());

            runInTransaction(() -> {
                final List<InvoicePaymentLine> payments = paymentRepository.findAll();
                assertEquals(F.getUniqueIds(payment1, payment3), F.getUniqueIds(payments));

                final Invoice updatedInvoice = invoiceRepository.getOne(invoice.getId());
                assertPaymentState(updatedInvoice, PAID, latestPaymentDate, new BigDecimal(30 + 50));

                verifyOutputDTO(outputDTO, updatedInvoice, payments);
            });
        });
    }

    @Test
    public void testRemoveInvoicePaymentLine_whenRemovingLastOfThreePayments() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final Invoice invoice = model().newPermitHarvestInvoice(invoiceDate).getInvoice();

        final InvoicePaymentLine payment1 = newInvoicePaymentLine(invoice, 30, invoiceDate.plusDays(1));
        final InvoicePaymentLine payment2 = newInvoicePaymentLine(invoice, 40, invoiceDate.plusDays(2));
        final InvoicePaymentLine payment3 = newInvoicePaymentLine(invoice, 50, invoiceDate.plusDays(3));

        invoice.setPaid(payment3.getPaymentDate());
        invoice.setReceivedAmount(new BigDecimal(30 + 40 + 50));

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {

            final InvoiceModeratorDTO outputDTO = feature.removeInvoicePaymentLine(payment3.getId());

            runInTransaction(() -> {
                final List<InvoicePaymentLine> payments = paymentRepository.findAll();
                assertEquals(F.getUniqueIds(payment1, payment2), F.getUniqueIds(payments));

                final Invoice updatedInvoice = invoiceRepository.getOne(invoice.getId());
                assertPaymentState(updatedInvoice, PAID, payment2.getPaymentDate(), new BigDecimal(30 + 40));

                verifyOutputDTO(outputDTO, updatedInvoice, payments);
            });
        });
    }

    @Test
    public void testRemoveInvoicePaymentLine_shouldFailIfRemovingAccountTransferBasedPaymentLine() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final LocalDate paymentDate = invoiceDate.plusDays(1);

        final Invoice invoice = model().newPermitHarvestInvoice(invoiceDate).getInvoice();
        invoice.setPaid(paymentDate);

        final AccountTransfer accountTransfer =
                model().newAccountTransfer(invoice, paymentDate.plusDays(1), paymentDate.plusDays(2));

        final InvoicePaymentLine payment = model().newInvoicePaymentLine(invoice, accountTransfer);

        expectException(CANNOT_REMOVE_ACCOUNT_TRANSFER_BASED_INVOICE_PAYMENT_LINE);

        onSavedAndAuthenticated(createNewModerator(ALTER_INVOICE_PAYMENT), () -> {
            feature.removeInvoicePaymentLine(payment.getId());
        });
    }

    private InvoicePaymentLine newInvoicePaymentLine(final Invoice invoice,
                                                     final int amount,
                                                     final LocalDate paymentDate) {

        return model().newInvoicePaymentLine(invoice, new BigDecimal(amount), paymentDate);
    }

    private static AddInvoicePaymentLineDTO newDTO(final LocalDate paymentDate, final BigDecimal amount) {
        return new AddInvoicePaymentLineDTO(paymentDate, amount);
    }

    private InvoiceModeratorDTO invokeAddInvoicePaymentLine(final Invoice invoice) {
        return invokeAddInvoicePaymentLine(invoice, invoice.getAmount());
    }

    private InvoiceModeratorDTO invokeAddInvoicePaymentLine(final Invoice invoice, final BigDecimal amount) {
        return feature.addInvoicePaymentLine(invoice.getId(), newDTO(today(), amount));
    }

    private void expectException(final InvoicePaymentUpdateErrorType errorType) {
        thrown.expect(InvoicePaymentLineUpdateException.class);
        thrown.expectMessage(InvoicePaymentLineUpdateException.from(errorType, localiser).getMessage());
    }

    private static void assertPaymentState(final Invoice invoice,
                                           final InvoiceState expectedState,
                                           final LocalDate expectedPaymentDate,
                                           final BigDecimal expectedReceivedAmount) {

        assertEquals(expectedState, invoice.getState());
        assertEquals(expectedPaymentDate, invoice.getPaymentDate());
        bigDecimalEquals(expectedReceivedAmount, invoice.getReceivedAmount());
    }

    private static void verifyOutputDTO(final InvoiceModeratorDTO outputDTO,
                                        final Invoice updatedInvoice,
                                        final List<InvoicePaymentLine> resultPayments) {

        bigDecimalEquals(
                Optional.ofNullable(updatedInvoice.getReceivedAmount()).orElse(BigDecimal.ZERO),
                outputDTO.getReceivedAmount());

        assertEquals(updatedInvoice.getState().name(), outputDTO.getState().name());
        assertEquals(F.getUniqueIds(resultPayments), F.getUniqueIds(outputDTO.getPayments()));
    }
}
