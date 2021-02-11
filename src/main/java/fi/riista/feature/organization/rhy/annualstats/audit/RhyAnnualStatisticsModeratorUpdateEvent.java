package fi.riista.feature.organization.rhy.annualstats.audit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsManuallyEditableFields;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
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
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.BASIC_INFO;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.COMMUNICATION;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.GAME_DAMAGE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_EXAMS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_EXAM_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.YOUTH_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.HUNTING_CONTROL;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.JHT_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.LUKE;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.METSAHALLITUS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTER_TRAINING;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_HUNTING_RELATED;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.OTHER_PUBLIC_ADMIN_TASKS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.PUBLIC_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SHOOTING_RANGES;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup.SHOOTING_TESTS;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

@Entity
@Access(value = AccessType.FIELD)
public class RhyAnnualStatisticsModeratorUpdateEvent extends BaseEntity<Long> {

    private static final Set<AnnualStatisticGroup> EDITABLE_GROUPS = unmodifiableSet(EnumSet.of(
            BASIC_INFO, HUNTER_EXAMS, SHOOTING_TESTS, GAME_DAMAGE, HUNTING_CONTROL, OTHER_PUBLIC_ADMIN_TASKS,
            HUNTER_EXAM_TRAINING, JHT_TRAINING, HUNTER_TRAINING, YOUTH_TRAINING, OTHER_HUNTER_TRAINING, PUBLIC_EVENTS,
            OTHER_HUNTING_RELATED, COMMUNICATION, SHOOTING_RANGES, LUKE, METSAHALLITUS));

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RhyAnnualStatistics statistics;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnualStatisticGroup dataGroup;

    @NotNull
    @Column(nullable = false)
    private DateTime eventTime;

    @Column(nullable = false)
    private long userId;

    // For Hibernate
    RhyAnnualStatisticsModeratorUpdateEvent() {
    }

    public RhyAnnualStatisticsModeratorUpdateEvent(@Nonnull final RhyAnnualStatistics statistics,
                                                   @Nonnull final AnnualStatisticsManuallyEditableFields<?> fieldset,
                                                   @Nonnull final SystemUser user) {

        this(statistics, fieldset.getGroup(), fieldset.getLastModified(), user.getId().longValue());
    }

    public RhyAnnualStatisticsModeratorUpdateEvent(@Nonnull final RhyAnnualStatistics statistics,
                                                   @Nonnull final AnnualStatisticGroup dataGroup,
                                                   @Nonnull final DateTime eventTime,
                                                   final long userId) {

        checkArgument(isModeratorEditable(dataGroup), "dataGroup must be moderator-editable");

        this.statistics = requireNonNull(statistics, "statistics is null");
        this.dataGroup = requireNonNull(dataGroup, "dataGroup is null");
        this.eventTime = requireNonNull(eventTime, "eventTime is null");
        this.userId = userId;
    }

    private static boolean isModeratorEditable(final AnnualStatisticGroup dataGroup) {
        return EDITABLE_GROUPS.contains(dataGroup);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rhy_annual_statistics_moderator_update_event_id", nullable = false)
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

    public AnnualStatisticGroup getDataGroup() {
        return dataGroup;
    }

    public void setDataGroup(final AnnualStatisticGroup dataGroup) {
        this.dataGroup = dataGroup;
    }

    public DateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(final DateTime eventTime) {
        this.eventTime = eventTime;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }
}
