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

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.GAME_COUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_GAME_COUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_OTHER_GAMEKEEPING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SHOOTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_TRACKER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.OTHER_GAMEKEEPING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SHOOTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.TRACKER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.OTHER_HUNTER_TRAINING_STATISTICS;
import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class OtherHunterTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsFieldsetParticipants,
        AnnualStatisticsManuallyEditableFields<OtherHunterTrainingStatistics>,
        Serializable {

    public static OtherHunterTrainingStatistics reduce(@Nullable final OtherHunterTrainingStatistics a,
                                                       @Nullable final OtherHunterTrainingStatistics b) {

        final OtherHunterTrainingStatistics result = new OtherHunterTrainingStatistics();
        result.setSmallCarnivoreHuntingTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getSmallCarnivoreHuntingTrainingEvents));
        result.setSmallCarnivoreHuntingTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getSmallCarnivoreHuntingTrainingParticipants));
        result.setGameCountingTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getGameCountingTrainingEvents));
        result.setGameCountingTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getGameCountingTrainingParticipants));
        result.setGamePopulationManagementTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getGamePopulationManagementTrainingEvents));
        result.setGamePopulationManagementTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getGamePopulationManagementTrainingParticipants));
        result.setGameEnvironmentalCareTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getGameEnvironmentalCareTrainingEvents));
        result.setGameEnvironmentalCareTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getGameEnvironmentalCareTrainingParticipants));
        result.setOtherGamekeepingTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getOtherGamekeepingTrainingEvents));
        result.setOtherGamekeepingTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getOtherGamekeepingTrainingParticipants));
        result.setShootingTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getShootingTrainingEvents));
        result.setShootingTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getShootingTrainingParticipants));
        result.setTrackerTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getTrackerTrainingEvents));
        result.setTrackerTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getTrackerTrainingParticipants));

        result.setNonSubsidizableSmallCarnivoreHuntingTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableSmallCarnivoreHuntingTrainingEvents));
        result.setNonSubsidizableSmallCarnivoreHuntingTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableSmallCarnivoreHuntingTrainingParticipants));
        result.setNonSubsidizableGameCountingTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableGameCountingTrainingEvents));
        result.setNonSubsidizableGameCountingTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableGameCountingTrainingParticipants));
        result.setNonSubsidizableGamePopulationManagementTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableGamePopulationManagementTrainingEvents));
        result.setNonSubsidizableGamePopulationManagementTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableGamePopulationManagementTrainingParticipants));
        result.setNonSubsidizableGameEnvironmentalCareTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableGameEnvironmentalCareTrainingEvents));
        result.setNonSubsidizableGameEnvironmentalCareTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableGameEnvironmentalCareTrainingParticipants));
        result.setNonSubsidizableOtherGamekeepingTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableOtherGamekeepingTrainingEvents));
        result.setNonSubsidizableOtherGamekeepingTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableOtherGamekeepingTrainingParticipants));
        result.setNonSubsidizableShootingTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableShootingTrainingEvents));
        result.setNonSubsidizableShootingTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableShootingTrainingParticipants));
        result.setNonSubsidizableTrackerTrainingEvents(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableTrackerTrainingEvents));
        result.setNonSubsidizableTrackerTrainingParticipants(nullableIntSum(a, b, OtherHunterTrainingStatistics::getNonSubsidizableTrackerTrainingParticipants));

        result.setLastModified(nullsafeMax(a, b, OtherHunterTrainingStatistics::getLastModified));
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
                trackerTrainingEvents, trackerTrainingParticipants) && hasParticipants();
    }

    private boolean hasParticipants() {
        return listMissingParticipants()._2.isEmpty();
    }

    @Override
    public Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> listMissingParticipants() {
        final List<AnnualStatisticsParticipantField> missing = new ArrayList<>();
        if (smallCarnivoreHuntingTrainingEvents != null && smallCarnivoreHuntingTrainingEvents > 0 && smallCarnivoreHuntingTrainingParticipants <= 0) {
            missing.add(SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS);
        }
        if (gameCountingTrainingEvents != null && gameCountingTrainingEvents > 0 && gameCountingTrainingParticipants <= 0) {
            missing.add(GAME_COUNTING_TRAINING_EVENTS);
        }
        if (gamePopulationManagementTrainingEvents != null && gamePopulationManagementTrainingEvents > 0 && gamePopulationManagementTrainingParticipants <= 0) {
            missing.add(GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS);
        }
        if (gameEnvironmentalCareTrainingEvents != null && gameEnvironmentalCareTrainingEvents > 0 && gameEnvironmentalCareTrainingParticipants <= 0) {
            missing.add(GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS);
        }
        if (otherGamekeepingTrainingEvents != null && otherGamekeepingTrainingEvents > 0 && otherGamekeepingTrainingParticipants <= 0) {
            missing.add(OTHER_GAMEKEEPING_TRAINING_EVENTS);
        }
        if (shootingTrainingEvents != null && shootingTrainingEvents > 0 && shootingTrainingParticipants <= 0) {
            missing.add(SHOOTING_TRAINING_EVENTS);
        }
        if (trackerTrainingEvents != null && trackerTrainingEvents > 0 && trackerTrainingParticipants <= 0) {
            missing.add(TRACKER_TRAINING_EVENTS);
        }
        if (nonSubsidizableSmallCarnivoreHuntingTrainingEvents != null && nonSubsidizableSmallCarnivoreHuntingTrainingEvents > 0 &&
                nonSubsidizableSmallCarnivoreHuntingTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_SMALL_CARNIVORE_HUNTING_TRAINING_EVENTS);
        }
        if (nonSubsidizableGameCountingTrainingEvents != null && nonSubsidizableGameCountingTrainingEvents > 0 &&
                nonSubsidizableGameCountingTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_GAME_COUNTING_TRAINING_EVENTS);
        }
        if (nonSubsidizableGamePopulationManagementTrainingEvents != null && nonSubsidizableGamePopulationManagementTrainingEvents > 0 &&
                nonSubsidizableGamePopulationManagementTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_GAME_POPULATION_MANAGEMENT_TRAINING_EVENTS);
        }
        if (nonSubsidizableGameEnvironmentalCareTrainingEvents != null && nonSubsidizableGameEnvironmentalCareTrainingEvents > 0 &&
                nonSubsidizableGameEnvironmentalCareTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_GAME_ENVIRONMENTAL_CARE_TRAINING_EVENTS);
        }
        if (nonSubsidizableOtherGamekeepingTrainingEvents != null && nonSubsidizableOtherGamekeepingTrainingEvents > 0 &&
                nonSubsidizableOtherGamekeepingTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_OTHER_GAMEKEEPING_TRAINING_EVENTS);
        }
        if (nonSubsidizableShootingTrainingEvents != null && nonSubsidizableShootingTrainingEvents > 0 &&
                nonSubsidizableShootingTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_SHOOTING_TRAINING_EVENTS);
        }
        if (nonSubsidizableTrackerTrainingEvents != null && nonSubsidizableTrackerTrainingEvents > 0 &&
                nonSubsidizableTrackerTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_TRACKER_TRAINING_EVENTS);
        }
        return Tuple.of(OTHER_HUNTER_TRAINING_STATISTICS, missing);
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
