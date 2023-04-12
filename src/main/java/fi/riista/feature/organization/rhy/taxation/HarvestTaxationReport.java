package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.HashSet;
import java.util.Set;

import static fi.riista.util.DateUtil.today;

@Entity
@Access(value = AccessType.FIELD)
public class HarvestTaxationReport extends LifecycleEntity<Long> {

    public static final int MIN_PERCENTAGE = 0;
    public static final int MAX_PERCENTAGE = 100;

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HarvestTaxationReportState harvestTaxationReportState;

    @NotNull
    @Column(nullable = false)
    private Integer huntingYear;


    @NotNull
    @JoinColumn(name = "game_species_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GameSpecies species;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Riistanhoitoyhdistys rhy;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GISHirvitalousalue hta;

    @NotNull
    @Column(nullable = false)
    private Boolean hasTaxationPlanning;

    @Column
    @Min(value = 0)
    @Max(99999)
    private Integer planningBasisPopulation;

    @Column
    @PositiveOrZero
    private Double genderDistribution;

    @Column
    @PositiveOrZero
    private Double plannedRemainingPopulation;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    @Column
    private Integer youngPercent;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    @Column
    private Integer plannedUtilizationRateOfThePermits;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    @Column
    private Integer shareOfBankingPermits;

    @Column
    @PositiveOrZero
    @Max(9999)
    private Integer plannedPermitMin;

    @Column
    @PositiveOrZero
    @Max(9999)
    private Integer plannedPermitMax;

    @Column
    @PositiveOrZero
    @Max(99999)
    private Integer plannedCatchMin;

    @Column
    @PositiveOrZero
    @Max(99999)
    private Integer plannedCatchMax;

    @Column
    @PositiveOrZero
    @Max(999)
    private Double plannedPreyDensityMin;

    @Column
    @PositiveOrZero
    @Max(999)
    private Double plannedPreyDensityMax;

    @Column
    @PositiveOrZero
    @Max(999)
    private Double plannedPermitDensityMin;

    @Column
    @PositiveOrZero
    @Max(999)
    private Double plannedPermitDensityMax;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    @Column
    private Integer plannedCatchYoungPercent;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    @Column
    private Integer plannedCatchMalePercent;

    @Column
    private LocalDate stakeholdersConsulted;

    @Column
    private LocalDate approvedAtTheBoardMeeting;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String justification;

    @OneToMany(mappedBy = "harvestTaxationReport")
    private Set<HarvestTaxationReportAttachment> attachments = new HashSet<>();

    public HarvestTaxationReport() {
    }

    @AssertTrue
    public boolean isAllNeededFieldsFilledBeforeConfirmation() {
        if (!this.getHarvestTaxationReportState().equals(HarvestTaxationReportState.CONFIRMED)) {
            return true;
        }
        if (!this.getHasTaxationPlanning()) {
            return true;
        }
        return this.getPlannedRemainingPopulation() != null &&
                this.getGenderDistribution() != null &&
                this.getYoungPercent() != null &&
                this.getPlannedUtilizationRateOfThePermits() != null &&
                this.getShareOfBankingPermits() != null &&
                this.getPlannedPermitMin() != null &&
                this.getPlannedPermitMax() != null &&
                this.getPlannedCatchMin() != null &&
                this.getPlannedCatchMax() != null &&
                this.getPlannedPreyDensityMin() != null &&
                this.getPlannedPreyDensityMax() != null &&
                this.getPlannedPermitDensityMin() != null &&
                this.getPlannedPermitDensityMax() != null &&
                this.getPlannedCatchYoungPercent() != null &&
                this.getPlannedCatchMalePercent() != null;
    }

    @AssertFalse
    public boolean isHuntingYearInThePast() {
        final int huntingYear = DateUtil.huntingYear();
        return this.getHuntingYear() < huntingYear;
    }

    @AssertTrue
    public boolean isValidSpecies() {
        return this.getSpecies() != null &&
                GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.contains(this.getSpecies().getOfficialCode());
    }

    @AssertFalse
    public boolean isBoardMeetingApprovalAfterConsultation() {
        return this.getStakeholdersConsulted() != null &&
                this.getApprovedAtTheBoardMeeting() != null &&
                this.getApprovedAtTheBoardMeeting().isBefore(this.getStakeholdersConsulted());
    }

    @AssertTrue
    public boolean isPlannedPermitMinSmallerOrEqualThanMax() {
        return this.getPlannedPermitMin() == null ||
                this.getPlannedPermitMax() == null ||
                this.getPlannedPermitMin() <= this.getPlannedPermitMax();
    }

    @AssertTrue
    public boolean isPlannedCatchMinSmallerOrEqualThanMax() {
        return this.getPlannedCatchMin() == null ||
                this.getPlannedCatchMax() == null ||
                this.getPlannedCatchMin() <= this.getPlannedCatchMax();
    }

    @AssertTrue
    public boolean isPlannedPreyDensityMinSmallerOrEqualThanMax() {
        return this.getPlannedPreyDensityMin() == null ||
                this.getPlannedPreyDensityMax() == null ||
                this.getPlannedPreyDensityMin() <= this.getPlannedPreyDensityMax();
    }

    @AssertTrue
    public boolean isPlannedPermitDensityMinSmallerOrEqualThanMax() {
        return this.getPlannedPermitDensityMin() == null ||
                this.getPlannedPermitDensityMax() == null ||
                this.getPlannedPermitDensityMin() <= this.getPlannedPermitDensityMax();
    }

    @AssertTrue
    public boolean isBoardMeetingPastOrPresent() {
        return this.getApprovedAtTheBoardMeeting() == null ||
                today().plusDays(1).isAfter(this.getApprovedAtTheBoardMeeting());
    }

    @AssertTrue
    public boolean isStakeholdersConsultedPastOrPresent() {
        return this.getStakeholdersConsulted() == null ||
                today().plusDays(1).isAfter(this.getStakeholdersConsulted());
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "harvest_taxation_report_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestTaxationReportState getHarvestTaxationReportState() {
        return harvestTaxationReportState;
    }

    public void setHarvestTaxationReportState(final HarvestTaxationReportState harvestTaxationReportState) {
        this.harvestTaxationReportState = harvestTaxationReportState;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public GISHirvitalousalue getHta() {
        return hta;
    }

    public void setHta(final GISHirvitalousalue hta) {
        this.hta = hta;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
    }

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpecies species) {
        this.species = species;
    }

    public Boolean getHasTaxationPlanning() {
        return hasTaxationPlanning;
    }

    public void setHasTaxationPlanning(final Boolean hasTaxationPlanning) {
        this.hasTaxationPlanning = hasTaxationPlanning;
    }

    public Integer getPlanningBasisPopulation() {
        return planningBasisPopulation;
    }

    public void setPlanningBasisPopulation(final Integer planningBasePopulation) {
        this.planningBasisPopulation = planningBasePopulation;
    }

    public Double getGenderDistribution() {
        return genderDistribution;
    }

    public void setGenderDistribution(final Double genderDistribution) {
        this.genderDistribution = genderDistribution;
    }

    public Double getPlannedRemainingPopulation() {
        return plannedRemainingPopulation;
    }

    public void setPlannedRemainingPopulation(final Double plannedRemainingPopulation) {
        this.plannedRemainingPopulation = plannedRemainingPopulation;
    }

    public Integer getYoungPercent() {
        return youngPercent;
    }

    public void setYoungPercent(final Integer youngPercent) {
        this.youngPercent = youngPercent;
    }

    public Integer getPlannedUtilizationRateOfThePermits() {
        return plannedUtilizationRateOfThePermits;
    }

    public void setPlannedUtilizationRateOfThePermits(final Integer plannedUtilizationRateOfThePermits) {
        this.plannedUtilizationRateOfThePermits = plannedUtilizationRateOfThePermits;
    }

    public Integer getShareOfBankingPermits() {
        return shareOfBankingPermits;
    }

    public void setShareOfBankingPermits(final Integer shareOfBankingPermits) {
        this.shareOfBankingPermits = shareOfBankingPermits;
    }

    public Integer getPlannedPermitMin() {
        return plannedPermitMin;
    }

    public void setPlannedPermitMin(final Integer plannedPermitMin) {
        this.plannedPermitMin = plannedPermitMin;
    }

    public Integer getPlannedPermitMax() {
        return plannedPermitMax;
    }

    public void setPlannedPermitMax(final Integer plannedPermitMax) {
        this.plannedPermitMax = plannedPermitMax;
    }

    public Integer getPlannedCatchMin() {
        return plannedCatchMin;
    }

    public void setPlannedCatchMin(final Integer plannedCatchMin) {
        this.plannedCatchMin = plannedCatchMin;
    }

    public Integer getPlannedCatchMax() {
        return plannedCatchMax;
    }

    public void setPlannedCatchMax(final Integer plannedCatchMax) {
        this.plannedCatchMax = plannedCatchMax;
    }

    public Double getPlannedPreyDensityMin() {
        return plannedPreyDensityMin;
    }

    public void setPlannedPreyDensityMin(final Double plannedPreyDensityMin) {
        this.plannedPreyDensityMin = plannedPreyDensityMin;
    }

    public Double getPlannedPreyDensityMax() {
        return plannedPreyDensityMax;
    }

    public void setPlannedPreyDensityMax(final Double plannedPreyDensityMax) {
        this.plannedPreyDensityMax = plannedPreyDensityMax;
    }

    public Double getPlannedPermitDensityMin() {
        return plannedPermitDensityMin;
    }

    public void setPlannedPermitDensityMin(final Double plannedPermitDensityMin) {
        this.plannedPermitDensityMin = plannedPermitDensityMin;
    }

    public Double getPlannedPermitDensityMax() {
        return plannedPermitDensityMax;
    }

    public void setPlannedPermitDensityMax(final Double plannedPermitDensityMax) {
        this.plannedPermitDensityMax = plannedPermitDensityMax;
    }

    public Integer getPlannedCatchYoungPercent() {
        return plannedCatchYoungPercent;
    }

    public void setPlannedCatchYoungPercent(final Integer plannedCatchYoungPercent) {
        this.plannedCatchYoungPercent = plannedCatchYoungPercent;
    }

    public Integer getPlannedCatchMalePercent() {
        return plannedCatchMalePercent;
    }

    public void setPlannedCatchMalePercent(final Integer plannedCatchMalePercent) {
        this.plannedCatchMalePercent = plannedCatchMalePercent;
    }

    public LocalDate getStakeholdersConsulted() {
        return stakeholdersConsulted;
    }

    public void setStakeholdersConsulted(final LocalDate stakeholdersConsulted) {
        this.stakeholdersConsulted = stakeholdersConsulted;
    }

    public LocalDate getApprovedAtTheBoardMeeting() {
        return approvedAtTheBoardMeeting;
    }

    public void setApprovedAtTheBoardMeeting(final LocalDate approvedAtTheBoardMeeting) {
        this.approvedAtTheBoardMeeting = approvedAtTheBoardMeeting;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    public Set<HarvestTaxationReportAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(final Set<HarvestTaxationReportAttachment> attachments) {
        this.attachments = attachments;
    }
}
