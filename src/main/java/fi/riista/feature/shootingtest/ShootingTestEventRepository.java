package fi.riista.feature.shootingtest;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.calendar.CalendarEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShootingTestEventRepository
        extends BaseRepository<ShootingTestEvent, Long>, ShootingTestEventRepositoryCustom {

    Optional<ShootingTestEvent> findByCalendarEvent(CalendarEvent calendarEvent);

    List<ShootingTestEvent> findByCalendarEventIn(Collection<CalendarEvent> calendarEvents);

}
