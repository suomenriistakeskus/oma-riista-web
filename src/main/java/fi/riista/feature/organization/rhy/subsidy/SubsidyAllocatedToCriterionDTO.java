package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;
import static java.util.Objects.requireNonNull;

public class SubsidyAllocatedToCriterionDTO {

    private final SubsidyAllocationCriterion criterion;

    // Total monetary amount allocated for criterion
    private final BigDecimal totalAmount;

    // Represents monetary amount that is obtained by dividing the total subsidy
    // amount allocated for one subsidy criterion by sum of sharing parties'
    // quantities related to the criterion.
    private final BigDecimal unitAmount;

    public SubsidyAllocatedToCriterionDTO(@Nonnull final SubsidyAllocationCriterion criterion,
                                          @Nonnull final BigDecimal totalAmount,
                                          @Nonnull final BigDecimal unitAmount) {

        this.criterion = requireNonNull(criterion);
        this.totalAmount = requireNonNull(totalAmount);
        this.unitAmount = requireNonNull(unitAmount);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof SubsidyAllocatedToCriterionDTO)) {
            return false;
        } else {
            final SubsidyAllocatedToCriterionDTO that = (SubsidyAllocatedToCriterionDTO) o;

            return this.criterion == that.criterion
                    && Objects.equals(this.totalAmount, that.totalAmount)
                    && Objects.equals(this.unitAmount, that.unitAmount);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(criterion, totalAmount, unitAmount);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", criterion.name(), totalAmount, unitAmount);
    }

    // Accessors -->

    public SubsidyAllocationCriterion getCriterion() {
        return criterion;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }
}
