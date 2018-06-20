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
public class OtherHunterTrainingStatistics
        implements AnnualStatisticsFieldsetStatus,
        HasLastModificationStatus<OtherHunterTrainingStatistics>,
        Serializable {

    public static final OtherHunterTrainingStatistics reduce(@Nullable final OtherHunterTrainingStatistics a,
                                                             @Nullable final OtherHunterTrainingStatistics b) {

        final OtherHunterTrainingStatistics result = new OtherHunterTrainingStatistics();
        result.setSmallCarnivoreHuntingTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getSmallCarnivoreHuntingTrainingEvents()));
        result.setSmallCarnivoreHuntingTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getSmallCarnivoreHuntingTrainingParticipants()));
        result.setGameCountingTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getGameCountingTrainingEvents()));
        result.setGameCountingTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getGameCountingTrainingParticipants()));
        result.setGamePopulationManagementTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getGamePopulationManagementTrainingEvents()));
        result.setGamePopulationManagementTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getGamePopulationManagementTrainingParticipants()));
        result.setGameEnvironmentalCareTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getGameEnvironmentalCareTrainingEvents()));
        result.setGameEnvironmentalCareTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getGameEnvironmentalCareTrainingParticipants()));
        result.setOtherGamekeepingTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getOtherGamekeepingTrainingEvents()));
        result.setOtherGamekeepingTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getOtherGamekeepingTrainingParticipants()));
        result.setOtherShootingTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getOtherShootingTrainingEvents()));
        result.setOtherShootingTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getOtherShootingTrainingParticipants()));
        result.setTrackerTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getTrackerTrainingEvents()));
        result.setTrackerTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getTrackerTrainingParticipants()));
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

    // Pienpetojen pyynti -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "small_carnivore_hunting_training_participants")
    private Integer smallCarnivoreHuntingTrainingParticipants;

    // Riistalaskentakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "game_counting_training_events")
    private Integer gameCountingTrainingEvents;

    // Riistalaskentakoulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "game_counting_training_participants")
    private Integer gameCountingTrainingParticipants;

    // Riistakantojen hoito -koulutustilaisuudet, kpl
    @Min(0)
    @Column(name = "game_population_management_training_events")
    private Integer gamePopulationManagementTrainingEvents;

    // Riistakantojen hoito -koulutukseen osallistuneet, kpl
    @Min(0)
    @Column(name = "game_population_management_training_participants")
    private Integer gamePopulationManagementTrainingParticipants;

    // Riistan elinympäristöjen hoito -koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "game_environmental_care_training_events")
    private Integer gameEnvironmentalCareTrainingEvents;

    // Riistan elinympäristöjen hoito -koulutukseen osallistuneet, lkm
    @Min(0)
    @Column(name = "game_environmental_care_training_participants")
    private Integer gameEnvironmentalCareTrainingParticipants;

    // Muut riistanhoidon koulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "other_gamekeeping_training_events")
    private Integer otherGamekeepingTrainingEvents;

    // Muihin riistanhoidon koulutuksiin osallistuneet, lkm
    @Min(0)
    @Column(name = "other_gamekeeping_training_participants")
    private Integer otherGamekeepingTrainingParticipants;

    // Muut ampumakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "other_shooting_training_events")
    private Integer otherShootingTrainingEvents;

    // Muihin ampumakoulutuksiin osallistuneet, lkm
    @Min(0)
    @Column(name = "other_shooting_training_participants")
    private Integer otherShootingTrainingParticipants;

    // Jäljestäjäkoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "tracker_training_events")
    private Integer trackerTrainingEvents;

    // Jäljestäjäkoulutuksiin osallistuneet, lkm
    @Min(0)
    @Column(name = "tracker_training_participants")
    private Integer trackerTrainingParticipants;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "other_hunter_trainings_last_modified")
    private DateTime lastModified;

    public OtherHunterTrainingStatistics() {
    }

    public OtherHunterTrainingStatistics(@Nonnull final OtherHunterTrainingStatistics that) {
        Objects.requireNonNull(that);

        this.smallCarnivoreHuntingTrainingEvents = that.smallCarnivoreHuntingTrainingEvents;
        this.smallCarnivoreHuntingTrainingParticipants = that.smallCarnivoreHuntingTrainingParticipants;
        this.gameCountingTrainingEvents = that.gameCountingTrainingEvents;
        this.gameCountingTrainingParticipants = that.gameCountingTrainingParticipants;
        this.gamePopulationManagementTrainingEvents = that.gamePopulationManagementTrainingEvents;
        this.gamePopulationManagementTrainingParticipants = that.gamePopulationManagementTrainingParticipants;
        this.gameEnvironmentalCareTrainingEvents = that.gameEnvironmentalCareTrainingEvents;
        this.gameEnvironmentalCareTrainingParticipants = that.gameEnvironmentalCareTrainingParticipants;
        this.otherGamekeepingTrainingEvents = that.otherGamekeepingTrainingEvents;
        this.otherGamekeepingTrainingParticipants = that.otherGamekeepingTrainingParticipants;
        this.otherShootingTrainingEvents = that.otherShootingTrainingEvents;
        this.otherShootingTrainingParticipants = that.otherShootingTrainingParticipants;
        this.trackerTrainingEvents = that.trackerTrainingEvents;
        this.trackerTrainingParticipants = that.trackerTrainingParticipants;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isEqualTo(final OtherHunterTrainingStatistics other) {
        // Includes manually updateable fields only.

        return Objects.equals(smallCarnivoreHuntingTrainingEvents, other.smallCarnivoreHuntingTrainingEvents) && 
                Objects.equals(smallCarnivoreHuntingTrainingParticipants, other.smallCarnivoreHuntingTrainingParticipants) &&
                Objects.equals(gameCountingTrainingEvents, other.gameCountingTrainingEvents) &&
                Objects.equals(gameCountingTrainingParticipants, other.gameCountingTrainingParticipants) &&
                Objects.equals(gamePopulationManagementTrainingEvents, other.gamePopulationManagementTrainingEvents) &&
                Objects.equals(gamePopulationManagementTrainingParticipants, other.gamePopulationManagementTrainingParticipants) &&
                Objects.equals(gameEnvironmentalCareTrainingEvents, other.gameEnvironmentalCareTrainingEvents) &&
                Objects.equals(gameEnvironmentalCareTrainingParticipants, other.gameEnvironmentalCareTrainingParticipants) &&
                Objects.equals(otherGamekeepingTrainingEvents, other.otherGamekeepingTrainingEvents) &&
                Objects.equals(otherGamekeepingTrainingParticipants, other.otherGamekeepingTrainingParticipants) &&
                Objects.equals(otherShootingTrainingEvents, other.otherShootingTrainingEvents) &&
                Objects.equals(otherShootingTrainingParticipants, other.otherShootingTrainingParticipants) &&
                Objects.equals(trackerTrainingEvents, other.trackerTrainingEvents) &&
                Objects.equals(trackerTrainingParticipants, other.trackerTrainingParticipants);
    }

    @Override
    public void updateModificationStatus() {
        lastModified = DateUtil.now();
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(
                smallCarnivoreHuntingTrainingEvents, smallCarnivoreHuntingTrainingParticipants,
                gameCountingTrainingEvents, gameCountingTrainingParticipants,
                gamePopulationManagementTrainingEvents, gamePopulationManagementTrainingParticipants,
                gameEnvironmentalCareTrainingEvents, gameEnvironmentalCareTrainingParticipants,
                otherGamekeepingTrainingEvents, otherGamekeepingTrainingParticipants,
                otherShootingTrainingEvents, otherShootingTrainingParticipants,
                trackerTrainingEvents, trackerTrainingParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    public int countOtherHunterTrainingEvents() {
        return Stream
                .of(smallCarnivoreHuntingTrainingEvents, gameCountingTrainingEvents,
                        gamePopulationManagementTrainingEvents, gameEnvironmentalCareTrainingEvents,
                        otherGamekeepingTrainingEvents, otherShootingTrainingEvents, trackerTrainingEvents)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    public int countOtherHunterTrainingParticipants() {
        return Stream
                .of(smallCarnivoreHuntingTrainingParticipants, gameCountingTrainingParticipants,
                        gamePopulationManagementTrainingParticipants, gameEnvironmentalCareTrainingParticipants,
                        otherGamekeepingTrainingParticipants, otherShootingTrainingParticipants,
                        trackerTrainingParticipants)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    // Accessors -->

    public Integer getSmallCarnivoreHuntingTrainingEvents() {
        return smallCarnivoreHuntingTrainingEvents;
    }

    public void setSmallCarnivoreHuntingTrainingEvents(final Integer smallCarnivoreHuntingTrainingEvents) {
        this.smallCarnivoreHuntingTrainingEvents = smallCarnivoreHuntingTrainingEvents;
    }

    public Integer getSmallCarnivoreHuntingTrainingParticipants() {
        return smallCarnivoreHuntingTrainingParticipants;
    }

    public void setSmallCarnivoreHuntingTrainingParticipants(final Integer smallCarnivoreHuntingTrainingParticipants) {
        this.smallCarnivoreHuntingTrainingParticipants = smallCarnivoreHuntingTrainingParticipants;
    }

    public Integer getGameCountingTrainingEvents() {
        return gameCountingTrainingEvents;
    }

    public void setGameCountingTrainingEvents(final Integer gameCountingTrainingEvents) {
        this.gameCountingTrainingEvents = gameCountingTrainingEvents;
    }

    public Integer getGameCountingTrainingParticipants() {
        return gameCountingTrainingParticipants;
    }

    public void setGameCountingTrainingParticipants(final Integer gameCountingTrainingParticipants) {
        this.gameCountingTrainingParticipants = gameCountingTrainingParticipants;
    }

    public Integer getGamePopulationManagementTrainingEvents() {
        return gamePopulationManagementTrainingEvents;
    }

    public void setGamePopulationManagementTrainingEvents(final Integer gamePopulationManagementTrainingEvents) {
        this.gamePopulationManagementTrainingEvents = gamePopulationManagementTrainingEvents;
    }

    public Integer getGamePopulationManagementTrainingParticipants() {
        return gamePopulationManagementTrainingParticipants;
    }

    public void setGamePopulationManagementTrainingParticipants(final Integer gamePopulationManagementTrainingParticipants) {
        this.gamePopulationManagementTrainingParticipants = gamePopulationManagementTrainingParticipants;
    }

    public Integer getGameEnvironmentalCareTrainingEvents() {
        return gameEnvironmentalCareTrainingEvents;
    }

    public void setGameEnvironmentalCareTrainingEvents(final Integer gameEnvironmentalCareTrainingEvents) {
        this.gameEnvironmentalCareTrainingEvents = gameEnvironmentalCareTrainingEvents;
    }

    public Integer getGameEnvironmentalCareTrainingParticipants() {
        return gameEnvironmentalCareTrainingParticipants;
    }

    public void setGameEnvironmentalCareTrainingParticipants(final Integer gameEnvironmentalCareTrainingParticipants) {
        this.gameEnvironmentalCareTrainingParticipants = gameEnvironmentalCareTrainingParticipants;
    }

    public Integer getOtherGamekeepingTrainingEvents() {
        return otherGamekeepingTrainingEvents;
    }

    public void setOtherGamekeepingTrainingEvents(final Integer otherGamekeepingTrainingEvents) {
        this.otherGamekeepingTrainingEvents = otherGamekeepingTrainingEvents;
    }

    public Integer getOtherGamekeepingTrainingParticipants() {
        return otherGamekeepingTrainingParticipants;
    }

    public void setOtherGamekeepingTrainingParticipants(final Integer otherGamekeepingTrainingParticipants) {
        this.otherGamekeepingTrainingParticipants = otherGamekeepingTrainingParticipants;
    }

    public Integer getOtherShootingTrainingEvents() {
        return otherShootingTrainingEvents;
    }

    public void setOtherShootingTrainingEvents(final Integer otherShootingTrainingEvents) {
        this.otherShootingTrainingEvents = otherShootingTrainingEvents;
    }

    public Integer getOtherShootingTrainingParticipants() {
        return otherShootingTrainingParticipants;
    }

    public void setOtherShootingTrainingParticipants(final Integer otherShootingTrainingParticipants) {
        this.otherShootingTrainingParticipants = otherShootingTrainingParticipants;
    }

    public Integer getTrackerTrainingEvents() {
        return trackerTrainingEvents;
    }

    public void setTrackerTrainingEvents(final Integer trackerTrainingEvents) {
        this.trackerTrainingEvents = trackerTrainingEvents;
    }

    public Integer getTrackerTrainingParticipants() {
        return trackerTrainingParticipants;
    }

    public void setTrackerTrainingParticipants(final Integer trackerTrainingParticipants) {
        this.trackerTrainingParticipants = trackerTrainingParticipants;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
