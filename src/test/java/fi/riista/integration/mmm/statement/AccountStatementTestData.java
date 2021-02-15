package fi.riista.integration.mmm.statement;

import fi.riista.util.fixedformat.LocalDateFormatter;
import org.joda.time.LocalDate;

import java.util.List;

public final class AccountStatementTestData {

    public static final String LINE_SAMPLE_DANSKE =
            "381299710011453180108180106121581A22964290500000020000157570010URUTAUKIJUUE1 00000120000 0";

    public static final String LINE_SAMPLE_OP =
            "350000121502875180108180107010759373147150800000217276028940015RANERAKSAILI1 00008110000 0";

    public static final String LINE_SAMPLE_OP_2 =
            "35000012150287518010818010801063589NGN1000400000016540019600013ABCDEFGHIJKL1 00000290000 0";

    // Each outer list represents a bank group. Each inner list represents an account transfer line.
    public static String generateFileContent(final LocalDate statementDate, final List<List<String>> bankGroups) {
        final StringBuilder buf = new StringBuilder();

        bankGroups.forEach(accountTransferLines -> {
            // Append header line.
            buf.append("0")
                    .append(LocalDateFormatter.formatDate(statementDate))
                    .append("...\n");

            accountTransferLines.forEach(line -> buf.append(line).append('\n'));

            // Append summary/assembly line.
            buf.append("9...\n").toString();
        });

        return buf.toString();
    }

    private AccountStatementTestData() {
        throw new AssertionError();
    }
}
