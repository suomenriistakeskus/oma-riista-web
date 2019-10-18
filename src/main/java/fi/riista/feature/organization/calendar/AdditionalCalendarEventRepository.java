package fi.riista.feature.organization.calendar;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AdditionalCalendarEventRepository extends BaseRepository<AdditionalCalendarEvent, Long> {
    List<AdditionalCalendarEvent> findByCalendarEvent(CalendarEvent event);

    List<AdditionalCalendarEvent> findByCalendarEventIn(Collection<CalendarEvent> events);

    @Modifying
    @Query("DELETE FROM AdditionalCalendarEvent e WHERE e.calendarEvent = ?1")
    void deleteByCalendarEvent(CalendarEvent event);
}
