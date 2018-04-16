package fi.riista.feature.pub.calendar;

import java.util.List;

public class PublicCalendarEventSearchResultDTO {
    public static final PublicCalendarEventSearchResultDTO TOO_MANY_RESULTS =
            new PublicCalendarEventSearchResultDTO(true, null);

    private final boolean tooManyResults;
    private final List<PublicCalendarEventDTO> events;

    public PublicCalendarEventSearchResultDTO(final List<PublicCalendarEventDTO> events) {
        this(false, events);
    }

    private PublicCalendarEventSearchResultDTO(final boolean tooManyResults,
                                               final List<PublicCalendarEventDTO> events) {
        this.tooManyResults = tooManyResults;
        this.events = events;
    }

    public boolean isTooManyResults() {
        return tooManyResults;
    }

    public List<PublicCalendarEventDTO> getEvents() {
        return events;
    }
}
