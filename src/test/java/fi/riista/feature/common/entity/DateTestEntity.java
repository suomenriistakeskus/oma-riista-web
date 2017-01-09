package fi.riista.feature.common.entity;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Access(value = AccessType.FIELD)
public class DateTestEntity implements HasBeginAndEndDate, HasID<Long>, Persistable<Long> {

    private Long id;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    public DateTestEntity() {
    }

    public DateTestEntity(@Nullable final LocalDate beginDate, @Nullable final LocalDate endDate) {
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    @Override
    public boolean isNew() {
        return getId() == null;
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

}
