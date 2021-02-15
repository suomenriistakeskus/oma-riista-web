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
public class JHTTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsManuallyEditableFields<JHTTrainingStatistics>,
        Serializable {

    public static final JHTTrainingStatistics reduce(@Nullable final JHTTrainingStatistics a,
                                                     @Nullable final JHTTrainingStatistics b) {

        final JHTTrainingStatistics result = new JHTTrainingStatistics();
        result.setShootingTestTrainingEvents(nullableIntSum(a, b, s -> s.getShootingTestTrainingEvents()));
        result.setShootingTestTrainingParticipants(nullableIntSum(a, b, s -> s.getShootingTestTrainingParticipants()));
        result.setHunterExamOfficialTrainingEvents(nullableIntSum(a, b, s -> s.getHunterExamOfficialTrainingEvents()));
        result.setHunterExamOfficialTrainingParticipants(nullableIntSum(a, b, s -> s.getHunterExamOfficialTrainingParticipants()));
        result.setGameDamageTrainingEvents(nullableIntSum(a, b, s -> s.getGameDamageTrainingEvents()));
        result.setGameDamageTrainingParticipants(nullableIntSum(a, b, s -> s.getGameDamageTrainingParticipants()));
        result.setHuntingControlTrainingEvents(nullableIntSum(a, b, s -> s.getHuntingControlTrainingEvents()));
        result.setHuntingControlTrainingParticipants(nullableIntSum(a, b, s -> s.getHuntingControlTrainingParticipants()));
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

    @Column(name = "jht_shooting_test_training_events_overridden", nullable = false)
    private boolean shootingTestTrainingEventsOverridden;

    // Ampumakoekoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_shooting_test_training_participants")
    private Integer shootingTestTrainingParticipants;

    @Column(name = "jht_shooting_test_training_participants_overridden", nullable = false)
    private boolean shootingTestTrainingParticipantsOverridden;

    // Metsästäjätutkintokoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_hunter_exam_training_events")
    private Integer hunterExamOfficialTrainingEvents;

    @Column(name = "jht_hunter_exam_training_events_overridden", nullable = false)
    private boolean hunterExamOfficialTrainingEventsOverridden;

    // Metsästäjätutkintokoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_hunter_exam_training_participants")
    private Integer hunterExamOfficialTrainingParticipants;

    @Column(name = "jht_hunter_exam_training_participants_overridden", nullable = false)
    private boolean hunterExamOfficialTrainingParticipantsOverridden;

    // Riistavahinkokoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_game_damage_training_events")
    private Integer gameDamageTrainingEvents;

    @Column(name = "jht_game_damage_training_events_overridden", nullable = false)
    private boolean gameDamageTrainingEventsOverridden;

    // Riistavahinkokoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_game_damage_training_participants")
    private Integer gameDamageTrainingParticipants;

    @Column(name = "jht_game_damage_training_participants_overridden", nullable = false)
    private boolean gameDamageTrainingParticipantsOverridden;

    // Metsästyksenvalvojakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_hunting_control_training_events")
    private Integer huntingControlTrainingEvents;

    @Column(name = "jht_hunting_control_training_events_overridden", nullable = false)
    private boolean huntingControlTrainingEventsOverridden;

    // Metsästyksenvalvojakoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_hunting_control_training_participants")
    private Integer huntingControlTrainingParticipants;

    @Column(name = "jht_hunting_control_training_participants_overridden", nullable = false)
    private boolean huntingControlTrainingParticipantsOverridden;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "jht_trainings_last_modified")
    private DateTime lastModified;

    public JHTTrainingStatistics() {
    }

    public JHTTrainingStatistics(@Nonnull final JHTTrainingStatistics that) {
        requireNonNull(that);

        this.shootingTestTrainingEvents = that.shootingTestTrainingEvents;
        this.shootingTestTrainingEventsOverridden = that.shootingTestTrainingEventsOverridden;

        this.shootingTestTrainingParticipants = that.shootingTestTrainingParticipants;
        this.shootingTestTrainingParticipantsOverridden = that.shootingTestTrainingParticipantsOverridden;

        this.hunterExamOfficialTrainingEvents = that.hunterExamOfficialTrainingEvents;
        this.hunterExamOfficialTrainingEventsOverridden = that.hunterExamOfficialTrainingEventsOverridden;

        this.hunterExamOfficialTrainingParticipants = that.hunterExamOfficialTrainingParticipants;
        this.hunterExamOfficialTrainingParticipantsOverridden = that.hunterExamOfficialTrainingParticipantsOverridden;

        this.gameDamageTrainingEvents = that.gameDamageTrainingEvents;
        this.gameDamageTrainingEventsOverridden = that.gameDamageTrainingEventsOverridden;

        this.gameDamageTrainingParticipants = that.gameDamageTrainingParticipants;
        this.gameDamageTrainingParticipantsOverridden = that.gameDamageTrainingParticipantsOverridden;

        this.huntingControlTrainingEvents = that.huntingControlTrainingEvents;
        this.huntingControlTrainingEventsOverridden = that.huntingControlTrainingEventsOverridden;

        this.huntingControlTrainingParticipants = that.huntingControlTrainingParticipants;
        this.huntingControlTrainingParticipantsOverridden = that.huntingControlTrainingParticipantsOverridden;

        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.JHT_TRAINING;
    }

    @Override
    public boolean isEqualTo(@Nonnull final JHTTrainingStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(shootingTestTrainingEvents, that.shootingTestTrainingEvents) &&
                Objects.equals(shootingTestTrainingParticipants, that.shootingTestTrainingParticipants) &&

                Objects.equals(hunterExamOfficialTrainingEvents, that.hunterExamOfficialTrainingEvents) &&
                Objects.equals(hunterExamOfficialTrainingParticipants, that.hunterExamOfficialTrainingParticipants) &&

                Objects.equals(gameDamageTrainingEvents, that.gameDamageTrainingEvents) &&
                Objects.equals(gameDamageTrainingParticipants, that.gameDamageTrainingParticipants) &&

                Objects.equals(huntingControlTrainingEvents, that.huntingControlTrainingEvents) &&
                Objects.equals(huntingControlTrainingParticipants, that.huntingControlTrainingParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final JHTTrainingStatistics that) {
        // Includes manually updateable fields only.

        if (!Objects.equals(this.shootingTestTrainingEvents, that.shootingTestTrainingEvents)) {
            this.shootingTestTrainingEventsOverridden = true;
        }
        this.shootingTestTrainingEvents = that.shootingTestTrainingEvents;

        if (!Objects.equals(this.shootingTestTrainingParticipants, that.shootingTestTrainingParticipants)) {
            this.shootingTestTrainingParticipantsOverridden = true;
        }
        this.shootingTestTrainingParticipants = that.shootingTestTrainingParticipants;

        if (!Objects.equals(this.hunterExamOfficialTrainingEvents, that.hunterExamOfficialTrainingEvents)) {
            this.hunterExamOfficialTrainingEventsOverridden = true;
        }
        this.hunterExamOfficialTrainingEvents = that.hunterExamOfficialTrainingEvents;

        if (!Objects.equals(this.hunterExamOfficialTrainingParticipants, that.hunterExamOfficialTrainingParticipants)) {
            this.hunterExamOfficialTrainingParticipantsOverridden = true;
        }
        this.hunterExamOfficialTrainingParticipants = that.hunterExamOfficialTrainingParticipants;

        if (!Objects.equals(this.gameDamageTrainingEvents, that.gameDamageTrainingEvents)) {
            this.gameDamageTrainingEventsOverridden = true;
        }
        this.gameDamageTrainingEvents = that.gameDamageTrainingEvents;

        if (!Objects.equals(this.gameDamageTrainingParticipants, that.gameDamageTrainingParticipants)) {
            this.gameDamageTrainingParticipantsOverridden = true;
        }
        this.gameDamageTrainingParticipants = that.gameDamageTrainingParticipants;

        if (!Objects.equals(this.huntingControlTrainingEvents, that.huntingControlTrainingEvents)) {
            this.huntingControlTrainingEventsOverridden = true;
        }
        this.huntingControlTrainingEvents = that.huntingControlTrainingEvents;

        if (!Objects.equals(this.huntingControlTrainingParticipants, that.huntingControlTrainingParticipants)) {
            this.huntingControlTrainingParticipantsOverridden = true;
        }
        this.huntingControlTrainingParticipants = that.huntingControlTrainingParticipants;
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

    @Nullable
    Integer countJhtTrainingEvents() {
        return nullableIntSum(
                shootingTestTrainingEvents, hunterExamOfficialTrainingEvents, gameDamageTrainingEvents,
                huntingControlTrainingEvents);
    }

    @Nullable
    Integer countJhtTrainingParticipants() {
        return nullableIntSum(
                shootingTestTrainingParticipants, hunterExamOfficialTrainingParticipants,
                gameDamageTrainingParticipants, huntingControlTrainingParticipants);
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

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isShootingTestTrainingEventsOverridden() {
        return shootingTestTrainingEventsOverridden;
    }

    public void setShootingTestTrainingEventsOverridden(final boolean shootingTestTrainingEventsOverridden) {
        this.shootingTestTrainingEventsOverridden = shootingTestTrainingEventsOverridden;
    }

    public boolean isShootingTestTrainingParticipantsOverridden() {
        return shootingTestTrainingParticipantsOverridden;
    }

    public void setShootingTestTrainingParticipantsOverridden(final boolean shootingTestTrainingParticipantsOverridden) {
        this.shootingTestTrainingParticipantsOverridden = shootingTestTrainingParticipantsOverridden;
    }

    public boolean isHunterExamOfficialTrainingEventsOverridden() {
        return hunterExamOfficialTrainingEventsOverridden;
    }

    public void setHunterExamOfficialTrainingEventsOverridden(final boolean hunterExamOfficialTrainingEventsOverridden) {
        this.hunterExamOfficialTrainingEventsOverridden = hunterExamOfficialTrainingEventsOverridden;
    }

    public boolean isHunterExamOfficialTrainingParticipantsOverridden() {
        return hunterExamOfficialTrainingParticipantsOverridden;
    }

    public void setHunterExamOfficialTrainingParticipantsOverridden(final boolean hunterExamOfficialTrainingParticipantsOverridden) {
        this.hunterExamOfficialTrainingParticipantsOverridden = hunterExamOfficialTrainingParticipantsOverridden;
    }

    public boolean isGameDamageTrainingEventsOverridden() {
        return gameDamageTrainingEventsOverridden;
    }

    public void setGameDamageTrainingEventsOverridden(final boolean gameDamageTrainingEventsOverridden) {
        this.gameDamageTrainingEventsOverridden = gameDamageTrainingEventsOverridden;
    }

    public boolean isGameDamageTrainingParticipantsOverridden() {
        return gameDamageTrainingParticipantsOverridden;
    }

    public void setGameDamageTrainingParticipantsOverridden(final boolean gameDamageTrainingParticipantsOverridden) {
        this.gameDamageTrainingParticipantsOverridden = gameDamageTrainingParticipantsOverridden;
    }

    public boolean isHuntingControlTrainingEventsOverridden() {
        return huntingControlTrainingEventsOverridden;
    }

    public void setHuntingControlTrainingEventsOverridden(final boolean huntingControlTrainingEventsOverridden) {
        this.huntingControlTrainingEventsOverridden = huntingControlTrainingEventsOverridden;
    }

    public boolean isHuntingControlTrainingParticipantsOverridden() {
        return huntingControlTrainingParticipantsOverridden;
    }

    public void setHuntingControlTrainingParticipantsOverridden(final boolean huntingControlTrainingParticipantsOverridden) {
        this.huntingControlTrainingParticipantsOverridden = huntingControlTrainingParticipantsOverridden;
    }
}
