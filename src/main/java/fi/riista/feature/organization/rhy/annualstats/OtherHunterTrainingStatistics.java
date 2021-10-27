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
public class OtherHunterTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsManuallyEditableFields<OtherHunterTrainingStatistics>,
        Serializable {

    public static final OtherHunterTrainingStatistics reduce(@Nullable final OtherHunterTrainingStatistics a,
                                                             @Nullable final OtherHunterTrainingStatistics b) {

        final OtherHunterTrainingStatistics result = new OtherHunterTrainingStatistics();
        result.setSmallCarnivoreHuntingTrainingEvents(nullableIntSum(a, b, s -> s.getSmallCarnivoreHuntingTrainingEvents()));
        result.setSmallCarnivoreHuntingTrainingParticipants(nullableIntSum(a, b, s -> s.getSmallCarnivoreHuntingTrainingParticipants()));
        result.setGameCountingTrainingEvents(nullableIntSum(a, b, s -> s.getGameCountingTrainingEvents()));
        result.setGameCountingTrainingParticipants(nullableIntSum(a, b, s -> s.getGameCountingTrainingParticipants()));
        result.setGamePopulationManagementTrainingEvents(nullableIntSum(a, b, s -> s.getGamePopulationManagementTrainingEvents()));
        result.setGamePopulationManagementTrainingParticipants(nullableIntSum(a, b, s -> s.getGamePopulationManagementTrainingParticipants()));
        result.setGameEnvironmentalCareTrainingEvents(nullableIntSum(a, b, s -> s.getGameEnvironmentalCareTrainingEvents()));
        result.setGameEnvironmentalCareTrainingParticipants(nullableIntSum(a, b, s -> s.getGameEnvironmentalCareTrainingParticipants()));
        result.setOtherGamekeepingTrainingEvents(nullableIntSum(a, b, s -> s.getOtherGamekeepingTrainingEvents()));
        result.setOtherGamekeepingTrainingParticipants(nullableIntSum(a, b, s -> s.getOtherGamekeepingTrainingParticipants()));
        result.setShootingTrainingEvents(nullableIntSum(a, b, s -> s.getShootingTrainingEvents()));
        result.setShootingTrainingParticipants(nullableIntSum(a, b, s -> s.getShootingTrainingParticipants()));
        result.setTrackerTrainingEvents(nullableIntSum(a, b, s -> s.getTrackerTrainingEvents()));
        result.setTrackerTrainingParticipants(nullableIntSum(a, b, s -> s.getTrackerTrainingParticipants()));

        result.setNonSubsidizableSmallCarnivoreHuntingTrainingEvents(nullableIntSum(a, b, s -> s.getNonSubsidizableSmallCarnivoreHuntingTrainingEvents()));
        result.setNonSubsidizableSmallCarnivoreHuntingTrainingParticipants(nullableIntSum(a, b, s -> s.getNonSubsidizableSmallCarnivoreHuntingTrainingParticipants()));
        result.setNonSubsidizableGameCountingTrainingEvents(nullableIntSum(a, b, s -> s.getNonSubsidizableGameCountingTrainingEvents()));
        result.setNonSubsidizableGameCountingTrainingParticipants(nullableIntSum(a, b, s -> s.getNonSubsidizableGameCountingTrainingParticipants()));
        result.setNonSubsidizableGamePopulationManagementTrainingEvents(nullableIntSum(a, b, s -> s.getNonSubsidizableGamePopulationManagementTrainingEvents()));
        result.setNonSubsidizableGamePopulationManagementTrainingParticipants(nullableIntSum(a, b, s -> s.getNonSubsidizableGamePopulationManagementTrainingParticipants()));
        result.setNonSubsidizableGameEnvironmentalCareTrainingEvents(nullableIntSum(a, b, s -> s.getNonSubsidizableGameEnvironmentalCareTrainingEvents()));
        result.setNonSubsidizableGameEnvironmentalCareTrainingParticipants(nullableIntSum(a, b, s -> s.getNonSubsidizableGameEnvironmentalCareTrainingParticipants()));
        result.setNonSubsidizableOtherGamekeepingTrainingEvents(nullableIntSum(a, b, s -> s.getNonSubsidizableOtherGamekeepingTrainingEvents()));
        result.setNonSubsidizableOtherGamekeepingTrainingParticipants(nullableIntSum(a, b, s -> s.getNonSubsidizableOtherGamekeepingTrainingParticipants()));
        result.setNonSubsidizableShootingTrainingEvents(nullableIntSum(a, b, s -> s.getNonSubsidizableShootingTrainingEvents()));
        result.setNonSubsidizableShootingTrainingParticipants(nullableIntSum(a, b, s -> s.getNonSubsidizableShootingTrainingParticipants()));
        result.setNonSubsidizableTrackerTrainingEvents(nullableIntSum(a, b, s -> s.getNonSubsidizableTrackerTrainingEvents()));
        result.setNonSubsidizableTrackerTrainingParticipants(nullableIntSum(a, b, s -> s.getNonSubsidizableTrackerTrainingParticipants()));

        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static OtherHunterTrainingStatistics reduce(@Nonnull final Stream<OtherHunterTrainingStatistics> items) {
        requireNonNull(items);
        return items.reduce(new OtherHunterTrainingStatistics(), OtherHunterTrainingStatistics::reduce);
    }

    public static <T> OtherHunterTrainingStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                           @Nonnull final Function<? super T, OtherHunterTrainingStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Pienpetojen pyynti -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "small_carnivore_hunting_training_events")
    private Integer smallCarnivoreHuntingTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_small_carnivore_hunting_training_events")
    private Integer nonSubsidizableSmallCarnivoreHuntingTrainingEvents;

    @Column(name = "small_carnivore_hunting_training_events_overridden", nullable = false)
    private boolean smallCarnivoreHuntingTrainingEventsOverridden;

    // Pienpetojen pyynti -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "small_carnivore_hunting_training_participants")
    private Integer smallCarnivoreHuntingTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_small_carnivore_hunting_training_participants")
    private Integer nonSubsidizableSmallCarnivoreHuntingTrainingParticipants;

    @Column(name = "small_carnivore_hunting_training_participants_overridden", nullable = false)
    private boolean smallCarnivoreHuntingTrainingParticipantsOverridden;

    // Riistalaskentakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "game_counting_training_events")
    private Integer gameCountingTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_game_counting_training_events")
    private Integer nonSubsidizableGameCountingTrainingEvents;

    @Column(name = "game_counting_training_events_overridden", nullable = false)
    private boolean gameCountingTrainingEventsOverridden;

    // Riistalaskentakoulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "game_counting_training_participants")
    private Integer gameCountingTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_game_counting_training_participants")
    private Integer nonSubsidizableGameCountingTrainingParticipants;

    @Column(name = "game_counting_training_participants_overridden", nullable = false)
    private boolean gameCountingTrainingParticipantsOverridden;

    // Riistakantojen hoito -koulutustilaisuudet, kpl
    @Min(0)
    @Column(name = "game_population_management_training_events")
    private Integer gamePopulationManagementTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_game_population_management_training_events")
    private Integer nonSubsidizableGamePopulationManagementTrainingEvents;

    @Column(name = "game_population_management_training_events_overridden", nullable = false)
    private boolean gamePopulationManagementTrainingEventsOverridden;

    // Riistakantojen hoito -koulutukseen osallistuneet, kpl
    @Min(0)
    @Column(name = "game_population_management_training_participants")
    private Integer gamePopulationManagementTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_game_population_management_training_participants")
    private Integer nonSubsidizableGamePopulationManagementTrainingParticipants;

    @Column(name = "game_population_management_training_participants_overridden", nullable = false)
    private boolean gamePopulationManagementTrainingParticipantsOverridden;

    // Riistan elinympäristöjen hoito -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "game_environmental_care_training_events")
    private Integer gameEnvironmentalCareTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_game_environmental_care_training_events")
    private Integer nonSubsidizableGameEnvironmentalCareTrainingEvents;

    @Column(name = "game_environmental_care_training_events_overridden", nullable = false)
    private boolean gameEnvironmentalCareTrainingEventsOverridden;

    // Riistan elinympäristöjen hoito -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "game_environmental_care_training_participants")
    private Integer gameEnvironmentalCareTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_game_environmental_care_training_participants")
    private Integer nonSubsidizableGameEnvironmentalCareTrainingParticipants;

    @Column(name = "game_environmental_care_training_participants_overridden", nullable = false)
    private boolean gameEnvironmentalCareTrainingParticipantsOverridden;

    // Muut riistanhoidon koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "other_gamekeeping_training_events")
    private Integer otherGamekeepingTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_other_gamekeeping_training_events")
    private Integer nonSubsidizableOtherGamekeepingTrainingEvents;

    @Column(name = "other_gamekeeping_training_events_overridden", nullable = false)
    private boolean otherGamekeepingTrainingEventsOverridden;

    // Muihin riistanhoidon koulutuksiin osallistuneet, lkm
    @Min(0)
    @Column(name = "other_gamekeeping_training_participants")
    private Integer otherGamekeepingTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_other_gamekeeping_training_participants")
    private Integer nonSubsidizableOtherGamekeepingTrainingParticipants;

    @Column(name = "other_gamekeeping_training_participants_overridden", nullable = false)
    private boolean otherGamekeepingTrainingParticipantsOverridden;

    // Ampumakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "shooting_training_events")
    private Integer shootingTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_shooting_training_events")
    private Integer nonSubsidizableShootingTrainingEvents;

    @Column(name = "shooting_training_events_overridden", nullable = false)
    private boolean shootingTrainingEventsOverridden;

    // Ampumakoulutuksiin osallistuneet, lkm
    @Min(0)
    @Column(name = "shooting_training_participants")
    private Integer shootingTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_shooting_training_participants")
    private Integer nonSubsidizableShootingTrainingParticipants;

    @Column(name = "shooting_training_participants_overridden", nullable = false)
    private boolean shootingTrainingParticipantsOverridden;

    // Jäljestäjäkoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "tracker_training_events")
    private Integer trackerTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_tracker_training_events")
    private Integer nonSubsidizableTrackerTrainingEvents;

    @Column(name = "tracker_training_events_overridden", nullable = false)
    private boolean trackerTrainingEventsOverridden;

    // Jäljestäjäkoulutuksiin osallistuneet, lkm
    @Min(0)
    @Column(name = "tracker_training_participants")
    private Integer trackerTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_tracker_training_participants")
    private Integer nonSubsidizableTrackerTrainingParticipants;

    @Column(name = "tracker_training_participants_overridden", nullable = false)
    private boolean trackerTrainingParticipantsOverridden;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "other_hunter_trainings_last_modified")
    private DateTime lastModified;

    public OtherHunterTrainingStatistics() {
    }

    public OtherHunterTrainingStatistics(@Nonnull final OtherHunterTrainingStatistics that) {
        requireNonNull(that);

        this.smallCarnivoreHuntingTrainingEvents = that.smallCarnivoreHuntingTrainingEvents;
        this.nonSubsidizableSmallCarnivoreHuntingTrainingEvents = that.nonSubsidizableSmallCarnivoreHuntingTrainingEvents;
        this.smallCarnivoreHuntingTrainingEventsOverridden = that.smallCarnivoreHuntingTrainingEventsOverridden;

        this.smallCarnivoreHuntingTrainingParticipants = that.smallCarnivoreHuntingTrainingParticipants;
        this.nonSubsidizableSmallCarnivoreHuntingTrainingParticipants = that.nonSubsidizableSmallCarnivoreHuntingTrainingParticipants;
        this.smallCarnivoreHuntingTrainingParticipantsOverridden = that.smallCarnivoreHuntingTrainingParticipantsOverridden;

        this.gameCountingTrainingEvents = that.gameCountingTrainingEvents;
        this.nonSubsidizableGameCountingTrainingEvents = that.nonSubsidizableGameCountingTrainingEvents;
        this.gameCountingTrainingEventsOverridden = that.gameCountingTrainingEventsOverridden;

        this.gameCountingTrainingParticipants = that.gameCountingTrainingParticipants;
        this.nonSubsidizableGameCountingTrainingParticipants = that.nonSubsidizableGameCountingTrainingParticipants;
        this.gameCountingTrainingParticipantsOverridden = that.gameCountingTrainingParticipantsOverridden;

        this.gamePopulationManagementTrainingEvents = that.gamePopulationManagementTrainingEvents;
        this.nonSubsidizableGamePopulationManagementTrainingEvents = that.nonSubsidizableGamePopulationManagementTrainingEvents;
        this.gamePopulationManagementTrainingEventsOverridden = that.gamePopulationManagementTrainingEventsOverridden;

        this.gamePopulationManagementTrainingParticipants = that.gamePopulationManagementTrainingParticipants;
        this.nonSubsidizableGamePopulationManagementTrainingParticipants = that.nonSubsidizableGamePopulationManagementTrainingParticipants;
        this.gamePopulationManagementTrainingParticipantsOverridden = that.gamePopulationManagementTrainingParticipantsOverridden;

        this.gameEnvironmentalCareTrainingEvents = that.gameEnvironmentalCareTrainingEvents;
        this.nonSubsidizableGameEnvironmentalCareTrainingEvents = that.nonSubsidizableGameEnvironmentalCareTrainingEvents;
        this.gameEnvironmentalCareTrainingEventsOverridden = that.gameEnvironmentalCareTrainingEventsOverridden;

        this.gameEnvironmentalCareTrainingParticipants = that.gameEnvironmentalCareTrainingParticipants;
        this.nonSubsidizableGameEnvironmentalCareTrainingParticipants = that.nonSubsidizableGameEnvironmentalCareTrainingParticipants;
        this.gameEnvironmentalCareTrainingParticipantsOverridden = that.gameEnvironmentalCareTrainingParticipantsOverridden;

        this.otherGamekeepingTrainingEvents = that.otherGamekeepingTrainingEvents;
        this.nonSubsidizableOtherGamekeepingTrainingEvents = that.nonSubsidizableOtherGamekeepingTrainingEvents;
        this.otherGamekeepingTrainingEventsOverridden = that.otherGamekeepingTrainingEventsOverridden;

        this.otherGamekeepingTrainingParticipants = that.otherGamekeepingTrainingParticipants;
        this.nonSubsidizableOtherGamekeepingTrainingParticipants = that.nonSubsidizableOtherGamekeepingTrainingParticipants;
        this.otherGamekeepingTrainingParticipantsOverridden = that.otherGamekeepingTrainingParticipantsOverridden;

        this.shootingTrainingEvents = that.shootingTrainingEvents;
        this.nonSubsidizableShootingTrainingEvents = that.nonSubsidizableShootingTrainingEvents;
        this.shootingTrainingEventsOverridden = that.shootingTrainingEventsOverridden;

        this.shootingTrainingParticipants = that.shootingTrainingParticipants;
        this.nonSubsidizableShootingTrainingParticipants = that.nonSubsidizableShootingTrainingParticipants;
        this.shootingTrainingParticipantsOverridden = that.shootingTrainingParticipantsOverridden;

        this.trackerTrainingEvents = that.trackerTrainingEvents;
        this.nonSubsidizableTrackerTrainingEvents = that.nonSubsidizableTrackerTrainingEvents;
        this.trackerTrainingEventsOverridden = that.trackerTrainingEventsOverridden;

        this.trackerTrainingParticipants = that.trackerTrainingParticipants;
        this.nonSubsidizableTrackerTrainingParticipants = that.nonSubsidizableTrackerTrainingParticipants;
        this.trackerTrainingParticipantsOverridden = that.trackerTrainingParticipantsOverridden;

        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.OTHER_HUNTER_TRAINING;
    }

    @Override
    public boolean isEqualTo(@Nonnull final OtherHunterTrainingStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(smallCarnivoreHuntingTrainingEvents, that.smallCarnivoreHuntingTrainingEvents) &&
                Objects.equals(smallCarnivoreHuntingTrainingParticipants, that.smallCarnivoreHuntingTrainingParticipants) &&
                Objects.equals(nonSubsidizableSmallCarnivoreHuntingTrainingEvents, that.nonSubsidizableSmallCarnivoreHuntingTrainingEvents) &&
                Objects.equals(nonSubsidizableSmallCarnivoreHuntingTrainingParticipants, that.nonSubsidizableSmallCarnivoreHuntingTrainingParticipants) &&

                Objects.equals(gameCountingTrainingEvents, that.gameCountingTrainingEvents) &&
                Objects.equals(gameCountingTrainingParticipants, that.gameCountingTrainingParticipants) &&
                Objects.equals(nonSubsidizableGameCountingTrainingEvents, that.nonSubsidizableGameCountingTrainingEvents) &&
                Objects.equals(nonSubsidizableGameCountingTrainingParticipants, that.nonSubsidizableGameCountingTrainingParticipants) &&

                Objects.equals(gamePopulationManagementTrainingEvents, that.gamePopulationManagementTrainingEvents) &&
                Objects.equals(gamePopulationManagementTrainingParticipants, that.gamePopulationManagementTrainingParticipants) &&
                Objects.equals(nonSubsidizableGamePopulationManagementTrainingEvents, that.nonSubsidizableGamePopulationManagementTrainingEvents) &&
                Objects.equals(nonSubsidizableGamePopulationManagementTrainingParticipants, that.nonSubsidizableGamePopulationManagementTrainingParticipants) &&

                Objects.equals(gameEnvironmentalCareTrainingEvents, that.gameEnvironmentalCareTrainingEvents) &&
                Objects.equals(gameEnvironmentalCareTrainingParticipants, that.gameEnvironmentalCareTrainingParticipants) &&
                Objects.equals(nonSubsidizableGameEnvironmentalCareTrainingEvents, that.nonSubsidizableGameEnvironmentalCareTrainingEvents) &&
                Objects.equals(nonSubsidizableGameEnvironmentalCareTrainingParticipants, that.nonSubsidizableGameEnvironmentalCareTrainingParticipants) &&

                Objects.equals(otherGamekeepingTrainingEvents, that.otherGamekeepingTrainingEvents) &&
                Objects.equals(otherGamekeepingTrainingParticipants, that.otherGamekeepingTrainingParticipants) &&
                Objects.equals(nonSubsidizableOtherGamekeepingTrainingEvents, that.nonSubsidizableOtherGamekeepingTrainingEvents) &&
                Objects.equals(nonSubsidizableOtherGamekeepingTrainingParticipants, that.nonSubsidizableOtherGamekeepingTrainingParticipants) &&

                Objects.equals(shootingTrainingEvents, that.shootingTrainingEvents) &&
                Objects.equals(shootingTrainingParticipants, that.shootingTrainingParticipants) &&
                Objects.equals(nonSubsidizableShootingTrainingEvents, that.nonSubsidizableShootingTrainingEvents) &&
                Objects.equals(nonSubsidizableShootingTrainingParticipants, that.nonSubsidizableShootingTrainingParticipants) &&

                Objects.equals(trackerTrainingEvents, that.trackerTrainingEvents) &&
                Objects.equals(trackerTrainingParticipants, that.trackerTrainingParticipants) &&
                Objects.equals(nonSubsidizableTrackerTrainingEvents, that.nonSubsidizableTrackerTrainingEvents) &&
                Objects.equals(nonSubsidizableTrackerTrainingParticipants, that.nonSubsidizableTrackerTrainingParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final OtherHunterTrainingStatistics that) {
        // Includes manually updateable fields only.

        if (!Objects.equals(this.smallCarnivoreHuntingTrainingEvents, that.smallCarnivoreHuntingTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableSmallCarnivoreHuntingTrainingEvents, that.nonSubsidizableSmallCarnivoreHuntingTrainingEvents)) {
            this.smallCarnivoreHuntingTrainingEventsOverridden = true;
        }
        this.smallCarnivoreHuntingTrainingEvents = that.smallCarnivoreHuntingTrainingEvents;
        this.nonSubsidizableSmallCarnivoreHuntingTrainingEvents = that.nonSubsidizableSmallCarnivoreHuntingTrainingEvents;

        if (!Objects.equals(this.smallCarnivoreHuntingTrainingParticipants, that.smallCarnivoreHuntingTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableSmallCarnivoreHuntingTrainingParticipants, that.nonSubsidizableSmallCarnivoreHuntingTrainingParticipants)) {
            this.smallCarnivoreHuntingTrainingParticipantsOverridden = true;
        }
        this.smallCarnivoreHuntingTrainingParticipants = that.smallCarnivoreHuntingTrainingParticipants;
        this.nonSubsidizableSmallCarnivoreHuntingTrainingParticipants = that.nonSubsidizableSmallCarnivoreHuntingTrainingParticipants;

        if (!Objects.equals(this.gameCountingTrainingEvents, that.gameCountingTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableGameCountingTrainingEvents, that.nonSubsidizableGameCountingTrainingEvents)) {
            this.gameCountingTrainingEventsOverridden = true;
        }
        this.gameCountingTrainingEvents = that.gameCountingTrainingEvents;
        this.nonSubsidizableGameCountingTrainingEvents = that.nonSubsidizableGameCountingTrainingEvents;

        if (!Objects.equals(this.gameCountingTrainingParticipants, that.gameCountingTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableGameCountingTrainingParticipants, that.nonSubsidizableGameCountingTrainingParticipants)) {
            this.gameCountingTrainingParticipantsOverridden = true;
        }
        this.gameCountingTrainingParticipants = that.gameCountingTrainingParticipants;
        this.nonSubsidizableGameCountingTrainingParticipants = that.nonSubsidizableGameCountingTrainingParticipants;

        if (!Objects.equals(this.gamePopulationManagementTrainingEvents, that.gamePopulationManagementTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableGamePopulationManagementTrainingEvents, that.nonSubsidizableGamePopulationManagementTrainingEvents)) {
            this.gamePopulationManagementTrainingEventsOverridden = true;
        }
        this.gamePopulationManagementTrainingEvents = that.gamePopulationManagementTrainingEvents;
        this.nonSubsidizableGamePopulationManagementTrainingEvents = that.nonSubsidizableGamePopulationManagementTrainingEvents;

        if (!Objects.equals(this.gamePopulationManagementTrainingParticipants, that.gamePopulationManagementTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableGamePopulationManagementTrainingParticipants, that.nonSubsidizableGamePopulationManagementTrainingParticipants)) {
            this.gamePopulationManagementTrainingParticipantsOverridden = true;
        }
        this.gamePopulationManagementTrainingParticipants = that.gamePopulationManagementTrainingParticipants;
        this.nonSubsidizableGamePopulationManagementTrainingParticipants = that.nonSubsidizableGamePopulationManagementTrainingParticipants;

        if (!Objects.equals(this.gameEnvironmentalCareTrainingEvents, that.gameEnvironmentalCareTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableGameEnvironmentalCareTrainingEvents, that.nonSubsidizableGameEnvironmentalCareTrainingEvents)) {
            this.gameEnvironmentalCareTrainingEventsOverridden = true;
        }
        this.gameEnvironmentalCareTrainingEvents = that.gameEnvironmentalCareTrainingEvents;
        this.nonSubsidizableGameEnvironmentalCareTrainingEvents = that.nonSubsidizableGameEnvironmentalCareTrainingEvents;

        if (!Objects.equals(this.gameEnvironmentalCareTrainingParticipants, that.gameEnvironmentalCareTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableGameEnvironmentalCareTrainingParticipants, that.nonSubsidizableGameEnvironmentalCareTrainingParticipants)) {
            this.gameEnvironmentalCareTrainingParticipantsOverridden = true;
        }
        this.gameEnvironmentalCareTrainingParticipants = that.gameEnvironmentalCareTrainingParticipants;
        this.nonSubsidizableGameEnvironmentalCareTrainingParticipants = that.nonSubsidizableGameEnvironmentalCareTrainingParticipants;

        if (!Objects.equals(this.otherGamekeepingTrainingEvents, that.otherGamekeepingTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableOtherGamekeepingTrainingEvents, that.nonSubsidizableOtherGamekeepingTrainingEvents)) {
            this.otherGamekeepingTrainingEventsOverridden = true;
        }
        this.otherGamekeepingTrainingEvents = that.otherGamekeepingTrainingEvents;
        this.nonSubsidizableOtherGamekeepingTrainingEvents = that.nonSubsidizableOtherGamekeepingTrainingEvents;

        if (!Objects.equals(this.otherGamekeepingTrainingParticipants, that.otherGamekeepingTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableOtherGamekeepingTrainingParticipants, that.nonSubsidizableOtherGamekeepingTrainingParticipants)) {
            this.otherGamekeepingTrainingParticipantsOverridden = true;
        }
        this.otherGamekeepingTrainingParticipants = that.otherGamekeepingTrainingParticipants;
        this.nonSubsidizableOtherGamekeepingTrainingParticipants = that.nonSubsidizableOtherGamekeepingTrainingParticipants;

        if (!Objects.equals(this.shootingTrainingEvents, that.shootingTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableShootingTrainingEvents, that.nonSubsidizableShootingTrainingEvents)) {
            this.shootingTrainingEventsOverridden = true;
        }
        this.shootingTrainingEvents = that.shootingTrainingEvents;
        this.nonSubsidizableShootingTrainingEvents = that.nonSubsidizableShootingTrainingEvents;

        if (!Objects.equals(this.shootingTrainingParticipants, that.shootingTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableShootingTrainingParticipants, that.nonSubsidizableShootingTrainingParticipants)) {
            this.shootingTrainingParticipantsOverridden = true;
        }
        this.shootingTrainingParticipants = that.shootingTrainingParticipants;
        this.nonSubsidizableShootingTrainingParticipants = that.nonSubsidizableShootingTrainingParticipants;

        if (!Objects.equals(this.trackerTrainingEvents, that.trackerTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableTrackerTrainingEvents, that.nonSubsidizableTrackerTrainingEvents)) {
            this.trackerTrainingEventsOverridden = true;
        }
        this.trackerTrainingEvents = that.trackerTrainingEvents;
        this.nonSubsidizableTrackerTrainingEvents = that.nonSubsidizableTrackerTrainingEvents;

        if (!Objects.equals(this.trackerTrainingParticipants, that.trackerTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableTrackerTrainingParticipants, that.nonSubsidizableTrackerTrainingParticipants)) {
            this.trackerTrainingParticipantsOverridden = true;
        }
        this.trackerTrainingParticipants = that.trackerTrainingParticipants;
        this.nonSubsidizableTrackerTrainingParticipants = that.nonSubsidizableTrackerTrainingParticipants;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(
                smallCarnivoreHuntingTrainingEvents, smallCarnivoreHuntingTrainingParticipants,
                gameCountingTrainingEvents, gameCountingTrainingParticipants,
                gamePopulationManagementTrainingEvents, gamePopulationManagementTrainingParticipants,
                gameEnvironmentalCareTrainingEvents, gameEnvironmentalCareTrainingParticipants,
                otherGamekeepingTrainingEvents, otherGamekeepingTrainingParticipants,
                shootingTrainingEvents, shootingTrainingParticipants,
                trackerTrainingEvents, trackerTrainingParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    @Nullable
    Integer countOtherHunterTrainingEvents() {
        return nullableIntSum(
                smallCarnivoreHuntingTrainingEvents, gameCountingTrainingEvents, gamePopulationManagementTrainingEvents,
                gameEnvironmentalCareTrainingEvents, otherGamekeepingTrainingEvents, shootingTrainingEvents,
                trackerTrainingEvents);
    }

    @Nullable
    Integer countNonSubsidizableOtherHunterTrainingEvents() {
        return nullableIntSum(
                nonSubsidizableSmallCarnivoreHuntingTrainingEvents, nonSubsidizableGameCountingTrainingEvents,
                nonSubsidizableGamePopulationManagementTrainingEvents, nonSubsidizableGameEnvironmentalCareTrainingEvents,
                nonSubsidizableOtherGamekeepingTrainingEvents, nonSubsidizableShootingTrainingEvents,
                nonSubsidizableTrackerTrainingEvents);
    }

    @Nullable
    Integer countOtherHunterTrainingParticipants() {
        return nullableIntSum(
                smallCarnivoreHuntingTrainingParticipants, gameCountingTrainingParticipants,
                gamePopulationManagementTrainingParticipants, gameEnvironmentalCareTrainingParticipants,
                otherGamekeepingTrainingParticipants, shootingTrainingParticipants, trackerTrainingParticipants);
    }

    @Nullable
    Integer countNonSubsidizableOtherHunterTrainingParticipants() {
        return nullableIntSum(
                nonSubsidizableSmallCarnivoreHuntingTrainingParticipants, nonSubsidizableGameCountingTrainingParticipants,
                nonSubsidizableGamePopulationManagementTrainingParticipants, nonSubsidizableGameEnvironmentalCareTrainingParticipants,
                nonSubsidizableOtherGamekeepingTrainingParticipants, nonSubsidizableShootingTrainingParticipants,
                nonSubsidizableTrackerTrainingParticipants);
    }

    // Accessors -->


    public Integer getSmallCarnivoreHuntingTrainingEvents() {
        return smallCarnivoreHuntingTrainingEvents;
    }

    public void setSmallCarnivoreHuntingTrainingEvents(final Integer smallCarnivoreHuntingTrainingEvents) {
        this.smallCarnivoreHuntingTrainingEvents = smallCarnivoreHuntingTrainingEvents;
    }

    public Integer getNonSubsidizableSmallCarnivoreHuntingTrainingEvents() {
        return nonSubsidizableSmallCarnivoreHuntingTrainingEvents;
    }

    public void setNonSubsidizableSmallCarnivoreHuntingTrainingEvents(final Integer nonSubsidizableSmallCarnivoreHuntingTrainingEvents) {
        this.nonSubsidizableSmallCarnivoreHuntingTrainingEvents = nonSubsidizableSmallCarnivoreHuntingTrainingEvents;
    }

    public Integer getSmallCarnivoreHuntingTrainingParticipants() {
        return smallCarnivoreHuntingTrainingParticipants;
    }

    public void setSmallCarnivoreHuntingTrainingParticipants(final Integer smallCarnivoreHuntingTrainingParticipants) {
        this.smallCarnivoreHuntingTrainingParticipants = smallCarnivoreHuntingTrainingParticipants;
    }

    public Integer getNonSubsidizableSmallCarnivoreHuntingTrainingParticipants() {
        return nonSubsidizableSmallCarnivoreHuntingTrainingParticipants;
    }

    public void setNonSubsidizableSmallCarnivoreHuntingTrainingParticipants(final Integer nonSubsidizableSmallCarnivoreHuntingTrainingParticipants) {
        this.nonSubsidizableSmallCarnivoreHuntingTrainingParticipants = nonSubsidizableSmallCarnivoreHuntingTrainingParticipants;
    }

    public Integer getGameCountingTrainingEvents() {
        return gameCountingTrainingEvents;
    }

    public void setGameCountingTrainingEvents(final Integer gameCountingTrainingEvents) {
        this.gameCountingTrainingEvents = gameCountingTrainingEvents;
    }

    public Integer getNonSubsidizableGameCountingTrainingEvents() {
        return nonSubsidizableGameCountingTrainingEvents;
    }

    public void setNonSubsidizableGameCountingTrainingEvents(final Integer nonSubsidizableGameCountingTrainingEvents) {
        this.nonSubsidizableGameCountingTrainingEvents = nonSubsidizableGameCountingTrainingEvents;
    }

    public Integer getGameCountingTrainingParticipants() {
        return gameCountingTrainingParticipants;
    }

    public void setGameCountingTrainingParticipants(final Integer gameCountingTrainingParticipants) {
        this.gameCountingTrainingParticipants = gameCountingTrainingParticipants;
    }

    public Integer getNonSubsidizableGameCountingTrainingParticipants() {
        return nonSubsidizableGameCountingTrainingParticipants;
    }

    public void setNonSubsidizableGameCountingTrainingParticipants(final Integer nonSubsidizableGameCountingTrainingParticipants) {
        this.nonSubsidizableGameCountingTrainingParticipants = nonSubsidizableGameCountingTrainingParticipants;
    }

    public Integer getGamePopulationManagementTrainingEvents() {
        return gamePopulationManagementTrainingEvents;
    }

    public void setGamePopulationManagementTrainingEvents(final Integer gamePopulationManagementTrainingEvents) {
        this.gamePopulationManagementTrainingEvents = gamePopulationManagementTrainingEvents;
    }

    public Integer getNonSubsidizableGamePopulationManagementTrainingEvents() {
        return nonSubsidizableGamePopulationManagementTrainingEvents;
    }

    public void setNonSubsidizableGamePopulationManagementTrainingEvents(final Integer nonSubsidizableGamePopulationManagementTrainingEvents) {
        this.nonSubsidizableGamePopulationManagementTrainingEvents = nonSubsidizableGamePopulationManagementTrainingEvents;
    }

    public Integer getGamePopulationManagementTrainingParticipants() {
        return gamePopulationManagementTrainingParticipants;
    }

    public void setGamePopulationManagementTrainingParticipants(final Integer gamePopulationManagementTrainingParticipants) {
        this.gamePopulationManagementTrainingParticipants = gamePopulationManagementTrainingParticipants;
    }

    public Integer getNonSubsidizableGamePopulationManagementTrainingParticipants() {
        return nonSubsidizableGamePopulationManagementTrainingParticipants;
    }

    public void setNonSubsidizableGamePopulationManagementTrainingParticipants(final Integer nonSubsidizableGamePopulationManagementTrainingParticipants) {
        this.nonSubsidizableGamePopulationManagementTrainingParticipants = nonSubsidizableGamePopulationManagementTrainingParticipants;
    }

    public Integer getGameEnvironmentalCareTrainingEvents() {
        return gameEnvironmentalCareTrainingEvents;
    }

    public void setGameEnvironmentalCareTrainingEvents(final Integer gameEnvironmentalCareTrainingEvents) {
        this.gameEnvironmentalCareTrainingEvents = gameEnvironmentalCareTrainingEvents;
    }

    public Integer getNonSubsidizableGameEnvironmentalCareTrainingEvents() {
        return nonSubsidizableGameEnvironmentalCareTrainingEvents;
    }

    public void setNonSubsidizableGameEnvironmentalCareTrainingEvents(final Integer nonSubsidizableGameEnvironmentalCareTrainingEvents) {
        this.nonSubsidizableGameEnvironmentalCareTrainingEvents = nonSubsidizableGameEnvironmentalCareTrainingEvents;
    }

    public Integer getGameEnvironmentalCareTrainingParticipants() {
        return gameEnvironmentalCareTrainingParticipants;
    }

    public void setGameEnvironmentalCareTrainingParticipants(final Integer gameEnvironmentalCareTrainingParticipants) {
        this.gameEnvironmentalCareTrainingParticipants = gameEnvironmentalCareTrainingParticipants;
    }

    public Integer getNonSubsidizableGameEnvironmentalCareTrainingParticipants() {
        return nonSubsidizableGameEnvironmentalCareTrainingParticipants;
    }

    public void setNonSubsidizableGameEnvironmentalCareTrainingParticipants(final Integer nonSubsidizableGameEnvironmentalCareTrainingParticipants) {
        this.nonSubsidizableGameEnvironmentalCareTrainingParticipants = nonSubsidizableGameEnvironmentalCareTrainingParticipants;
    }

    public Integer getOtherGamekeepingTrainingEvents() {
        return otherGamekeepingTrainingEvents;
    }

    public void setOtherGamekeepingTrainingEvents(final Integer otherGamekeepingTrainingEvents) {
        this.otherGamekeepingTrainingEvents = otherGamekeepingTrainingEvents;
    }

    public Integer getNonSubsidizableOtherGamekeepingTrainingEvents() {
        return nonSubsidizableOtherGamekeepingTrainingEvents;
    }

    public void setNonSubsidizableOtherGamekeepingTrainingEvents(final Integer nonSubsidizableOtherGamekeepingTrainingEvents) {
        this.nonSubsidizableOtherGamekeepingTrainingEvents = nonSubsidizableOtherGamekeepingTrainingEvents;
    }

    public Integer getOtherGamekeepingTrainingParticipants() {
        return otherGamekeepingTrainingParticipants;
    }

    public void setOtherGamekeepingTrainingParticipants(final Integer otherGamekeepingTrainingParticipants) {
        this.otherGamekeepingTrainingParticipants = otherGamekeepingTrainingParticipants;
    }

    public Integer getNonSubsidizableOtherGamekeepingTrainingParticipants() {
        return nonSubsidizableOtherGamekeepingTrainingParticipants;
    }

    public void setNonSubsidizableOtherGamekeepingTrainingParticipants(final Integer nonSubsidizableOtherGamekeepingTrainingParticipants) {
        this.nonSubsidizableOtherGamekeepingTrainingParticipants = nonSubsidizableOtherGamekeepingTrainingParticipants;
    }

    public Integer getShootingTrainingEvents() {
        return shootingTrainingEvents;
    }

    public void setShootingTrainingEvents(final Integer shootingTrainingEvents) {
        this.shootingTrainingEvents = shootingTrainingEvents;
    }

    public Integer getNonSubsidizableShootingTrainingEvents() {
        return nonSubsidizableShootingTrainingEvents;
    }

    public void setNonSubsidizableShootingTrainingEvents(final Integer nonSubsidizableShootingTrainingEvents) {
        this.nonSubsidizableShootingTrainingEvents = nonSubsidizableShootingTrainingEvents;
    }

    public Integer getShootingTrainingParticipants() {
        return shootingTrainingParticipants;
    }

    public void setShootingTrainingParticipants(final Integer shootingTrainingParticipants) {
        this.shootingTrainingParticipants = shootingTrainingParticipants;
    }

    public Integer getNonSubsidizableShootingTrainingParticipants() {
        return nonSubsidizableShootingTrainingParticipants;
    }

    public void setNonSubsidizableShootingTrainingParticipants(final Integer nonSubsidizableShootingTrainingParticipants) {
        this.nonSubsidizableShootingTrainingParticipants = nonSubsidizableShootingTrainingParticipants;
    }

    public Integer getTrackerTrainingEvents() {
        return trackerTrainingEvents;
    }

    public void setTrackerTrainingEvents(final Integer trackerTrainingEvents) {
        this.trackerTrainingEvents = trackerTrainingEvents;
    }

    public Integer getNonSubsidizableTrackerTrainingEvents() {
        return nonSubsidizableTrackerTrainingEvents;
    }

    public void setNonSubsidizableTrackerTrainingEvents(final Integer nonSubsidizableTrackerTrainingEvents) {
        this.nonSubsidizableTrackerTrainingEvents = nonSubsidizableTrackerTrainingEvents;
    }

    public Integer getTrackerTrainingParticipants() {
        return trackerTrainingParticipants;
    }

    public void setTrackerTrainingParticipants(final Integer trackerTrainingParticipants) {
        this.trackerTrainingParticipants = trackerTrainingParticipants;
    }

    public Integer getNonSubsidizableTrackerTrainingParticipants() {
        return nonSubsidizableTrackerTrainingParticipants;
    }

    public void setNonSubsidizableTrackerTrainingParticipants(final Integer nonSubsidizableTrackerTrainingParticipants) {
        this.nonSubsidizableTrackerTrainingParticipants = nonSubsidizableTrackerTrainingParticipants;
    }

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isSmallCarnivoreHuntingTrainingEventsOverridden() {
        return smallCarnivoreHuntingTrainingEventsOverridden;
    }

    public void setSmallCarnivoreHuntingTrainingEventsOverridden(final boolean smallCarnivoreHuntingTrainingEventsOverridden) {
        this.smallCarnivoreHuntingTrainingEventsOverridden = smallCarnivoreHuntingTrainingEventsOverridden;
    }

    public boolean isSmallCarnivoreHuntingTrainingParticipantsOverridden() {
        return smallCarnivoreHuntingTrainingParticipantsOverridden;
    }

    public void setSmallCarnivoreHuntingTrainingParticipantsOverridden(final boolean smallCarnivoreHuntingTrainingParticipantsOverridden) {
        this.smallCarnivoreHuntingTrainingParticipantsOverridden = smallCarnivoreHuntingTrainingParticipantsOverridden;
    }

    public boolean isGameCountingTrainingEventsOverridden() {
        return gameCountingTrainingEventsOverridden;
    }

    public void setGameCountingTrainingEventsOverridden(final boolean gameCountingTrainingEventsOverridden) {
        this.gameCountingTrainingEventsOverridden = gameCountingTrainingEventsOverridden;
    }

    public boolean isGameCountingTrainingParticipantsOverridden() {
        return gameCountingTrainingParticipantsOverridden;
    }

    public void setGameCountingTrainingParticipantsOverridden(final boolean gameCountingTrainingParticipantsOverridden) {
        this.gameCountingTrainingParticipantsOverridden = gameCountingTrainingParticipantsOverridden;
    }

    public boolean isGamePopulationManagementTrainingEventsOverridden() {
        return gamePopulationManagementTrainingEventsOverridden;
    }

    public void setGamePopulationManagementTrainingEventsOverridden(final boolean gamePopulationManagementTrainingEventsOverridden) {
        this.gamePopulationManagementTrainingEventsOverridden = gamePopulationManagementTrainingEventsOverridden;
    }

    public boolean isGamePopulationManagementTrainingParticipantsOverridden() {
        return gamePopulationManagementTrainingParticipantsOverridden;
    }

    public void setGamePopulationManagementTrainingParticipantsOverridden(final boolean gamePopulationManagementTrainingParticipantsOverridden) {
        this.gamePopulationManagementTrainingParticipantsOverridden = gamePopulationManagementTrainingParticipantsOverridden;
    }

    public boolean isGameEnvironmentalCareTrainingEventsOverridden() {
        return gameEnvironmentalCareTrainingEventsOverridden;
    }

    public void setGameEnvironmentalCareTrainingEventsOverridden(final boolean gameEnvironmentalCareTrainingEventsOverridden) {
        this.gameEnvironmentalCareTrainingEventsOverridden = gameEnvironmentalCareTrainingEventsOverridden;
    }

    public boolean isGameEnvironmentalCareTrainingParticipantsOverridden() {
        return gameEnvironmentalCareTrainingParticipantsOverridden;
    }

    public void setGameEnvironmentalCareTrainingParticipantsOverridden(final boolean gameEnvironmentalCareTrainingParticipantsOverridden) {
        this.gameEnvironmentalCareTrainingParticipantsOverridden = gameEnvironmentalCareTrainingParticipantsOverridden;
    }

    public boolean isOtherGamekeepingTrainingEventsOverridden() {
        return otherGamekeepingTrainingEventsOverridden;
    }

    public void setOtherGamekeepingTrainingEventsOverridden(final boolean otherGamekeepingTrainingEventsOverridden) {
        this.otherGamekeepingTrainingEventsOverridden = otherGamekeepingTrainingEventsOverridden;
    }

    public boolean isOtherGamekeepingTrainingParticipantsOverridden() {
        return otherGamekeepingTrainingParticipantsOverridden;
    }

    public void setOtherGamekeepingTrainingParticipantsOverridden(final boolean otherGamekeepingTrainingParticipantsOverridden) {
        this.otherGamekeepingTrainingParticipantsOverridden = otherGamekeepingTrainingParticipantsOverridden;
    }

    public boolean isShootingTrainingEventsOverridden() {
        return shootingTrainingEventsOverridden;
    }

    public void setShootingTrainingEventsOverridden(final boolean shootingTrainingEventsOverridden) {
        this.shootingTrainingEventsOverridden = shootingTrainingEventsOverridden;
    }

    public boolean isShootingTrainingParticipantsOverridden() {
        return shootingTrainingParticipantsOverridden;
    }

    public void setShootingTrainingParticipantsOverridden(final boolean shootingTrainingParticipantsOverridden) {
        this.shootingTrainingParticipantsOverridden = shootingTrainingParticipantsOverridden;
    }

    public boolean isTrackerTrainingEventsOverridden() {
        return trackerTrainingEventsOverridden;
    }

    public void setTrackerTrainingEventsOverridden(final boolean trackerTrainingEventsOverridden) {
        this.trackerTrainingEventsOverridden = trackerTrainingEventsOverridden;
    }

    public boolean isTrackerTrainingParticipantsOverridden() {
        return trackerTrainingParticipantsOverridden;
    }

    public void setTrackerTrainingParticipantsOverridden(final boolean trackerTrainingParticipantsOverridden) {
        this.trackerTrainingParticipantsOverridden = trackerTrainingParticipantsOverridden;
    }
}
