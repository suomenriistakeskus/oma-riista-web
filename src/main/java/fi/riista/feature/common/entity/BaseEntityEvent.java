package fi.riista.feature.common.entity;

import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;

import static fi.riista.util.DateUtil.now;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class BaseEntityEvent extends BaseEntity<Long> {

    @NotNull
    @Column(nullable = false, updatable = false)
    private DateTime eventTime;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Long userId;

    protected BaseEntityEvent() {
    }

    public DateTime getEventTime() {
        return eventTime;
    }

    private void setEventTime(final DateTime eventTime) {
        this.eventTime = eventTime;
    }

    public Long getUserId() {
        return userId;
    }

    private void setUserId(final Long userId) {
        this.userId = userId;
    }

    @PrePersist
    protected void prePersist() {
        setEventTime(now());
        setUserId(getActiveUserId());
    }
}
