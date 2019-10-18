package fi.riista.feature.organization.rhy.subsidy;

import java.math.BigDecimal;

// Holds input parameters for RHY subsidy allocation.
public class SubsidyAllocationInputDTO {

    private int subsidyYear;

    private BigDecimal totalSubsidyAmount;

    public SubsidyAllocationInputDTO() {
    }

    public SubsidyAllocationInputDTO(final int subsidyYear, final BigDecimal totalSubsidyAmount) {
        this.subsidyYear = subsidyYear;
        this.totalSubsidyAmount = totalSubsidyAmount;
    }

    public int getSubsidyYear() {
        return subsidyYear;
    }

    public void setSubsidyYear(final int subsidyYear) {
        this.subsidyYear = subsidyYear;
    }

    public BigDecimal getTotalSubsidyAmount() {
        return totalSubsidyAmount;
    }

    public void setTotalSubsidyAmount(final BigDecimal totalSubsidyAmount) {
        this.totalSubsidyAmount = totalSubsidyAmount;
    }
}
