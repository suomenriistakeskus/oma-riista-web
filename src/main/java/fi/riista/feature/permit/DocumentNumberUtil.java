package fi.riista.feature.permit;

import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Utility class for forming and parsing official number for permits and decisions. The form of the number
 * is YYYY-V-NNN-NNNNN-P where Y is year, V is validity in years, N is order number and V is
 * parity check character. E.g. 2020-1-123-45678-1.
 */
public class DocumentNumberUtil {
    private static final Pattern REGEX = Pattern.compile("([1-9]\\d{3})-([1-5])-(\\d{3})-(\\d{5})-(\\d)");

    public static int extractYear(final @Nonnull String documentNumber) {
        return Integer.parseInt(parseOrFail(documentNumber).group(1));
    }

    public static int extractOrderNumber(final @Nonnull String documentNumber) {
        final Matcher matcher = parseOrFail(documentNumber);
        return Integer.parseInt(matcher.group(3) + matcher.group(4));
    }

    private static Matcher parseOrFail(final @Nonnull String documentNumber) {
        final Matcher matcher = REGEX.matcher(requireNonNull(documentNumber));

        if (matcher.matches()) {
            return matcher;
        }

        throw new IllegalArgumentException("Invalid permit number: " + documentNumber);
    }

    public static String createDocumentNumber(final int year, final int validYears, final int orderNumber) {
        final String appNoStr = StringUtils.leftPad(Integer.toString(orderNumber), 8, '0');
        final String permitNumber = String.format("%d-%d-%s-%s",
                year, validYears, appNoStr.substring(0, 3), appNoStr.substring(3));
        return permitNumber + '-' + FinnishHuntingPermitNumberValidator.calculateChecksum(permitNumber);
    }

    private DocumentNumberUtil() {
        throw new AssertionError();
    }
}
