package fi.riista.feature.shootingtest;

import javax.validation.constraints.AssertTrue;
import java.util.Set;

public class ShootingTestOfficialsDTO {

    private Long calendarEventId;
    private Long shootingTestEventId;
    private Set<Long> occupationIds;

    @AssertTrue
    public boolean isEitherCalendarEventIdOrShootingTestEventIdPresent() {
        return calendarEventId != null || shootingTestEventId != null;
    }

    public Long getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(final Long calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public Long getShootingTestEventId() {
        return shootingTestEventId;
    }

    public void setShootingTestEventId(final Long shootingTestEventId) {
        this.shootingTestEventId = shootingTestEventId;
    }

    public Set<Long> getOccupationIds() {
        return occupationIds;
    }

    public void setOccupationIds(final Set<Long> occupationIds) {
        this.occupationIds = occupationIds;
    }
}
