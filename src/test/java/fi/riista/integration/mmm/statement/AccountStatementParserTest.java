package fi.riista.integration.mmm.statement;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE_DANSKE;
import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE_OP;
import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE_OP_2;
import static fi.riista.integration.mmm.statement.MMMConstants.VALID_ACCOUNT_NUMBERS;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.fixedformat.FixedFormatHelper.scaleMonetaryAmount;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountStatementParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testParseLine() {
        final AccountStatementLine line = doParseLine(LINE_SAMPLE_OP);

        assertCommonsParts(line);
        assertEquals(ld(2018, 1, 8), line.getBookingDate());
        assertEquals(ld(2018, 1, 7), line.getTransactionDate());
        assertEquals("0107593731471508", line.getAccountServiceReference());
        assertEquals("00000217276028940015", line.getCreditorReference());
        assertEquals("RANERAKSAILI", line.getDebtorNameAbbrv());
        assertEquals(scaleMonetaryAmount(new BigDecimal(8110)), line.getAmount());
    }

    @Test
    public void testParseLine2() {
        final AccountStatementLine line = doParseLine(LINE_SAMPLE_OP_2);

        assertCommonsParts(line);
        assertEquals(ld(2018, 1, 8), line.getBookingDate());
        assertEquals(ld(2018, 1, 8), line.getTransactionDate());
        assertEquals("01063589NGN10004", line.getAccountServiceReference());
        assertEquals("00000016540019600013", line.getCreditorReference());
        assertEquals("ABCDEFGHIJKL", line.getDebtorNameAbbrv());
        assertEquals(scaleMonetaryAmount(new BigDecimal(290)), line.getAmount());
    }

    @Test
    public void testParseLine3() {
        final AccountStatementLine line = doParseLine(LINE_SAMPLE_DANSKE);

        assertCommonsParts(line);
        assertEquals(ld(2018, 1, 8), line.getBookingDate());
        assertEquals(ld(2018, 1, 6), line.getTransactionDate());
        assertEquals("121581A229642905", line.getAccountServiceReference());
        assertEquals("00000020000157570010", line.getCreditorReference());
        assertEquals("URUTAUKIJUUE", line.getDebtorNameAbbrv());
        assertEquals(scaleMonetaryAmount(new BigDecimal(120)), line.getAmount());
    }

    private static void assertCommonsParts(final AccountStatementLine line) {
        assertEquals("3", line.getTransactionType());
        assertTrue(VALID_ACCOUNT_NUMBERS.contains(line.getCreditorAccountNumber()));
        assertEquals(Integer.valueOf(1), line.getCurrencyCode());
        assertEquals("", line.getNameOrigin());
        assertEquals(Integer.valueOf(0), line.getReversalIndicator());
        assertEquals("", line.getMediationType());
        assertEquals(Integer.valueOf(0), line.getReturnCode());
    }

    private static AccountStatementLine doParseLine(final String line) {
        return AccountStatementParser.parseAccountTransferLine(line, -1);
    }

    @Test
    public void testParseFile_withOneBankAccount() {
        final LocalDate date = ld(2018, 1, 8);
        final String content = AccountStatementTestData.generateFileContent(
                date, singletonList(asList(LINE_SAMPLE_OP, LINE_SAMPLE_OP_2)));

        final AccountStatement result = AccountStatementParser.parseFile(content);

        final List<AccountStatementLine> expectedLines =
                asList(doParseLine(LINE_SAMPLE_OP), doParseLine(LINE_SAMPLE_OP_2));

        assertEquals(expectedLines, result.getLines());
    }

    @Test
    public void testParseFile_withTwoBankAccounts() {
        final LocalDate date = ld(2018, 1, 8);
        final String content = AccountStatementTestData.generateFileContent(
                date, asList(singletonList(LINE_SAMPLE_DANSKE), singletonList(LINE_SAMPLE_OP)));

        final AccountStatement result = AccountStatementParser.parseFile(content);

        final List<AccountStatementLine> expectedLines =
                asList(doParseLine(LINE_SAMPLE_DANSKE), doParseLine(LINE_SAMPLE_OP));

        assertEquals(expectedLines, result.getLines());
    }

    @Test
    public void testParseFile_withUnparseableLine() {
        thrown.expect(AccountStatementParseException.class);
        thrown.expectMessage(AccountStatementParseException.invalidContent(3, "foobar").getMessage());

        final String content = "0180101...\n"
                + LINE_SAMPLE_OP + "\n"
                + "foobar\n";

        AccountStatementParser.parseFile(content);
    }
}
