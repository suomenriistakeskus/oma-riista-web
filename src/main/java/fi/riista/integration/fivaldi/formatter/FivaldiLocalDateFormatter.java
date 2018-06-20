package fi.riista.integration.fivaldi.formatter;

import com.ancientprogramming.fixedformat4j.format.AbstractFixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class FivaldiLocalDateFormatter extends AbstractFixedFormatter<LocalDate> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyMMdd");

    @Override
    public LocalDate asObject(final String input, final FormatInstructions instructions) {
        return StringUtils.isBlank(input) ? null : DATE_FORMAT.parseLocalDate(input);
    }

    @Override
    public String asString(final LocalDate date, final FormatInstructions instructions) {
        return date == null ? null : DATE_FORMAT.print(date);
    }
}
