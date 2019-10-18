package fi.riista.integration.mmm.statement;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.error.NotFoundException;
import fi.riista.integration.mmm.transfer.AccountTransfer;
import fi.riista.integration.mmm.transfer.AccountTransferBatchRepository;
import fi.riista.integration.mmm.transfer.AccountTransferRepository;
import fi.riista.integration.mmm.transfer.AccountTransfer_;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.BigDecimalComparison;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE;
import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE_2;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.DateUtil.today;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AccountStatementImportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private AccountStatementImportFeature feature;

    @Resource
    private AccountTransferRepository transferRepo;

    @Resource
    private AccountTransferBatchRepository batchRepo;

    @Test
    public void testImportAccountTransfers_smokeTest() {
        final LocalDate date = ld(2018, 1, 8);

        final AccountStatementLine firstInput = newAccountStatementLine(LINE_SAMPLE);
        final AccountStatementLine secondInput = newAccountStatementLine(LINE_SAMPLE_2);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.importAccountTransfers(newAccountStatement(date, firstInput, secondInput));
        });

        runInTransaction(() -> {
            batchRepo.findByStatementDate(date).orElseThrow(() -> {
                return new NotFoundException(format("Account transfer batch for date %s not found", date));
            });

            assertEquals(1, batchRepo.findAll().size());

            final List<AccountTransfer> transfers = transferRepo.findAll(new JpaSort(AccountTransfer_.id));

            assertEquals(2, transfers.size());

            assertTransferResult(firstInput, transfers.get(0));
            assertTransferResult(secondInput, transfers.get(1));
        });
    }

    @Test(expected = AccountStatementImportException.class)
    public void testImportAccountTransfers_duplicateDateFails() {
        final LocalDate date = ld(2018, 1, 8);

        final AccountStatement firstInput = newAccountStatement(date,
                newAccountStatementLine(LINE_SAMPLE), newAccountStatementLine(LINE_SAMPLE_2));

        final AccountStatement secondInput = newAccountStatement(date);

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.importAccountTransfers(firstInput);
            feature.importAccountTransfers(secondInput);
        });
    }

    @Test
    public void testImportAccountTransfers_mustFailOnInvalidData() {
        final LocalDate date = today();

        final AccountStatementLine line = newAccountStatementLine(LINE_SAMPLE);
        line.setCreditorReference("1234");

        onSavedAndAuthenticated(createNewAdmin(), () -> {

            try {
                feature.importAccountTransfers(newAccountStatement(date, line));
                fail("Should have failed because of invalid creditor reference");
            } catch (final AccountStatementImportException e) {
                assertEmpty(batchRepo.findAll());
                assertEmpty(transferRepo.findAll());
            }
        });
    }

    private static AccountStatement newAccountStatement(final LocalDate date, final AccountStatementLine... lines) {
        return new AccountStatement(date, asList(lines));
    }

    private static AccountStatementLine newAccountStatementLine(final String str) {
        return AccountStatementParser.parseLine(str, Optional.empty());
    }

    private static void assertTransferResult(final AccountStatementLine input, final AccountTransfer output) {
        assertEquals(input.getCreditorAccountNumberAsIban(), output.getCreditorIban());
        assertEquals(input.getTransactionDate(), output.getTransactionDate());
        assertEquals(input.getBookingDate(), output.getBookingDate());
        assertEquals(input.getDebtorNameAbbrv(), output.getDebtorName());
        assertTrue(BigDecimalComparison.nullsafeEq(input.getAmount(), output.getAmount()));
        assertEquals(CreditorReference.fromNullable(input.getCreditorReference()), output.getCreditorReference());
        assertEquals(input.getAccountServiceReference(), output.getAccountServiceReference());
    }
}
