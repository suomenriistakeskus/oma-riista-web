package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.DateUtil;
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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class StateAidTrainingStatistics
        implements AnnualStatisticsFieldsetStatus, HasLastModificationStatus<StateAidTrainingStatistics>, Serializable {

    public static final StateAidTrainingStatistics reduce(@Nullable final StateAidTrainingStatistics a,
                                                          @Nullable final StateAidTrainingStatistics b) {

        final StateAidTrainingStatistics result = new StateAidTrainingStatistics();
        result.setMooselikeHuntingTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getMooselikeHuntingTrainingEvents()));
        result.setMooselikeHuntingTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getMooselikeHuntingTrainingParticipants()));
        result.setMooselikeHuntingLeaderTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getMooselikeHuntingLeaderTrainingEvents()));
        result.setMooselikeHuntingLeaderTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getMooselikeHuntingLeaderTrainingParticipants()));
        result.setCarnivoreHuntingTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getCarnivoreHuntingTrainingEvents()));
        result.setCarnivoreHuntingTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getCarnivoreHuntingTrainingParticipants()));
        result.setCarnivoreHuntingLeaderTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getCarnivoreHuntingLeaderTrainingEvents()));
        result.setCarnivoreHuntingLeaderTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getCarnivoreHuntingLeaderTrainingParticipants()));
        result.setSrvaTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getSrvaTrainingEvents()));
        result.setSrvaTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getSrvaTrainingParticipants()));
        result.setCarnivoreContactPersonTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getCarnivoreContactPersonTrainingEvents()));
        result.setCarnivoreContactPersonTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getCarnivoreContactPersonTrainingParticipants()));
        result.setSchoolTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getSchoolTrainingEvents()));
        result.setSchoolTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getSchoolTrainingParticipants()));
        result.setCollegeTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getCollegeTrainingEvents()));
        result.setCollegeTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getCollegeTrainingParticipants()));
        result.setOtherYouthTargetedTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getOtherYouthTargetedTrainingEvents()));
        result.setOtherYouthTargetedTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getOtherYouthTargetedTrainingParticipants()));
        result.setAccidentPreventionTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getAccidentPreventionTrainingEvents()));
        result.setAccidentPreventionTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getAccidentPreventionTrainingParticipants()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static StateAidTrainingStatistics reduce(@Nonnull final Stream<StateAidTrainingStatistics> items) {
        requireNonNull(items);
        return items.reduce(new StateAidTrainingStatistics(), StateAidTrainingStatistics::reduce);
    }

    public static <T> StateAidTrainingStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                        @Nonnull final Function<? super T, StateAidTrainingStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Hirvieläinten metsästäjät -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_training_events")
    private Integer mooselikeHuntingTrainingEvents;

    // Hirvieläinten metsästäjät -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_training_participants")
    private Integer mooselikeHuntingTrainingParticipants;

    // Hirvieläinten metsästäjät -johtajakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_leader_training_events")
    private Integer mooselikeHuntingLeaderTrainingEvents;

    // Hirvieläinten metsästäjät -johtajakoulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_leader_training_participants")
    private Integer mooselikeHuntingLeaderTrainingParticipants;

    // Suurpetojen metsästäjät -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "carnivore_hunting_training_events")
    private Integer carnivoreHuntingTrainingEvents;

    // Suurpetojen metsästäjät -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "carnivore_hunting_training_participants")
    private Integer carnivoreHuntingTrainingParticipants;

    // Suurpetojen metsästäjät -johtajakoulutustilaisuudet, kpl
    @Min(0)
    @Column(name = "carnivore_hunting_leader_training_events")
    private Integer carnivoreHuntingLeaderTrainingEvents;

    // Suurpetojen metsästäjät -johtajakoulutukseen osallistuneet, kpl
    @Min(0)
    @Column(name = "carnivore_hunting_leader_training_participants")
    private Integer carnivoreHuntingLeaderTrainingParticipants;

    // SRVA-koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "srva_training_events")
    private Integer srvaTrainingEvents;

    // SRVA-koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "srva_training_participants")
    private Integer srvaTrainingParticipants;

    // Petoyhdyshenkilökoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "carnivore_contact_person_training_events")
    private Integer carnivoreContactPersonTrainingEvents;

    // Petoyhdyshenkilökoulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "carnivore_contact_person_training_participants")
    private Integer carnivoreContactPersonTrainingParticipants;

    // Kouluissa pidetyt koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "school_training_events")
    private Integer schoolTrainingEvents;

    // Kouluissa pidettyjen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "school_training_participants")
    private Integer schoolTrainingParticipants;

    // Oppilaitoksissa pidetyt koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "college_training_events")
    private Integer collegeTrainingEvents;

    // Oppilaitoksissa pidettyjen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "college_training_participants")
    private Integer collegeTrainingParticipants;

    // Muut nuorisolle suunnatut koulutukset, lkm
    @Min(0)
    @Column(name = "other_youth_targeted_training_events")
    private Integer otherYouthTargetedTrainingEvents;

    // Muiden nuorisolle suunnattujen koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "other_youth_targeted_training_participants")
    private Integer otherYouthTargetedTrainingParticipants;

    // Vahinkojen ennaltaehkäisy -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "accident_prevention_training_events")
    private Integer accidentPreventionTrainingEvents;

    // Vahinkojen ennaltaehkäisy -koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "accident_prevention_training_participants")
    private Integer accidentPreventionTrainingParticipants;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "state_aid_hunter_trainings_last_modified")
    private DateTime lastModified;

    public StateAidTrainingStatistics() {
    }

    public StateAidTrainingStatistics(@Nonnull final StateAidTrainingStatistics that) {
        Objects.requireNonNull(that);

        this.mooselikeHuntingTrainingEvents = that.mooselikeHuntingTrainingEvents;
        this.mooselikeHuntingTrainingParticipants = that.mooselikeHuntingTrainingParticipants;
        this.mooselikeHuntingLeaderTrainingEvents = that.mooselikeHuntingLeaderTrainingEvents;
        this.mooselikeHuntingLeaderTrainingParticipants = that.mooselikeHuntingLeaderTrainingParticipants;
        this.carnivoreHuntingTrainingEvents = that.carnivoreHuntingTrainingEvents;
        this.carnivoreHuntingTrainingParticipants = that.carnivoreHuntingTrainingParticipants;
        this.carnivoreHuntingLeaderTrainingEvents = that.carnivoreHuntingLeaderTrainingEvents;
        this.carnivoreHuntingLeaderTrainingParticipants = that.carnivoreHuntingLeaderTrainingParticipants;
        this.srvaTrainingEvents = that.srvaTrainingEvents;
        this.srvaTrainingParticipants = that.srvaTrainingParticipants;
        this.carnivoreContactPersonTrainingEvents = that.carnivoreContactPersonTrainingEvents;
        this.carnivoreContactPersonTrainingParticipants = that.carnivoreContactPersonTrainingParticipants;
        this.schoolTrainingEvents = that.schoolTrainingEvents;
        this.schoolTrainingParticipants = that.schoolTrainingParticipants;
        this.collegeTrainingEvents = that.collegeTrainingEvents;
        this.collegeTrainingParticipants = that.collegeTrainingParticipants;
        this.otherYouthTargetedTrainingEvents = that.otherYouthTargetedTrainingEvents;
        this.otherYouthTargetedTrainingParticipants = that.otherYouthTargetedTrainingParticipants;
        this.accidentPreventionTrainingEvents = that.accidentPreventionTrainingEvents;
        this.accidentPreventionTrainingParticipants = that.accidentPreventionTrainingParticipants;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isEqualTo(final StateAidTrainingStatistics other) {
        // Includes manually updateable fields only.

        return Objects.equals(mooselikeHuntingTrainingEvents, other.mooselikeHuntingTrainingEvents) &&
                Objects.equals(mooselikeHuntingTrainingParticipants, other.mooselikeHuntingTrainingParticipants) &&
                Objects.equals(mooselikeHuntingLeaderTrainingEvents, other.mooselikeHuntingLeaderTrainingEvents) &&
                Objects.equals(mooselikeHuntingLeaderTrainingParticipants, other.mooselikeHuntingLeaderTrainingParticipants) &&
                Objects.equals(carnivoreHuntingTrainingEvents, other.carnivoreHuntingTrainingEvents) &&
                Objects.equals(carnivoreHuntingTrainingParticipants, other.carnivoreHuntingTrainingParticipants) &&
                Objects.equals(carnivoreHuntingLeaderTrainingEvents, other.carnivoreHuntingLeaderTrainingEvents) &&
                Objects.equals(carnivoreHuntingLeaderTrainingParticipants, other.carnivoreHuntingLeaderTrainingParticipants) &&
                Objects.equals(srvaTrainingEvents, other.srvaTrainingEvents) &&
                Objects.equals(srvaTrainingParticipants, other.srvaTrainingParticipants) &&
                Objects.equals(carnivoreContactPersonTrainingEvents, other.carnivoreContactPersonTrainingEvents) &&
                Objects.equals(carnivoreContactPersonTrainingParticipants, other.carnivoreContactPersonTrainingParticipants) &&
                Objects.equals(schoolTrainingEvents, other.schoolTrainingEvents) &&
                Objects.equals(schoolTrainingParticipants, other.schoolTrainingParticipants) &&
                Objects.equals(collegeTrainingEvents, other.collegeTrainingEvents) &&
                Objects.equals(collegeTrainingParticipants, other.collegeTrainingParticipants) &&
                Objects.equals(otherYouthTargetedTrainingEvents, other.otherYouthTargetedTrainingEvents) &&
                Objects.equals(otherYouthTargetedTrainingParticipants, other.otherYouthTargetedTrainingParticipants) &&
                Objects.equals(accidentPreventionTrainingEvents, other.accidentPreventionTrainingEvents) &&
                Objects.equals(accidentPreventionTrainingParticipants, other.accidentPreventionTrainingParticipants);
    }

    @Override
    public void updateModificationStatus() {
        lastModified = DateUtil.now();
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(
                mooselikeHuntingTrainingEvents, mooselikeHuntingTrainingParticipants,
                mooselikeHuntingLeaderTrainingEvents, mooselikeHuntingLeaderTrainingParticipants,
                carnivoreHuntingTrainingEvents, carnivoreHuntingTrainingParticipants,
                carnivoreHuntingLeaderTrainingEvents, carnivoreHuntingLeaderTrainingParticipants,
                srvaTrainingEvents, srvaTrainingParticipants,
                carnivoreContactPersonTrainingEvents, carnivoreContactPersonTrainingParticipants,
                schoolTrainingEvents, schoolTrainingParticipants,
                collegeTrainingEvents, collegeTrainingParticipants,
                otherYouthTargetedTrainingEvents, otherYouthTargetedTrainingParticipants,
                accidentPreventionTrainingEvents, accidentPreventionTrainingParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    public int countAllStateAidTrainingEvents() {
        return Stream
                .of(mooselikeHuntingTrainingEvents, mooselikeHuntingLeaderTrainingEvents,
                        carnivoreHuntingTrainingEvents, carnivoreHuntingLeaderTrainingEvents, srvaTrainingEvents,
                        carnivoreContactPersonTrainingEvents, schoolTrainingEvents, collegeTrainingEvents,
                        otherYouthTargetedTrainingEvents, accidentPreventionTrainingEvents)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    public int countAllStateAidTrainingParticipants() {
        return Stream
                .of(mooselikeHuntingTrainingParticipants, mooselikeHuntingLeaderTrainingParticipants,
                        carnivoreHuntingTrainingParticipants, carnivoreHuntingLeaderTrainingParticipants,
                        srvaTrainingParticipants, carnivoreContactPersonTrainingParticipants,
                        schoolTrainingParticipants, collegeTrainingParticipants, otherYouthTargetedTrainingParticipants,
                        accidentPreventionTrainingParticipants)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    public int countStateAidHunterTrainingEvents() {
        return Stream
                .of(mooselikeHuntingTrainingEvents, mooselikeHuntingLeaderTrainingEvents,
                        carnivoreHuntingTrainingEvents, carnivoreHuntingLeaderTrainingEvents, srvaTrainingEvents,
                        carnivoreContactPersonTrainingEvents, accidentPreventionTrainingEvents)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    public int countSchoolAndCollegeTrainingEvents() {
        return Stream
                .of(schoolTrainingEvents, collegeTrainingEvents)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    // Accessors -->

    public Integer getMooselikeHuntingTrainingEvents() {
        return mooselikeHuntingTrainingEvents;
    }

    public void setMooselikeHuntingTrainingEvents(final Integer mooselikeHuntingTrainingEvents) {
        this.mooselikeHuntingTrainingEvents = mooselikeHuntingTrainingEvents;
    }

    public Integer getMooselikeHuntingTrainingParticipants() {
        return mooselikeHuntingTrainingParticipants;
    }

    public void setMooselikeHuntingTrainingParticipants(final Integer mooselikeHuntingTrainingParticipants) {
        this.mooselikeHuntingTrainingParticipants = mooselikeHuntingTrainingParticipants;
    }

    public Integer getMooselikeHuntingLeaderTrainingParticipants() {
        return mooselikeHuntingLeaderTrainingParticipants;
    }

    public void setMooselikeHuntingLeaderTrainingParticipants(final Integer mooselikeHuntingLeaderTrainingParticipants) {
        this.mooselikeHuntingLeaderTrainingParticipants = mooselikeHuntingLeaderTrainingParticipants;
    }

    public Integer getMooselikeHuntingLeaderTrainingEvents() {
        return mooselikeHuntingLeaderTrainingEvents;
    }

    public void setMooselikeHuntingLeaderTrainingEvents(final Integer mooselikeHuntingLeaderTrainingEvents) {
        this.mooselikeHuntingLeaderTrainingEvents = mooselikeHuntingLeaderTrainingEvents;
    }

    public Integer getCarnivoreHuntingTrainingEvents() {
        return carnivoreHuntingTrainingEvents;
    }

    public void setCarnivoreHuntingTrainingEvents(final Integer carnivoreHuntingTrainingEvents) {
        this.carnivoreHuntingTrainingEvents = carnivoreHuntingTrainingEvents;
    }

    public Integer getCarnivoreHuntingTrainingParticipants() {
        return carnivoreHuntingTrainingParticipants;
    }

    public void setCarnivoreHuntingTrainingParticipants(final Integer carnivoreHuntingTrainingParticipants) {
        this.carnivoreHuntingTrainingParticipants = carnivoreHuntingTrainingParticipants;
    }

    public Integer getCarnivoreHuntingLeaderTrainingEvents() {
        return carnivoreHuntingLeaderTrainingEvents;
    }

    public void setCarnivoreHuntingLeaderTrainingEvents(final Integer carnivoreHuntingLeaderTrainingEvents) {
        this.carnivoreHuntingLeaderTrainingEvents = carnivoreHuntingLeaderTrainingEvents;
    }

    public Integer getCarnivoreHuntingLeaderTrainingParticipants() {
        return carnivoreHuntingLeaderTrainingParticipants;
    }

    public void setCarnivoreHuntingLeaderTrainingParticipants(final Integer carnivoreHuntingLeaderTrainingParticipants) {
        this.carnivoreHuntingLeaderTrainingParticipants = carnivoreHuntingLeaderTrainingParticipants;
    }

    public Integer getSrvaTrainingEvents() {
        return srvaTrainingEvents;
    }

    public void setSrvaTrainingEvents(final Integer srvaTrainingEvents) {
        this.srvaTrainingEvents = srvaTrainingEvents;
    }

    public Integer getSrvaTrainingParticipants() {
        return srvaTrainingParticipants;
    }

    public void setSrvaTrainingParticipants(final Integer srvaTrainingParticipants) {
        this.srvaTrainingParticipants = srvaTrainingParticipants;
    }

    public Integer getCarnivoreContactPersonTrainingEvents() {
        return carnivoreContactPersonTrainingEvents;
    }

    public void setCarnivoreContactPersonTrainingEvents(final Integer carnivoreContactPersonTrainingEvents) {
        this.carnivoreContactPersonTrainingEvents = carnivoreContactPersonTrainingEvents;
    }

    public Integer getCarnivoreContactPersonTrainingParticipants() {
        return carnivoreContactPersonTrainingParticipants;
    }

    public void setCarnivoreContactPersonTrainingParticipants(final Integer carnivoreContactPersonTrainingParticipants) {
        this.carnivoreContactPersonTrainingParticipants = carnivoreContactPersonTrainingParticipants;
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

    public Integer getAccidentPreventionTrainingEvents() {
        return accidentPreventionTrainingEvents;
    }

    public void setAccidentPreventionTrainingEvents(final Integer accidentPreventionTrainingEvents) {
        this.accidentPreventionTrainingEvents = accidentPreventionTrainingEvents;
    }

    public Integer getAccidentPreventionTrainingParticipants() {
        return accidentPreventionTrainingParticipants;
    }

    public void setAccidentPreventionTrainingParticipants(final Integer accidentPreventionTrainingParticipants) {
        this.accidentPreventionTrainingParticipants = accidentPreventionTrainingParticipants;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
