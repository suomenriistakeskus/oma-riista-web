package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationResultDTO;
import org.iban4j.Iban;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class AllSubsidyAllocationInfoDTO {

    private final int subsidyYear;

    private final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations;
    private final List<RhySubsidyStage5DTO> calculatedRhyAllocations;

    private final SubsidyAllocationCompensationResultDTO compensationResult;

    private final Map<Long, Iban> rhyIdToIbanMapping;

    public AllSubsidyAllocationInfoDTO(final int subsidyYear,
                                       @Nonnull final List<SubsidyAllocatedToCriterionDTO> criteriaSpecificAllocations,
                                       @Nonnull final List<RhySubsidyStage5DTO> calculatedRhyAllocations,
                                       @Nonnull final SubsidyAllocationCompensationResultDTO compensationResult,
                                       @Nonnull final Map<Long, Iban> rhyIdToIbanMapping) {

        this.subsidyYear = subsidyYear;

        this.criteriaSpecificAllocations = requireNonNull(criteriaSpecificAllocations);
        this.calculatedRhyAllocations = requireNonNull(calculatedRhyAllocations);

        this.compensationResult = requireNonNull(compensationResult);

        this.rhyIdToIbanMapping = requireNonNull(rhyIdToIbanMapping);
    }

    // Accessors -->

    public int getSubsidyYear() {
        return subsidyYear;
    }

    public List<SubsidyAllocatedToCriterionDTO> getCriteriaSpecificAllocations() {
        return criteriaSpecificAllocations;
    }

    public List<RhySubsidyStage5DTO> getCalculatedRhyAllocations() {
        return calculatedRhyAllocations;
    }

    public SubsidyAllocationCompensationResultDTO getCompensationResult() {
        return compensationResult;
    }

    public Map<Long, Iban> getRhyIdToIbanMapping() {
        return rhyIdToIbanMapping;
    }
}
