package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.F;
import fi.riista.util.NumberUtils;
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
public class HunterExamStatistics implements AnnualStatisticsFieldsetStatus, Serializable {

    public static final HunterExamStatistics reduce(@Nullable final HunterExamStatistics a,
                                                    @Nullable final HunterExamStatistics b) {

        final HunterExamStatistics result = new HunterExamStatistics();
        result.hunterExamEvents = nullsafeSumAsInt(a, b, HunterExamStatistics::getHunterExamEvents);
        result.passedHunterExams = nullsafeSumAsInt(a, b, HunterExamStatistics::getPassedHunterExams);
        result.failedHunterExams = nullsafeSumAsInt(a, b, HunterExamStatistics::getFailedHunterExams);
        result.hunterExamOfficials = nullsafeSumAsInt(a, b, HunterExamStatistics::getHunterExamOfficials);
        result.hunterExamEventsLastOverridden = nullsafeMax(a, b, HunterExamStatistics::getHunterExamEventsLastOverridden);
        result.lastModified = nullsafeMax(a, b, HunterExamStatistics::getLastModified);
        return result;
    }

    public static HunterExamStatistics reduce(@Nonnull final Stream<HunterExamStatistics> items) {
        requireNonNull(items);
        return items.reduce(new HunterExamStatistics(), HunterExamStatistics::reduce);
    }

    public static <T> HunterExamStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                  @Nonnull final Function<? super T, HunterExamStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Metsästäjätutkintotilaisuuksien määrä
    @Min(0)
    @Column(name = "hunter_exam_events")
    private Integer hunterExamEvents;

    // Updated when moderator overrides automatically computed value.
    @Column(name = "hunter_exam_events_last_overridden")
    private DateTime hunterExamEventsLastOverridden;

    // Hyväksyttyjen metsästäjätutkintoyritysten määrä
    @Min(0)
    @Column(name = "passed_hunter_exams")
    private Integer passedHunterExams;

    // Hylättyjen metsästäjätutkintoyritysten määrä
    @Min(0)
    @Column(name = "failed_hunter_exams")
    private Integer failedHunterExams;

    // Metsästäjätutkinnon vastaanottajien määrä
    @Min(0)
    @Column(name = "hunter_exam_officials")
    private Integer hunterExamOfficials;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "hunter_exams_last_modified")
    private DateTime lastModified;

    public HunterExamStatistics() {
    }

    public HunterExamStatistics(@Nonnull final HunterExamStatistics that) {
        requireNonNull(that);

        this.hunterExamEvents = that.hunterExamEvents;
        this.hunterExamEventsLastOverridden = that.hunterExamEventsLastOverridden;
        this.passedHunterExams = that.passedHunterExams;
        this.failedHunterExams = that.failedHunterExams;
        this.hunterExamOfficials = that.hunterExamOfficials;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(hunterExamEvents, passedHunterExams, failedHunterExams, hunterExamOfficials);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    public boolean isHunterExamEventsManuallyOverridden() {
        return this.hunterExamEventsLastOverridden != null;
    }

    public void setHunterExamEventsWithModeratorOverride(@Nonnull final Integer hunterExamEvents,
                                                         @Nonnull final DateTime overriddenAt) {

        this.hunterExamEvents = requireNonNull(hunterExamEvents, "hunterExamEvents is null");
        this.hunterExamEventsLastOverridden = requireNonNull(overriddenAt, "overriddenAt is null");
    }

    public int countAllAttempts() {
        return NumberUtils.getIntValueOrZero(passedHunterExams) + NumberUtils.getIntValueOrZero(failedHunterExams);
    }

    // Accessors -->

    public Integer getHunterExamEvents() {
        return hunterExamEvents;
    }

    public void setHunterExamEvents(final Integer hunterExamEvents) {
        this.hunterExamEvents = hunterExamEvents;
    }

    public DateTime getHunterExamEventsLastOverridden() {
        return hunterExamEventsLastOverridden;
    }

    public Integer getPassedHunterExams() {
        return passedHunterExams;
    }

    public void setPassedHunterExams(final Integer passedHunterExams) {
        this.passedHunterExams = passedHunterExams;
    }

    public Integer getFailedHunterExams() {
        return failedHunterExams;
    }

    public void setFailedHunterExams(final Integer failedHunterExams) {
        this.failedHunterExams = failedHunterExams;
    }

    public Integer getHunterExamOfficials() {
        return hunterExamOfficials;
    }

    public void setHunterExamOfficials(final Integer hunterExamOfficials) {
        this.hunterExamOfficials = hunterExamOfficials;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
