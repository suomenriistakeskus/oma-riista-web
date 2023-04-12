package fi.riista.feature.organization.calendar;

import fi.riista.feature.organization.Organisation;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

public interface CalendarEventRepositoryCustom {

    Map<CalendarEventType, Long> countEventTypes(Organisation organisation,
                                                 LocalDate beginDate,
                                                 LocalDate endDate);

    Map<CalendarEventType, Long> countSubsidisedEventTypes(Organisation organisation,
                                                 LocalDate beginDate,
                                                 LocalDate endDate);

    Map<CalendarEventType, Long> countNonSubsidisedEventTypes(Organisation organisation,
                                                 LocalDate beginDate,
                                                 LocalDate endDate);

    Map<CalendarEventType, Integer> countSubsidisedEventParticipants(Organisation organisation,
                                                           LocalDate beginDate,
                                                           LocalDate endDate);

    Map<CalendarEventType, Integer> countNonSubsidisedEventParticipants(Organisation organisation,
                                                           LocalDate beginDate,
                                                           LocalDate endDate);

    default Map<CalendarEventType, Long> countEventTypes(final Organisation organisation, final int calendarYear) {
        final LocalDate beginDate = new LocalDate(calendarYear, 1, 1);
        final LocalDate endDate = new LocalDate(calendarYear, 12, 31);

        return countEventTypes(organisation, beginDate, endDate);
    }

    List<CalendarEventSearchResultDTO> getCalendarEvents(CalendarEventSearchParamsDTO params);

    Tuple2<Integer, Integer> countAttemptResults(Organisation organisation, LocalDate beginDate, LocalDate endDate, CalendarEventType type);
}
