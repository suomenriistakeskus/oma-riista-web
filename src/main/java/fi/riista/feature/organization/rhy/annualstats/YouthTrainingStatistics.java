package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
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

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.COLLEGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_COLLEGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_OTHER_YOUTH_TARGETED_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SCHOOL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.OTHER_YOUTH_TARGETED_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SCHOOL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.YOUTH_TRAINING_STATISTICS;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class YouthTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsFieldsetParticipants,
        AnnualStatisticsManuallyEditableFields<YouthTrainingStatistics>,
        Serializable {
    public static YouthTrainingStatistics reduce(@Nullable final YouthTrainingStatistics a,
                                                 @Nullable final YouthTrainingStatistics b) {

        final YouthTrainingStatistics result = new YouthTrainingStatistics();
        result.setSchoolTrainingEvents(nullableIntSum(a, b, YouthTrainingStatistics::getSchoolTrainingEvents));
        result.setSchoolTrainingParticipants(nullableIntSum(a, b, YouthTrainingStatistics::getSchoolTrainingParticipants));
        result.setCollegeTrainingEvents(nullableIntSum(a, b, YouthTrainingStatistics::getCollegeTrainingEvents));
        result.setCollegeTrainingParticipants(nullableIntSum(a, b, YouthTrainingStatistics::getCollegeTrainingParticipants));
        result.setOtherYouthTargetedTrainingEvents(nullableIntSum(a, b, YouthTrainingStatistics::getOtherYouthTargetedTrainingEvents));
        result.setOtherYouthTargetedTrainingParticipants(nullableIntSum(a, b, YouthTrainingStatistics::getOtherYouthTargetedTrainingParticipants));

        result.setNonSubsidizableSchoolTrainingEvents(nullableIntSum(a, b, YouthTrainingStatistics::getNonSubsidizableSchoolTrainingEvents));
        result.setNonSubsidizableSchoolTrainingParticipants(nullableIntSum(a, b, YouthTrainingStatistics::getNonSubsidizableSchoolTrainingParticipants));
        result.setNonSubsidizableCollegeTrainingEvents(nullableIntSum(a, b, YouthTrainingStatistics::getNonSubsidizableCollegeTrainingEvents));
        result.setNonSubsidizableCollegeTrainingParticipants(nullableIntSum(a, b, YouthTrainingStatistics::getNonSubsidizableCollegeTrainingParticipants));
        result.setNonSubsidizableOtherYouthTargetedTrainingEvents(nullableIntSum(a, b, YouthTrainingStatistics::getNonSubsidizableOtherYouthTargetedTrainingEvents));
        result.setNonSubsidizableOtherYouthTargetedTrainingParticipants(nullableIntSum(a, b, YouthTrainingStatistics::getNonSubsidizableOtherYouthTargetedTrainingParticipants));

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

    @Min(0)
    @Column(name = "non_subsidizable_school_training_events")
    private Integer nonSubsidizableSchoolTrainingEvents;

    @Column(name = "school_training_events_overridden", nullable = false)
    private boolean schoolTrainingEventsOverridden;

    // Kouluissa pidettyjen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "school_training_participants")
    private Integer schoolTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_school_training_participants")
    private Integer nonSubsidizableSchoolTrainingParticipants;

    @Column(name = "school_training_participants_overridden", nullable = false)
    private boolean schoolTrainingParticipantsOverridden;

    // Oppilaitoksissa pidetyt koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "college_training_events")
    private Integer collegeTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_college_training_events")
    private Integer nonSubsidizableCollegeTrainingEvents;

    @Column(name = "college_training_events_overridden", nullable = false)
    private boolean collegeTrainingEventsOverridden;

    // Oppilaitoksissa pidettyjen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "college_training_participants")
    private Integer collegeTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_college_training_participants")
    private Integer nonSubsidizableCollegeTrainingParticipants;

    @Column(name = "college_training_participants_overridden", nullable = false)
    private boolean collegeTrainingParticipantsOverridden;

    // Muut nuorisolle suunnatut koulutukset, lkm
    @Min(0)
    @Column(name = "other_youth_targeted_training_events")
    private Integer otherYouthTargetedTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_other_youth_targeted_training_events")
    private Integer nonSubsidizableOtherYouthTargetedTrainingEvents;

    @Column(name = "other_youth_targeted_training_events_overridden", nullable = false)
    private boolean otherYouthTargetedTrainingEventsOverridden;

    // Muiden nuorisolle suunnattujen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "other_youth_targeted_training_participants")
    private Integer otherYouthTargetedTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_other_youth_targeted_training_participants")
    private Integer nonSubsidizableOtherYouthTargetedTrainingParticipants;

    @Column(name = "other_youth_targeted_training_participants_overridden", nullable = false)
    private boolean otherYouthTargetedTrainingParticipantsOverridden;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "youth_trainings_last_modified")
    private DateTime lastModified;

    public YouthTrainingStatistics() {
    }

    public YouthTrainingStatistics(@Nonnull final YouthTrainingStatistics that) {
        requireNonNull(that);

        this.schoolTrainingEvents = that.schoolTrainingEvents;
        this.nonSubsidizableSchoolTrainingEvents = that.nonSubsidizableSchoolTrainingEvents;
        this.schoolTrainingEventsOverridden = that.schoolTrainingEventsOverridden;

        this.schoolTrainingParticipants = that.schoolTrainingParticipants;
        this.nonSubsidizableSchoolTrainingParticipants = that.nonSubsidizableSchoolTrainingParticipants;
        this.schoolTrainingParticipantsOverridden = that.schoolTrainingParticipantsOverridden;

        this.collegeTrainingEvents = that.collegeTrainingEvents;
        this.nonSubsidizableCollegeTrainingEvents = that.nonSubsidizableCollegeTrainingEvents;
        this.collegeTrainingEventsOverridden = that.collegeTrainingEventsOverridden;

        this.collegeTrainingParticipants = that.collegeTrainingParticipants;
        this.nonSubsidizableCollegeTrainingParticipants = that.nonSubsidizableCollegeTrainingParticipants;
        this.collegeTrainingParticipantsOverridden = that.collegeTrainingParticipantsOverridden;

        this.otherYouthTargetedTrainingEvents = that.otherYouthTargetedTrainingEvents;
        this.nonSubsidizableOtherYouthTargetedTrainingEvents = that.nonSubsidizableOtherYouthTargetedTrainingEvents;
        this.otherYouthTargetedTrainingEventsOverridden = that.otherYouthTargetedTrainingEventsOverridden;

        this.otherYouthTargetedTrainingParticipants = that.otherYouthTargetedTrainingParticipants;
        this.nonSubsidizableOtherYouthTargetedTrainingParticipants = that.nonSubsidizableOtherYouthTargetedTrainingParticipants;
        this.otherYouthTargetedTrainingParticipantsOverridden = that.otherYouthTargetedTrainingParticipantsOverridden;

        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(schoolTrainingEvents, schoolTrainingParticipants,
                collegeTrainingEvents, collegeTrainingParticipants,
                otherYouthTargetedTrainingEvents, otherYouthTargetedTrainingParticipants) && hasParticipants();
    }

    private boolean hasParticipants() {
        return listMissingParticipants()._2.isEmpty();
    }

    @Override
    public Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> listMissingParticipants() {
        final List<AnnualStatisticsParticipantField> missing = new ArrayList<>();
        if (schoolTrainingEvents != null && schoolTrainingEvents > 0 && schoolTrainingParticipants <= 0) {
            missing.add(SCHOOL_TRAINING_EVENTS);
        }
        if (collegeTrainingEvents != null && collegeTrainingEvents > 0 && collegeTrainingParticipants <= 0) {
            missing.add(COLLEGE_TRAINING_EVENTS);
        }
        if (otherYouthTargetedTrainingEvents != null && otherYouthTargetedTrainingEvents > 0 && otherYouthTargetedTrainingParticipants <= 0) {
            missing.add(OTHER_YOUTH_TARGETED_TRAINING_EVENTS);
        }

        if (nonSubsidizableSchoolTrainingEvents != null && nonSubsidizableSchoolTrainingEvents > 0 &&
                nonSubsidizableSchoolTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_SCHOOL_TRAINING_EVENTS);
        }
        if (nonSubsidizableCollegeTrainingEvents != null && nonSubsidizableCollegeTrainingEvents > 0 &&
                nonSubsidizableCollegeTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_COLLEGE_TRAINING_EVENTS);
        }
        if (nonSubsidizableOtherYouthTargetedTrainingEvents != null && nonSubsidizableOtherYouthTargetedTrainingEvents > 0 &&
                nonSubsidizableOtherYouthTargetedTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_OTHER_YOUTH_TARGETED_TRAINING_EVENTS);
        }

        return Tuple.of(YOUTH_TRAINING_STATISTICS, missing);
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
                Objects.equals(nonSubsidizableSchoolTrainingEvents, that.nonSubsidizableSchoolTrainingEvents) &&
                Objects.equals(nonSubsidizableSchoolTrainingParticipants, that.nonSubsidizableSchoolTrainingParticipants) &&

                Objects.equals(collegeTrainingEvents, that.collegeTrainingEvents) &&
                Objects.equals(collegeTrainingParticipants, that.collegeTrainingParticipants) &&
                Objects.equals(nonSubsidizableCollegeTrainingEvents, that.nonSubsidizableCollegeTrainingEvents) &&
                Objects.equals(nonSubsidizableCollegeTrainingParticipants, that.nonSubsidizableCollegeTrainingParticipants) &&

                Objects.equals(otherYouthTargetedTrainingEvents, that.otherYouthTargetedTrainingEvents) &&
                Objects.equals(otherYouthTargetedTrainingParticipants, that.otherYouthTargetedTrainingParticipants) &&
                Objects.equals(nonSubsidizableOtherYouthTargetedTrainingEvents, that.nonSubsidizableOtherYouthTargetedTrainingEvents) &&
                Objects.equals(nonSubsidizableOtherYouthTargetedTrainingParticipants, that.nonSubsidizableOtherYouthTargetedTrainingParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final YouthTrainingStatistics that) {
        if (!Objects.equals(this.schoolTrainingEvents, that.schoolTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableSchoolTrainingEvents, that.nonSubsidizableSchoolTrainingEvents)) {
            this.schoolTrainingEventsOverridden = true;
        }
        this.schoolTrainingEvents = that.schoolTrainingEvents;
        this.nonSubsidizableSchoolTrainingEvents = that.nonSubsidizableSchoolTrainingEvents;

        if (!Objects.equals(this.schoolTrainingParticipants, that.schoolTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableSchoolTrainingParticipants, that.nonSubsidizableSchoolTrainingParticipants)) {
            this.schoolTrainingParticipantsOverridden = true;
        }
        this.schoolTrainingParticipants = that.schoolTrainingParticipants;
        this.nonSubsidizableSchoolTrainingParticipants = that.nonSubsidizableSchoolTrainingParticipants;

        if (!Objects.equals(this.collegeTrainingEvents, that.collegeTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableCollegeTrainingEvents, that.nonSubsidizableCollegeTrainingEvents)) {
            this.collegeTrainingEventsOverridden = true;
        }
        this.collegeTrainingEvents = that.collegeTrainingEvents;
        this.nonSubsidizableCollegeTrainingEvents = that.nonSubsidizableCollegeTrainingEvents;

        if (!Objects.equals(this.collegeTrainingParticipants, that.collegeTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableCollegeTrainingParticipants, that.nonSubsidizableCollegeTrainingParticipants)) {
            this.collegeTrainingParticipantsOverridden = true;
        }
        this.collegeTrainingParticipants = that.collegeTrainingParticipants;
        this.nonSubsidizableCollegeTrainingParticipants = that.nonSubsidizableCollegeTrainingParticipants;

        if (!Objects.equals(this.otherYouthTargetedTrainingEvents, that.otherYouthTargetedTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableOtherYouthTargetedTrainingEvents, that.nonSubsidizableOtherYouthTargetedTrainingEvents)) {
            this.otherYouthTargetedTrainingEventsOverridden = true;
        }
        this.otherYouthTargetedTrainingEvents = that.otherYouthTargetedTrainingEvents;
        this.nonSubsidizableOtherYouthTargetedTrainingEvents = that.nonSubsidizableOtherYouthTargetedTrainingEvents;

        if (!Objects.equals(this.otherYouthTargetedTrainingParticipants, that.otherYouthTargetedTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableOtherYouthTargetedTrainingParticipants, that.nonSubsidizableOtherYouthTargetedTrainingParticipants)) {
            this.otherYouthTargetedTrainingParticipantsOverridden = true;
        }
        this.otherYouthTargetedTrainingParticipants = that.otherYouthTargetedTrainingParticipants;
        this.nonSubsidizableOtherYouthTargetedTrainingParticipants = that.nonSubsidizableOtherYouthTargetedTrainingParticipants;
    }

    @Nullable
    Integer countStudentTrainingEvents() {
        return nullableIntSum(schoolTrainingEvents, collegeTrainingEvents);
    }

    @Nullable
    Integer countNonSubsidizableStudentTrainingEvents() {
        return nullableIntSum(nonSubsidizableSchoolTrainingEvents, nonSubsidizableCollegeTrainingEvents);
    }

    @Nullable
    Integer countStudentAndYouthTrainingEvents() {
        return nullableIntSum(schoolTrainingEvents, collegeTrainingEvents, otherYouthTargetedTrainingEvents);
    }

    @Nullable
    Integer countNonSubsidizableStudentAndYouthTrainingEvents() {
        return nullableIntSum(nonSubsidizableSchoolTrainingEvents, nonSubsidizableCollegeTrainingEvents, nonSubsidizableOtherYouthTargetedTrainingEvents);
    }

    @Nullable
    Integer countStudentAndYouthTrainingParticipants() {
        return nullableIntSum(schoolTrainingParticipants, collegeTrainingParticipants, otherYouthTargetedTrainingParticipants);
    }

    @Nullable
    Integer countNonSubsidizableStudentAndYouthTrainingParticipants() {
        return nullableIntSum(nonSubsidizableSchoolTrainingParticipants, nonSubsidizableCollegeTrainingParticipants, nonSubsidizableOtherYouthTargetedTrainingParticipants);
    }

    public Integer getSchoolTrainingEvents() {
        return schoolTrainingEvents;
    }

    public void setSchoolTrainingEvents(final Integer schoolTrainingEvents) {
        this.schoolTrainingEvents = schoolTrainingEvents;
    }

    public Integer getNonSubsidizableSchoolTrainingEvents() {
        return nonSubsidizableSchoolTrainingEvents;
    }

    public void setNonSubsidizableSchoolTrainingEvents(final Integer nonSubsidizableSchoolTrainingEvents) {
        this.nonSubsidizableSchoolTrainingEvents = nonSubsidizableSchoolTrainingEvents;
    }

    public Integer getSchoolTrainingParticipants() {
        return schoolTrainingParticipants;
    }

    public void setSchoolTrainingParticipants(final Integer schoolTrainingParticipants) {
        this.schoolTrainingParticipants = schoolTrainingParticipants;
    }

    public Integer getNonSubsidizableSchoolTrainingParticipants() {
        return nonSubsidizableSchoolTrainingParticipants;
    }

    public void setNonSubsidizableSchoolTrainingParticipants(final Integer nonSubsidizableSchoolTrainingParticipants) {
        this.nonSubsidizableSchoolTrainingParticipants = nonSubsidizableSchoolTrainingParticipants;
    }

    public Integer getCollegeTrainingEvents() {
        return collegeTrainingEvents;
    }

    public void setCollegeTrainingEvents(final Integer collegeTrainingEvents) {
        this.collegeTrainingEvents = collegeTrainingEvents;
    }

    public Integer getNonSubsidizableCollegeTrainingEvents() {
        return nonSubsidizableCollegeTrainingEvents;
    }

    public void setNonSubsidizableCollegeTrainingEvents(final Integer nonSubsidizableCollegeTrainingEvents) {
        this.nonSubsidizableCollegeTrainingEvents = nonSubsidizableCollegeTrainingEvents;
    }

    public Integer getCollegeTrainingParticipants() {
        return collegeTrainingParticipants;
    }

    public void setCollegeTrainingParticipants(final Integer collegeTrainingParticipants) {
        this.collegeTrainingParticipants = collegeTrainingParticipants;
    }

    public Integer getNonSubsidizableCollegeTrainingParticipants() {
        return nonSubsidizableCollegeTrainingParticipants;
    }

    public void setNonSubsidizableCollegeTrainingParticipants(final Integer nonSubsidizableCollegeTrainingParticipants) {
        this.nonSubsidizableCollegeTrainingParticipants = nonSubsidizableCollegeTrainingParticipants;
    }

    public Integer getOtherYouthTargetedTrainingEvents() {
        return otherYouthTargetedTrainingEvents;
    }

    public void setOtherYouthTargetedTrainingEvents(final Integer otherYouthTargetedTrainingEvents) {
        this.otherYouthTargetedTrainingEvents = otherYouthTargetedTrainingEvents;
    }

    public Integer getNonSubsidizableOtherYouthTargetedTrainingEvents() {
        return nonSubsidizableOtherYouthTargetedTrainingEvents;
    }

    public void setNonSubsidizableOtherYouthTargetedTrainingEvents(final Integer nonSubsidizableOtherYouthTargetedTrainingEvents) {
        this.nonSubsidizableOtherYouthTargetedTrainingEvents = nonSubsidizableOtherYouthTargetedTrainingEvents;
    }

    public Integer getOtherYouthTargetedTrainingParticipants() {
        return otherYouthTargetedTrainingParticipants;
    }

    public void setOtherYouthTargetedTrainingParticipants(final Integer otherYouthTargetedTrainingParticipants) {
        this.otherYouthTargetedTrainingParticipants = otherYouthTargetedTrainingParticipants;
    }

    public Integer getNonSubsidizableOtherYouthTargetedTrainingParticipants() {
        return nonSubsidizableOtherYouthTargetedTrainingParticipants;
    }

    public void setNonSubsidizableOtherYouthTargetedTrainingParticipants(final Integer nonSubsidizableOtherYouthTargetedTrainingParticipants) {
        this.nonSubsidizableOtherYouthTargetedTrainingParticipants = nonSubsidizableOtherYouthTargetedTrainingParticipants;
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
