package fi.riista.feature.organization.rhy.subsidy.compensation;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

// Contains basic mathematical input used to perform subsidy compensation.
public class SubsidyAllocationCompensationBasis {

    private final BigDecimal totalCompensationNeed;
    private final BigDecimal sumOfSubsidiesAboveLowerLimit;
    private final BigDecimal decrementCoefficient;

    public SubsidyAllocationCompensationBasis(@Nonnull final BigDecimal totalCompensationNeed,
                                              @Nonnull final BigDecimal sumOfSubsidiesAboveLowerLimit,
                                              @Nonnull final BigDecimal decrementCoefficient) {

        this.totalCompensationNeed = requireNonNull(totalCompensationNeed);
        this.sumOfSubsidiesAboveLowerLimit = requireNonNull(sumOfSubsidiesAboveLowerLimit);
        this.decrementCoefficient = requireNonNull(decrementCoefficient);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof SubsidyAllocationCompensationBasis)) {
            return false;
        } else {
            final SubsidyAllocationCompensationBasis that = (SubsidyAllocationCompensationBasis) o;

            return Objects.equals(this.totalCompensationNeed, that.totalCompensationNeed)
                    && Objects.equals(this.sumOfSubsidiesAboveLowerLimit, that.sumOfSubsidiesAboveLowerLimit)
                    && Objects.equals(this.decrementCoefficient, that.decrementCoefficient);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCompensationNeed, sumOfSubsidiesAboveLowerLimit, decrementCoefficient);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    // Accessors -->

    public BigDecimal getTotalCompensationNeed() {
        return totalCompensationNeed;
    }

    public BigDecimal getSumOfSubsidiesAboveLowerLimit() {
        return sumOfSubsidiesAboveLowerLimit;
    }

    public BigDecimal getDecrementCoefficient() {
        return decrementCoefficient;
    }
}
