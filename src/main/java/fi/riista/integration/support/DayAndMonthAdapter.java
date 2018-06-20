package fi.riista.integration.support;

import com.google.common.base.Strings;
import fi.riista.util.DateUtil;
import fi.riista.util.Patterns;
import org.joda.time.LocalDate;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DayAndMonthAdapter extends XmlAdapter<String, LocalDate> {

    private static final Pattern DAY_MONTH_PATTERN = Pattern.compile(Patterns.DATE_DDMM);

    @Override
    public String marshal(final LocalDate date) {
        return date != null ? String.format("%d.%d.", date.getDayOfMonth(), date.getMonthOfYear()) : null;
    }

    @Override
    public LocalDate unmarshal(@Nullable final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        final Matcher m = DAY_MONTH_PATTERN.matcher(value);

        if (m.matches()) {
            try {

                final int dayOfMonth = Integer.parseInt(m.group(1));
                final int monthOfYear = Integer.parseInt(m.group(2));

                return new LocalDate(inferCalendarYear(monthOfYear), monthOfYear, dayOfMonth);

            } catch (final NumberFormatException nfe) {
                // Fall-through to throwing exception.
            }
        }

        throw new IllegalArgumentException("Could not parse day and month from string: '" + value + "'");
    }

    private static int inferCalendarYear(final int monthOfYear) {
        final int huntingYear = DateUtil.huntingYear();
        return monthOfYear < DateUtil.HUNTING_YEAR_BEGIN_MONTH ? huntingYear + 1 : huntingYear;
    }
}
