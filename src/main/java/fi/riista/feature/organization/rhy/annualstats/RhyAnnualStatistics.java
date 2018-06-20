package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.IN_PROGRESS;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "rhy_annual_statistics", uniqueConstraints = {@UniqueConstraint(columnNames = {"rhy_id", "year"})})
@Access(value = AccessType.FIELD)
public class RhyAnnualStatistics extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Riistanhoitoyhdistys rhy;

    @Column(nullable = false, insertable = true, updatable = false)
    private int year;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RhyAnnualStatisticsState state;

    @Valid
    @NotNull
    @Embedded
    private RhyBasicInfo basicInfo = new RhyBasicInfo();

    @Valid
    @NotNull
    @Embedded
    private HunterExamStatistics hunterExams = new HunterExamStatistics();

    @Valid
    @NotNull
    @Embedded
    private AnnualShootingTestStatistics shootingTests = new AnnualShootingTestStatistics();

    @Valid
    @NotNull
    @Embedded
    private HuntingControlStatistics huntingControl = new HuntingControlStatistics();

    @Valid
    @NotNull
    @Embedded
    private GameDamageStatistics gameDamage = new GameDamageStatistics();

    @Valid
    @NotNull
    @Embedded
    private OtherPublicAdminStatistics otherPublicAdmin = new OtherPublicAdminStatistics();

    @Valid
    @NotNull
    @Embedded
    private SrvaEventStatistics srva = new SrvaEventStatistics();

    @Valid
    @NotNull
    @Embedded
    private HunterExamTrainingStatistics hunterExamTraining = new HunterExamTrainingStatistics();

    @Valid
    @NotNull
    @Embedded
    private JHTTrainingStatistics jhtTraining = new JHTTrainingStatistics();

    @Valid
    @NotNull
    @Embedded
    private StateAidTrainingStatistics stateAidTraining = new StateAidTrainingStatistics();

    @Valid
    @NotNull
    @Embedded
    private OtherHunterTrainingStatistics otherHunterTraining = new OtherHunterTrainingStatistics();

    @Valid
    @NotNull
    @Embedded
    private OtherTrainingStatistics otherTraining = new OtherTrainingStatistics();

    @Valid
    @NotNull
    @Embedded
    private OtherHuntingRelatedStatistics otherHuntingRelated = new OtherHuntingRelatedStatistics();

    @Valid
    @NotNull
    @Embedded
    private CommunicationStatistics communication = new CommunicationStatistics();

    @Valid
    @NotNull
    @Embedded
    private ShootingRangeStatistics shootingRanges = new ShootingRangeStatistics();

    @Valid
    @NotNull
    @Embedded
    private LukeStatistics luke = new LukeStatistics();

    @Valid
    @NotNull
    @Embedded
    private MetsahallitusStatistics metsahallitus = new MetsahallitusStatistics();

    public RhyAnnualStatistics() {
        this.state = RhyAnnualStatisticsState.IN_PROGRESS;
    }

    public RhyAnnualStatistics(@Nonnull final Riistanhoitoyhdistys rhy, final int year) {
        this();
        this.rhy = requireNonNull(rhy);
        this.year = year;
    }

    @PrePersist
    @Override
    protected void prePersist() {
        super.prePersist();

        final DateTime creationTime = new DateTime(getLifecycleFields().getCreationTime());

        hunterExams.setLastModified(creationTime);
        shootingTests.setLastModified(creationTime);
        huntingControl.setLastModified(creationTime);
        gameDamage.setLastModified(creationTime);
        otherPublicAdmin.setLastModified(creationTime);
        jhtTraining.setLastModified(creationTime);
        stateAidTraining.setLastModified(creationTime);
        hunterExamTraining.setLastModified(creationTime);
        otherHunterTraining.setLastModified(creationTime);
        otherTraining.setLastModified(creationTime);
        otherHuntingRelated.setLastModified(creationTime);
        communication.setLastModified(creationTime);
        shootingRanges.setLastModified(creationTime);
        luke.setLastModified(creationTime);
        metsahallitus.setLastModified(creationTime);
    }

    @AssertTrue
    public boolean isYearLessThanOrEqualToCurrentYear() {
        return DateUtil.today().getYear() >= year;
    }

    public boolean isUpdateable(final boolean byModerator) {
        return byModerator ? state != RhyAnnualStatisticsState.APPROVED : state == RhyAnnualStatisticsState.IN_PROGRESS;
    }

    public boolean canComputedPropertiesBeRefreshed() {
        return state == RhyAnnualStatisticsState.IN_PROGRESS;
    }

    public RhyBasicInfo getOrCreateBasicInfo() {
        return basicInfo != null ? basicInfo : new RhyBasicInfo();
    }

    public HunterExamStatistics getOrCreateHunterExams() {
        return hunterExams != null ? hunterExams : new HunterExamStatistics();
    }

    public AnnualShootingTestStatistics getOrCreateShootingTests() {
        return shootingTests != null ? shootingTests : new AnnualShootingTestStatistics();
    }

    public HuntingControlStatistics getOrCreateHuntingControl() {
        return huntingControl != null ? huntingControl : new HuntingControlStatistics();
    }

    public GameDamageStatistics getOrCreateGameDamage() {
        return gameDamage != null ? gameDamage : new GameDamageStatistics();
    }

    public OtherPublicAdminStatistics getOrCreateOtherPublicAdmin() {
        return otherPublicAdmin != null ? otherPublicAdmin : new OtherPublicAdminStatistics();
    }

    public SrvaEventStatistics getOrCreateSrva() {
        return srva != null ? srva : new SrvaEventStatistics();
    }

    public HunterExamTrainingStatistics getOrCreateHunterExamTraining() {
        return hunterExamTraining != null ? hunterExamTraining : new HunterExamTrainingStatistics();
    }

    public JHTTrainingStatistics getOrCreateJhtTraining() {
        return jhtTraining != null ? jhtTraining : new JHTTrainingStatistics();
    }

    public StateAidTrainingStatistics getOrCreateStateAidTraining() {
        return stateAidTraining != null ? stateAidTraining : new StateAidTrainingStatistics();
    }

    public OtherHunterTrainingStatistics getOrCreateOtherHunterTraining() {
        return otherHunterTraining != null ? otherHunterTraining : new OtherHunterTrainingStatistics();
    }

    public OtherTrainingStatistics getOrCreateOtherTraining() {
        return otherTraining != null ? otherTraining : new OtherTrainingStatistics();
    }

    public OtherHuntingRelatedStatistics getOrCreateOtherHuntingRelated() {
        return otherHuntingRelated != null ? otherHuntingRelated : new OtherHuntingRelatedStatistics();
    }

    public CommunicationStatistics getOrCreateCommunication() {
        return communication != null ? communication : new CommunicationStatistics();
    }

    public ShootingRangeStatistics getOrCreateShootingRanges() {
        return shootingRanges != null ? shootingRanges : new ShootingRangeStatistics();
    }

    public LukeStatistics getOrCreateLuke() {
        return luke != null ? luke : new LukeStatistics();
    }

    public MetsahallitusStatistics getOrCreateMetsahallitus() {
        return metsahallitus != null ? metsahallitus : new MetsahallitusStatistics();
    }

    public int countAllShootingTestAttempts() {
        return shootingTests != null ? shootingTests.countAllShootingTestAttempts() : 0;
    }

    public int countHunterExamTrainingEvents() {
        return hunterExamTraining != null
                ? NumberUtils.getIntValueOrZero(hunterExamTraining.getHunterExamTrainingEvents())
                : 0;
    }

    public int countHunterExamTrainingParticipants() {
        return hunterExamTraining != null
                ? NumberUtils.getIntValueOrZero(hunterExamTraining.getHunterExamTrainingParticipants())
                : 0;
    }

    public int countJhtTrainingEvents() {
        return jhtTraining != null ? jhtTraining.countJhtTrainingEvents() : 0;
    }

    public int countJhtTrainingParticipants() {
        return jhtTraining != null ? jhtTraining.countJhtTrainingParticipants() : 0;
    }

    public int countAllStateAidTrainingEvents() {
        return stateAidTraining != null ? stateAidTraining.countAllStateAidTrainingEvents() : 0;
    }

    public int countAllStateAidTrainingParticipants() {
        return stateAidTraining != null ? stateAidTraining.countAllStateAidTrainingParticipants() : 0;
    }

    public int countOtherHunterTrainingEvents() {
        return otherHunterTraining != null ? otherHunterTraining.countOtherHunterTrainingEvents() : 0;
    }

    public int countOtherHunterTrainingParticipants() {
        return otherHunterTraining != null ? otherHunterTraining.countOtherHunterTrainingParticipants() : 0;
    }

    public int countOtherTrainingEvents() {
        return otherTraining != null ? NumberUtils.getIntValueOrZero(otherTraining.getOtherTrainingEvents()) : 0;
    }

    public int countOtherTrainingParticipants() {
        return otherTraining != null ? NumberUtils.getIntValueOrZero(otherTraining.getOtherTrainingParticipants()) : 0;
    }

    public int countAllTrainingEvents() {
        return countHunterExamTrainingEvents()
                + countJhtTrainingEvents()
                + countAllStateAidTrainingEvents()
                + countOtherHunterTrainingEvents()
                + countOtherTrainingEvents();
    }

    public int countAllTrainingParticipants() {
        return countHunterExamTrainingParticipants()
                + countJhtTrainingParticipants()
                + countAllStateAidTrainingParticipants()
                + countOtherHunterTrainingParticipants()
                + countOtherTrainingParticipants();
    }

    public DateTime getTimeOfLastManualModificationToStateAidAffectingQuantities() {
        final Stream.Builder<DateTime> timestamps = Stream.<DateTime> builder();

        if (huntingControl != null) {
            timestamps.add(huntingControl.getLastModified());
        }

        if (hunterExamTraining != null) {
            timestamps.add(hunterExamTraining.getHunterExamTrainingEventsLastOverridden());
        }

        if (stateAidTraining != null) {
            timestamps.add(stateAidTraining.getLastModified());
        }

        if (luke != null) {
            timestamps.add(luke.getLastModified());
        }

        if (metsahallitus != null) {
            timestamps.add(metsahallitus.getLastModified());
        }

        return timestamps.build().filter(Objects::nonNull).max(comparingLong(DateTime::getMillis)).orElse(null);
    }

    public DateTime getTimeOfLastManualModificationToPublicAdministrationTasks() {
        final Stream.Builder<DateTime> timestamps = Stream.<DateTime> builder();

        if (hunterExams != null) {
            timestamps.add(hunterExams.getHunterExamEventsLastOverridden());
        }

        if (shootingTests != null) {
            timestamps.add(shootingTests.getFirearmTestEventsLastOverridden());
            timestamps.add(shootingTests.getBowTestEventsLastOverridden());
        }

        if (gameDamage != null) {
            timestamps.add(gameDamage.getLastModified());
        }

        if (huntingControl != null) {
            timestamps.add(huntingControl.getLastModified());
        }

        if (otherPublicAdmin != null) {
            timestamps.add(otherPublicAdmin.getLastModified());
        }

        return timestamps.build().filter(Objects::nonNull).max(comparingLong(DateTime::getMillis)).orElse(null);
    }

    @AssertTrue
    public boolean isValid() {
        if (state == APPROVED) {
            return allMandatoryFieldsPresent();
        }
        if (state == UNDER_INSPECTION) {
            // exception for year 2017 by intention
            return year == 2017 || requiredFieldsPresentForInspection();
        }
        return state == IN_PROGRESS;
    }

    public boolean isReadyForInspection() {
        return state == IN_PROGRESS && requiredFieldsPresentForInspection();
    }

    public boolean isCompleteForApproval() {
        return state == UNDER_INSPECTION && allMandatoryFieldsPresent();
    }

    private boolean requiredFieldsPresentForInspection() {
        return streamManuallyEditableFieldsets().allMatch(f -> f != null && f.isReadyForInspection());
    }

    private boolean allMandatoryFieldsPresent() {
        return streamManuallyEditableFieldsets().allMatch(f -> f != null && f.isCompleteForApproval());
    }

    private Stream<? extends AnnualStatisticsFieldsetStatus> streamManuallyEditableFieldsets() {
        return Stream.of(
                basicInfo, hunterExams, shootingTests, gameDamage, huntingControl, otherPublicAdmin, hunterExamTraining,
                jhtTraining, stateAidTraining, otherHunterTraining, otherTraining, otherHuntingRelated, communication,
                shootingRanges, luke, metsahallitus);
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "rhy_annual_statistics_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public RhyAnnualStatisticsState getState() {
        return state;
    }

    public void setState(final RhyAnnualStatisticsState state) {
        this.state = state;
    }

    public void setBasicInfo(@Nonnull final RhyBasicInfo basicInfo) {
        this.basicInfo = requireNonNull(basicInfo);
    }

    public void setHunterExams(@Nonnull final HunterExamStatistics hunterExams) {
        this.hunterExams = requireNonNull(hunterExams);
    }

    public void setShootingTests(@Nonnull final AnnualShootingTestStatistics shootingTests) {
        this.shootingTests = requireNonNull(shootingTests);
    }

    public void setHuntingControl(@Nonnull final HuntingControlStatistics huntingControl) {
        requireNonNull(huntingControl);
        huntingControl.updateModificationStatusIfNotEqualTo(getOrCreateHuntingControl());
        this.huntingControl = huntingControl;
    }

    public void setGameDamage(@Nonnull final GameDamageStatistics gameDamage) {
        requireNonNull(gameDamage);
        gameDamage.updateModificationStatusIfNotEqualTo(getOrCreateGameDamage());
        this.gameDamage = gameDamage;
    }

    public void setOtherPublicAdmin(@Nonnull final OtherPublicAdminStatistics otherPublicAdmin) {
        requireNonNull(otherPublicAdmin);
        otherPublicAdmin.updateModificationStatusIfNotEqualTo(getOrCreateOtherPublicAdmin());
        this.otherPublicAdmin = otherPublicAdmin;
    }

    public void setSrva(@Nonnull final SrvaEventStatistics srva) {
        this.srva = requireNonNull(srva);
    }

    public void setHunterExamTraining(@Nonnull final HunterExamTrainingStatistics hunterExamTraining) {
        this.hunterExamTraining = requireNonNull(hunterExamTraining);
    }

    public void setJhtTraining(@Nonnull final JHTTrainingStatistics jhtTraining) {
        requireNonNull(jhtTraining);
        jhtTraining.updateModificationStatusIfNotEqualTo(getOrCreateJhtTraining());
        this.jhtTraining = jhtTraining;
    }

    public void setStateAidTraining(@Nonnull final StateAidTrainingStatistics stateAidTraining) {
        requireNonNull(stateAidTraining);
        stateAidTraining.updateModificationStatusIfNotEqualTo(getOrCreateStateAidTraining());
        this.stateAidTraining = stateAidTraining;
    }

    public void setOtherHunterTraining(@Nonnull final OtherHunterTrainingStatistics otherHunterTraining) {
        requireNonNull(otherHunterTraining);
        otherHunterTraining.updateModificationStatusIfNotEqualTo(getOrCreateOtherHunterTraining());
        this.otherHunterTraining = otherHunterTraining;
    }

    public void setOtherTraining(@Nonnull final OtherTrainingStatistics otherTraining) {
        requireNonNull(otherTraining);
        otherTraining.updateModificationStatusIfNotEqualTo(getOrCreateOtherTraining());
        this.otherTraining = otherTraining;
    }

    public void setOtherHuntingRelated(@Nonnull final OtherHuntingRelatedStatistics otherHuntingRelated) {
        requireNonNull(otherHuntingRelated);
        otherHuntingRelated.updateModificationStatusIfNotEqualTo(getOrCreateOtherHuntingRelated());
        this.otherHuntingRelated = otherHuntingRelated;
    }

    public void setCommunication(@Nonnull final CommunicationStatistics communication) {
        requireNonNull(communication);
        communication.updateModificationStatusIfNotEqualTo(getOrCreateCommunication());
        this.communication = communication;
    }

    public void setShootingRanges(@Nonnull final ShootingRangeStatistics shootingRanges) {
        requireNonNull(shootingRanges);
        shootingRanges.updateModificationStatusIfNotEqualTo(getOrCreateShootingRanges());
        this.shootingRanges = shootingRanges;
    }

    public void setLuke(@Nonnull final LukeStatistics luke) {
        requireNonNull(luke);
        luke.updateModificationStatusIfNotEqualTo(getOrCreateLuke());
        this.luke = luke;
    }

    public void setMetsahallitus(@Nonnull final MetsahallitusStatistics metsahallitus) {
        requireNonNull(metsahallitus);
        metsahallitus.updateModificationStatusIfNotEqualTo(getOrCreateMetsahallitus());
        this.metsahallitus = metsahallitus;
    }
}
