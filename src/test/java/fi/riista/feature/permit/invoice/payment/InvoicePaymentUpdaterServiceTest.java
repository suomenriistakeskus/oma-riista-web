package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceRepository;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import fi.riista.integration.mmm.transfer.AccountTransferRepository;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.feature.permit.invoice.InvoiceState.DELIVERED;
import static fi.riista.feature.permit.invoice.InvoiceState.PAID;
import static fi.riista.util.Collect.mappingTo;
import static fi.riista.util.DateUtil.today;
import static fi.riista.util.NumberUtils.bigDecimalEquals;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InvoicePaymentUpdaterServiceTest implements DefaultEntitySupplierProvider {

    @InjectMocks
    private InvoicePaymentUpdaterService service;

    @Mock
    private AccountTransferRepository transferRepository;

    @Mock
    private InvoicePaymentLineRepository paymentLineRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Test
    public void testFindMatchingAccountTransfersAndCreateInvoicePaymentLines_whenInvoiceNotPaidAndAccountTransferNotPresent() {
        final Invoice invoice = getEntitySupplier().newPermitHarvestInvoice(today().minusWeeks(1)).getInvoice();

        testFindMatchingAccountTransfersAndCreateInvoicePaymentLines(invoice, emptyList());

        assertPaymentState(invoice, DELIVERED, null, null);
    }

    @Test
    public void testFindMatchingAccountTransfersAndCreateInvoicePaymentLines_whenInvoicePaidAndAccountTransferNotPresent() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final LocalDate paymentDate = invoiceDate.plusDays(1);

        final Invoice invoice = getEntitySupplier().newPermitHarvestInvoice(invoiceDate).getInvoice();
        invoice.setPaid(paymentDate);
        invoice.setPaytrailPaymentId("1");

        testFindMatchingAccountTransfersAndCreateInvoicePaymentLines(invoice, emptyList());

        assertPaymentState(invoice, PAID, paymentDate, null);
    }

    @Test
    public void testFindMatchingAccountTransfersAndCreateInvoicePaymentLines_whenInvoiceNotPaidAndOneAccountTransferExists() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final Invoice invoice = getEntitySupplier().newPermitHarvestInvoice(invoiceDate).getInvoice();

        final AccountTransfer transfer =
                getEntitySupplier().newAccountTransfer(invoice, invoiceDate.plusDays(1), invoiceDate.plusDays(2));

        testFindMatchingAccountTransfersAndCreateInvoicePaymentLines(invoice, singletonList(transfer));

        assertPaymentState(invoice, PAID, transfer.getTransactionDate(), transfer.getAmount());
    }

    @Test
    public void testFindMatchingAccountTransfersAndCreateInvoicePaymentLines_whenInvoicePaidAndOneAccountTransferExists() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final LocalDate paymentDate = invoiceDate.plusDays(1);
        final BigDecimal paymentAmount = new BigDecimal("120.00");

        final Invoice invoice = getEntitySupplier().newPermitHarvestInvoice(invoiceDate).getInvoice();
        invoice.setPaid(paymentDate);
        invoice.setAmount(paymentAmount);
        invoice.setPaytrailPaymentId("1");

        final AccountTransfer transfer =
                getEntitySupplier().newAccountTransfer(invoice, paymentDate.plusDays(1), paymentDate.plusDays(2));

        testFindMatchingAccountTransfersAndCreateInvoicePaymentLines(invoice, singletonList(transfer));

        assertPaymentState(invoice, PAID, paymentDate, paymentAmount);
    }

    @Test
    public void testFindMatchingAccountTransfersAndCreateInvoicePaymentLines_whenInvoicePaidAndTwoAccountTransfersExist() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final LocalDate paymentDate = invoiceDate.plusDays(1);
        final BigDecimal paymentAmount = new BigDecimal("240.00");

        final Invoice invoice = getEntitySupplier().newPermitHarvestInvoice(invoiceDate).getInvoice();
        invoice.setPaid(paymentDate);
        invoice.setAmount(paymentAmount);
        invoice.setPaytrailPaymentId("1");

        final AccountTransfer transfer =
                getEntitySupplier().newAccountTransfer(invoice, paymentDate.plusDays(1), paymentDate.plusDays(2));
        transfer.setAmount(new BigDecimal(120));

        final AccountTransfer transfer2 =
                getEntitySupplier().newAccountTransfer(invoice, paymentDate.plusDays(3), paymentDate.plusDays(4));
        transfer2.setAmount(new BigDecimal(120));

        testFindMatchingAccountTransfersAndCreateInvoicePaymentLines(invoice, asList(transfer, transfer2));

        assertPaymentState(invoice, PAID, transfer2.getTransactionDate(), paymentAmount);
    }

    @Test
    public void testFindMatchingAccountTransfersAndCreateInvoicePaymentLines_whenSumOfAccountTransferAmountsIsNegative() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final Invoice invoice = getEntitySupplier().newPermitHarvestInvoice(invoiceDate).getInvoice();

        final AccountTransfer transfer =
                getEntitySupplier().newAccountTransfer(invoice, invoiceDate.plusDays(1), invoiceDate.plusDays(2));
        transfer.setAmount(new BigDecimal(1));

        final AccountTransfer transfer2 =
                getEntitySupplier().newAccountTransfer(invoice, invoiceDate.plusDays(3), invoiceDate.plusDays(4));
        transfer2.setAmount(new BigDecimal(-2));

        try {
            testFindMatchingAccountTransfersAndCreateInvoicePaymentLines(invoice, asList(transfer, transfer2));
            fail("Should have failed because of negative sum");

        } catch (final InvoicePaymentLineUpdateException e) {
            // Expected path

            assertPaymentState(invoice, DELIVERED, null, null);
        }
    }

    private void testFindMatchingAccountTransfersAndCreateInvoicePaymentLines(final Invoice invoice,
                                                                              final List<AccountTransfer> relatedAccountTransfers) {
        // First, mock repositories.
        when(transferRepository.findAccountTransfersNotAssociatedWithInvoice(eq(invoice.getCreditorReference())))
                .thenReturn(relatedAccountTransfers);

        final List<InvoicePaymentLine> expectedResultingPaymentLines = relatedAccountTransfers.stream()
                .map(transfer -> new InvoicePaymentLine(invoice, transfer))
                .collect(toList());

        when(paymentLineRepository.findByInvoice(eq(invoice))).thenReturn(expectedResultingPaymentLines);

        // Then call method under test.
        service.findMatchingAccountTransfersAndCreateInvoicePaymentLines(invoice);

        // And finally run verifications.

        verify(transferRepository, times(1))
                .findAccountTransfersNotAssociatedWithInvoice(eq(invoice.getCreditorReference()));
        verifyResultingInvoicePaymentLines(expectedResultingPaymentLines);

        if (!relatedAccountTransfers.isEmpty()) {
            verify(paymentLineRepository, times(1)).findByInvoice(eq(invoice));
        }

        verifyNoMoreInteractions(transferRepository, paymentLineRepository, invoiceRepository);
    }

    @Test
    public void testCreateInvoicePaymentLinesFromAccountTransfers_whenUnassociatedAccountTransfersNotFound() {
        testCreateInvoicePaymentLinesFromAccountTransfers(emptyList(), emptyList());
    }

    @Test
    public void testCreateInvoicePaymentLinesFromAccountTransfers_whenNoInvoicesMatchingFoundAccountTransfers() {
        final LocalDate today = today();

        final AccountTransfer transfer1 =
                getEntitySupplier().newAccountTransfer(today.minusDays(2), today.minusDays(1));

        final AccountTransfer transfer2 =
                getEntitySupplier().newAccountTransfer(today.minusDays(4), today.minusDays(3));

        testCreateInvoicePaymentLinesFromAccountTransfers(asList(transfer1, transfer2), emptyList());
    }

    @Test
    public void testCreateInvoicePaymentLinesFromAccountTransfers_withMultipleInvoicesPaidViaPaytrail() {
        final LocalDate today = today();

        final LocalDate invoice1Date = today.minusWeeks(2);
        final LocalDate invoice1PaytrailPaymentDate = invoice1Date.plusDays(1);

        final Invoice invoice1 = getEntitySupplier().newPermitHarvestInvoice(invoice1Date).getInvoice();
        invoice1.setAmount(new BigDecimal("170.00"));
        invoice1.setPaid(invoice1PaytrailPaymentDate);
        invoice1.setPaytrailPaymentId("1");

        final AccountTransfer invoice1Transfer = getEntitySupplier().newAccountTransfer(
                invoice1, invoice1PaytrailPaymentDate.plusDays(1), invoice1PaytrailPaymentDate.plusDays(2));
        invoice1Transfer.setAmount(invoice1.getAmount());

        final LocalDate invoice2Date = today.minusWeeks(1);
        final LocalDate invoice2PaytrailPaymentDate = invoice2Date.plusDays(1);

        final Invoice invoice2 = getEntitySupplier().newPermitHarvestInvoice(invoice2Date).getInvoice();
        invoice2.setAmount(new BigDecimal("120.00"));
        invoice2.setPaid(invoice2PaytrailPaymentDate);
        invoice2.setPaytrailPaymentId("2");

        final AccountTransfer invoice2Transfer = getEntitySupplier().newAccountTransfer(
                invoice2, invoice2PaytrailPaymentDate.plusDays(1), invoice2PaytrailPaymentDate.plusDays(2));
        invoice2Transfer.setAmount(invoice2.getAmount());

        testCreateInvoicePaymentLinesFromAccountTransfers(
                asList(invoice1Transfer, invoice2Transfer),
                asList(invoice1, invoice2));

        assertPaymentState(invoice1, PAID, invoice1PaytrailPaymentDate, invoice1.getAmount());
        assertPaymentState(invoice2, PAID, invoice2PaytrailPaymentDate, invoice2.getAmount());
    }

    @Test
    public void testCreateInvoicePaymentLinesFromAccountTransfers_whenOfflinePaidInvoiceHasMultipleAccountTransfers() {
        final LocalDate invoiceDate = today().minusWeeks(1);
        final Invoice invoice = getEntitySupplier().newPermitHarvestInvoice(invoiceDate).getInvoice();

        final AccountTransfer transfer1 =
                getEntitySupplier().newAccountTransfer(invoice, invoiceDate.plusDays(1), invoiceDate.plusDays(2));
        transfer1.setAmount(new BigDecimal("120.00"));

        final AccountTransfer transfer2 =
                getEntitySupplier().newAccountTransfer(invoice, invoiceDate.plusDays(3), invoiceDate.plusDays(4));
        transfer2.setAmount(new BigDecimal("50.00"));

        final AccountTransfer transfer3 =
                getEntitySupplier().newAccountTransfer(invoice, invoiceDate.plusDays(5), invoiceDate.plusDays(6));
        transfer3.setAmount(new BigDecimal("50.00"));

        testCreateInvoicePaymentLinesFromAccountTransfers(asList(transfer1, transfer2, transfer3), asList(invoice));

        assertPaymentState(invoice, PAID, transfer3.getTransactionDate(), new BigDecimal("220.00"));
    }

    private void testCreateInvoicePaymentLinesFromAccountTransfers(final List<AccountTransfer> foundAccountTransfers,
                                                                   final List<Invoice> matchingInvoices) {

        // First, mock repositories.
        when(transferRepository.findAccountTransfersNotAssociatedWithInvoice()).thenReturn(foundAccountTransfers);

        final Set<CreditorReference> transferReferences =
                foundAccountTransfers.stream().map(AccountTransfer::getCreditorReference).collect(toSet());

        when(invoiceRepository.findByCreditorReferences(eq(transferReferences))).thenReturn(matchingInvoices);

        final Map<Invoice, List<InvoicePaymentLine>> expectedPaymentLineGrouping =
                matchingInvoices.stream().collect(mappingTo(invoice -> {

                    return foundAccountTransfers
                            .stream()
                            .filter(transfer -> transfer.getCreditorReference().equals(invoice.getCreditorReference()))
                            .map(transfer -> new InvoicePaymentLine(invoice, transfer))
                            .collect(toList());
                }));

        when(paymentLineRepository.findAndGroupByInvoices(eq(matchingInvoices)))
                .thenReturn(expectedPaymentLineGrouping);

        // Then call method under test.
        service.createInvoicePaymentLinesFromAccountTransfers();

        // And finally do verifications.

        verify(transferRepository, times(1)).findAccountTransfersNotAssociatedWithInvoice();

        verifyResultingInvoicePaymentLines(
                expectedPaymentLineGrouping.values().stream().flatMap(List::stream).collect(toList()));

        if (!foundAccountTransfers.isEmpty()) {
            verify(invoiceRepository, times(1)).findByCreditorReferences(eq(transferReferences));

            if (!matchingInvoices.isEmpty()) {
                verify(paymentLineRepository, times(1)).findAndGroupByInvoices(argThat(actualInvoiceParamList -> {
                    return new HashSet<>(actualInvoiceParamList).equals(new HashSet<>(matchingInvoices));
                }));
            }
        }

        verifyNoMoreInteractions(transferRepository, paymentLineRepository, invoiceRepository);
    }

    private void verifyResultingInvoicePaymentLines(final List<InvoicePaymentLine> expectedResultingPaymentLines) {
        final int expectedPaymentLineCount = expectedResultingPaymentLines.size();

        final ArgumentCaptor<InvoicePaymentLine> paymentLineCaptor = ArgumentCaptor.forClass(InvoicePaymentLine.class);
        verify(paymentLineRepository, times(expectedPaymentLineCount)).save(paymentLineCaptor.capture());

        final List<InvoicePaymentLine> actualResultedPaymentLines = paymentLineCaptor.getAllValues();

        assertThat(actualResultedPaymentLines, containsInAnyOrder(expectedResultingPaymentLines
                .stream()
                .map(InvoicePaymentLineMatcher::isEqualAnnouncement)
                .collect(toList())));
    }

    private static void assertPaymentState(final Invoice invoice,
                                           final InvoiceState expectedState,
                                           final LocalDate expectedPaymentDate,
                                           final BigDecimal expectedReceivedAmount) {

        assertEquals(expectedState, invoice.getState());
        assertEquals(expectedPaymentDate, invoice.getPaymentDate());
        bigDecimalEquals(expectedReceivedAmount, invoice.getReceivedAmount());
    }
}
