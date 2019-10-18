package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class YouthTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsNonComputedFields<YouthTrainingStatistics>,
        Serializable {
    public static final YouthTrainingStatistics reduce(@Nullable final YouthTrainingStatistics a,
                                                        @Nullable final YouthTrainingStatistics b) {

        final YouthTrainingStatistics result = new YouthTrainingStatistics();
        result.setSchoolTrainingEvents(nullableIntSum(a, b, s -> s.getSchoolTrainingEvents()));
        result.setSchoolTrainingParticipants(nullableIntSum(a, b, s -> s.getSchoolTrainingParticipants()));
        result.setCollegeTrainingEvents(nullableIntSum(a, b, s -> s.getCollegeTrainingEvents()));
        result.setCollegeTrainingParticipants(nullableIntSum(a, b, s -> s.getCollegeTrainingParticipants()));
        result.setOtherYouthTargetedTrainingEvents(nullableIntSum(a, b, s -> s.getOtherYouthTargetedTrainingEvents()));
        result.setOtherYouthTargetedTrainingParticipants(nullableIntSum(a, b, s -> s.getOtherYouthTargetedTrainingParticipants()));

        return result;
    }

    public static YouthTrainingStatistics reduce(@Nonnull final Stream<YouthTrainingStatistics> items) {
        requireNonNull(items);
        return items.reduce(new YouthTrainingStatistics(), YouthTrainingStatistics::reduce);
    }

    public static <T> YouthTrainingStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                      @Nonnull final Function<? super T, YouthTrainingStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Kouluissa pidetyt koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "school_training_events")
    private Integer schoolTrainingEvents;

    @Column(name = "school_training_events_overridden", nullable = false)
    private boolean schoolTrainingEventsOverridden;

    // Kouluissa pidettyjen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "school_training_participants")
    private Integer schoolTrainingParticipants;

    @Column(name = "school_training_participants_overridden", nullable = false)
    private boolean schoolTrainingParticipantsOverridden;

    // Oppilaitoksissa pidetyt koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "college_training_events")
    private Integer collegeTrainingEvents;

    @Column(name = "college_training_events_overridden", nullable = false)
    private boolean collegeTrainingEventsOverridden;

    // Oppilaitoksissa pidettyjen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "college_training_participants")
    private Integer collegeTrainingParticipants;

    @Column(name = "college_training_participants_overridden", nullable = false)
    private boolean collegeTrainingParticipantsOverridden;

    // Muut nuorisolle suunnatut koulutukset, lkm
    @Min(0)
    @Column(name = "other_youth_targeted_training_events")
    private Integer otherYouthTargetedTrainingEvents;

    @Column(name = "other_youth_targeted_training_events_overridden", nullable = false)
    private boolean otherYouthTargetedTrainingEventsOverridden;

    // Muiden nuorisolle suunnattujen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "other_youth_targeted_training_participants")
    private Integer otherYouthTargetedTrainingParticipants;

    @Column(name = "other_youth_targeted_training_participants_overridden", nullable = false)
    private boolean otherYouthTargetedTrainingParticipantsOverridden;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "youth_trainings_last_modified")
    private DateTime lastModified;

    public YouthTrainingStatistics() {
    }

    public YouthTrainingStatistics(@Nonnull final YouthTrainingStatistics that) {
        requireNonNull(that);

        this.schoolTrainingEvents = that.schoolTrainingEvents;
        this.schoolTrainingEventsOverridden = that.schoolTrainingEventsOverridden;

        this.schoolTrainingParticipants = that.schoolTrainingParticipants;
        this.schoolTrainingParticipantsOverridden = that.schoolTrainingParticipantsOverridden;

        this.collegeTrainingEvents = that.collegeTrainingEvents;
        this.collegeTrainingEventsOverridden = that.collegeTrainingEventsOverridden;

        this.collegeTrainingParticipants = that.collegeTrainingParticipants;
        this.collegeTrainingParticipantsOverridden = that.collegeTrainingParticipantsOverridden;

        this.otherYouthTargetedTrainingEvents = that.otherYouthTargetedTrainingEvents;
        this.otherYouthTargetedTrainingEventsOverridden = that.otherYouthTargetedTrainingEventsOverridden;

        this.otherYouthTargetedTrainingParticipants = that.otherYouthTargetedTrainingParticipants;
        this.otherYouthTargetedTrainingParticipantsOverridden = that.otherYouthTargetedTrainingParticipantsOverridden;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(schoolTrainingEvents, schoolTrainingParticipants,
                collegeTrainingEvents, collegeTrainingParticipants,
                otherYouthTargetedTrainingEvents, otherYouthTargetedTrainingParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.YOUTH_TRAINING;
    }

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public boolean isEqualTo(@Nonnull final YouthTrainingStatistics that) {
        return Objects.equals(schoolTrainingEvents, that.schoolTrainingEvents) &&
                Objects.equals(schoolTrainingParticipants, that.schoolTrainingParticipants) &&

                Objects.equals(collegeTrainingEvents, that.collegeTrainingEvents) &&
                Objects.equals(collegeTrainingParticipants, that.collegeTrainingParticipants) &&

                Objects.equals(otherYouthTargetedTrainingEvents, that.otherYouthTargetedTrainingEvents) &&
                Objects.equals(otherYouthTargetedTrainingParticipants, that.otherYouthTargetedTrainingParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final YouthTrainingStatistics that) {
        this.schoolTrainingEvents = that.schoolTrainingEvents;
        this.schoolTrainingEventsOverridden = that.schoolTrainingEventsOverridden;

        this.schoolTrainingParticipants = that.schoolTrainingParticipants;
        this.schoolTrainingParticipantsOverridden = that.schoolTrainingParticipantsOverridden;

        this.collegeTrainingEvents = that.collegeTrainingEvents;
        this.collegeTrainingEventsOverridden = that.collegeTrainingEventsOverridden;

        this.collegeTrainingParticipants = that.collegeTrainingParticipants;
        this.collegeTrainingParticipantsOverridden = that.collegeTrainingParticipantsOverridden;

        this.otherYouthTargetedTrainingEvents = that.otherYouthTargetedTrainingEvents;
        this.otherYouthTargetedTrainingEventsOverridden = that.otherYouthTargetedTrainingEventsOverridden;

        this.otherYouthTargetedTrainingParticipants = that.otherYouthTargetedTrainingParticipants;
        this.otherYouthTargetedTrainingParticipantsOverridden = that.otherYouthTargetedTrainingParticipantsOverridden;
    }

    @Nullable
    Integer countStudentTrainingEvents() {
        return nullableIntSum(schoolTrainingEvents, collegeTrainingEvents);
    }

    @Nullable
    Integer countStudentAndYouthTrainingEvents() {
        return nullableIntSum(schoolTrainingEvents, collegeTrainingEvents, otherYouthTargetedTrainingEvents);
    }

    @Nullable
    Integer countStudentAndYouthTrainingParticipants() {
        return nullableIntSum(schoolTrainingParticipants, collegeTrainingParticipants, otherYouthTargetedTrainingParticipants);
    }

    public Integer getSchoolTrainingEvents() {
        return schoolTrainingEvents;
    }

    public void setSchoolTrainingEvents(final Integer schoolTrainingEvents) {
        this.schoolTrainingEvents = schoolTrainingEvents;
    }

    public Integer getSchoolTrainingParticipants() {
        return schoolTrainingParticipants;
    }

    public void setSchoolTrainingParticipants(final Integer schoolTrainingParticipants) {
        this.schoolTrainingParticipants = schoolTrainingParticipants;
    }

    public Integer getCollegeTrainingEvents() {
        return collegeTrainingEvents;
    }

    public void setCollegeTrainingEvents(final Integer collegeTrainingEvents) {
        this.collegeTrainingEvents = collegeTrainingEvents;
    }

    public Integer getCollegeTrainingParticipants() {
        return collegeTrainingParticipants;
    }

    public void setCollegeTrainingParticipants(final Integer collegeTrainingParticipants) {
        this.collegeTrainingParticipants = collegeTrainingParticipants;
    }

    public Integer getOtherYouthTargetedTrainingEvents() {
        return otherYouthTargetedTrainingEvents;
    }

    public void setOtherYouthTargetedTrainingEvents(final Integer otherYouthTargetedTrainingEvents) {
        this.otherYouthTargetedTrainingEvents = otherYouthTargetedTrainingEvents;
    }

    public Integer getOtherYouthTargetedTrainingParticipants() {
        return otherYouthTargetedTrainingParticipants;
    }

    public void setOtherYouthTargetedTrainingParticipants(final Integer otherYouthTargetedTrainingParticipants) {
        this.otherYouthTargetedTrainingParticipants = otherYouthTargetedTrainingParticipants;
    }

    public boolean isSchoolTrainingEventsOverridden() {
        return schoolTrainingEventsOverridden;
    }

    public void setSchoolTrainingEventsOverridden(final boolean schoolTrainingEventsOverridden) {
        this.schoolTrainingEventsOverridden = schoolTrainingEventsOverridden;
    }

    public boolean isSchoolTrainingParticipantsOverridden() {
        return schoolTrainingParticipantsOverridden;
    }

    public void setSchoolTrainingParticipantsOverridden(final boolean schoolTrainingParticipantsOverridden) {
        this.schoolTrainingParticipantsOverridden = schoolTrainingParticipantsOverridden;
    }

    public boolean isCollegeTrainingEventsOverridden() {
        return collegeTrainingEventsOverridden;
    }

    public void setCollegeTrainingEventsOverridden(final boolean collegeTrainingEventsOverridden) {
        this.collegeTrainingEventsOverridden = collegeTrainingEventsOverridden;
    }

    public boolean isCollegeTrainingParticipantsOverridden() {
        return collegeTrainingParticipantsOverridden;
    }

    public void setCollegeTrainingParticipantsOverridden(final boolean collegeTrainingParticipantsOverridden) {
        this.collegeTrainingParticipantsOverridden = collegeTrainingParticipantsOverridden;
    }

    public boolean isOtherYouthTargetedTrainingEventsOverridden() {
        return otherYouthTargetedTrainingEventsOverridden;
    }

    public void setOtherYouthTargetedTrainingEventsOverridden(final boolean otherYouthTargetedTrainingEventsOverridden) {
        this.otherYouthTargetedTrainingEventsOverridden = otherYouthTargetedTrainingEventsOverridden;
    }

    public boolean isOtherYouthTargetedTrainingParticipantsOverridden() {
        return otherYouthTargetedTrainingParticipantsOverridden;
    }

    public void setOtherYouthTargetedTrainingParticipantsOverridden(final boolean otherYouthTargetedTrainingParticipantsOverridden) {
        this.otherYouthTargetedTrainingParticipantsOverridden = otherYouthTargetedTrainingParticipantsOverridden;
    }
}
