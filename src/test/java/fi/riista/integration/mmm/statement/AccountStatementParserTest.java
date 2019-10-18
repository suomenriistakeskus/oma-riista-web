package fi.riista.integration.mmm.statement;

import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE;
import static fi.riista.integration.mmm.statement.AccountStatementTestData.LINE_SAMPLE_2;
import static fi.riista.integration.mmm.statement.MMMConstants.MMM_ACCOUNT_NUMBER;
import static fi.riista.test.TestUtils.ld;
import static fi.riista.util.fixedformat.FixedFormatHelper.scaleMonetaryAmount;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class AccountStatementParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testParseLine() {
        final AccountStatementLine line = doParseLine(LINE_SAMPLE);

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
        final AccountStatementLine line = doParseLine(LINE_SAMPLE_2);

        assertCommonsParts(line);
        assertEquals(ld(2018, 1, 8), line.getBookingDate());
        assertEquals(ld(2018, 1, 8), line.getTransactionDate());
        assertEquals("01063589NGN10004", line.getAccountServiceReference());
        assertEquals("00000016540019600013", line.getCreditorReference());
        assertEquals("ABCDEFGHIJKL", line.getDebtorNameAbbrv());
        assertEquals(scaleMonetaryAmount(new BigDecimal(290)), line.getAmount());
    }

    private static void assertCommonsParts(final AccountStatementLine line) {
        assertEquals("3", line.getTransactionType());
        assertEquals(MMM_ACCOUNT_NUMBER, line.getCreditorAccountNumber());
        assertEquals(Integer.valueOf(1), line.getCurrencyCode());
        assertEquals("", line.getNameOrigin());
        assertEquals(Integer.valueOf(0), line.getReversalIndicator());
        assertEquals("", line.getMediationType());
        assertEquals(Integer.valueOf(0), line.getReturnCode());
    }

    private static AccountStatementLine doParseLine(final String line) {
        return AccountStatementParser.parseLine(line, Optional.empty());
    }

    @Test
    public void testParseFile() {
        final LocalDate date = ld(2018, 1, 8);
        final String content = AccountStatementTestData.generateFileContent(date, asList(LINE_SAMPLE, LINE_SAMPLE_2));
        final AccountStatement result = AccountStatementParser.parseFile(content);

        assertEquals(date, result.getStatementDate());

        final List<AccountStatementLine> expectedLines = asList(doParseLine(LINE_SAMPLE), doParseLine(LINE_SAMPLE_2));

        assertEquals(expectedLines, result.getLines());
    }

    @Test
    public void testParseFile_whenEndingWithTwoUnparseableLines() {
        thrown.expect(AccountStatementParseException.class);
        thrown.expectMessage(AccountStatementParseException.invalidContent(4, "9abc").getMessage());

        final String content = "0180101...\n"
                + LINE_SAMPLE + "\n"
                + LINE_SAMPLE_2 + "\n"
                + "9abc\n"
                + "9xyz\n";

        AccountStatementParser.parseFile(content);
    }

    @Test
    public void testParseFile_whenUnparseableLineInBetween() {
        thrown.expect(AccountStatementParseException.class);
        thrown.expectMessage(AccountStatementParseException.invalidContent(3, "foobar").getMessage());

        final String content = "0180101...\n"
                + LINE_SAMPLE + "\n"
                + "foobar\n"
                + LINE_SAMPLE_2 + "\n"
                + "9...\n";

        AccountStatementParser.parseFile(content);
    }
}
