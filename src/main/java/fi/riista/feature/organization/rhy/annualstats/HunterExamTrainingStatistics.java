package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class HunterExamTrainingStatistics implements AnnualStatisticsFieldsetStatus, Serializable {

    public static final HunterExamTrainingStatistics reduce(@Nullable final HunterExamTrainingStatistics a,
                                                            @Nullable final HunterExamTrainingStatistics b) {

        final HunterExamTrainingStatistics result = new HunterExamTrainingStatistics();
        result.hunterExamTrainingEvents = nullsafeSumAsInt(a, b, s -> s.getHunterExamTrainingEvents());
        result.hunterExamTrainingParticipants = nullsafeSumAsInt(a, b, s -> s.getHunterExamTrainingParticipants());
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

    // Updated when moderator overrides automatically computed value.
    @Column(name = "hunter_exam_training_events_last_overridden")
    private DateTime hunterExamTrainingEventsLastOverridden;

    // Metsästäjätutkintoon valmistavien koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "hunter_exam_training_participants")
    private Integer hunterExamTrainingParticipants;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "hunter_exam_training_last_modified")
    private DateTime lastModified;

    public HunterExamTrainingStatistics() {
    }

    public HunterExamTrainingStatistics(@Nonnull final HunterExamTrainingStatistics that) {
        requireNonNull(that);

        this.hunterExamTrainingEvents = that.hunterExamTrainingEvents;
        this.hunterExamTrainingEventsLastOverridden = that.hunterExamTrainingEventsLastOverridden;
        this.hunterExamTrainingParticipants = that.hunterExamTrainingParticipants;
        this.lastModified = that.lastModified;
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

    public void setHunterExamEventsWithModeratorOverride(@Nonnull final Integer hunterExamTrainingEvents,
                                                         @Nonnull final DateTime overriddenAt) {

        this.hunterExamTrainingEvents = requireNonNull(hunterExamTrainingEvents, "hunterExamEvents is null");
        this.hunterExamTrainingEventsLastOverridden = requireNonNull(overriddenAt, "overriddenAt is null");
    }

    // Accessors -->

    public Integer getHunterExamTrainingEvents() {
        return hunterExamTrainingEvents;
    }

    public void setHunterExamTrainingEvents(final Integer hunterExamTrainingEvents) {
        this.hunterExamTrainingEvents = hunterExamTrainingEvents;
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

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
