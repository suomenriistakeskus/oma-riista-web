package fi.riista.integration.fivaldi.formatter;

import com.ancientprogramming.fixedformat4j.format.AbstractFixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;
import com.google.common.base.Strings;
import fi.riista.util.fixedformat.FixedFormatHelper;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FivaldiCurrencySumFormatter extends AbstractFixedFormatter<BigDecimal> {

    public static final int TOTAL_LENGTH = 19;

    // Latter group is optional because trailing space may have been trimmed out before conversion.
    private static final Pattern PATTERN = Pattern.compile("(\\d{18})([ -]?)");

    @Override
    public BigDecimal asObject(final String input, final FormatInstructions instructions) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        final Matcher matcher = PATTERN.matcher(input);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid currency sum: " + FixedFormatHelper.asString(input));
        }

        final BigDecimal number = new BigDecimal(matcher.group(1)).movePointLeft(2);
        final boolean negative = "-".equals(matcher.group(2));

        return negative ? number.negate() : number;
    }

    @Override
    public String asString(final BigDecimal number, final FormatInstructions instructions) {
        if (number == null) {
            return Strings.repeat(" ", TOTAL_LENGTH);
        }

        final boolean negative = number.compareTo(BigDecimal.ZERO) < 0;

        return String.format("%018d%c", number.abs().movePointRight(2).longValue(), negative ? '-' : ' ');
    }
}
