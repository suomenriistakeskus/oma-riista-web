package fi.riista.feature.organization.calendar;

import fi.riista.feature.organization.Organisation;
import org.joda.time.LocalDate;

import java.util.Map;

public interface CalendarEventRepositoryCustom {

    public Map<CalendarEventType, Long> countEventTypes(Organisation organisation,
                                                        LocalDate beginDate,
                                                        LocalDate endDate);

    default Map<CalendarEventType, Long> countEventTypes(final Organisation organisation, final int calendarYear) {
        final LocalDate beginDate = new LocalDate(calendarYear, 1, 1);
        final LocalDate endDate = new LocalDate(calendarYear, 12, 31);

        return countEventTypes(organisation, beginDate, endDate);
    }
}
