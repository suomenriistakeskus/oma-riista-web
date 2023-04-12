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

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.ACCIDENT_PREVENTION_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.MOOSELIKE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_ACCIDENT_PREVENTION_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SRVA_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SRVA_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.HUNTER_TRAINING_STATISTICS;
import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class HunterTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsFieldsetParticipants,
        AnnualStatisticsManuallyEditableFields<HunterTrainingStatistics>,
        Serializable {

    public static HunterTrainingStatistics reduce(@Nullable final HunterTrainingStatistics a,
                                                  @Nullable final HunterTrainingStatistics b) {

        final HunterTrainingStatistics result = new HunterTrainingStatistics();
        result.setMooselikeHuntingTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getMooselikeHuntingTrainingEvents));
        result.setMooselikeHuntingTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getMooselikeHuntingTrainingParticipants));
        result.setMooselikeHuntingLeaderTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getMooselikeHuntingLeaderTrainingEvents));
        result.setMooselikeHuntingLeaderTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getMooselikeHuntingLeaderTrainingParticipants));
        result.setCarnivoreHuntingTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getCarnivoreHuntingTrainingEvents));
        result.setCarnivoreHuntingTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getCarnivoreHuntingTrainingParticipants));
        result.setCarnivoreHuntingLeaderTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getCarnivoreHuntingLeaderTrainingEvents));
        result.setCarnivoreHuntingLeaderTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getCarnivoreHuntingLeaderTrainingParticipants));
        result.setSrvaTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getSrvaTrainingEvents));
        result.setSrvaTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getSrvaTrainingParticipants));
        result.setCarnivoreContactPersonTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getCarnivoreContactPersonTrainingEvents));
        result.setCarnivoreContactPersonTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getCarnivoreContactPersonTrainingParticipants));
        result.setAccidentPreventionTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getAccidentPreventionTrainingEvents));
        result.setAccidentPreventionTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getAccidentPreventionTrainingParticipants));

        result.setNonSubsidizableMooselikeHuntingTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableMooselikeHuntingTrainingEvents));
        result.setNonSubsidizableMooselikeHuntingTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableMooselikeHuntingTrainingParticipants));
        result.setNonSubsidizableMooselikeHuntingLeaderTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableMooselikeHuntingLeaderTrainingEvents));
        result.setNonSubsidizableMooselikeHuntingLeaderTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableMooselikeHuntingLeaderTrainingParticipants));
        result.setNonSubsidizableCarnivoreHuntingTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableCarnivoreHuntingTrainingEvents));
        result.setNonSubsidizableCarnivoreHuntingTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableCarnivoreHuntingTrainingParticipants));
        result.setNonSubsidizableCarnivoreHuntingLeaderTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableCarnivoreHuntingLeaderTrainingEvents));
        result.setNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants));
        result.setNonSubsidizableSrvaTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableSrvaTrainingEvents));
        result.setNonSubsidizableSrvaTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableSrvaTrainingParticipants));
        result.setNonSubsidizableCarnivoreContactPersonTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableCarnivoreContactPersonTrainingEvents));
        result.setNonSubsidizableCarnivoreContactPersonTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableCarnivoreContactPersonTrainingParticipants));
        result.setNonSubsidizableAccidentPreventionTrainingEvents(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableAccidentPreventionTrainingEvents));
        result.setNonSubsidizableAccidentPreventionTrainingParticipants(nullableIntSum(a, b, HunterTrainingStatistics::getNonSubsidizableAccidentPreventionTrainingParticipants));

        result.setLastModified(nullsafeMax(a, b, HunterTrainingStatistics::getLastModified));
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

    @Min(0)
    @Column(name = "non_subsidizable_mooselike_hunting_training_events")
    private Integer nonSubsidizableMooselikeHuntingTrainingEvents;

    @Column(name = "mooselike_hunting_training_events_overridden", nullable = false)
    private boolean mooselikeHuntingTrainingEventsOverridden;

    // Hirvieläinten metsästäjät -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_training_participants")
    private Integer mooselikeHuntingTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_mooselike_hunting_training_participants")
    private Integer nonSubsidizableMooselikeHuntingTrainingParticipants;

    @Column(name = "mooselike_hunting_training_participants_overridden", nullable = false)
    private boolean mooselikeHuntingTrainingParticipantsOverridden;

    // Hirvieläinten metsästäjät -johtajakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_leader_training_events")
    private Integer mooselikeHuntingLeaderTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_mooselike_hunting_leader_training_events")
    private Integer nonSubsidizableMooselikeHuntingLeaderTrainingEvents;

    @Column(name = "mooselike_hunting_leader_training_events_overridden", nullable = false)
    private boolean mooselikeHuntingLeaderTrainingEventsOverridden;

    // Hirvieläinten metsästäjät -johtajakoulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "mooselike_hunting_leader_training_participants")
    private Integer mooselikeHuntingLeaderTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_mooselike_hunting_leader_training_participants")
    private Integer nonSubsidizableMooselikeHuntingLeaderTrainingParticipants;

    @Column(name = "mooselike_hunting_leader_training_participants_overridden", nullable = false)
    private boolean mooselikeHuntingLeaderTrainingParticipantsOverridden;

    // Suurpetojen metsästäjät -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "carnivore_hunting_training_events")
    private Integer carnivoreHuntingTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_carnivore_hunting_training_events")
    private Integer nonSubsidizableCarnivoreHuntingTrainingEvents;

    @Column(name = "carnivore_hunting_training_events_overridden", nullable = false)
    private boolean carnivoreHuntingTrainingEventsOverridden;

    // Suurpetojen metsästäjät -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "carnivore_hunting_training_participants")
    private Integer carnivoreHuntingTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_carnivore_hunting_training_participants")
    private Integer nonSubsidizableCarnivoreHuntingTrainingParticipants;

    @Column(name = "carnivore_hunting_training_participants_overridden", nullable = false)
    private boolean carnivoreHuntingTrainingParticipantsOverridden;

    // Suurpetojen metsästäjät -johtajakoulutustilaisuudet, kpl
    @Min(0)
    @Column(name = "carnivore_hunting_leader_training_events")
    private Integer carnivoreHuntingLeaderTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_carnivore_hunting_leader_training_events")
    private Integer nonSubsidizableCarnivoreHuntingLeaderTrainingEvents;

    @Column(name = "carnivore_hunting_leader_training_events_overridden", nullable = false)
    private boolean carnivoreHuntingLeaderTrainingEventsOverridden;

    // Suurpetojen metsästäjät -johtajakoulutukseen osallistuneet, kpl
    @Min(0)
    @Column(name = "carnivore_hunting_leader_training_participants")
    private Integer carnivoreHuntingLeaderTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_carnivore_hunting_leader_training_participants")
    private Integer nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants;

    @Column(name = "carnivore_hunting_leader_training_participants_overridden", nullable = false)
    private boolean carnivoreHuntingLeaderTrainingParticipantsOverridden;

    // SRVA-koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "srva_training_events")
    private Integer srvaTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_srva_training_events")
    private Integer nonSubsidizableSrvaTrainingEvents;

    @Column(name = "srva_training_events_overridden", nullable = false)
    private boolean srvaTrainingEventsOverridden;

    // SRVA-koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "srva_training_participants")
    private Integer srvaTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_srva_training_participants")
    private Integer nonSubsidizableSrvaTrainingParticipants;

    @Column(name = "srva_training_participants_overridden", nullable = false)
    private boolean srvaTrainingParticipantsOverridden;

    // Petoyhdyshenkilökoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "carnivore_contact_person_training_events")
    private Integer carnivoreContactPersonTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_carnivore_contact_person_training_events")
    private Integer nonSubsidizableCarnivoreContactPersonTrainingEvents;

    @Column(name = "carnivore_contact_person_training_events_overridden", nullable = false)
    private boolean carnivoreContactPersonTrainingEventsOverridden;

    // Petoyhdyshenkilökoulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "carnivore_contact_person_training_participants")
    private Integer carnivoreContactPersonTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_carnivore_contact_person_training_participants")
    private Integer nonSubsidizableCarnivoreContactPersonTrainingParticipants;

    @Column(name = "carnivore_contact_person_training_participants_overridden", nullable = false)
    private boolean carnivoreContactPersonTrainingParticipantsOverridden;

    // Vahinkojen ennaltaehkäisy -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "accident_prevention_training_events")
    private Integer accidentPreventionTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_accident_prevention_training_events")
    private Integer nonSubsidizableAccidentPreventionTrainingEvents;

    @Column(name = "accident_prevention_training_events_overridden", nullable = false)
    private boolean accidentPreventionTrainingEventsOverridden;

    // Vahinkojen ennaltaehkäisy -koulutusten osallistujat, lkm
    @Min(0)
    @Column(name = "accident_prevention_training_participants")
    private Integer accidentPreventionTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_accident_prevention_training_participants")
    private Integer nonSubsidizableAccidentPreventionTrainingParticipants;

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
        this.nonSubsidizableMooselikeHuntingTrainingEvents = that.nonSubsidizableMooselikeHuntingTrainingEvents;
        this.mooselikeHuntingTrainingEventsOverridden = that.mooselikeHuntingTrainingEventsOverridden;

        this.mooselikeHuntingTrainingParticipants = that.mooselikeHuntingTrainingParticipants;
        this.nonSubsidizableMooselikeHuntingTrainingParticipants = that.nonSubsidizableMooselikeHuntingTrainingParticipants;
        this.mooselikeHuntingTrainingParticipantsOverridden = that.mooselikeHuntingTrainingParticipantsOverridden;

        this.mooselikeHuntingLeaderTrainingEvents = that.mooselikeHuntingLeaderTrainingEvents;
        this.nonSubsidizableMooselikeHuntingLeaderTrainingEvents = that.nonSubsidizableMooselikeHuntingLeaderTrainingEvents;
        this.mooselikeHuntingLeaderTrainingEventsOverridden = that.mooselikeHuntingLeaderTrainingEventsOverridden;

        this.mooselikeHuntingLeaderTrainingParticipants = that.mooselikeHuntingLeaderTrainingParticipants;
        this.nonSubsidizableMooselikeHuntingLeaderTrainingParticipants = that.nonSubsidizableMooselikeHuntingLeaderTrainingParticipants;
        this.mooselikeHuntingLeaderTrainingParticipantsOverridden = that.mooselikeHuntingLeaderTrainingParticipantsOverridden;

        this.carnivoreHuntingTrainingEvents = that.carnivoreHuntingTrainingEvents;
        this.nonSubsidizableCarnivoreHuntingTrainingEvents = that.nonSubsidizableCarnivoreHuntingTrainingEvents;
        this.carnivoreHuntingTrainingEventsOverridden = that.carnivoreHuntingTrainingEventsOverridden;

        this.carnivoreHuntingTrainingParticipants = that.carnivoreHuntingTrainingParticipants;
        this.nonSubsidizableCarnivoreHuntingTrainingParticipants = that.nonSubsidizableCarnivoreHuntingTrainingParticipants;
        this.carnivoreHuntingTrainingParticipantsOverridden = that.carnivoreHuntingTrainingParticipantsOverridden;

        this.carnivoreHuntingLeaderTrainingEvents = that.carnivoreHuntingLeaderTrainingEvents;
        this.nonSubsidizableCarnivoreHuntingLeaderTrainingEvents = that.nonSubsidizableCarnivoreHuntingLeaderTrainingEvents;
        this.carnivoreHuntingLeaderTrainingEventsOverridden = that.carnivoreHuntingLeaderTrainingEventsOverridden;

        this.carnivoreHuntingLeaderTrainingParticipants = that.carnivoreHuntingLeaderTrainingParticipants;
        this.nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants = that.nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants;
        this.carnivoreHuntingLeaderTrainingParticipantsOverridden = that.carnivoreHuntingLeaderTrainingParticipantsOverridden;

        this.srvaTrainingEvents = that.srvaTrainingEvents;
        this.nonSubsidizableSrvaTrainingEvents = that.nonSubsidizableSrvaTrainingEvents;
        this.srvaTrainingEventsOverridden = that.srvaTrainingEventsOverridden;

        this.srvaTrainingParticipants = that.srvaTrainingParticipants;
        this.nonSubsidizableSrvaTrainingParticipants = that.nonSubsidizableSrvaTrainingParticipants;
        this.srvaTrainingParticipantsOverridden = that.srvaTrainingParticipantsOverridden;

        this.carnivoreContactPersonTrainingEvents = that.carnivoreContactPersonTrainingEvents;
        this.nonSubsidizableCarnivoreContactPersonTrainingEvents = that.nonSubsidizableCarnivoreContactPersonTrainingEvents;
        this.carnivoreContactPersonTrainingEventsOverridden = that.carnivoreContactPersonTrainingEventsOverridden;

        this.carnivoreContactPersonTrainingParticipants = that.carnivoreContactPersonTrainingParticipants;
        this.nonSubsidizableCarnivoreContactPersonTrainingParticipants = that.nonSubsidizableCarnivoreContactPersonTrainingParticipants;
        this.carnivoreContactPersonTrainingParticipantsOverridden = that.carnivoreContactPersonTrainingParticipantsOverridden;

        this.accidentPreventionTrainingEvents = that.accidentPreventionTrainingEvents;
        this.nonSubsidizableAccidentPreventionTrainingEvents = that.nonSubsidizableAccidentPreventionTrainingEvents;
        this.accidentPreventionTrainingEventsOverridden = that.accidentPreventionTrainingEventsOverridden;

        this.accidentPreventionTrainingParticipants = that.accidentPreventionTrainingParticipants;
        this.nonSubsidizableAccidentPreventionTrainingParticipants = that.nonSubsidizableAccidentPreventionTrainingParticipants;
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
                Objects.equals(nonSubsidizableMooselikeHuntingTrainingEvents, that.nonSubsidizableMooselikeHuntingTrainingEvents) &&
                Objects.equals(nonSubsidizableMooselikeHuntingTrainingParticipants, that.nonSubsidizableMooselikeHuntingTrainingParticipants) &&

                Objects.equals(mooselikeHuntingLeaderTrainingEvents, that.mooselikeHuntingLeaderTrainingEvents) &&
                Objects.equals(mooselikeHuntingLeaderTrainingParticipants, that.mooselikeHuntingLeaderTrainingParticipants) &&
                Objects.equals(nonSubsidizableMooselikeHuntingLeaderTrainingEvents, that.nonSubsidizableMooselikeHuntingLeaderTrainingEvents) &&
                Objects.equals(nonSubsidizableMooselikeHuntingLeaderTrainingParticipants, that.nonSubsidizableMooselikeHuntingLeaderTrainingParticipants) &&

                Objects.equals(carnivoreHuntingTrainingEvents, that.carnivoreHuntingTrainingEvents) &&
                Objects.equals(carnivoreHuntingTrainingParticipants, that.carnivoreHuntingTrainingParticipants) &&
                Objects.equals(nonSubsidizableCarnivoreHuntingTrainingEvents, that.nonSubsidizableCarnivoreHuntingTrainingEvents) &&
                Objects.equals(nonSubsidizableCarnivoreHuntingTrainingParticipants, that.nonSubsidizableCarnivoreHuntingTrainingParticipants) &&

                Objects.equals(carnivoreHuntingLeaderTrainingEvents, that.carnivoreHuntingLeaderTrainingEvents) &&
                Objects.equals(carnivoreHuntingLeaderTrainingParticipants, that.carnivoreHuntingLeaderTrainingParticipants) &&
                Objects.equals(nonSubsidizableCarnivoreHuntingLeaderTrainingEvents, that.nonSubsidizableCarnivoreHuntingLeaderTrainingEvents) &&
                Objects.equals(nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants, that.nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants) &&

                Objects.equals(srvaTrainingEvents, that.srvaTrainingEvents) &&
                Objects.equals(srvaTrainingParticipants, that.srvaTrainingParticipants) &&
                Objects.equals(nonSubsidizableSrvaTrainingEvents, that.nonSubsidizableSrvaTrainingEvents) &&
                Objects.equals(nonSubsidizableSrvaTrainingParticipants, that.nonSubsidizableSrvaTrainingParticipants) &&

                Objects.equals(carnivoreContactPersonTrainingEvents, that.carnivoreContactPersonTrainingEvents) &&
                Objects.equals(carnivoreContactPersonTrainingParticipants, that.carnivoreContactPersonTrainingParticipants) &&
                Objects.equals(nonSubsidizableCarnivoreContactPersonTrainingEvents, that.nonSubsidizableCarnivoreContactPersonTrainingEvents) &&
                Objects.equals(nonSubsidizableCarnivoreContactPersonTrainingParticipants, that.nonSubsidizableCarnivoreContactPersonTrainingParticipants) &&

                Objects.equals(accidentPreventionTrainingEvents, that.accidentPreventionTrainingEvents) &&
                Objects.equals(accidentPreventionTrainingParticipants, that.accidentPreventionTrainingParticipants) &&
                Objects.equals(nonSubsidizableAccidentPreventionTrainingEvents, that.nonSubsidizableAccidentPreventionTrainingEvents) &&
                Objects.equals(nonSubsidizableAccidentPreventionTrainingParticipants, that.nonSubsidizableAccidentPreventionTrainingParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final HunterTrainingStatistics that) {
        // Includes manually updateable fields only.

        if (!Objects.equals(this.mooselikeHuntingTrainingEvents, that.mooselikeHuntingTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableMooselikeHuntingTrainingEvents, that.nonSubsidizableMooselikeHuntingTrainingEvents)) {
            this.mooselikeHuntingTrainingEventsOverridden = true;
        }
        this.mooselikeHuntingTrainingEvents = that.mooselikeHuntingTrainingEvents;
        this.nonSubsidizableMooselikeHuntingTrainingEvents = that.nonSubsidizableMooselikeHuntingTrainingEvents;

        if (!Objects.equals(this.mooselikeHuntingTrainingParticipants, that.mooselikeHuntingTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableMooselikeHuntingTrainingParticipants, that.nonSubsidizableMooselikeHuntingTrainingParticipants)) {
            this.mooselikeHuntingTrainingParticipantsOverridden = true;
        }
        this.mooselikeHuntingTrainingParticipants = that.mooselikeHuntingTrainingParticipants;
        this.nonSubsidizableMooselikeHuntingTrainingParticipants = that.nonSubsidizableMooselikeHuntingTrainingParticipants;

        if (!Objects.equals(this.mooselikeHuntingLeaderTrainingEvents, that.mooselikeHuntingLeaderTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableMooselikeHuntingLeaderTrainingEvents, that.nonSubsidizableMooselikeHuntingLeaderTrainingEvents)) {
            this.mooselikeHuntingLeaderTrainingEventsOverridden = true;
        }
        this.mooselikeHuntingLeaderTrainingEvents = that.mooselikeHuntingLeaderTrainingEvents;
        this.nonSubsidizableMooselikeHuntingLeaderTrainingEvents = that.nonSubsidizableMooselikeHuntingLeaderTrainingEvents;

        if (!Objects.equals(this.mooselikeHuntingLeaderTrainingParticipants, that.mooselikeHuntingLeaderTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableMooselikeHuntingLeaderTrainingParticipants, that.nonSubsidizableMooselikeHuntingLeaderTrainingParticipants)) {
            this.mooselikeHuntingLeaderTrainingParticipantsOverridden = true;
        }
        this.mooselikeHuntingLeaderTrainingParticipants = that.mooselikeHuntingLeaderTrainingParticipants;
        this.nonSubsidizableMooselikeHuntingLeaderTrainingParticipants = that.nonSubsidizableMooselikeHuntingLeaderTrainingParticipants;

        if (!Objects.equals(this.carnivoreHuntingTrainingEvents, that.carnivoreHuntingTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableCarnivoreHuntingTrainingEvents, that.nonSubsidizableCarnivoreHuntingTrainingEvents)) {
            this.carnivoreHuntingTrainingEventsOverridden = true;
        }
        this.carnivoreHuntingTrainingEvents = that.carnivoreHuntingTrainingEvents;
        this.nonSubsidizableCarnivoreHuntingTrainingEvents = that.nonSubsidizableCarnivoreHuntingTrainingEvents;

        if (!Objects.equals(this.carnivoreHuntingTrainingParticipants, that.carnivoreHuntingTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableCarnivoreHuntingTrainingParticipants, that.nonSubsidizableCarnivoreHuntingTrainingParticipants)) {
            this.carnivoreHuntingTrainingParticipantsOverridden = true;
        }
        this.carnivoreHuntingTrainingParticipants = that.carnivoreHuntingTrainingParticipants;
        this.nonSubsidizableCarnivoreHuntingTrainingParticipants = that.nonSubsidizableCarnivoreHuntingTrainingParticipants;

        if (!Objects.equals(this.carnivoreHuntingLeaderTrainingEvents, that.carnivoreHuntingLeaderTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableCarnivoreHuntingLeaderTrainingEvents, that.nonSubsidizableCarnivoreHuntingLeaderTrainingEvents)) {
            this.carnivoreHuntingLeaderTrainingEventsOverridden = true;
        }
        this.carnivoreHuntingLeaderTrainingEvents = that.carnivoreHuntingLeaderTrainingEvents;
        this.nonSubsidizableCarnivoreHuntingLeaderTrainingEvents = that.nonSubsidizableCarnivoreHuntingLeaderTrainingEvents;

        if (!Objects.equals(this.carnivoreHuntingLeaderTrainingParticipants, that.carnivoreHuntingLeaderTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants, that.nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants)) {
            this.carnivoreHuntingLeaderTrainingParticipantsOverridden = true;
        }
        this.carnivoreHuntingLeaderTrainingParticipants = that.carnivoreHuntingLeaderTrainingParticipants;
        this.nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants = that.nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants;

        if (!Objects.equals(this.srvaTrainingEvents, that.srvaTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableSrvaTrainingEvents, that.nonSubsidizableSrvaTrainingEvents)) {
            this.srvaTrainingEventsOverridden = true;
        }
        this.srvaTrainingEvents = that.srvaTrainingEvents;
        this.nonSubsidizableSrvaTrainingEvents = that.nonSubsidizableSrvaTrainingEvents;

        if (!Objects.equals(this.srvaTrainingParticipants, that.srvaTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableSrvaTrainingParticipants, that.nonSubsidizableSrvaTrainingParticipants)) {
            this.srvaTrainingParticipantsOverridden = true;
        }
        this.srvaTrainingParticipants = that.srvaTrainingParticipants;
        this.nonSubsidizableSrvaTrainingParticipants = that.nonSubsidizableSrvaTrainingParticipants;

        if (!Objects.equals(this.carnivoreContactPersonTrainingEvents, that.carnivoreContactPersonTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableCarnivoreContactPersonTrainingEvents, that.nonSubsidizableCarnivoreContactPersonTrainingEvents)) {
            this.carnivoreContactPersonTrainingEventsOverridden = true;
        }
        this.carnivoreContactPersonTrainingEvents = that.carnivoreContactPersonTrainingEvents;
        this.nonSubsidizableCarnivoreContactPersonTrainingEvents = that.nonSubsidizableCarnivoreContactPersonTrainingEvents;

        if (!Objects.equals(this.carnivoreContactPersonTrainingParticipants, that.carnivoreContactPersonTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableCarnivoreContactPersonTrainingParticipants, that.nonSubsidizableCarnivoreContactPersonTrainingParticipants)) {
            this.carnivoreContactPersonTrainingParticipantsOverridden = true;
        }
        this.carnivoreContactPersonTrainingParticipants = that.carnivoreContactPersonTrainingParticipants;
        this.nonSubsidizableCarnivoreContactPersonTrainingParticipants = that.nonSubsidizableCarnivoreContactPersonTrainingParticipants;

        if (!Objects.equals(this.accidentPreventionTrainingEvents, that.accidentPreventionTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableAccidentPreventionTrainingEvents, that.nonSubsidizableAccidentPreventionTrainingEvents)) {
            this.accidentPreventionTrainingEventsOverridden = true;
        }
        this.accidentPreventionTrainingEvents = that.accidentPreventionTrainingEvents;
        this.nonSubsidizableAccidentPreventionTrainingEvents = that.nonSubsidizableAccidentPreventionTrainingEvents;

        if (!Objects.equals(this.accidentPreventionTrainingParticipants, that.accidentPreventionTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableAccidentPreventionTrainingParticipants, that.nonSubsidizableAccidentPreventionTrainingParticipants)) {
            this.accidentPreventionTrainingParticipantsOverridden = true;
        }
        this.accidentPreventionTrainingParticipants = that.accidentPreventionTrainingParticipants;
        this.nonSubsidizableAccidentPreventionTrainingParticipants = that.nonSubsidizableAccidentPreventionTrainingParticipants;
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
                accidentPreventionTrainingEvents, accidentPreventionTrainingParticipants) && hasParticipants();
    }

    private boolean hasParticipants() {
        return listMissingParticipants()._2.isEmpty();
    }

    @Override
    public Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> listMissingParticipants() {
        final List<AnnualStatisticsParticipantField> missing = new ArrayList<>();
        if (mooselikeHuntingTrainingEvents != null && mooselikeHuntingTrainingEvents > 0 &&  mooselikeHuntingTrainingParticipants <= 0) {
            missing.add(MOOSELIKE_HUNTING_TRAINING_EVENTS);
        }
        if (mooselikeHuntingLeaderTrainingEvents != null && mooselikeHuntingLeaderTrainingEvents > 0 &&  mooselikeHuntingLeaderTrainingParticipants <= 0) {
            missing.add(MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS);
        }
        if (carnivoreHuntingTrainingEvents != null && carnivoreHuntingTrainingEvents > 0 &&  carnivoreHuntingTrainingParticipants <= 0) {
            missing.add(CARNIVORE_HUNTING_TRAINING_EVENTS);
        }
        if (carnivoreHuntingLeaderTrainingEvents != null && carnivoreHuntingLeaderTrainingEvents > 0 &&  carnivoreHuntingLeaderTrainingParticipants <= 0) {
            missing.add(CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS);
        }
        if (srvaTrainingEvents != null && srvaTrainingEvents > 0 &&  srvaTrainingParticipants <= 0) {
            missing.add(SRVA_TRAINING_EVENTS);
        }
        if (carnivoreContactPersonTrainingEvents != null && carnivoreContactPersonTrainingEvents > 0 &&  carnivoreContactPersonTrainingParticipants <= 0) {
            missing.add(CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS);
        }
        if (accidentPreventionTrainingEvents != null && accidentPreventionTrainingEvents > 0 &&  accidentPreventionTrainingParticipants <= 0) {
            missing.add(ACCIDENT_PREVENTION_TRAINING_EVENTS);
        }
        if (nonSubsidizableMooselikeHuntingTrainingEvents != null && nonSubsidizableMooselikeHuntingTrainingEvents > 0 &&
                nonSubsidizableMooselikeHuntingTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_TRAINING_EVENTS);
        }
        if (nonSubsidizableMooselikeHuntingLeaderTrainingEvents != null && nonSubsidizableMooselikeHuntingLeaderTrainingEvents > 0 &&
                nonSubsidizableMooselikeHuntingLeaderTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_MOOSELIKE_HUNTING_LEADER_TRAINING_EVENTS);
        }
        if (nonSubsidizableCarnivoreHuntingTrainingEvents != null && nonSubsidizableCarnivoreHuntingTrainingEvents > 0 &&
                nonSubsidizableCarnivoreHuntingTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_CARNIVORE_HUNTING_TRAINING_EVENTS);
        }
        if (nonSubsidizableCarnivoreHuntingLeaderTrainingEvents != null && nonSubsidizableCarnivoreHuntingLeaderTrainingEvents > 0 &&
                nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_CARNIVORE_HUNTING_LEADER_TRAINING_EVENTS);
        }
        if (nonSubsidizableSrvaTrainingEvents != null && nonSubsidizableSrvaTrainingEvents > 0 &&
                nonSubsidizableSrvaTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_SRVA_TRAINING_EVENTS);
        }
        if (nonSubsidizableCarnivoreContactPersonTrainingEvents != null && nonSubsidizableCarnivoreContactPersonTrainingEvents > 0 &&
                nonSubsidizableCarnivoreContactPersonTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_CARNIVORE_CONTACT_PERSON_TRAINING_EVENTS);
        }
        if (nonSubsidizableAccidentPreventionTrainingEvents != null && nonSubsidizableAccidentPreventionTrainingEvents > 0 &&
                nonSubsidizableAccidentPreventionTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_ACCIDENT_PREVENTION_TRAINING_EVENTS);
        }
        return Tuple.of(HUNTER_TRAINING_STATISTICS, missing);
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
    Integer countNonSubsidizableHunterTrainingEvents() {
        return nullableIntSum(
                nonSubsidizableMooselikeHuntingTrainingEvents, nonSubsidizableMooselikeHuntingLeaderTrainingEvents,
                nonSubsidizableCarnivoreHuntingTrainingEvents, nonSubsidizableCarnivoreHuntingLeaderTrainingEvents,
                nonSubsidizableSrvaTrainingEvents, nonSubsidizableCarnivoreContactPersonTrainingEvents,
                nonSubsidizableAccidentPreventionTrainingEvents);
    }

    @Nullable
    Integer countHunterTrainingParticipants() {
        return nullableIntSum(
                mooselikeHuntingTrainingParticipants, mooselikeHuntingLeaderTrainingParticipants,
                carnivoreHuntingTrainingParticipants, carnivoreHuntingLeaderTrainingParticipants,
                srvaTrainingParticipants, carnivoreContactPersonTrainingParticipants,
                accidentPreventionTrainingParticipants);
    }

    @Nullable
    Integer countNonSubsidizableHunterTrainingParticipants() {
        return nullableIntSum(
                nonSubsidizableMooselikeHuntingTrainingParticipants, nonSubsidizableMooselikeHuntingLeaderTrainingParticipants,
                nonSubsidizableCarnivoreHuntingTrainingParticipants, nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants,
                nonSubsidizableSrvaTrainingParticipants, nonSubsidizableCarnivoreContactPersonTrainingParticipants,
                nonSubsidizableAccidentPreventionTrainingParticipants);
    }

    // Accessors -->


    public Integer getMooselikeHuntingTrainingEvents() {
        return mooselikeHuntingTrainingEvents;
    }

    public void setMooselikeHuntingTrainingEvents(final Integer mooselikeHuntingTrainingEvents) {
        this.mooselikeHuntingTrainingEvents = mooselikeHuntingTrainingEvents;
    }

    public Integer getNonSubsidizableMooselikeHuntingTrainingEvents() {
        return nonSubsidizableMooselikeHuntingTrainingEvents;
    }

    public void setNonSubsidizableMooselikeHuntingTrainingEvents(final Integer nonSubsidizableMooselikeHuntingTrainingEvents) {
        this.nonSubsidizableMooselikeHuntingTrainingEvents = nonSubsidizableMooselikeHuntingTrainingEvents;
    }

    public Integer getMooselikeHuntingTrainingParticipants() {
        return mooselikeHuntingTrainingParticipants;
    }

    public void setMooselikeHuntingTrainingParticipants(final Integer mooselikeHuntingTrainingParticipants) {
        this.mooselikeHuntingTrainingParticipants = mooselikeHuntingTrainingParticipants;
    }

    public Integer getNonSubsidizableMooselikeHuntingTrainingParticipants() {
        return nonSubsidizableMooselikeHuntingTrainingParticipants;
    }

    public void setNonSubsidizableMooselikeHuntingTrainingParticipants(final Integer nonSubsidizableMooselikeHuntingTrainingParticipants) {
        this.nonSubsidizableMooselikeHuntingTrainingParticipants = nonSubsidizableMooselikeHuntingTrainingParticipants;
    }

    public Integer getMooselikeHuntingLeaderTrainingEvents() {
        return mooselikeHuntingLeaderTrainingEvents;
    }

    public void setMooselikeHuntingLeaderTrainingEvents(final Integer mooselikeHuntingLeaderTrainingEvents) {
        this.mooselikeHuntingLeaderTrainingEvents = mooselikeHuntingLeaderTrainingEvents;
    }

    public Integer getNonSubsidizableMooselikeHuntingLeaderTrainingEvents() {
        return nonSubsidizableMooselikeHuntingLeaderTrainingEvents;
    }

    public void setNonSubsidizableMooselikeHuntingLeaderTrainingEvents(final Integer nonSubsidizableMooselikeHuntingLeaderTrainingEvents) {
        this.nonSubsidizableMooselikeHuntingLeaderTrainingEvents = nonSubsidizableMooselikeHuntingLeaderTrainingEvents;
    }

    public Integer getMooselikeHuntingLeaderTrainingParticipants() {
        return mooselikeHuntingLeaderTrainingParticipants;
    }

    public void setMooselikeHuntingLeaderTrainingParticipants(final Integer mooselikeHuntingLeaderTrainingParticipants) {
        this.mooselikeHuntingLeaderTrainingParticipants = mooselikeHuntingLeaderTrainingParticipants;
    }

    public Integer getNonSubsidizableMooselikeHuntingLeaderTrainingParticipants() {
        return nonSubsidizableMooselikeHuntingLeaderTrainingParticipants;
    }

    public void setNonSubsidizableMooselikeHuntingLeaderTrainingParticipants(final Integer nonSubsidizableMooselikeHuntingLeaderTrainingParticipants) {
        this.nonSubsidizableMooselikeHuntingLeaderTrainingParticipants = nonSubsidizableMooselikeHuntingLeaderTrainingParticipants;
    }

    public Integer getCarnivoreHuntingTrainingEvents() {
        return carnivoreHuntingTrainingEvents;
    }

    public void setCarnivoreHuntingTrainingEvents(final Integer carnivoreHuntingTrainingEvents) {
        this.carnivoreHuntingTrainingEvents = carnivoreHuntingTrainingEvents;
    }

    public Integer getNonSubsidizableCarnivoreHuntingTrainingEvents() {
        return nonSubsidizableCarnivoreHuntingTrainingEvents;
    }

    public void setNonSubsidizableCarnivoreHuntingTrainingEvents(final Integer nonSubsidizableCarnivoreHuntingTrainingEvents) {
        this.nonSubsidizableCarnivoreHuntingTrainingEvents = nonSubsidizableCarnivoreHuntingTrainingEvents;
    }

    public Integer getCarnivoreHuntingTrainingParticipants() {
        return carnivoreHuntingTrainingParticipants;
    }

    public void setCarnivoreHuntingTrainingParticipants(final Integer carnivoreHuntingTrainingParticipants) {
        this.carnivoreHuntingTrainingParticipants = carnivoreHuntingTrainingParticipants;
    }

    public Integer getNonSubsidizableCarnivoreHuntingTrainingParticipants() {
        return nonSubsidizableCarnivoreHuntingTrainingParticipants;
    }

    public void setNonSubsidizableCarnivoreHuntingTrainingParticipants(final Integer nonSubsidizableCarnivoreHuntingTrainingParticipants) {
        this.nonSubsidizableCarnivoreHuntingTrainingParticipants = nonSubsidizableCarnivoreHuntingTrainingParticipants;
    }

    public Integer getCarnivoreHuntingLeaderTrainingEvents() {
        return carnivoreHuntingLeaderTrainingEvents;
    }

    public void setCarnivoreHuntingLeaderTrainingEvents(final Integer carnivoreHuntingLeaderTrainingEvents) {
        this.carnivoreHuntingLeaderTrainingEvents = carnivoreHuntingLeaderTrainingEvents;
    }

    public Integer getNonSubsidizableCarnivoreHuntingLeaderTrainingEvents() {
        return nonSubsidizableCarnivoreHuntingLeaderTrainingEvents;
    }

    public void setNonSubsidizableCarnivoreHuntingLeaderTrainingEvents(final Integer nonSubsidizableCarnivoreHuntingLeaderTrainingEvents) {
        this.nonSubsidizableCarnivoreHuntingLeaderTrainingEvents = nonSubsidizableCarnivoreHuntingLeaderTrainingEvents;
    }

    public Integer getCarnivoreHuntingLeaderTrainingParticipants() {
        return carnivoreHuntingLeaderTrainingParticipants;
    }

    public void setCarnivoreHuntingLeaderTrainingParticipants(final Integer carnivoreHuntingLeaderTrainingParticipants) {
        this.carnivoreHuntingLeaderTrainingParticipants = carnivoreHuntingLeaderTrainingParticipants;
    }

    public Integer getNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants() {
        return nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants;
    }

    public void setNonSubsidizableCarnivoreHuntingLeaderTrainingParticipants(final Integer nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants) {
        this.nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants = nonSubsidizableCarnivoreHuntingLeaderTrainingParticipants;
    }

    public Integer getSrvaTrainingEvents() {
        return srvaTrainingEvents;
    }

    public void setSrvaTrainingEvents(final Integer srvaTrainingEvents) {
        this.srvaTrainingEvents = srvaTrainingEvents;
    }

    public Integer getNonSubsidizableSrvaTrainingEvents() {
        return nonSubsidizableSrvaTrainingEvents;
    }

    public void setNonSubsidizableSrvaTrainingEvents(final Integer nonSubsidizableSrvaTrainingEvents) {
        this.nonSubsidizableSrvaTrainingEvents = nonSubsidizableSrvaTrainingEvents;
    }

    public Integer getSrvaTrainingParticipants() {
        return srvaTrainingParticipants;
    }

    public void setSrvaTrainingParticipants(final Integer srvaTrainingParticipants) {
        this.srvaTrainingParticipants = srvaTrainingParticipants;
    }

    public Integer getNonSubsidizableSrvaTrainingParticipants() {
        return nonSubsidizableSrvaTrainingParticipants;
    }

    public void setNonSubsidizableSrvaTrainingParticipants(final Integer nonSubsidizableSrvaTrainingParticipants) {
        this.nonSubsidizableSrvaTrainingParticipants = nonSubsidizableSrvaTrainingParticipants;
    }

    public Integer getCarnivoreContactPersonTrainingEvents() {
        return carnivoreContactPersonTrainingEvents;
    }

    public void setCarnivoreContactPersonTrainingEvents(final Integer carnivoreContactPersonTrainingEvents) {
        this.carnivoreContactPersonTrainingEvents = carnivoreContactPersonTrainingEvents;
    }

    public Integer getNonSubsidizableCarnivoreContactPersonTrainingEvents() {
        return nonSubsidizableCarnivoreContactPersonTrainingEvents;
    }

    public void setNonSubsidizableCarnivoreContactPersonTrainingEvents(final Integer nonSubsidizableCarnivoreContactPersonTrainingEvents) {
        this.nonSubsidizableCarnivoreContactPersonTrainingEvents = nonSubsidizableCarnivoreContactPersonTrainingEvents;
    }

    public Integer getCarnivoreContactPersonTrainingParticipants() {
        return carnivoreContactPersonTrainingParticipants;
    }

    public void setCarnivoreContactPersonTrainingParticipants(final Integer carnivoreContactPersonTrainingParticipants) {
        this.carnivoreContactPersonTrainingParticipants = carnivoreContactPersonTrainingParticipants;
    }

    public Integer getNonSubsidizableCarnivoreContactPersonTrainingParticipants() {
        return nonSubsidizableCarnivoreContactPersonTrainingParticipants;
    }

    public void setNonSubsidizableCarnivoreContactPersonTrainingParticipants(final Integer nonSubsidizableCarnivoreContactPersonTrainingParticipants) {
        this.nonSubsidizableCarnivoreContactPersonTrainingParticipants = nonSubsidizableCarnivoreContactPersonTrainingParticipants;
    }

    public Integer getAccidentPreventionTrainingEvents() {
        return accidentPreventionTrainingEvents;
    }

    public void setAccidentPreventionTrainingEvents(final Integer accidentPreventionTrainingEvents) {
        this.accidentPreventionTrainingEvents = accidentPreventionTrainingEvents;
    }

    public Integer getNonSubsidizableAccidentPreventionTrainingEvents() {
        return nonSubsidizableAccidentPreventionTrainingEvents;
    }

    public void setNonSubsidizableAccidentPreventionTrainingEvents(final Integer nonSubsidizableAccidentPreventionTrainingEvents) {
        this.nonSubsidizableAccidentPreventionTrainingEvents = nonSubsidizableAccidentPreventionTrainingEvents;
    }

    public Integer getAccidentPreventionTrainingParticipants() {
        return accidentPreventionTrainingParticipants;
    }

    public void setAccidentPreventionTrainingParticipants(final Integer accidentPreventionTrainingParticipants) {
        this.accidentPreventionTrainingParticipants = accidentPreventionTrainingParticipants;
    }

    public Integer getNonSubsidizableAccidentPreventionTrainingParticipants() {
        return nonSubsidizableAccidentPreventionTrainingParticipants;
    }

    public void setNonSubsidizableAccidentPreventionTrainingParticipants(final Integer nonSubsidizableAccidentPreventionTrainingParticipants) {
        this.nonSubsidizableAccidentPreventionTrainingParticipants = nonSubsidizableAccidentPreventionTrainingParticipants;
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
