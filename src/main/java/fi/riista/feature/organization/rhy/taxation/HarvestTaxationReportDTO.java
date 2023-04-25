package fi.riista.feature.organization.rhy.taxation;

import static fi.riista.feature.organization.rhy.taxation.HarvestTaxationReport.MAX_PERCENTAGE;
import static fi.riista.feature.organization.rhy.taxation.HarvestTaxationReport.MIN_PERCENTAGE;
import static java.util.Objects.requireNonNull;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.organization.Organisation;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.validation.DoNotValidate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

public class HarvestTaxationReportDTO extends BaseEntityDTO<Long> {

    private Long id;

    private Integer rev;

    @NotNull
    private HarvestTaxationReportState state;

    @NotNull
    private Integer huntingYear;
    @NotNull
    private Integer gameSpeciesCode;
    @NotNull
    private long rhyId;
    @NotNull
    private Integer htaId;
    private Boolean hasTaxationPlanning;

    @Min(value = 0)
    private Integer planningBasisPopulation;

    @PositiveOrZero
    private Double plannedRemainingPopulation;
    @PositiveOrZero
    private Double genderDistribution;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    private Integer youngPercent;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    private Integer plannedUtilizationRateOfThePermits;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    private Integer shareOfBankingPermits;

    @PositiveOrZero
    @Max(9999)
    private Integer plannedPermitMin;
    @PositiveOrZero
    @Max(9999)
    private Integer plannedPermitMax;
    @PositiveOrZero
    @Max(99999)
    private Integer plannedCatchMin;
    @PositiveOrZero
    @Max(99999)
    private Integer plannedCatchMax;
    @PositiveOrZero
    @Max(999)
    private Double plannedPreyDensityMin;
    @PositiveOrZero
    @Max(999)
    private Double plannedPreyDensityMax;
    @PositiveOrZero
    @Max(999)
    private Double plannedPermitDensityMin;
    @PositiveOrZero
    @Max(999)
    private Double plannedPermitDensityMax;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    private Integer plannedCatchYoungPercent;

    @Min(MIN_PERCENTAGE)
    @Max(MAX_PERCENTAGE)
    private Integer plannedCatchMalePercent;

    private LocalDate stakeholdersConsulted;

    private LocalDate approvedAtTheBoardMeeting;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String modifiedUser;
    private LocalDate modifiedDate;

    @DoNotValidate
    private List<HarvestTaxationReportAttachmentDTO> attachments = new ArrayList<>();

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String justification;

    @AssertTrue
    public boolean isAllNeededFieldsFilledBeforeConfirmation() {
        if (!this.getState().equals(HarvestTaxationReportState.CONFIRMED)) {
            return true;
        }
        if (!this.getHasTaxationPlanning()) {
            return true;
        }
        return this.planningBasisPopulation != null &&
                this.getPlannedRemainingPopulation() != null &&
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
        return this.getGameSpeciesCode() != null &&
                GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.contains(this.getGameSpeciesCode());
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
                DateUtil.today().plusDays(1).isAfter(this.getApprovedAtTheBoardMeeting());
    }

    @AssertTrue
    public boolean isStakeholdersConsultedPastOrPresent() {
        return this.getStakeholdersConsulted() == null ||
                DateUtil.today().plusDays(1).isAfter(this.getStakeholdersConsulted());
    }


    public static HarvestTaxationReportDTO create(@Nonnull final HarvestTaxationReport entity,
                                                  @Nonnull final GameSpecies species,
                                                  @Nonnull final Organisation rhy,
                                                  @Nonnull final GISHirvitalousalue hta,
                                                  final List<HarvestTaxationReportAttachment> attachments,
                                                  final LastModifierDTO lastModifier) {
        requireNonNull(entity);
        requireNonNull(species);
        requireNonNull(rhy);
        requireNonNull(hta);


        final HarvestTaxationReportDTO dto = new HarvestTaxationReportDTO();
        DtoUtil.copyBaseFields(entity, dto);

        dto.setGameSpeciesCode(species.getOfficialCode());

        dto.setHuntingYear(entity.getHuntingYear());

        dto.setRhyId(rhy.getId());

        dto.setHtaId(hta.getId());


        dto.setHasTaxationPlanning(entity.getHasTaxationPlanning());
        dto.setPlanningBasisPopulation(entity.getPlanningBasisPopulation());
        dto.setPlannedRemainingPopulation(entity.getPlannedRemainingPopulation());
        dto.setGenderDistribution(entity.getGenderDistribution());
        dto.setYoungPercent(entity.getYoungPercent());
        dto.setPlannedUtilizationRateOfThePermits(entity.getPlannedUtilizationRateOfThePermits());
        dto.setShareOfBankingPermits(entity.getShareOfBankingPermits());
        dto.setPlannedPermitMin(entity.getPlannedPermitMin());
        dto.setPlannedPermitMax(entity.getPlannedPermitMax());
        dto.setPlannedCatchMin(entity.getPlannedCatchMin());
        dto.setPlannedCatchMax(entity.getPlannedCatchMax());
        dto.setPlannedPreyDensityMin(entity.getPlannedPreyDensityMin());
        dto.setPlannedPreyDensityMax(entity.getPlannedPreyDensityMax());
        dto.setPlannedPermitDensityMin(entity.getPlannedPermitDensityMin());
        dto.setPlannedPermitDensityMax(entity.getPlannedPermitDensityMax());
        dto.setPlannedCatchYoungPercent(entity.getPlannedCatchYoungPercent());
        dto.setPlannedCatchMalePercent(entity.getPlannedCatchMalePercent());
        dto.setStakeholdersConsulted(entity.getStakeholdersConsulted());
        dto.setApprovedAtTheBoardMeeting(entity.getApprovedAtTheBoardMeeting());
        dto.setJustification(entity.getJustification());

        dto.setState(entity.getHarvestTaxationReportState());

        dto.setModifiedUser(F.mapNullable(lastModifier, LastModifierDTO::getFullName));
        dto.setModifiedDate(DateUtil.toLocalDateNullSafe(entity.getModificationTime()));

        attachments.forEach(attachment -> dto.getAttachments().add(HarvestTaxationReportAttachmentDTO.create(attachment))
        );

        return dto;
    }

    public HarvestTaxationReportDTO() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final long rhyId) {
        this.rhyId = rhyId;
    }

    public Integer getHtaId() {
        return htaId;
    }

    public void setHtaId(final Integer htaId) {
        this.htaId = htaId;
    }

    public HarvestTaxationReportState getState() {
        return state;
    }

    public void setState(final HarvestTaxationReportState state) {
        this.state = state;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Boolean getHasTaxationPlanning() {
        return hasTaxationPlanning;
    }

    public void setHasTaxationPlanning(final Boolean hasTaxationPlanning) {
        this.hasTaxationPlanning = hasTaxationPlanning;
    }

    public Double getPlannedRemainingPopulation() {
        return plannedRemainingPopulation;
    }

    public Integer getPlanningBasisPopulation() {
        return planningBasisPopulation;
    }

    public void setPlanningBasisPopulation(final Integer planningBasisPopulation) {
        this.planningBasisPopulation = planningBasisPopulation;
    }

    public void setPlannedRemainingPopulation(final Double plannedRemainingPopulation) {
        this.plannedRemainingPopulation = plannedRemainingPopulation;
    }

    public Double getGenderDistribution() {
        return genderDistribution;
    }

    public void setGenderDistribution(final Double genderDistribution) {
        this.genderDistribution = genderDistribution;
    }

    public void setAttachments(final List<HarvestTaxationReportAttachmentDTO> attachments) {
        this.attachments = attachments;
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

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(final String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(final LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public List<HarvestTaxationReportAttachmentDTO> getAttachments() {
        return attachments;
    }
}
