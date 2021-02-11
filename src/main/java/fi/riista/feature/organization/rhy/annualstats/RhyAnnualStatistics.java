package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

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
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.IN_PROGRESS;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.NOT_STARTED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;
import static fi.riista.util.DateUtil.today;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.requireNonNull;

@Entity
//@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"rhy_id", "year"})}
@Access(value = AccessType.FIELD)
public class RhyAnnualStatistics extends LifecycleEntity<Long> {

    public static final int FIRST_SUBSIDY_AFFECTING_YEAR = 2018;

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
    private HunterTrainingStatistics hunterTraining = new HunterTrainingStatistics();

    @Valid
    @NotNull
    @Embedded
    private YouthTrainingStatistics youthTraining = new YouthTrainingStatistics();

    @Valid
    @NotNull
    @Embedded
    private OtherHunterTrainingStatistics otherHunterTraining = new OtherHunterTrainingStatistics();

    @Valid
    @NotNull
    @Embedded
    private PublicEventStatistics publicEvents = new PublicEventStatistics();

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
        this.state = NOT_STARTED;
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

        basicInfo.setLastModified(creationTime);
        hunterExams.setLastModified(creationTime);
        shootingTests.setLastModified(creationTime);
        huntingControl.setLastModified(creationTime);
        gameDamage.setLastModified(creationTime);
        otherPublicAdmin.setLastModified(creationTime);
        jhtTraining.setLastModified(creationTime);
        hunterTraining.setLastModified(creationTime);
        youthTraining.setLastModified(creationTime);
        hunterExamTraining.setLastModified(creationTime);
        otherHunterTraining.setLastModified(creationTime);
        publicEvents.setLastModified(creationTime);
        otherHuntingRelated.setLastModified(creationTime);
        communication.setLastModified(creationTime);
        shootingRanges.setLastModified(creationTime);
        luke.setLastModified(creationTime);
        metsahallitus.setLastModified(creationTime);
    }

    @AssertTrue
    public boolean isYearLessThanOrEqualToCurrentYear() {
        return today().getYear() >= year;
    }

    @AssertTrue
    public boolean isStateValid() {
        if (state == APPROVED) {
            return allMandatoryFieldsPresent();
        }
        if (state == UNDER_INSPECTION) {
            // exception for year 2017 by intention
            return year == 2017 || requiredFieldsPresentForInspection();
        }

        return true;
    }

    public void assertIsUpdateable(final boolean byModerator, @Nonnull final EnumLocaliser localiser) {
        requireNonNull(localiser);
        checkState(!isNew(), "updateable check should not be called for new entity");

        if (byModerator) {
            if (state == APPROVED) {
                throw AnnualStatisticsLockedException.moderatorUpdatesNotAllowedAfterApproval(localiser);
            }
        } else {
            assertIsUpdateableByCoordinator(localiser);
        }
    }

    private void assertIsUpdateableByCoordinator(@Nonnull final EnumLocaliser localiser) {
        if (!isInCoordinatorUpdateableState()) {
            throw AnnualStatisticsLockedException.coordinatorUpdatesNotAllowedAfterSubmit(localiser);
        }

        if (isEndDateForCoordinatorUpdatesPassed()) {
            throw AnnualStatisticsLockedException.endDateForCoordinatorUpdatesPassed(localiser);
        }
    }

    public boolean isUpdateableByCoordinator() {
        return isInCoordinatorUpdateableState() && !isEndDateForCoordinatorUpdatesPassed();
    }

    private boolean isInCoordinatorUpdateableState() {
        return state == NOT_STARTED || state == IN_PROGRESS;
    }

    private boolean isEndDateForCoordinatorUpdatesPassed() {
        return today().isAfter(getEndDateForCoordinatorUpdates());
    }

    // Editing is blocked from coordinator after 15.1. next year.
    public LocalDate getEndDateForCoordinatorUpdates() {
        return new LocalDate(year + 1, 1, 15);
    }

    public RhyBasicInfo getOrCreateBasicInfo() {
        if (basicInfo == null) {
            basicInfo = new RhyBasicInfo();
        }
        return basicInfo;
    }

    public HunterExamStatistics getOrCreateHunterExams() {
        if (hunterExams == null) {
            hunterExams = new HunterExamStatistics();
        }
        return hunterExams;
    }

    public AnnualShootingTestStatistics getOrCreateShootingTests() {
        if (shootingTests == null) {
            shootingTests = new AnnualShootingTestStatistics();
        }
        return shootingTests;
    }

    public HuntingControlStatistics getOrCreateHuntingControl() {
        if (huntingControl == null) {
            huntingControl = new HuntingControlStatistics();
        }
        return huntingControl;
    }

    public GameDamageStatistics getOrCreateGameDamage() {
        if (gameDamage == null) {
            gameDamage = new GameDamageStatistics();
        }
        return gameDamage;
    }

    public OtherPublicAdminStatistics getOrCreateOtherPublicAdmin() {
        if (otherPublicAdmin == null) {
            otherPublicAdmin = new OtherPublicAdminStatistics();
        }
        return otherPublicAdmin;
    }

    public SrvaEventStatistics getOrCreateSrva() {
        if (srva == null) {
            srva = new SrvaEventStatistics();
        }
        return srva;
    }

    public HunterExamTrainingStatistics getOrCreateHunterExamTraining() {
        if (hunterExamTraining == null) {
            hunterExamTraining = new HunterExamTrainingStatistics();
        }
        return hunterExamTraining;
    }

    public JHTTrainingStatistics getOrCreateJhtTraining() {
        if (jhtTraining == null) {
            jhtTraining = new JHTTrainingStatistics();
        }
        return jhtTraining;
    }

    public HunterTrainingStatistics getOrCreateHunterTraining() {
        if (hunterTraining == null) {
            hunterTraining = new HunterTrainingStatistics();
        }
        return hunterTraining;
    }

    public YouthTrainingStatistics getOrCreateYouthTraining() {
        if (youthTraining == null) {
            youthTraining = new YouthTrainingStatistics();
        }
        return youthTraining;
    }

    public OtherHunterTrainingStatistics getOrCreateOtherHunterTraining() {
        if (otherHunterTraining == null) {
            otherHunterTraining = new OtherHunterTrainingStatistics();
        }
        return otherHunterTraining;
    }

    public PublicEventStatistics getOrCreatePublicEvents() {
        if (publicEvents == null) {
            publicEvents = new PublicEventStatistics();
        }
        return publicEvents;
    }

    public OtherHuntingRelatedStatistics getOrCreateOtherHuntingRelated() {
        if (otherHuntingRelated == null) {
            otherHuntingRelated = new OtherHuntingRelatedStatistics();
        }
        return otherHuntingRelated;
    }

    public CommunicationStatistics getOrCreateCommunication() {
        if (communication == null) {
            communication = new CommunicationStatistics();
        }
        return communication;
    }

    public ShootingRangeStatistics getOrCreateShootingRanges() {
        if (shootingRanges == null) {
            shootingRanges = new ShootingRangeStatistics();
        }
        return shootingRanges;
    }

    public LukeStatistics getOrCreateLuke() {
        if (luke == null) {
            luke = new LukeStatistics();
        }
        return luke;
    }

    public MetsahallitusStatistics getOrCreateMetsahallitus() {
        if (metsahallitus == null) {
            metsahallitus = new MetsahallitusStatistics();
        }
        return metsahallitus;
    }

    public DateTime getLastModifiedTimeOfQuantitiesContributingToSubsidy() {
        final Stream<DateTime> editTimestamps = Stream
                .<AnnualStatisticsManuallyEditableFields> of(huntingControl, jhtTraining, hunterTraining, youthTraining,
                        otherHunterTraining, otherHuntingRelated, luke, metsahallitus)
                .map(AnnualStatisticsManuallyEditableFields::getLastModified);

        return Stream
                .concat(editTimestamps, Stream.of(hunterExamTraining.getHunterExamTrainingEventsLastOverridden()))
                .filter(Objects::nonNull)
                .max(comparingLong(DateTime::getMillis))
                .orElse(null);
    }

    public DateTime getLastModifiedTimeOfJhtQuantities() {
        final Stream<DateTime> editTimestamps = Stream
                .<AnnualStatisticsManuallyEditableFields> of(gameDamage, huntingControl, otherPublicAdmin)
                .map(AnnualStatisticsManuallyEditableFields::getLastModified);

        final Stream<DateTime> moderatorOverrideTmestamps = Stream
                .of(hunterExams.getHunterExamEventsLastOverridden(),
                        shootingTests.getFirearmTestEventsLastOverridden(),
                        shootingTests.getBowTestEventsLastOverridden());

        return Stream
                .concat(editTimestamps, moderatorOverrideTmestamps)
                .filter(Objects::nonNull)
                .max(comparingLong(DateTime::getMillis))
                .orElse(null);
    }

    public boolean isReadyForInspection() {
        return isInCoordinatorUpdateableState() && requiredFieldsPresentForInspection();
    }

    public boolean isCompleteForApproval() {
        return state == UNDER_INSPECTION && allMandatoryFieldsPresent();
    }

    private boolean requiredFieldsPresentForInspection() {
        return streamFieldsets().allMatch(f -> f != null && f.isReadyForInspection());
    }

    private boolean allMandatoryFieldsPresent() {
        return streamFieldsets().allMatch(f -> f != null && f.isCompleteForApproval());
    }

    private Stream<? extends AnnualStatisticsFieldsetReadiness> streamFieldsets() {
        return Stream.of(
                basicInfo, hunterExams, shootingTests, gameDamage, huntingControl, otherPublicAdmin, hunterExamTraining,
                jhtTraining, hunterTraining, youthTraining, otherHunterTraining, publicEvents, otherHuntingRelated, communication,
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
        this.huntingControl = requireNonNull(huntingControl);
    }

    public void setGameDamage(@Nonnull final GameDamageStatistics gameDamage) {
        this.gameDamage = requireNonNull(gameDamage);
    }

    public void setOtherPublicAdmin(@Nonnull final OtherPublicAdminStatistics otherPublicAdmin) {
        this.otherPublicAdmin = requireNonNull(otherPublicAdmin);
    }

    public void setSrva(@Nonnull final SrvaEventStatistics srva) {
        this.srva = requireNonNull(srva);
    }

    public void setHunterExamTraining(@Nonnull final HunterExamTrainingStatistics hunterExamTraining) {
        this.hunterExamTraining = requireNonNull(hunterExamTraining);
    }

    public void setJhtTraining(@Nonnull final JHTTrainingStatistics jhtTraining) {
        this.jhtTraining = requireNonNull(jhtTraining);
    }

    public void setHunterTraining(@Nonnull final HunterTrainingStatistics hunterTraining) {
        this.hunterTraining = requireNonNull(hunterTraining);
    }

    public void setYouthTraining(@Nonnull final YouthTrainingStatistics youthTraining) {
        this.youthTraining = requireNonNull(youthTraining);
    }

    public void setOtherHunterTraining(@Nonnull final OtherHunterTrainingStatistics otherHunterTraining) {
        this.otherHunterTraining = requireNonNull(otherHunterTraining);
    }

    public void setPublicEvents(@Nonnull final PublicEventStatistics publicEvents) {
        this.publicEvents = requireNonNull(publicEvents);
    }

    public void setOtherHuntingRelated(@Nonnull final OtherHuntingRelatedStatistics otherHuntingRelated) {
        this.otherHuntingRelated = requireNonNull(otherHuntingRelated);
    }

    public void setCommunication(@Nonnull final CommunicationStatistics communication) {
        this.communication = requireNonNull(communication);
    }

    public void setShootingRanges(@Nonnull final ShootingRangeStatistics shootingRanges) {
        this.shootingRanges = requireNonNull(shootingRanges);
    }

    public void setLuke(@Nonnull final LukeStatistics luke) {
        this.luke = requireNonNull(luke);
    }

    public void setMetsahallitus(@Nonnull final MetsahallitusStatistics metsahallitus) {
        this.metsahallitus = requireNonNull(metsahallitus);
    }
}
