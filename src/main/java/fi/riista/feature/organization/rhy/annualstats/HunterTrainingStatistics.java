package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
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
public class HunterTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsManuallyEditableFields<HunterTrainingStatistics>,
        Serializable {

    public static final HunterTrainingStatistics reduce(@Nullable final HunterTrainingStatistics a,
                                                        @Nullable final HunterTrainingStatistics b) {

        final HunterTrainingStatistics result = new HunterTrainingStatistics();
        result.setMooselikeHuntingTrainingEvents(nullableIntSum(a, b, s -> s.getMooselikeHuntingTrainingEvents()));
        result.setMooselikeHuntingTrainingParticipants(nullableIntSum(a, b, s -> s.getMooselikeHuntingTrainingParticipants()));
        result.setMooselikeHuntingLeaderTrainingEvents(nullableIntSum(a, b, s -> s.getMooselikeHuntingLeaderTrainingEvents()));
        result.setMooselikeHuntingLeaderTrainingParticipants(nullableIntSum(a, b, s -> s.getMooselikeHuntingLeaderTrainingParticipants()));
        result.setCarnivoreHuntingTrainingEvents(nullableIntSum(a, b, s -> s.getCarnivoreHuntingTrainingEvents()));
        result.setCarnivoreHuntingTrainingParticipants(nullableIntSum(a, b, s -> s.getCarnivoreHuntingTrainingParticipants()));
        result.setCarnivoreHuntingLeaderTrainingEvents(nullableIntSum(a, b, s -> s.getCarnivoreHuntingLeaderTrainingEvents()));
        result.setCarnivoreHuntingLeaderTrainingParticipants(nullableIntSum(a, b, s -> s.getCarnivoreHuntingLeaderTrainingParticipants()));
        result.setSrvaTrainingEvents(nullableIntSum(a, b, s -> s.getSrvaTrainingEvents()));
        result.setSrvaTrainingParticipants(nullableIntSum(a, b, s -> s.getSrvaTrainingParticipants()));
        result.setCarnivoreContactPersonTrainingEvents(nullableIntSum(a, b, s -> s.getCarnivoreContactPersonTrainingEvents()));
        result.setCarnivoreContactPersonTrainingParticipants(nullableIntSum(a, b, s -> s.getCarnivoreContactPersonTrainingParticipants()));
        result.setAccidentPreventionTrainingEvents(nullableIntSum(a, b, s -> s.getAccidentPreventionTrainingEvents()));
        result.setAccidentPreventionTrainingParticipants(nullableIntSum(a, b, s -> s.getAccidentPreventionTrainingParticipants()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static HunterTrainingStatistics reduce(@Nonnull final Stream<HunterTrainingStatistics> items) {
        requireNonNull(items);
        return items.reduce(new HunterTrainingStatistics(), HunterTrainingStatistics::reduce);
    }

    public static <T> HunterTrainingStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                      @Nonnull final Function<? super T, HunterTrainingStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Hirvieläinten metsästäjät -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_training_events")
    private Integer mooselikeHuntingTrainingEvents;

    @Column(name= "mooselike_hunting_training_events_overridden", nullable = false)
    private boolean mooselikeHuntingTrainingEventsOverridden;

    // Hirvieläinten metsästäjät -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_training_participants")
    private Integer mooselikeHuntingTrainingParticipants;

    @Column(name = "mooselike_hunting_training_participants_overridden", nullable = false)
    private boolean mooselikeHuntingTrainingParticipantsOverridden;

    // Hirvieläinten metsästäjät -johtajakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_leader_training_events")
    private Integer mooselikeHuntingLeaderTrainingEvents;

    @Column(name = "mooselike_hunting_leader_training_events_overridden", nullable = false)
    private boolean mooselikeHuntingLeaderTrainingEventsOverridden;

    // Hirvieläinten metsästäjät -johtajakoulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_leader_training_participants")
    private Integer mooselikeHuntingLeaderTrainingParticipants;

    @Column(name = "mooselike_hunting_leader_training_participants_overridden", nullable = false)
    private boolean mooselikeHuntingLeaderTrainingParticipantsOverridden;

    // Suurpetojen metsästäjät -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "carnivore_hunting_training_events")
    private Integer carnivoreHuntingTrainingEvents;

    @Column(name = "carnivore_hunting_training_events_overridden", nullable = false)
    private boolean carnivoreHuntingTrainingEventsOverridden;

    // Suurpetojen metsästäjät -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "carnivore_hunting_training_participants")
    private Integer carnivoreHuntingTrainingParticipants;

    @Column(name = "carnivore_hunting_training_participants_overridden", nullable = false)
    private boolean carnivoreHuntingTrainingParticipantsOverridden;

    // Suurpetojen metsästäjät -johtajakoulutustilaisuudet, kpl
    @Min(0)
    @Column(name = "carnivore_hunting_leader_training_events")
    private Integer carnivoreHuntingLeaderTrainingEvents;

    @Column(name = "carnivore_hunting_leader_training_events_overridden", nullable = false)
    private boolean carnivoreHuntingLeaderTrainingEventsOverridden;

    // Suurpetojen metsästäjät -johtajakoulutukseen osallistuneet, kpl
    @Min(0)
    @Column(name = "carnivore_hunting_leader_training_participants")
    private Integer carnivoreHuntingLeaderTrainingParticipants;

    @Column(name = "carnivore_hunting_leader_training_participants_overridden", nullable = false)
    private boolean carnivoreHuntingLeaderTrainingParticipantsOverridden;

    // SRVA-koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "srva_training_events")
    private Integer srvaTrainingEvents;

    @Column(name = "srva_training_events_overridden", nullable = false)
    private boolean srvaTrainingEventsOverridden;

    // SRVA-koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "srva_training_participants")
    private Integer srvaTrainingParticipants;

    @Column(name = "srva_training_participants_overridden", nullable = false)
    private boolean srvaTrainingParticipantsOverridden;

    // Petoyhdyshenkilökoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "carnivore_contact_person_training_events")
    private Integer carnivoreContactPersonTrainingEvents;

    @Column(name = "carnivore_contact_person_training_events_overridden", nullable = false)
    private boolean carnivoreContactPersonTrainingEventsOverridden;

    // Petoyhdyshenkilökoulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "carnivore_contact_person_training_participants")
    private Integer carnivoreContactPersonTrainingParticipants;

    @Column(name = "carnivore_contact_person_training_participants_overridden", nullable = false)
    private boolean carnivoreContactPersonTrainingParticipantsOverridden;

    // Vahinkojen ennaltaehkäisy -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "accident_prevention_training_events")
    private Integer accidentPreventionTrainingEvents;

    @Column(name = "accident_prevention_training_events_overridden", nullable = false)
    private boolean accidentPreventionTrainingEventsOverridden;

    // Vahinkojen ennaltaehkäisy -koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "accident_prevention_training_participants")
    private Integer accidentPreventionTrainingParticipants;

    @Column(name = "accident_prevention_training_participants_overridden", nullable = false)
    private boolean accidentPreventionTrainingParticipantsOverridden;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "hunter_trainings_last_modified")
    private DateTime lastModified;

    public HunterTrainingStatistics() {
    }

    public HunterTrainingStatistics(@Nonnull final HunterTrainingStatistics that) {
        requireNonNull(that);

        this.mooselikeHuntingTrainingEvents = that.mooselikeHuntingTrainingEvents;
        this.mooselikeHuntingTrainingEventsOverridden = that.mooselikeHuntingTrainingEventsOverridden;

        this.mooselikeHuntingTrainingParticipants = that.mooselikeHuntingTrainingParticipants;
        this.mooselikeHuntingTrainingParticipantsOverridden = that.mooselikeHuntingTrainingParticipantsOverridden;

        this.mooselikeHuntingLeaderTrainingEvents = that.mooselikeHuntingLeaderTrainingEvents;
        this.mooselikeHuntingLeaderTrainingEventsOverridden = that.mooselikeHuntingLeaderTrainingEventsOverridden;

        this.mooselikeHuntingLeaderTrainingParticipants = that.mooselikeHuntingLeaderTrainingParticipants;
        this.mooselikeHuntingLeaderTrainingParticipantsOverridden = that.mooselikeHuntingLeaderTrainingParticipantsOverridden;

        this.carnivoreHuntingTrainingEvents = that.carnivoreHuntingTrainingEvents;
        this.carnivoreHuntingTrainingEventsOverridden = that.carnivoreHuntingTrainingEventsOverridden;

        this.carnivoreHuntingTrainingParticipants = that.carnivoreHuntingTrainingParticipants;
        this.carnivoreHuntingTrainingParticipantsOverridden = that.carnivoreHuntingTrainingParticipantsOverridden;

        this.carnivoreHuntingLeaderTrainingEvents = that.carnivoreHuntingLeaderTrainingEvents;
        this.carnivoreHuntingLeaderTrainingEventsOverridden = that.carnivoreHuntingLeaderTrainingEventsOverridden;

        this.carnivoreHuntingLeaderTrainingParticipants = that.carnivoreHuntingLeaderTrainingParticipants;
        this.carnivoreHuntingLeaderTrainingParticipantsOverridden = that.carnivoreHuntingLeaderTrainingParticipantsOverridden;

        this.srvaTrainingEvents = that.srvaTrainingEvents;
        this.srvaTrainingEventsOverridden = that.srvaTrainingEventsOverridden;

        this.srvaTrainingParticipants = that.srvaTrainingParticipants;
        this.srvaTrainingParticipantsOverridden = that.srvaTrainingParticipantsOverridden;

        this.carnivoreContactPersonTrainingEvents = that.carnivoreContactPersonTrainingEvents;
        this.carnivoreContactPersonTrainingEventsOverridden = that.carnivoreContactPersonTrainingEventsOverridden;

        this.carnivoreContactPersonTrainingParticipants = that.carnivoreContactPersonTrainingParticipants;
        this.carnivoreContactPersonTrainingParticipantsOverridden = that.carnivoreContactPersonTrainingParticipantsOverridden;

        this.accidentPreventionTrainingEvents = that.accidentPreventionTrainingEvents;
        this.accidentPreventionTrainingEventsOverridden = that.accidentPreventionTrainingEventsOverridden;

        this.accidentPreventionTrainingParticipants = that.accidentPreventionTrainingParticipants;
        this.accidentPreventionTrainingParticipantsOverridden = that.accidentPreventionTrainingParticipantsOverridden;

        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.HUNTER_TRAINING;
    }

    @Override
    public boolean isEqualTo(@Nonnull final HunterTrainingStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(mooselikeHuntingTrainingEvents, that.mooselikeHuntingTrainingEvents) &&
                Objects.equals(mooselikeHuntingTrainingParticipants, that.mooselikeHuntingTrainingParticipants) &&

                Objects.equals(mooselikeHuntingLeaderTrainingEvents, that.mooselikeHuntingLeaderTrainingEvents) &&
                Objects.equals(mooselikeHuntingLeaderTrainingParticipants, that.mooselikeHuntingLeaderTrainingParticipants) &&

                Objects.equals(carnivoreHuntingTrainingEvents, that.carnivoreHuntingTrainingEvents) &&
                Objects.equals(carnivoreHuntingTrainingParticipants, that.carnivoreHuntingTrainingParticipants) &&

                Objects.equals(carnivoreHuntingLeaderTrainingEvents, that.carnivoreHuntingLeaderTrainingEvents) &&
                Objects.equals(carnivoreHuntingLeaderTrainingParticipants, that.carnivoreHuntingLeaderTrainingParticipants) &&

                Objects.equals(srvaTrainingEvents, that.srvaTrainingEvents) &&
                Objects.equals(srvaTrainingParticipants, that.srvaTrainingParticipants) &&

                Objects.equals(carnivoreContactPersonTrainingEvents, that.carnivoreContactPersonTrainingEvents) &&
                Objects.equals(carnivoreContactPersonTrainingParticipants, that.carnivoreContactPersonTrainingParticipants) &&

                Objects.equals(accidentPreventionTrainingEvents, that.accidentPreventionTrainingEvents) &&
                Objects.equals(accidentPreventionTrainingParticipants, that.accidentPreventionTrainingParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final HunterTrainingStatistics that) {
        // Includes manually updateable fields only.

        if (!Objects.equals(this.mooselikeHuntingTrainingEvents, that.mooselikeHuntingTrainingEvents)) {
            this.mooselikeHuntingTrainingEventsOverridden = true;
        }
        this.mooselikeHuntingTrainingEvents = that.mooselikeHuntingTrainingEvents;

        if (!Objects.equals(this.mooselikeHuntingTrainingParticipants, that.mooselikeHuntingTrainingParticipants)) {
            this.mooselikeHuntingTrainingParticipantsOverridden = true;
        }
        this.mooselikeHuntingTrainingParticipants = that.mooselikeHuntingTrainingParticipants;

        if (!Objects.equals(this.mooselikeHuntingLeaderTrainingEvents, that.mooselikeHuntingLeaderTrainingEvents)) {
            this.mooselikeHuntingLeaderTrainingEventsOverridden = true;
        }
        this.mooselikeHuntingLeaderTrainingEvents = that.mooselikeHuntingLeaderTrainingEvents;

        if (!Objects.equals(this.mooselikeHuntingLeaderTrainingParticipants, that.mooselikeHuntingLeaderTrainingParticipants)) {
            this.mooselikeHuntingLeaderTrainingParticipantsOverridden = true;
        }
        this.mooselikeHuntingLeaderTrainingParticipants = that.mooselikeHuntingLeaderTrainingParticipants;

        if (!Objects.equals(this.carnivoreHuntingTrainingEvents, that.carnivoreHuntingTrainingEvents)) {
            this.carnivoreHuntingTrainingEventsOverridden = true;
        }
        this.carnivoreHuntingTrainingEvents = that.carnivoreHuntingTrainingEvents;

        if (!Objects.equals(this.carnivoreHuntingTrainingParticipants, that.carnivoreHuntingTrainingParticipants)) {
            this.carnivoreHuntingTrainingParticipantsOverridden = true;
        }
        this.carnivoreHuntingTrainingParticipants = that.carnivoreHuntingTrainingParticipants;

        if (!Objects.equals(this.carnivoreHuntingLeaderTrainingEvents, that.carnivoreHuntingLeaderTrainingEvents)) {
            this.carnivoreHuntingLeaderTrainingEventsOverridden = true;
        }
        this.carnivoreHuntingLeaderTrainingEvents = that.carnivoreHuntingLeaderTrainingEvents;

        if (!Objects.equals(this.carnivoreHuntingLeaderTrainingParticipants, that.carnivoreHuntingLeaderTrainingParticipants)) {
            this.carnivoreHuntingLeaderTrainingParticipantsOverridden = true;
        }
        this.carnivoreHuntingLeaderTrainingParticipants = that.carnivoreHuntingLeaderTrainingParticipants;

        if (!Objects.equals(this.srvaTrainingEvents, that.srvaTrainingEvents)) {
            this.srvaTrainingEventsOverridden = true;
        }
        this.srvaTrainingEvents = that.srvaTrainingEvents;

        if (!Objects.equals(this.srvaTrainingParticipants, that.srvaTrainingParticipants)) {
            this.srvaTrainingParticipantsOverridden = true;
        }
        this.srvaTrainingParticipants = that.srvaTrainingParticipants;

        if (!Objects.equals(this.carnivoreContactPersonTrainingEvents, that.carnivoreContactPersonTrainingEvents)) {
            this.carnivoreContactPersonTrainingEventsOverridden = true;
        }
        this.carnivoreContactPersonTrainingEvents = that.carnivoreContactPersonTrainingEvents;

        if (!Objects.equals(this.carnivoreContactPersonTrainingParticipants, that.carnivoreContactPersonTrainingParticipants)) {
            this.carnivoreContactPersonTrainingParticipantsOverridden = true;
        }
        this.carnivoreContactPersonTrainingParticipants = that.carnivoreContactPersonTrainingParticipants;

        if (!Objects.equals(this.accidentPreventionTrainingEvents, that.accidentPreventionTrainingEvents)) {
            this.accidentPreventionTrainingEventsOverridden = true;
        }
        this.accidentPreventionTrainingEvents = that.accidentPreventionTrainingEvents;

        if (!Objects.equals(this.accidentPreventionTrainingParticipants, that.accidentPreventionTrainingParticipants)) {
            this.accidentPreventionTrainingParticipantsOverridden = true;
        }
        this.accidentPreventionTrainingParticipants = that.accidentPreventionTrainingParticipants;
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
                accidentPreventionTrainingEvents, accidentPreventionTrainingParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    @Nullable
    Integer countHunterTrainingEvents() {
        return nullableIntSum(
                mooselikeHuntingTrainingEvents, mooselikeHuntingLeaderTrainingEvents, carnivoreHuntingTrainingEvents,
                carnivoreHuntingLeaderTrainingEvents, srvaTrainingEvents, carnivoreContactPersonTrainingEvents,
                accidentPreventionTrainingEvents);
    }

    @Nullable
    Integer countHunterTrainingParticipants() {
        return nullableIntSum(
                mooselikeHuntingTrainingParticipants, mooselikeHuntingLeaderTrainingParticipants,
                carnivoreHuntingTrainingParticipants, carnivoreHuntingLeaderTrainingParticipants,
                srvaTrainingParticipants, carnivoreContactPersonTrainingParticipants,
                accidentPreventionTrainingParticipants);
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

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isMooselikeHuntingTrainingEventsOverridden() {
        return mooselikeHuntingTrainingEventsOverridden;
    }

    public void setMooselikeHuntingTrainingEventsOverridden(final boolean mooselikeHuntingTrainingEventsOverridden) {
        this.mooselikeHuntingTrainingEventsOverridden = mooselikeHuntingTrainingEventsOverridden;
    }

    public boolean isMooselikeHuntingTrainingParticipantsOverridden() {
        return mooselikeHuntingTrainingParticipantsOverridden;
    }

    public void setMooselikeHuntingTrainingParticipantsOverridden(final boolean mooselikeHuntingTrainingParticipantsOverridden) {
        this.mooselikeHuntingTrainingParticipantsOverridden = mooselikeHuntingTrainingParticipantsOverridden;
    }

    public boolean isMooselikeHuntingLeaderTrainingEventsOverridden() {
        return mooselikeHuntingLeaderTrainingEventsOverridden;
    }

    public void setMooselikeHuntingLeaderTrainingEventsOverridden(final boolean mooselikeHuntingLeaderTrainingEventsOverridden) {
        this.mooselikeHuntingLeaderTrainingEventsOverridden = mooselikeHuntingLeaderTrainingEventsOverridden;
    }

    public boolean isMooselikeHuntingLeaderTrainingParticipantsOverridden() {
        return mooselikeHuntingLeaderTrainingParticipantsOverridden;
    }

    public void setMooselikeHuntingLeaderTrainingParticipantsOverridden(final boolean mooselikeHuntingLeaderTrainingParticipantsOverridden) {
        this.mooselikeHuntingLeaderTrainingParticipantsOverridden = mooselikeHuntingLeaderTrainingParticipantsOverridden;
    }

    public boolean isCarnivoreHuntingTrainingEventsOverridden() {
        return carnivoreHuntingTrainingEventsOverridden;
    }

    public void setCarnivoreHuntingTrainingEventsOverridden(final boolean carnivoreHuntingTrainingEventsOverridden) {
        this.carnivoreHuntingTrainingEventsOverridden = carnivoreHuntingTrainingEventsOverridden;
    }

    public boolean isCarnivoreHuntingTrainingParticipantsOverridden() {
        return carnivoreHuntingTrainingParticipantsOverridden;
    }

    public void setCarnivoreHuntingTrainingParticipantsOverridden(final boolean carnivoreHuntingTrainingParticipantsOverridden) {
        this.carnivoreHuntingTrainingParticipantsOverridden = carnivoreHuntingTrainingParticipantsOverridden;
    }

    public boolean isCarnivoreHuntingLeaderTrainingEventsOverridden() {
        return carnivoreHuntingLeaderTrainingEventsOverridden;
    }

    public void setCarnivoreHuntingLeaderTrainingEventsOverridden(final boolean carnivoreHuntingLeaderTrainingEventsOverridden) {
        this.carnivoreHuntingLeaderTrainingEventsOverridden = carnivoreHuntingLeaderTrainingEventsOverridden;
    }

    public boolean isCarnivoreHuntingLeaderTrainingParticipantsOverridden() {
        return carnivoreHuntingLeaderTrainingParticipantsOverridden;
    }

    public void setCarnivoreHuntingLeaderTrainingParticipantsOverridden(final boolean carnivoreHuntingLeaderTrainingParticipantsOverridden) {
        this.carnivoreHuntingLeaderTrainingParticipantsOverridden = carnivoreHuntingLeaderTrainingParticipantsOverridden;
    }

    public boolean isSrvaTrainingEventsOverridden() {
        return srvaTrainingEventsOverridden;
    }

    public void setSrvaTrainingEventsOverridden(final boolean srvaTrainingEventsOverridden) {
        this.srvaTrainingEventsOverridden = srvaTrainingEventsOverridden;
    }

    public boolean isSrvaTrainingParticipantsOverridden() {
        return srvaTrainingParticipantsOverridden;
    }

    public void setSrvaTrainingParticipantsOverridden(final boolean srvaTrainingParticipantsOverridden) {
        this.srvaTrainingParticipantsOverridden = srvaTrainingParticipantsOverridden;
    }

    public boolean isCarnivoreContactPersonTrainingEventsOverridden() {
        return carnivoreContactPersonTrainingEventsOverridden;
    }

    public void setCarnivoreContactPersonTrainingEventsOverridden(final boolean carnivoreContactPersonTrainingEventsOverridden) {
        this.carnivoreContactPersonTrainingEventsOverridden = carnivoreContactPersonTrainingEventsOverridden;
    }

    public boolean isCarnivoreContactPersonTrainingParticipantsOverridden() {
        return carnivoreContactPersonTrainingParticipantsOverridden;
    }

    public void setCarnivoreContactPersonTrainingParticipantsOverridden(final boolean carnivoreContactPersonTrainingParticipantsOverridden) {
        this.carnivoreContactPersonTrainingParticipantsOverridden = carnivoreContactPersonTrainingParticipantsOverridden;
    }

    public boolean isAccidentPreventionTrainingEventsOverridden() {
        return accidentPreventionTrainingEventsOverridden;
    }

    public void setAccidentPreventionTrainingEventsOverridden(final boolean accidentPreventionTrainingEventsOverridden) {
        this.accidentPreventionTrainingEventsOverridden = accidentPreventionTrainingEventsOverridden;
    }

    public boolean isAccidentPreventionTrainingParticipantsOverridden() {
        return accidentPreventionTrainingParticipantsOverridden;
    }

    public void setAccidentPreventionTrainingParticipantsOverridden(final boolean accidentPreventionTrainingParticipantsOverridden) {
        this.accidentPreventionTrainingParticipantsOverridden = accidentPreventionTrainingParticipantsOverridden;
    }
}
