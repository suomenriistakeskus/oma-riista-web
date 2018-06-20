package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class RhyAnnualStatisticsStateChangeEvent extends BaseEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RhyAnnualStatistics statistics;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RhyAnnualStatisticsState state;

    @NotNull
    @Column(nullable = false)
    private DateTime pointOfTime;

    @Column
    private Long userId;

    // For Hibernate
    RhyAnnualStatisticsStateChangeEvent() {
    }

    public RhyAnnualStatisticsStateChangeEvent(@Nonnull final RhyAnnualStatistics statistics,
                                               @Nonnull final RhyAnnualStatisticsState state,
                                               @Nonnull final SystemUser user) {

        this.statistics = requireNonNull(statistics, "statistics is null");
        this.state = requireNonNull(state, "state is null");
        this.userId = requireNonNull(user, "user is null").getId();
        this.pointOfTime = DateUtil.now();
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rhy_annual_statistics_state_change_event_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public RhyAnnualStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(final RhyAnnualStatistics statistics) {
        this.statistics = statistics;
    }

    public RhyAnnualStatisticsState getState() {
        return state;
    }

    public void setState(final RhyAnnualStatisticsState state) {
        this.state = state;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }
}
