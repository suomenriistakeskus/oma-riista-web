package fi.riista.util.fixedformat;

import com.ancientprogramming.fixedformat4j.format.AbstractFixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class LocalDateFormatter extends AbstractFixedFormatter<LocalDate> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyMMdd");

    public static LocalDate parseDate(final String input) {
        return DATE_FORMAT.parseLocalDate(input);
    }

    public static String formatDate(final LocalDate date) {
        return date == null ? null : DATE_FORMAT.print(date);
    }

    @Override
    public LocalDate asObject(final String input, final FormatInstructions instructions) {
        return StringUtils.isBlank(input) ? null : parseDate(input);
    }

    @Override
    public String asString(final LocalDate date, final FormatInstructions instructions) {
        return formatDate(date);
    }
}
