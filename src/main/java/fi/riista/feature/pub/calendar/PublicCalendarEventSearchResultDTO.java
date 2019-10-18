package fi.riista.feature.pub.calendar;

import java.util.List;

public class PublicCalendarEventSearchResultDTO {
    public static final PublicCalendarEventSearchResultDTO TOO_MANY_RESULTS =
            new PublicCalendarEventSearchResultDTO(true, true, null);

    private final boolean tooManyResults;
    private final boolean lastPage;
    private final List<PublicCalendarEventDTO> events;

    public PublicCalendarEventSearchResultDTO(final List<PublicCalendarEventDTO> events, final boolean lastPage) {
        this(false, lastPage, events);
    }

    private PublicCalendarEventSearchResultDTO(final boolean tooManyResults,
                                               final boolean lastPage,
                                               final List<PublicCalendarEventDTO> events) {
        this.tooManyResults = tooManyResults;
        this.lastPage = lastPage;
        this.events = events;
    }

    public boolean isTooManyResults() {
        return tooManyResults;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public List<PublicCalendarEventDTO> getEvents() {
        return events;
    }
}
