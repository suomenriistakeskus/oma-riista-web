package fi.riista.feature.organization.rhy.subsidy;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.MAX_ANNUAL_SUBSIDY_DECREASE_COEFFICIENT;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation.roundSubsidyLowerLimit;
import static fi.riista.util.NumberUtils.isNegative;
import static fi.riista.util.NumberUtils.nullableSum;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

// In addition to containing calculated subsidy for an RHY, this class holds
// also subsidy amounts granted last year and in the first batch of the current
// year.
public class SubsidyBatchInfoDTO {

    private final BigDecimal subsidyCalculatedForSecondBatchBeforeCompensation;

    private final BigDecimal subsidyGrantedInFirstBatch;

    private final BigDecimal subsidyGrantedLastYear;

    // Currently, 20% off from subsidyGrantedLastYear
    private final BigDecimal subsidyLowerLimitBasedOnLastYear;

    public static SubsidyBatchInfoDTO create(@Nonnull final BigDecimal subsidyCalculatedForSecondBatchBeforeCompensation,
                                             @Nonnull final BigDecimal subsidyGrantedInFirstBatch,
                                             @Nonnull final BigDecimal subsidyGrantedLastYear) {

        requireNonNull(subsidyGrantedLastYear);

        final BigDecimal subsidyLowerLimitBasedOnLastYear =
                roundSubsidyLowerLimit(subsidyGrantedLastYear.multiply(MAX_ANNUAL_SUBSIDY_DECREASE_COEFFICIENT));

        return new SubsidyBatchInfoDTO(
                subsidyCalculatedForSecondBatchBeforeCompensation,
                subsidyGrantedInFirstBatch,
                subsidyGrantedLastYear,
                subsidyLowerLimitBasedOnLastYear);
    }

    public static SubsidyBatchInfoDTO aggregate(@Nonnull final Iterable<SubsidyBatchInfoDTO> iterable) {

        final BigDecimal sumOfSubsidiesCalculatedForSecondBatch =
                nullableSum(iterable, SubsidyBatchInfoDTO::getSubsidyCalculatedForSecondBatchBeforeCompensation);

        final BigDecimal sumOfSubsidiesGrantedInFirstBatch =
                nullableSum(iterable, SubsidyBatchInfoDTO::getSubsidyGrantedInFirstBatch);

        final BigDecimal sumOfSubsidiesGrantedLastYear =
                nullableSum(iterable, SubsidyBatchInfoDTO::getSubsidyGrantedLastYear);

        final BigDecimal sumOfSubsidyLowerLimitsBasedOnLastYear =
                nullableSum(iterable, SubsidyBatchInfoDTO::getSubsidyLowerLimitBasedOnLastYear);

        return new SubsidyBatchInfoDTO(
                sumOfSubsidiesCalculatedForSecondBatch,
                sumOfSubsidiesGrantedInFirstBatch,
                sumOfSubsidiesGrantedLastYear,
                sumOfSubsidyLowerLimitsBasedOnLastYear);
    }

    // Exposed as package-private for tests.
    SubsidyBatchInfoDTO(@Nonnull final BigDecimal subsidyCalculatedForSecondBatchBeforeCompensation,
                        @Nonnull final BigDecimal subsidyGrantedInFirstBatch,
                        @Nonnull final BigDecimal subsidyGrantedLastYear,
                        @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear) {

        this.subsidyCalculatedForSecondBatchBeforeCompensation =
                requireNonNull(subsidyCalculatedForSecondBatchBeforeCompensation);
        this.subsidyGrantedInFirstBatch = requireNonNull(subsidyGrantedInFirstBatch);
        this.subsidyGrantedLastYear = requireNonNull(subsidyGrantedLastYear);
        this.subsidyLowerLimitBasedOnLastYear = requireNonNull(subsidyLowerLimitBasedOnLastYear);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof SubsidyBatchInfoDTO)) {
            return false;
        } else {
            final SubsidyBatchInfoDTO that = (SubsidyBatchInfoDTO) o;

            return Objects.equals(this.subsidyCalculatedForSecondBatchBeforeCompensation, that.subsidyCalculatedForSecondBatchBeforeCompensation)
                    && Objects.equals(this.subsidyGrantedInFirstBatch, that.subsidyGrantedInFirstBatch)
                    && Objects.equals(this.subsidyGrantedLastYear, that.subsidyGrantedLastYear)
                    && Objects.equals(this.subsidyLowerLimitBasedOnLastYear, that.subsidyLowerLimitBasedOnLastYear);
        }
    }

    @Override
    public int hashCode() {
        return hash(subsidyCalculatedForSecondBatchBeforeCompensation, subsidyGrantedInFirstBatch,
                subsidyGrantedLastYear, subsidyLowerLimitBasedOnLastYear);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public BigDecimal calculateTotalSubsidyForCurrentYearBeforeCompensation() {
        return subsidyGrantedInFirstBatch.add(subsidyCalculatedForSecondBatchBeforeCompensation);
    }

    public BigDecimal calculateDifferenceOfTotalSubsidyBeforeCompensationToLowerLimit() {
        return calculateTotalSubsidyForCurrentYearBeforeCompensation().subtract(subsidyLowerLimitBasedOnLastYear);
    }

    public boolean isCalculatedSubsidyBelowLowerLimit() {
        return isNegative(calculateDifferenceOfTotalSubsidyBeforeCompensationToLowerLimit());
    }

    // Accessors -->

    public BigDecimal getSubsidyCalculatedForSecondBatchBeforeCompensation() {
        return subsidyCalculatedForSecondBatchBeforeCompensation;
    }

    public BigDecimal getSubsidyGrantedInFirstBatch() {
        return subsidyGrantedInFirstBatch;
    }

    public BigDecimal getSubsidyGrantedLastYear() {
        return subsidyGrantedLastYear;
    }

    public BigDecimal getSubsidyLowerLimitBasedOnLastYear() {
        return subsidyLowerLimitBasedOnLastYear;
    }
}
