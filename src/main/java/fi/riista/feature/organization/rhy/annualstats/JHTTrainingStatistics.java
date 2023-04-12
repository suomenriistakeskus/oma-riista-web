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

import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.GAME_DAMAGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.HUNTING_CONTROL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_HUNTING_CONTROL_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.NON_SUBSIDIZABLE_SHOOTING_TEST_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantField.SHOOTING_TEST_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsParticipantFieldGroup.JHT_TRAINING_STATISTICS;
import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class JHTTrainingStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsFieldsetParticipants,
        AnnualStatisticsManuallyEditableFields<JHTTrainingStatistics>,
        Serializable {

    public static JHTTrainingStatistics reduce(@Nullable final JHTTrainingStatistics a,
                                               @Nullable final JHTTrainingStatistics b) {

        final JHTTrainingStatistics result = new JHTTrainingStatistics();
        result.setShootingTestTrainingEvents(nullableIntSum(a, b, JHTTrainingStatistics::getShootingTestTrainingEvents));
        result.setShootingTestTrainingParticipants(nullableIntSum(a, b, JHTTrainingStatistics::getShootingTestTrainingParticipants));
        result.setHunterExamOfficialTrainingEvents(nullableIntSum(a, b, JHTTrainingStatistics::getHunterExamOfficialTrainingEvents));
        result.setHunterExamOfficialTrainingParticipants(nullableIntSum(a, b, JHTTrainingStatistics::getHunterExamOfficialTrainingParticipants));
        result.setGameDamageTrainingEvents(nullableIntSum(a, b, JHTTrainingStatistics::getGameDamageTrainingEvents));
        result.setGameDamageTrainingParticipants(nullableIntSum(a, b, JHTTrainingStatistics::getGameDamageTrainingParticipants));
        result.setHuntingControlTrainingEvents(nullableIntSum(a, b, JHTTrainingStatistics::getHuntingControlTrainingEvents));
        result.setHuntingControlTrainingParticipants(nullableIntSum(a, b, JHTTrainingStatistics::getHuntingControlTrainingParticipants));

        result.setNonSubsidizableShootingTestTrainingEvents(nullableIntSum(a, b, JHTTrainingStatistics::getNonSubsidizableShootingTestTrainingEvents));
        result.setNonSubsidizableShootingTestTrainingParticipants(nullableIntSum(a, b, JHTTrainingStatistics::getNonSubsidizableShootingTestTrainingParticipants));
        result.setNonSubsidizableHunterExamOfficialTrainingEvents(nullableIntSum(a, b, JHTTrainingStatistics::getNonSubsidizableHunterExamOfficialTrainingEvents));
        result.setNonSubsidizableHunterExamOfficialTrainingParticipants(nullableIntSum(a, b, JHTTrainingStatistics::getNonSubsidizableHunterExamOfficialTrainingParticipants));
        result.setNonSubsidizableGameDamageTrainingEvents(nullableIntSum(a, b, JHTTrainingStatistics::getNonSubsidizableGameDamageTrainingEvents));
        result.setNonSubsidizableGameDamageTrainingParticipants(nullableIntSum(a, b, JHTTrainingStatistics::getNonSubsidizableGameDamageTrainingParticipants));
        result.setNonSubsidizableHuntingControlTrainingEvents(nullableIntSum(a, b, JHTTrainingStatistics::getNonSubsidizableHuntingControlTrainingEvents));
        result.setNonSubsidizableHuntingControlTrainingParticipants(nullableIntSum(a, b, JHTTrainingStatistics::getNonSubsidizableHuntingControlTrainingParticipants));

        result.setLastModified(nullsafeMax(a, b, JHTTrainingStatistics::getLastModified));
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

    @Min(0)
    @Column(name = "non_subsidizable_jht_shooting_test_training_events")
    private Integer nonSubsidizableShootingTestTrainingEvents;

    @Column(name = "jht_shooting_test_training_events_overridden", nullable = false)
    private boolean shootingTestTrainingEventsOverridden;

    // Ampumakoekoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_shooting_test_training_participants")
    private Integer shootingTestTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_jht_shooting_test_training_participants")
    private Integer nonSubsidizableShootingTestTrainingParticipants;

    @Column(name = "jht_shooting_test_training_participants_overridden", nullable = false)
    private boolean shootingTestTrainingParticipantsOverridden;

    // Metsästäjätutkintokoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_hunter_exam_training_events")
    private Integer hunterExamOfficialTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_jht_hunter_exam_training_events")
    private Integer nonSubsidizableHunterExamOfficialTrainingEvents;

    @Column(name = "jht_hunter_exam_training_events_overridden", nullable = false)
    private boolean hunterExamOfficialTrainingEventsOverridden;

    // Metsästäjätutkintokoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_hunter_exam_training_participants")
    private Integer hunterExamOfficialTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_jht_hunter_exam_training_participants")
    private Integer nonSubsidizableHunterExamOfficialTrainingParticipants;

    @Column(name = "jht_hunter_exam_training_participants_overridden", nullable = false)
    private boolean hunterExamOfficialTrainingParticipantsOverridden;

    // Riistavahinkokoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_game_damage_training_events")
    private Integer gameDamageTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_jht_game_damage_training_events")
    private Integer nonSubsidizableGameDamageTrainingEvents;

    @Column(name = "jht_game_damage_training_events_overridden", nullable = false)
    private boolean gameDamageTrainingEventsOverridden;

    // Riistavahinkokoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_game_damage_training_participants")
    private Integer gameDamageTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_jht_game_damage_training_participants")
    private Integer nonSubsidizableGameDamageTrainingParticipants;

    @Column(name = "jht_game_damage_training_participants_overridden", nullable = false)
    private boolean gameDamageTrainingParticipantsOverridden;

    // Metsästyksenvalvojakoulutustilaisuudet, lkm
    @Min(0)
    @Column(name = "jht_hunting_control_training_events")
    private Integer huntingControlTrainingEvents;

    @Min(0)
    @Column(name = "non_subsidizable_jht_hunting_control_training_events")
    private Integer nonSubsidizableHuntingControlTrainingEvents;

    @Column(name = "jht_hunting_control_training_events_overridden", nullable = false)
    private boolean huntingControlTrainingEventsOverridden;

    // Metsästyksenvalvojakoulutukseen osallistujat, lkm
    @Min(0)
    @Column(name = "jht_hunting_control_training_participants")
    private Integer huntingControlTrainingParticipants;

    @Min(0)
    @Column(name = "non_subsidizable_jht_hunting_control_training_participants")
    private Integer nonSubsidizableHuntingControlTrainingParticipants;

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
        this.nonSubsidizableShootingTestTrainingEvents = that.nonSubsidizableShootingTestTrainingEvents;
        this.shootingTestTrainingEventsOverridden = that.shootingTestTrainingEventsOverridden;

        this.shootingTestTrainingParticipants = that.shootingTestTrainingParticipants;
        this.nonSubsidizableShootingTestTrainingParticipants = that.nonSubsidizableShootingTestTrainingParticipants;
        this.shootingTestTrainingParticipantsOverridden = that.shootingTestTrainingParticipantsOverridden;

        this.hunterExamOfficialTrainingEvents = that.hunterExamOfficialTrainingEvents;
        this.nonSubsidizableHunterExamOfficialTrainingEvents = that.nonSubsidizableHunterExamOfficialTrainingEvents;
        this.hunterExamOfficialTrainingEventsOverridden = that.hunterExamOfficialTrainingEventsOverridden;

        this.hunterExamOfficialTrainingParticipants = that.hunterExamOfficialTrainingParticipants;
        this.nonSubsidizableHunterExamOfficialTrainingParticipants = that.nonSubsidizableHunterExamOfficialTrainingParticipants;
        this.hunterExamOfficialTrainingParticipantsOverridden = that.hunterExamOfficialTrainingParticipantsOverridden;

        this.gameDamageTrainingEvents = that.gameDamageTrainingEvents;
        this.nonSubsidizableGameDamageTrainingEvents = that.nonSubsidizableGameDamageTrainingEvents;
        this.gameDamageTrainingEventsOverridden = that.gameDamageTrainingEventsOverridden;

        this.gameDamageTrainingParticipants = that.gameDamageTrainingParticipants;
        this.nonSubsidizableGameDamageTrainingParticipants = that.nonSubsidizableGameDamageTrainingParticipants;
        this.gameDamageTrainingParticipantsOverridden = that.gameDamageTrainingParticipantsOverridden;

        this.huntingControlTrainingEvents = that.huntingControlTrainingEvents;
        this.nonSubsidizableHuntingControlTrainingEvents = that.nonSubsidizableHuntingControlTrainingEvents;
        this.huntingControlTrainingEventsOverridden = that.huntingControlTrainingEventsOverridden;

        this.huntingControlTrainingParticipants = that.huntingControlTrainingParticipants;
        this.nonSubsidizableHuntingControlTrainingParticipants = that.nonSubsidizableHuntingControlTrainingParticipants;
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
                Objects.equals(nonSubsidizableShootingTestTrainingEvents, that.nonSubsidizableShootingTestTrainingEvents) &&
                Objects.equals(nonSubsidizableShootingTestTrainingParticipants, that.nonSubsidizableShootingTestTrainingParticipants) &&

                Objects.equals(hunterExamOfficialTrainingEvents, that.hunterExamOfficialTrainingEvents) &&
                Objects.equals(hunterExamOfficialTrainingParticipants, that.hunterExamOfficialTrainingParticipants) &&
                Objects.equals(nonSubsidizableHunterExamOfficialTrainingEvents, that.nonSubsidizableHunterExamOfficialTrainingEvents) &&
                Objects.equals(nonSubsidizableHunterExamOfficialTrainingParticipants, that.nonSubsidizableHunterExamOfficialTrainingParticipants) &&

                Objects.equals(gameDamageTrainingEvents, that.gameDamageTrainingEvents) &&
                Objects.equals(gameDamageTrainingParticipants, that.gameDamageTrainingParticipants) &&
                Objects.equals(nonSubsidizableGameDamageTrainingEvents, that.nonSubsidizableGameDamageTrainingEvents) &&

                Objects.equals(huntingControlTrainingEvents, that.huntingControlTrainingEvents) &&
                Objects.equals(huntingControlTrainingParticipants, that.huntingControlTrainingParticipants) &&
                Objects.equals(nonSubsidizableHuntingControlTrainingEvents, that.nonSubsidizableHuntingControlTrainingEvents) &&
                Objects.equals(nonSubsidizableHuntingControlTrainingParticipants, that.nonSubsidizableHuntingControlTrainingParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final JHTTrainingStatistics that) {
        // Includes manually updateable fields only.

        if (!Objects.equals(this.shootingTestTrainingEvents, that.shootingTestTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableShootingTestTrainingEvents, that.nonSubsidizableShootingTestTrainingEvents)) {
            this.shootingTestTrainingEventsOverridden = true;
        }
        this.shootingTestTrainingEvents = that.shootingTestTrainingEvents;
        this.nonSubsidizableShootingTestTrainingEvents = that.nonSubsidizableShootingTestTrainingEvents;

        if (!Objects.equals(this.shootingTestTrainingParticipants, that.shootingTestTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableShootingTestTrainingParticipants, that.nonSubsidizableShootingTestTrainingParticipants)) {
            this.shootingTestTrainingParticipantsOverridden = true;
        }
        this.shootingTestTrainingParticipants = that.shootingTestTrainingParticipants;
        this.nonSubsidizableShootingTestTrainingParticipants = that.nonSubsidizableShootingTestTrainingParticipants;

        if (!Objects.equals(this.hunterExamOfficialTrainingEvents, that.hunterExamOfficialTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableHunterExamOfficialTrainingEvents, that.nonSubsidizableHunterExamOfficialTrainingEvents)) {
            this.hunterExamOfficialTrainingEventsOverridden = true;
        }
        this.hunterExamOfficialTrainingEvents = that.hunterExamOfficialTrainingEvents;
        this.nonSubsidizableHunterExamOfficialTrainingEvents = that.nonSubsidizableHunterExamOfficialTrainingEvents;

        if (!Objects.equals(this.hunterExamOfficialTrainingParticipants, that.hunterExamOfficialTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableHunterExamOfficialTrainingParticipants, that.nonSubsidizableHunterExamOfficialTrainingParticipants)) {
            this.hunterExamOfficialTrainingParticipantsOverridden = true;
        }
        this.hunterExamOfficialTrainingParticipants = that.hunterExamOfficialTrainingParticipants;
        this.nonSubsidizableHunterExamOfficialTrainingParticipants = that.nonSubsidizableHunterExamOfficialTrainingParticipants;

        if (!Objects.equals(this.gameDamageTrainingEvents, that.gameDamageTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableGameDamageTrainingEvents, that.nonSubsidizableGameDamageTrainingEvents)) {
            this.gameDamageTrainingEventsOverridden = true;
        }
        this.gameDamageTrainingEvents = that.gameDamageTrainingEvents;
        this.nonSubsidizableGameDamageTrainingEvents = that.nonSubsidizableGameDamageTrainingEvents;

        if (!Objects.equals(this.gameDamageTrainingParticipants, that.gameDamageTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableGameDamageTrainingParticipants, that.nonSubsidizableGameDamageTrainingParticipants)) {
            this.gameDamageTrainingParticipantsOverridden = true;
        }
        this.gameDamageTrainingParticipants = that.gameDamageTrainingParticipants;
        this.nonSubsidizableGameDamageTrainingParticipants = that.nonSubsidizableGameDamageTrainingParticipants;

        if (!Objects.equals(this.huntingControlTrainingEvents, that.huntingControlTrainingEvents) ||
                !Objects.equals(this.nonSubsidizableHuntingControlTrainingEvents, that.nonSubsidizableHuntingControlTrainingEvents)) {
            this.huntingControlTrainingEventsOverridden = true;
        }
        this.huntingControlTrainingEvents = that.huntingControlTrainingEvents;
        this.nonSubsidizableHuntingControlTrainingEvents = that.nonSubsidizableHuntingControlTrainingEvents;

        if (!Objects.equals(this.huntingControlTrainingParticipants, that.huntingControlTrainingParticipants) ||
                !Objects.equals(this.nonSubsidizableHuntingControlTrainingParticipants, that.nonSubsidizableHuntingControlTrainingParticipants)) {
            this.huntingControlTrainingParticipantsOverridden = true;
        }
        this.huntingControlTrainingParticipants = that.huntingControlTrainingParticipants;
        this.nonSubsidizableHuntingControlTrainingParticipants = that.nonSubsidizableHuntingControlTrainingParticipants;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(
                shootingTestTrainingEvents, shootingTestTrainingParticipants,
                hunterExamOfficialTrainingEvents, hunterExamOfficialTrainingParticipants,
                gameDamageTrainingEvents, gameDamageTrainingParticipants,
                huntingControlTrainingEvents, huntingControlTrainingParticipants) && hasParticipants();
    }

    private boolean hasParticipants() {
        return listMissingParticipants()._2.isEmpty();
    }

    @Override
    public Tuple2<AnnualStatisticsParticipantFieldGroup, List<AnnualStatisticsParticipantField>> listMissingParticipants() {
        final List<AnnualStatisticsParticipantField> missing = new ArrayList<>();
        if (shootingTestTrainingEvents != null && shootingTestTrainingEvents > 0 &&  shootingTestTrainingParticipants <= 0) {
            missing.add(SHOOTING_TEST_TRAINING_EVENTS);
        }
        if (hunterExamOfficialTrainingEvents != null && hunterExamOfficialTrainingEvents > 0 &&  hunterExamOfficialTrainingParticipants <= 0) {
            missing.add(HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS);
        }
        if (gameDamageTrainingEvents != null && gameDamageTrainingEvents > 0 &&  gameDamageTrainingParticipants <= 0) {
            missing.add(GAME_DAMAGE_TRAINING_EVENTS);
        }
        if (huntingControlTrainingEvents != null && huntingControlTrainingEvents > 0 &&  huntingControlTrainingParticipants <= 0) {
            missing.add(HUNTING_CONTROL_TRAINING_EVENTS);
        }
        if (nonSubsidizableShootingTestTrainingEvents != null && nonSubsidizableShootingTestTrainingEvents > 0 &&
                nonSubsidizableShootingTestTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_SHOOTING_TEST_TRAINING_EVENTS);
        }
        if (nonSubsidizableHunterExamOfficialTrainingEvents != null && nonSubsidizableHunterExamOfficialTrainingEvents > 0 &&
                nonSubsidizableHunterExamOfficialTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_HUNTER_EXAM_OFFICIAL_TRAINING_EVENTS);
        }
        if (nonSubsidizableGameDamageTrainingEvents != null && nonSubsidizableGameDamageTrainingEvents > 0 &&
                nonSubsidizableGameDamageTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_GAME_DAMAGE_TRAINING_EVENTS);
        }
        if (nonSubsidizableHuntingControlTrainingEvents != null && nonSubsidizableHuntingControlTrainingEvents > 0 &&
                nonSubsidizableHuntingControlTrainingParticipants <= 0) {
            missing.add(NON_SUBSIDIZABLE_HUNTING_CONTROL_TRAINING_EVENTS);
        }
        return Tuple.of(JHT_TRAINING_STATISTICS, missing);
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
    Integer countJhtNonSubsidizableTrainingEvents() {
        return nullableIntSum(
                nonSubsidizableShootingTestTrainingEvents, nonSubsidizableHunterExamOfficialTrainingEvents,
                nonSubsidizableGameDamageTrainingEvents, nonSubsidizableHuntingControlTrainingEvents);
    }

    @Nullable
    Integer countJhtTrainingParticipants() {
        return nullableIntSum(
                shootingTestTrainingParticipants, hunterExamOfficialTrainingParticipants,
                gameDamageTrainingParticipants, huntingControlTrainingParticipants);
    }

    @Nullable
    Integer countNonSubsidizableJhtTrainingParticipants() {
        return nullableIntSum(
                nonSubsidizableShootingTestTrainingParticipants, nonSubsidizableHunterExamOfficialTrainingParticipants,
                nonSubsidizableGameDamageTrainingParticipants, nonSubsidizableHuntingControlTrainingParticipants);
    }

    // Accessors -->


    public Integer getShootingTestTrainingEvents() {
        return shootingTestTrainingEvents;
    }

    public void setShootingTestTrainingEvents(final Integer shootingTestTrainingEvents) {
        this.shootingTestTrainingEvents = shootingTestTrainingEvents;
    }

    public Integer getNonSubsidizableShootingTestTrainingEvents() {
        return nonSubsidizableShootingTestTrainingEvents;
    }

    public void setNonSubsidizableShootingTestTrainingEvents(final Integer nonSubsidizableShootingTestTrainingEvents) {
        this.nonSubsidizableShootingTestTrainingEvents = nonSubsidizableShootingTestTrainingEvents;
    }

    public Integer getShootingTestTrainingParticipants() {
        return shootingTestTrainingParticipants;
    }

    public void setShootingTestTrainingParticipants(final Integer shootingTestTrainingParticipants) {
        this.shootingTestTrainingParticipants = shootingTestTrainingParticipants;
    }

    public Integer getNonSubsidizableShootingTestTrainingParticipants() {
        return nonSubsidizableShootingTestTrainingParticipants;
    }

    public void setNonSubsidizableShootingTestTrainingParticipants(final Integer nonSubsidizableShootingTestTrainingParticipants) {
        this.nonSubsidizableShootingTestTrainingParticipants = nonSubsidizableShootingTestTrainingParticipants;
    }

    public Integer getHunterExamOfficialTrainingEvents() {
        return hunterExamOfficialTrainingEvents;
    }

    public void setHunterExamOfficialTrainingEvents(final Integer hunterExamOfficialTrainingEvents) {
        this.hunterExamOfficialTrainingEvents = hunterExamOfficialTrainingEvents;
    }

    public Integer getNonSubsidizableHunterExamOfficialTrainingEvents() {
        return nonSubsidizableHunterExamOfficialTrainingEvents;
    }

    public void setNonSubsidizableHunterExamOfficialTrainingEvents(final Integer nonSubsidizableHunterExamOfficialTrainingEvents) {
        this.nonSubsidizableHunterExamOfficialTrainingEvents = nonSubsidizableHunterExamOfficialTrainingEvents;
    }

    public Integer getHunterExamOfficialTrainingParticipants() {
        return hunterExamOfficialTrainingParticipants;
    }

    public void setHunterExamOfficialTrainingParticipants(final Integer hunterExamOfficialTrainingParticipants) {
        this.hunterExamOfficialTrainingParticipants = hunterExamOfficialTrainingParticipants;
    }

    public Integer getNonSubsidizableHunterExamOfficialTrainingParticipants() {
        return nonSubsidizableHunterExamOfficialTrainingParticipants;
    }

    public void setNonSubsidizableHunterExamOfficialTrainingParticipants(final Integer nonSubsidizableHunterExamOfficialTrainingParticipants) {
        this.nonSubsidizableHunterExamOfficialTrainingParticipants = nonSubsidizableHunterExamOfficialTrainingParticipants;
    }

    public Integer getGameDamageTrainingEvents() {
        return gameDamageTrainingEvents;
    }

    public void setGameDamageTrainingEvents(final Integer gameDamageTrainingEvents) {
        this.gameDamageTrainingEvents = gameDamageTrainingEvents;
    }

    public Integer getNonSubsidizableGameDamageTrainingEvents() {
        return nonSubsidizableGameDamageTrainingEvents;
    }

    public void setNonSubsidizableGameDamageTrainingEvents(final Integer nonSubsidizableGameDamageTrainingEvents) {
        this.nonSubsidizableGameDamageTrainingEvents = nonSubsidizableGameDamageTrainingEvents;
    }

    public Integer getGameDamageTrainingParticipants() {
        return gameDamageTrainingParticipants;
    }

    public void setGameDamageTrainingParticipants(final Integer numberOfGameDamageTrainingParticipants) {
        this.gameDamageTrainingParticipants = numberOfGameDamageTrainingParticipants;
    }

    public Integer getNonSubsidizableGameDamageTrainingParticipants() {
        return nonSubsidizableGameDamageTrainingParticipants;
    }

    public void setNonSubsidizableGameDamageTrainingParticipants(final Integer nonSubsidizableGameDamageTrainingParticipants) {
        this.nonSubsidizableGameDamageTrainingParticipants = nonSubsidizableGameDamageTrainingParticipants;
    }

    public Integer getHuntingControlTrainingEvents() {
        return huntingControlTrainingEvents;
    }

    public void setHuntingControlTrainingEvents(final Integer huntingControlTrainingEvents) {
        this.huntingControlTrainingEvents = huntingControlTrainingEvents;
    }

    public Integer getNonSubsidizableHuntingControlTrainingEvents() {
        return nonSubsidizableHuntingControlTrainingEvents;
    }

    public void setNonSubsidizableHuntingControlTrainingEvents(final Integer nonSubsidizableHuntingControlTrainingEvents) {
        this.nonSubsidizableHuntingControlTrainingEvents = nonSubsidizableHuntingControlTrainingEvents;
    }

    public Integer getHuntingControlTrainingParticipants() {
        return huntingControlTrainingParticipants;
    }

    public void setHuntingControlTrainingParticipants(final Integer huntingControlTrainingParticipants) {
        this.huntingControlTrainingParticipants = huntingControlTrainingParticipants;
    }

    public Integer getNonSubsidizableHuntingControlTrainingParticipants() {
        return nonSubsidizableHuntingControlTrainingParticipants;
    }

    public void setNonSubsidizableHuntingControlTrainingParticipants(final Integer nonSubsidizableHuntingControlTrainingParticipants) {
        this.nonSubsidizableHuntingControlTrainingParticipants = nonSubsidizableHuntingControlTrainingParticipants;
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
