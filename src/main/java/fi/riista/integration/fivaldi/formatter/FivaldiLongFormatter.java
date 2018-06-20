package fi.riista.integration.fivaldi.formatter;

import com.ancientprogramming.fixedformat4j.format.AbstractFixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

// Custom Long formatter pads nulls with spaces and non-null values with zeros.
public class FivaldiLongFormatter extends AbstractFixedFormatter<Long> {

    @Override
    public Long asObject(final String input, final FormatInstructions instructions) {
        return StringUtils.isBlank(input) ? null : Long.parseLong(input.trim());
    }

    @Override
    public String asString(final Long number, final FormatInstructions instructions) {
        final int len = instructions.getLength();
        if (number == null) {
            return Strings.repeat(" ", len);
        }
        return String.format("%0" + len + "d", number);
    }
}
