package fi.riista.integration.mmm.statement.formatter;

import com.ancientprogramming.fixedformat4j.format.AbstractFixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;
import com.google.common.base.Strings;
import fi.riista.integration.mmm.statement.AccountStatementParseException;
import fi.riista.util.fixedformat.FixedFormatHelper;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

public class AccountStatementAmountFormatter extends AbstractFixedFormatter<BigDecimal> {

    public static final int TOTAL_LENGTH = 10;

    @Override
    public BigDecimal asObject(final String input, final FormatInstructions instructions) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        try {
            return new BigDecimal(input).movePointLeft(2);
        } catch (final NumberFormatException e) {
            throw new AccountStatementParseException(
                    "Invalid account statement amount: " + FixedFormatHelper.asString(input));
        }
    }

    @Override
    public String asString(final BigDecimal number, final FormatInstructions instructions) {
        if (number == null) {
            return Strings.repeat(" ", TOTAL_LENGTH);
        }

        if (number.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Amount must not be negative");
        }

        return String.format("%010d", number.movePointRight(2).longValue());
    }
}
