package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class HuntingControlEventChange extends BaseEntity<Long> {

    public static final String ID_COLUMN_NAME = "hunting_control_event_change_id";

    // Factories

    public static HuntingControlEventChange create(@Nonnull final HuntingControlEvent huntingControlEvent,
                                                   @Nonnull final DateTime pointOfTime,
                                                   final long userId,
                                                   @Nonnull final ChangeHistory.ChangeType changeType,
                                                   final String reasonForChange) {

        requireNonNull(huntingControlEvent);
        requireNonNull(pointOfTime);
        requireNonNull(changeType);

        final ChangeHistory changeHistory = new ChangeHistory(
                pointOfTime,
                userId,
                changeType,
                reasonForChange
        );
        final HuntingControlEventChange huntingControlEventChange = new HuntingControlEventChange();
        huntingControlEventChange.setHuntingControlEvent(huntingControlEvent);
        huntingControlEventChange.setChangeHistory(changeHistory);

        return huntingControlEventChange;
    }

    // Attributes

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingControlEvent huntingControlEvent;

    @Valid
    @Embedded
    private ChangeHistory changeHistory;

    // Constructors

    public HuntingControlEventChange() {
    }

    // Methods

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HuntingControlEvent getHuntingControlEvent() {
        return huntingControlEvent;
    }

    public void setHuntingControlEvent(final HuntingControlEvent huntingControlEvent) {
        CriteriaUtils.updateInverseCollection(HuntingControlEvent_.changeHistory, this, this.huntingControlEvent, huntingControlEvent);
        this.huntingControlEvent = huntingControlEvent;
    }

    public ChangeHistory getChangeHistory() {
        return changeHistory;
    }

    public void setChangeHistory(final ChangeHistory changeHistory) {
        this.changeHistory = changeHistory;
    }
}
