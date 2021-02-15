package fi.riista.feature.organization.rhy.subsidy2019;

import fi.riista.feature.organization.rhy.subsidy.SubsidyAllocatedToCriterionDTO;
import fi.riista.feature.organization.rhy.subsidy2019.compensation.SubsidyAllocation2019CompensationResultDTO;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

import static fi.riista.util.NumberUtils.isPositive;
import static java.util.Objects.requireNonNull;

public class AllSubsidyAllocation2019InfoDTO {

    private final int subsidyYear;

    private final BigDecimal totalSubsidyAmountForBatch1;
    private final BigDecimal totalSubsidyAmountForBatch2;

    private final boolean subsidyBatch1AlreadyGranted;

    private final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations;
    private final List<SubsidyAllocation2019Stage4DTO> calculatedRhyAllocations;

    private final SubsidyAllocation2019CompensationResultDTO compensationResult;

    public AllSubsidyAllocation2019InfoDTO(final int subsidyYear,
                                           @Nonnull final BigDecimal totalSubsidyAmountForBatch1,
                                           @Nonnull final BigDecimal totalSubsidyAmountForBatch2,
                                           @Nonnull final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations,
                                           @Nonnull final List<SubsidyAllocation2019Stage4DTO> calculatedRhyAllocations,
                                           @Nonnull final SubsidyAllocation2019CompensationResultDTO compensationResult) {

        this.subsidyYear = subsidyYear;

        this.totalSubsidyAmountForBatch1 = requireNonNull(totalSubsidyAmountForBatch1);
        this.totalSubsidyAmountForBatch2 = requireNonNull(totalSubsidyAmountForBatch2);

        this.subsidyBatch1AlreadyGranted = isPositive(totalSubsidyAmountForBatch1);

        this.criteriaSpecificAllocations = requireNonNull(criteriaSpecificAllocations);
        this.calculatedRhyAllocations = requireNonNull(calculatedRhyAllocations);

        this.compensationResult = requireNonNull(compensationResult);
    }

    // Accessors -->

    public int getSubsidyYear() {
        return subsidyYear;
    }

    public BigDecimal getTotalSubsidyAmountForBatch1() {
        return totalSubsidyAmountForBatch1;
    }

    public BigDecimal getTotalSubsidyAmountForBatch2() {
        return totalSubsidyAmountForBatch2;
    }

    public boolean isSubsidyBatch1AlreadyGranted() {
        return subsidyBatch1AlreadyGranted;
    }

    public List<SubsidyAllocatedToCriterionDTO> getCriteriaSpecificAllocations() {
        return criteriaSpecificAllocations;
    }

    public List<SubsidyAllocation2019Stage4DTO> getCalculatedRhyAllocations() {
        return calculatedRhyAllocations;
    }

    public SubsidyAllocation2019CompensationResultDTO getCompensationResult() {
        return compensationResult;
    }
}
