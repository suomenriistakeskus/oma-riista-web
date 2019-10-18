package fi.riista.util.fixedformat;

import com.ancientprogramming.fixedformat4j.format.AbstractFixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

// Custom Integer formatter pads nulls with spaces and non-null values with
// zeros.
public class IntegerFormatter extends AbstractFixedFormatter<Integer> {

    @Override
    public Integer asObject(final String input, final FormatInstructions instructions) {
        return StringUtils.isBlank(input) ? null : Integer.parseInt(input.trim());
    }

    @Override
    public String asString(final Integer number, final FormatInstructions instructions) {
        final int len = instructions.getLength();
        if (number == null) {
            return Strings.repeat(" ", len);
        }
        return String.format("%0" + len + "d", number);
    }
}
