package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple2;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTER_EXAM_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.HUNTER_EXAM_STATISTICS;
import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class HunterExamStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsFieldsetParticipants,
        AnnualStatisticsManuallyEditableFields<HunterExamStatistics>,
        Serializable {

    public static HunterExamStatistics reduce(@Nullable final HunterExamStatistics a,
                                              @Nullable final HunterExamStatistics b) {

        final HunterExamStatistics result = new HunterExamStatistics();
        result.hunterExamEvents = nullableIntSum(a, b, HunterExamStatistics::getHunterExamEvents);
        result.passedHunterExams = nullableIntSum(a, b, HunterExamStatistics::getPassedHunterExams);
        result.failedHunterExams = nullableIntSum(a, b, HunterExamStatistics::getFailedHunterExams);
        result.hunterExamOfficials = nullableIntSum(a, b, HunterExamStatistics::getHunterExamOfficials);
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
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
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

    @Column(name = "hunter_exam_attempt_results_overridden", nullable = false)
    private boolean hunterExamAttemptResultsOverridden;

    // Metsästäjätutkinnon vastaanottajien määrä
    @Min(0)
    @Column(name = "hunter_exam_officials")
    private Integer hunterExamOfficials;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "hunter_exams_last_modified")
    private DateTime lastModified;

    public HunterExamStatistics() {
    }

    public HunterExamStatistics makeCopy() {
        final HunterExamStatistics copy = new HunterExamStatistics();
        copy.hunterExamEvents = this.hunterExamEvents;
        copy.hunterExamEventsLastOverridden = this.hunterExamEventsLastOverridden;
        copy.passedHunterExams = this.passedHunterExams;
        copy.failedHunterExams = this.failedHunterExams;
        copy.hunterExamAttemptResultsOverridden = this.hunterExamAttemptResultsOverridden;
        copy.hunterExamOfficials = this.hunterExamOfficials;
        copy.lastModified = this.lastModified;
        return copy;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.HUNTER_EXAMS;
    }

    @Override
    public boolean isEqualTo(@Nonnull final HunterExamStatistics that) {
        // Includes only fields manually updateable by coordinator.

        return Objects.equals(passedHunterExams, that.passedHunterExams) &&
                Objects.equals(failedHunterExams, that.failedHunterExams);
    }

    @Override
    public void assignFrom(@Nonnull final HunterExamStatistics that) {
        // Includes only fields manually updateable by coordinator.

        if (!Objects.equals(this.passedHunterExams, that.passedHunterExams) ||
                !Objects.equals(this.failedHunterExams, that.failedHunterExams)) {
            this.hunterExamAttemptResultsOverridden = true;
        }
        this.passedHunterExams = that.passedHunterExams;
        this.failedHunterExams = that.failedHunterExams;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(hunterExamEvents, passedHunterExams, failedHunterExams, hunterExamOfficials) && hasParticipants();
    }

    private boolean hasParticipants() {
        return listMissingParticipants()._2.isEmpty();
    }

    @Override
    public Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> listMissingParticipants() {
        final List<AnnualStatisticsParticipantField> missing = new ArrayList<>();
        if (hunterExamEvents != null && hunterExamEvents > 0 &&  passedHunterExams + failedHunterExams <= 0) {
            missing.add(HUNTER_EXAM_EVENTS);
        }
        return Tuple.of(HUNTER_EXAM_STATISTICS, missing);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    public boolean isHunterExamEventsManuallyOverridden() {
        return this.hunterExamEventsLastOverridden != null;
    }

    public void setHunterExamEventsOverridden(@Nonnull final Integer moderatorOverriddenEvents) {
        this.hunterExamEvents = requireNonNull(moderatorOverriddenEvents);

        final DateTime now = DateUtil.now();
        this.hunterExamEventsLastOverridden = now;
        this.lastModified = now;
    }

    @Nullable
    public Integer countAllAttempts() {
        return nullableIntSum(passedHunterExams, failedHunterExams);
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

    public boolean isHunterExamAttemptResultsOverridden() {
        return hunterExamAttemptResultsOverridden;
    }

    public void setHunterExamAttemptResultsOverridden(final boolean hunterExamAttemptResultsOverridden) {
        this.hunterExamAttemptResultsOverridden = hunterExamAttemptResultsOverridden;
    }

    public Integer getHunterExamOfficials() {
        return hunterExamOfficials;
    }

    public void setHunterExamOfficials(final Integer hunterExamOfficials) {
        this.hunterExamOfficials = hunterExamOfficials;
    }

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
