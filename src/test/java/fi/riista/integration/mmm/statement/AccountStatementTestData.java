package fi.riista.integration.mmm.statement;

import fi.riista.util.fixedformat.LocalDateFormatter;
import org.joda.time.LocalDate;

import java.util.List;

public final class AccountStatementTestData {

    public static final String LINE_SAMPLE =
            "350000121502875180108180107010759373147150800000217276028940015RANERAKSAILI1 00008110000 0";

    public static final String LINE_SAMPLE_2 =
            "35000012150287518010818010801063589NGN1000400000016540019600013ABCDEFGHIJKL1 00000290000 0";

    public static String generateFileContent(final LocalDate statementDate, final List<String> lines) {
        // Append first line.
        final StringBuilder buf = new StringBuilder("0");
        buf.append(LocalDateFormatter.formatDate(statementDate));
        buf.append("...\n");

        lines.forEach(line -> buf.append(line).append('\n'));

        // Append last line.
        return buf.append("9...\n").toString();
    }

    private AccountStatementTestData() {
        throw new AssertionError();
    }
}
