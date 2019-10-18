package fi.riista.feature.shootingtest.official;

import javax.validation.constraints.AssertTrue;
import java.util.List;

public class ShootingTestOfficialsDTO {

    private Long calendarEventId;
    private Long shootingTestEventId;
    private List<Long> occupationIds;
    private Long responsibleOccupationId;

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

    public List<Long> getOccupationIds() {
        return occupationIds;
    }

    public void setOccupationIds(final List<Long> occupationIds) {
        this.occupationIds = occupationIds;
    }

    public Long getResponsibleOccupationId() {
        return responsibleOccupationId;
    }

    public void setResponsibleOccupationId(final Long responsibleOccupationId) {
        this.responsibleOccupationId = responsibleOccupationId;
    }
}
