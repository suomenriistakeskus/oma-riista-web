package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class HunterExamTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsManuallyEditableFields<HunterExamTrainingStatistics>,
        Serializable {

    public static final HunterExamTrainingStatistics reduce(@Nullable final HunterExamTrainingStatistics a,
                                                            @Nullable final HunterExamTrainingStatistics b) {

        final HunterExamTrainingStatistics result = new HunterExamTrainingStatistics();
        result.hunterExamTrainingEvents = nullableIntSum(a, b, s -> s.getHunterExamTrainingEvents());
        result.nonSubsidizableHunterExamTrainingEvents = nullableIntSum(a, b, s -> s.getNonSubsidizableHunterExamTrainingEvents());
        result.hunterExamTrainingParticipants = nullableIntSum(a, b, s -> s.getHunterExamTrainingParticipants());
        result.nonSubsidizableHunterExamTrainingParticipants = nullableIntSum(a, b, s -> s.getNonSubsidizableHunterExamTrainingParticipants());
        result.lastModified = nullsafeMax(a, b, s -> s.getLastModified());
        return result;
    }

    public static HunterExamTrainingStatistics reduce(@Nonnull final Stream<HunterExamTrainingStatistics> items) {
        requireNonNull(items);
        return items.reduce(new HunterExamTrainingStatistics(), HunterExamTrainingStatistics::reduce);
    }

    public static <T> HunterExamTrainingStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                          @Nonnull final Function<? super T, HunterExamTrainingStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Metsästäjätutkintoon valmistavat koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "hunter_exam_training_events")
    private Integer hunterExamTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_hunter_exam_training_events")
    private Integer nonSubsidizableHunterExamTrainingEvents;

    @Column(name = "non_subsidizable_hunter_exam_training_events_overridden", nullable = false)
    private boolean nonSubsidizableHunterExamTrainingEventsOverridden;

    // Updated when moderator overrides automatically computed value.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "hunter_exam_training_events_last_overridden")
    private DateTime hunterExamTrainingEventsLastOverridden;

    // Metsästäjätutkintoon valmistavien koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "hunter_exam_training_participants")
    private Integer hunterExamTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_hunter_exam_training_participants")
    private Integer nonSubsidizableHunterExamTrainingParticipants;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "hunter_exam_training_last_modified")
    private DateTime lastModified;

    @Column(name = "hunter_exam_training_participants_overridden", nullable = false)
    private boolean hunterExamTrainingParticipantsOverridden;

    public HunterExamTrainingStatistics() {
        hunterExamTrainingParticipantsOverridden = false;
    }

    public HunterExamTrainingStatistics makeCopy() {
        final HunterExamTrainingStatistics copy = new HunterExamTrainingStatistics();
        copy.hunterExamTrainingEvents = this.hunterExamTrainingEvents;
        copy.nonSubsidizableHunterExamTrainingEvents = this.nonSubsidizableHunterExamTrainingEvents;
        copy.nonSubsidizableHunterExamTrainingEventsOverridden = this.nonSubsidizableHunterExamTrainingEventsOverridden;
        copy.hunterExamTrainingEventsLastOverridden = this.hunterExamTrainingEventsLastOverridden;
        copy.hunterExamTrainingParticipants = this.hunterExamTrainingParticipants;
        copy.nonSubsidizableHunterExamTrainingParticipants = this.nonSubsidizableHunterExamTrainingParticipants;
        copy.lastModified = this.lastModified;
        copy.hunterExamTrainingParticipantsOverridden = this.hunterExamTrainingParticipantsOverridden;
        return copy;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.HUNTER_EXAM_TRAINING;
    }

    @Override
    public boolean isEqualTo(@Nonnull final HunterExamTrainingStatistics that) {
        // Includes only fields manually updateable by coordinator.
        return Objects.equals(this.nonSubsidizableHunterExamTrainingEvents, that.nonSubsidizableHunterExamTrainingEvents) &&
                Objects.equals(hunterExamTrainingParticipants, that.hunterExamTrainingParticipants) &&
                Objects.equals(nonSubsidizableHunterExamTrainingParticipants, that.nonSubsidizableHunterExamTrainingParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final HunterExamTrainingStatistics that) {
        // Includes only fields manually updateable by coordinator.
        if (!Objects.equals(this.nonSubsidizableHunterExamTrainingEvents, that.nonSubsidizableHunterExamTrainingEvents)) {
            this.nonSubsidizableHunterExamTrainingEventsOverridden = true;
        }
        this.nonSubsidizableHunterExamTrainingEvents = that.nonSubsidizableHunterExamTrainingEvents;

        if (!Objects.equals(this.hunterExamTrainingParticipants, that.hunterExamTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableHunterExamTrainingParticipants, that.nonSubsidizableHunterExamTrainingParticipants)) {
            this.hunterExamTrainingParticipantsOverridden = true;
        }
        this.hunterExamTrainingParticipants = that.hunterExamTrainingParticipants;
        this.nonSubsidizableHunterExamTrainingParticipants = that.nonSubsidizableHunterExamTrainingParticipants;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(hunterExamTrainingEvents, hunterExamTrainingParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    public boolean isHunterExamTrainingEventsManuallyOverridden() {
        return this.hunterExamTrainingEventsLastOverridden != null;
    }

    public void setHunterExamTrainingEventsOverridden(@Nonnull final Integer moderatorOverriddenEvents) {
        this.hunterExamTrainingEvents = requireNonNull(moderatorOverriddenEvents);

        final DateTime now = DateUtil.now();
        this.hunterExamTrainingEventsLastOverridden = now;
        this.lastModified = now;
    }

    // Accessors -->

    public Integer getHunterExamTrainingEvents() {
        return hunterExamTrainingEvents;
    }

    public void setHunterExamTrainingEvents(final Integer hunterExamTrainingEvents) {
        this.hunterExamTrainingEvents = hunterExamTrainingEvents;
    }

    public Integer getNonSubsidizableHunterExamTrainingEvents() {
        return nonSubsidizableHunterExamTrainingEvents;
    }

    public void setNonSubsidizableHunterExamTrainingEvents(final Integer nonSubsidizableHunterExamTrainingEvents) {
        this.nonSubsidizableHunterExamTrainingEvents = nonSubsidizableHunterExamTrainingEvents;
    }

    public boolean isNonSubsidizableHunterExamTrainingEventsOverridden() {
        return nonSubsidizableHunterExamTrainingEventsOverridden;
    }

    public void setNonSubsidizableHunterExamTrainingEventsOverridden(final boolean nonSubsidizableHunterExamTrainingEventsOverridden) {
        this.nonSubsidizableHunterExamTrainingEventsOverridden = nonSubsidizableHunterExamTrainingEventsOverridden;
    }

    public DateTime getHunterExamTrainingEventsLastOverridden() {
        return hunterExamTrainingEventsLastOverridden;
    }

    public Integer getHunterExamTrainingParticipants() {
        return hunterExamTrainingParticipants;
    }

    public void setHunterExamTrainingParticipants(final Integer hunterExamTrainingParticipants) {
        this.hunterExamTrainingParticipants = hunterExamTrainingParticipants;
    }

    public Integer getNonSubsidizableHunterExamTrainingParticipants() {
        return nonSubsidizableHunterExamTrainingParticipants;
    }

    public void setNonSubsidizableHunterExamTrainingParticipants(final Integer nonSubsidizableHunterExamTrainingParticipants) {
        this.nonSubsidizableHunterExamTrainingParticipants = nonSubsidizableHunterExamTrainingParticipants;
    }

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isHunterExamTraininingParticipantsOverridden() {
        return hunterExamTrainingParticipantsOverridden;
    }

    public void setHunterExamTrainingParticipantsOverridden(boolean hunterExamTrainingParticipantsOverridden) {
        this.hunterExamTrainingParticipantsOverridden = hunterExamTrainingParticipantsOverridden;
    }
}
