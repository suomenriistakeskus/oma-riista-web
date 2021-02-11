package fi.riista.feature.dashboard;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.organization.calendar.CalendarEventSearchParamsDTO;
import fi.riista.feature.organization.calendar.CalendarEventType;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.google.common.collect.Sets.immutableEnumSet;
import static java.util.Collections.singletonList;

public class EventSearchConditionDTO {

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rkaCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rhyCode;

    private int year;

    private CalendarEventType eventType;

    public CalendarEventSearchParamsDTO toCalendarEventSearchParams() {
        final Collection<String> rhyIds =
                Optional.ofNullable(this.rhyCode).map(rhyCode -> singletonList(rhyCode)).orElse(Collections.emptyList());
        final ImmutableSet<CalendarEventType> eventTypes =
                Optional.ofNullable(this.eventType).map(eventType -> immutableEnumSet(eventType)).orElse(ImmutableSet.of());
        final LocalDate beginTime = new LocalDate(this.year, 1, 1);
        final LocalDate endTime = new LocalDate(this.year, 12, 31);

        return new CalendarEventSearchParamsDTO(this.rkaCode,
                rhyIds,
                eventTypes,
                beginTime,
                endTime,
                false,
                false,
                null,
                0);
    }

    public String getRkaCode() {
        return rkaCode;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public int getYear() {
        return year;
    }

    public CalendarEventType getEventType() {
        return eventType;
    }
}
