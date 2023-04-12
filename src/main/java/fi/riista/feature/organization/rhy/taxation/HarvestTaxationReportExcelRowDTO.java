package fi.riista.feature.organization.rhy.taxation;

import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class HarvestTaxationReportExcelRowDTO {

    private LocalisedString rhyName;
    private String htaCode;
    private Long areaSize;
    private Integer planningBasisPopulation;
    private Double plannedRemainingPopulation;
    private Double genderDistribution;
    private Integer youngPercent;
    private Integer plannedUtilizationRateOfThePermits;
    private Integer shareOfBankingPermits;
    private Integer plannedPermitMin;
    private Integer plannedPermitMax;
    private Integer plannedCatchMin;
    private Integer plannedCatchMax;
    private Double plannedPreyDensityMin;
    private Double plannedPreyDensityMax;
    private Double plannedPermitDensityMin;
    private Double plannedPermitDensityMax;
    private Integer plannedCatchYoungPercent;
    private Integer plannedCatchMalePercent;
    private LocalDate approvedAtTheBoardMeeting;
    private LocalDate confirmedDate;

    public HarvestTaxationReportExcelRowDTO(final String htaCode) {
        this.htaCode = htaCode;
    }

    public HarvestTaxationReportExcelRowDTO(@Nonnull final HarvestTaxationReport harvestTaxationReport,
                                            final Double areaSize
    ) {
        Objects.requireNonNull(harvestTaxationReport, "harvest taxation report is null");

        this.rhyName = harvestTaxationReport.getRhy().getNameLocalisation();
        this.htaCode = harvestTaxationReport.getHta().getNameAbbrv();
        this.areaSize = Math.round(areaSize);

        setPlanningBasisPopulation(harvestTaxationReport.getPlanningBasisPopulation());
        setPlannedRemainingPopulation(harvestTaxationReport.getPlannedRemainingPopulation());
        setGenderDistribution(harvestTaxationReport.getGenderDistribution());
        setYoungPercent(harvestTaxationReport.getYoungPercent());
        setPlannedUtilizationRateOfThePermits(harvestTaxationReport.getPlannedUtilizationRateOfThePermits());
        setShareOfBankingPermits(harvestTaxationReport.getShareOfBankingPermits());
        setPlannedPermitMin(harvestTaxationReport.getPlannedPermitMin());
        setPlannedPermitMax(harvestTaxationReport.getPlannedPermitMax());
        setPlannedCatchMin(harvestTaxationReport.getPlannedCatchMin());
        setPlannedCatchMax(harvestTaxationReport.getPlannedCatchMax());
        setPlannedPreyDensityMin(harvestTaxationReport.getPlannedPreyDensityMin());
        setPlannedPreyDensityMax(harvestTaxationReport.getPlannedPreyDensityMax());
        setPlannedPermitDensityMin(harvestTaxationReport.getPlannedPermitDensityMin());
        setPlannedPermitDensityMax(harvestTaxationReport.getPlannedPermitDensityMax());
        setPlannedCatchYoungPercent(harvestTaxationReport.getPlannedCatchYoungPercent());
        setPlannedCatchMalePercent(harvestTaxationReport.getPlannedCatchMalePercent());
        setApprovedAtTheBoardMeeting(harvestTaxationReport.getApprovedAtTheBoardMeeting());
        if (harvestTaxationReport.getHarvestTaxationReportState().equals(HarvestTaxationReportState.CONFIRMED)) {
            setConfirmedDate(harvestTaxationReport.getModificationTime().toLocalDate());
        }
    }

    public void addDataFromAnotherDTO(final HarvestTaxationReportExcelRowDTO anotherDto) {
        this.areaSize = sum(this.areaSize, anotherDto.getAreaSize());
        this.plannedPermitMin = sum(this.plannedPermitMin, anotherDto.getPlannedPermitMin());
        this.plannedPermitMax = sum(this.plannedPermitMax, anotherDto.getPlannedPermitMax());
        this.plannedCatchMin = sum(this.plannedCatchMin, anotherDto.getPlannedCatchMin());
        this.plannedCatchMax = sum(this.plannedCatchMax, anotherDto.getPlannedCatchMax());
    }

    private Integer sum(final Integer first, final Integer second) {
        if (Objects.isNull(first) && Objects.isNull(second)) {
            return null;
        }
        return Integer.sum(
                Optional.ofNullable(first).orElse(0),
                Optional.ofNullable(second).orElse(0));
    }

    private Long sum(final Long first, final Long second) {
        if (Objects.isNull(first) && Objects.isNull(second)) {
            return null;
        }
        return Long.sum(
                Optional.ofNullable(first).orElse(0L),
                Optional.ofNullable(second).orElse(0L));
    }

    public LocalisedString getRhyName() {
        return rhyName;
    }

    public void setRhyName(final LocalisedString rhyName) {
        this.rhyName = rhyName;
    }

    public String getHtaCode() {
        return htaCode;
    }

    public void setHtaCode(final String htaCode) {
        this.htaCode = htaCode;
    }

    public Long getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(final Long areaSize) {
        this.areaSize = areaSize;
    }

    public Integer getPlanningBasisPopulation() {
        return planningBasisPopulation;
    }

    public void setPlanningBasisPopulation(final Integer planningBasisPopulation) {
        this.planningBasisPopulation = planningBasisPopulation;
    }

    public Double getPlannedRemainingPopulation() {
        return plannedRemainingPopulation;
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

    public LocalDate getApprovedAtTheBoardMeeting() {
        return approvedAtTheBoardMeeting;
    }

    public void setApprovedAtTheBoardMeeting(final LocalDate approvedAtTheBoardMeeting) {
        this.approvedAtTheBoardMeeting = approvedAtTheBoardMeeting;
    }

    public LocalDate getConfirmedDate() {
        return confirmedDate;
    }

    public void setConfirmedDate(final LocalDate confirmedDate) {
        this.confirmedDate = confirmedDate;
    }
}
