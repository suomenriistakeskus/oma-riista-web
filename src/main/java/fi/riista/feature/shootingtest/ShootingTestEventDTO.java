package fi.riista.feature.shootingtest;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.joda.time.LocalDateTime;

public class ShootingTestEventDTO extends BaseEntityDTO<Long> {

    public static ShootingTestEventDTO create(final ShootingTestEvent entity) {
        final ShootingTestEventDTO dto = new ShootingTestEventDTO();
        DtoUtil.copyBaseFields(entity, dto);

        final CalendarEvent calendarEvent = entity.getCalendarEvent();
        dto.setCalendarEventId(calendarEvent.getId());
        dto.setLockedTime(DateUtil.toLocalDateTimeNullSafe(entity.getLockedTime()));
        return dto;
    }

    private Long id;
    private Integer rev;

    private long calendarEventId;
    private LocalDateTime lockedTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public long getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(final long calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public LocalDateTime getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(final LocalDateTime lockedTime) {
        this.lockedTime = lockedTime;
    }
}
