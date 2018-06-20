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
public class JHTTrainingStatistics
        implements AnnualStatisticsFieldsetStatus, HasLastModificationStatus<JHTTrainingStatistics>, Serializable {

    public static final JHTTrainingStatistics reduce(@Nullable final JHTTrainingStatistics a,
                                                     @Nullable final JHTTrainingStatistics b) {

        final JHTTrainingStatistics result = new JHTTrainingStatistics();
        result.setShootingTestTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getShootingTestTrainingEvents()));
        result.setShootingTestTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getShootingTestTrainingParticipants()));
        result.setHunterExamOfficialTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getHunterExamOfficialTrainingEvents()));
        result.setHunterExamOfficialTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getHunterExamOfficialTrainingParticipants()));
        result.setGameDamageTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getGameDamageTrainingEvents()));
        result.setGameDamageTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getGameDamageTrainingParticipants()));
        result.setHuntingControlTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getHuntingControlTrainingEvents()));
        result.setHuntingControlTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getHuntingControlTrainingParticipants()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static JHTTrainingStatistics reduce(@Nonnull final Stream<JHTTrainingStatistics> items) {
        requireNonNull(items);
        return items.reduce(new JHTTrainingStatistics(), JHTTrainingStatistics::reduce);
    }

    public static <T> JHTTrainingStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                   @Nonnull final Function<? super T, JHTTrainingStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Ampumakoekoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_shooting_test_training_events")
    private Integer shootingTestTrainingEvents;

    // Ampumakoekoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_shooting_test_training_participants")
    private Integer shootingTestTrainingParticipants;

    // Metsästäjätutkintokoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_hunter_exam_training_events")
    private Integer hunterExamOfficialTrainingEvents;

    // Metsästäjätutkintokoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_hunter_exam_training_participants")
    private Integer hunterExamOfficialTrainingParticipants;

    // Riistavahinkokoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_game_damage_training_events")
    private Integer gameDamageTrainingEvents;

    // Riistavahinkokoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_game_damage_training_participants")
    private Integer gameDamageTrainingParticipants;

    // Metsästyksenvalvojakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_hunting_control_training_events")
    private Integer huntingControlTrainingEvents;

    // Metsästyksenvalvojakoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_hunting_control_training_participants")
    private Integer huntingControlTrainingParticipants;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "jht_trainings_last_modified")
    private DateTime lastModified;

    public JHTTrainingStatistics() {
    }

    public JHTTrainingStatistics(@Nonnull final JHTTrainingStatistics that) {
        Objects.requireNonNull(that);

        this.shootingTestTrainingEvents = that.shootingTestTrainingEvents;
        this.shootingTestTrainingParticipants = that.shootingTestTrainingParticipants;
        this.hunterExamOfficialTrainingEvents = that.hunterExamOfficialTrainingEvents;
        this.hunterExamOfficialTrainingParticipants = that.hunterExamOfficialTrainingParticipants;
        this.gameDamageTrainingEvents = that.gameDamageTrainingEvents;
        this.gameDamageTrainingParticipants = that.gameDamageTrainingParticipants;
        this.huntingControlTrainingEvents = that.huntingControlTrainingEvents;
        this.huntingControlTrainingParticipants = that.huntingControlTrainingParticipants;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isEqualTo(final JHTTrainingStatistics other) {
        // Includes manually updateable fields only.

        return Objects.equals(shootingTestTrainingEvents, other.shootingTestTrainingEvents) &&
                Objects.equals(shootingTestTrainingParticipants, other.shootingTestTrainingParticipants) &&
                Objects.equals(hunterExamOfficialTrainingEvents, other.hunterExamOfficialTrainingEvents) &&
                Objects.equals(hunterExamOfficialTrainingParticipants, other.hunterExamOfficialTrainingParticipants) &&
                Objects.equals(gameDamageTrainingEvents, other.gameDamageTrainingEvents) &&
                Objects.equals(gameDamageTrainingParticipants, other.gameDamageTrainingParticipants) &&
                Objects.equals(huntingControlTrainingEvents, other.huntingControlTrainingEvents) &&
                Objects.equals(huntingControlTrainingParticipants, other.huntingControlTrainingParticipants);
    }

    @Override
    public void updateModificationStatus() {
        lastModified = DateUtil.now();
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(
                shootingTestTrainingEvents, shootingTestTrainingParticipants,
                hunterExamOfficialTrainingEvents, hunterExamOfficialTrainingParticipants,
                gameDamageTrainingEvents, gameDamageTrainingParticipants,
                huntingControlTrainingEvents, huntingControlTrainingParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    public int countJhtTrainingEvents() {
        return Stream
                .of(shootingTestTrainingEvents, hunterExamOfficialTrainingEvents,
                        gameDamageTrainingEvents, huntingControlTrainingEvents)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    public int countJhtTrainingParticipants() {
        return Stream
                .of(shootingTestTrainingParticipants, hunterExamOfficialTrainingParticipants,
                        gameDamageTrainingParticipants, huntingControlTrainingParticipants)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    // Accessors -->

    public Integer getShootingTestTrainingEvents() {
        return shootingTestTrainingEvents;
    }

    public void setShootingTestTrainingEvents(final Integer shootingTestTrainingEvents) {
        this.shootingTestTrainingEvents = shootingTestTrainingEvents;
    }

    public Integer getShootingTestTrainingParticipants() {
        return shootingTestTrainingParticipants;
    }

    public void setShootingTestTrainingParticipants(final Integer shootingTestTrainingParticipants) {
        this.shootingTestTrainingParticipants = shootingTestTrainingParticipants;
    }

    public Integer getHunterExamOfficialTrainingEvents() {
        return hunterExamOfficialTrainingEvents;
    }

    public void setHunterExamOfficialTrainingEvents(final Integer hunterExamOfficialTrainingEvents) {
        this.hunterExamOfficialTrainingEvents = hunterExamOfficialTrainingEvents;
    }

    public Integer getHunterExamOfficialTrainingParticipants() {
        return hunterExamOfficialTrainingParticipants;
    }

    public void setHunterExamOfficialTrainingParticipants(final Integer hunterExamOfficialTrainingParticipants) {
        this.hunterExamOfficialTrainingParticipants = hunterExamOfficialTrainingParticipants;
    }

    public Integer getGameDamageTrainingEvents() {
        return gameDamageTrainingEvents;
    }

    public void setGameDamageTrainingEvents(final Integer gameDamageTrainingEvents) {
        this.gameDamageTrainingEvents = gameDamageTrainingEvents;
    }

    public Integer getGameDamageTrainingParticipants() {
        return gameDamageTrainingParticipants;
    }

    public void setGameDamageTrainingParticipants(final Integer numberOfGameDamageTrainingParticipants) {
        this.gameDamageTrainingParticipants = numberOfGameDamageTrainingParticipants;
    }

    public Integer getHuntingControlTrainingEvents() {
        return huntingControlTrainingEvents;
    }

    public void setHuntingControlTrainingEvents(final Integer huntingControlTrainingEvents) {
        this.huntingControlTrainingEvents = huntingControlTrainingEvents;
    }

    public Integer getHuntingControlTrainingParticipants() {
        return huntingControlTrainingParticipants;
    }

    public void setHuntingControlTrainingParticipants(final Integer huntingControlTrainingParticipants) {
        this.huntingControlTrainingParticipants = huntingControlTrainingParticipants;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
