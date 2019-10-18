package fi.riista.feature.permit;

import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class PermitNumberUtil {
    private static final Pattern REGEX = Pattern.compile("([1-9]\\d{3})-([1-5])-(\\d{3})-(\\d{5})-(\\d)");

    public static int extractYear(final @Nonnull String permitNumber) {
        return Integer.parseInt(parseOrFail(permitNumber).group(1));
    }

    public static int extractOrderNumber(final @Nonnull String permitNumber) {
        final Matcher matcher = parseOrFail(permitNumber);
        return Integer.parseInt(matcher.group(3) + matcher.group(4));
    }

    private static Matcher parseOrFail(final @Nonnull String permitNumber) {
        final Matcher matcher = REGEX.matcher(requireNonNull(permitNumber));

        if (matcher.matches()) {
            return matcher;
        }

        throw new IllegalArgumentException("Invalid permit number: " + permitNumber);
    }

    public static String createPermitNumber(final int year, final int validYears, final int orderNumber) {
        final String appNoStr = StringUtils.leftPad(Integer.toString(orderNumber), 8, '0');
        final String permitNumber = String.format("%d-%d-%s-%s",
                year, validYears, appNoStr.substring(0, 3), appNoStr.substring(3));
        return permitNumber + '-' + FinnishHuntingPermitNumberValidator.calculateChecksum(permitNumber);
    }

    private PermitNumberUtil() {
        throw new AssertionError();
    }
}
