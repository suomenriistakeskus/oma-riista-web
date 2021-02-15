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

public class SubsidyComparisonToLastYearDTO {

    private final BigDecimal subsidyCalculatedBasedOnStatistics;

    private final BigDecimal subsidyGrantedLastYear;

    // Currently, 20% off from subsidyGrantedLastYear
    private final BigDecimal subsidyLowerLimitBasedOnLastYear;

    public static SubsidyComparisonToLastYearDTO create(@Nonnull final BigDecimal subsidyCalculatedBasedOnStatistics,
                                                        @Nonnull final BigDecimal subsidyGrantedLastYear) {

        requireNonNull(subsidyGrantedLastYear);

        final BigDecimal subsidyLowerLimitBasedOnLastYear =
                roundSubsidyLowerLimit(subsidyGrantedLastYear.multiply(MAX_ANNUAL_SUBSIDY_DECREASE_COEFFICIENT));

        return new SubsidyComparisonToLastYearDTO(
                subsidyCalculatedBasedOnStatistics, subsidyGrantedLastYear, subsidyLowerLimitBasedOnLastYear);
    }

    public static SubsidyComparisonToLastYearDTO aggregate(@Nonnull final Iterable<SubsidyComparisonToLastYearDTO> iterable) {

        final BigDecimal sumOfCalculatedSubsidies =
                nullableSum(iterable, SubsidyComparisonToLastYearDTO::getSubsidyCalculatedBasedOnStatistics);

        final BigDecimal sumOfSubsidiesGrantedLastYear =
                nullableSum(iterable, SubsidyComparisonToLastYearDTO::getSubsidyGrantedLastYear);

        final BigDecimal sumOfSubsidyLowerLimitsBasedOnLastYear =
                nullableSum(iterable, SubsidyComparisonToLastYearDTO::getSubsidyLowerLimitBasedOnLastYear);

        return new SubsidyComparisonToLastYearDTO(
                sumOfCalculatedSubsidies, sumOfSubsidiesGrantedLastYear, sumOfSubsidyLowerLimitsBasedOnLastYear);
    }

    // Exposed as package-private for tests.
    SubsidyComparisonToLastYearDTO(@Nonnull final BigDecimal subsidyCalculatedBasedOnStatistics,
                                   @Nonnull final BigDecimal subsidyGrantedLastYear,
                                   @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear) {

        this.subsidyCalculatedBasedOnStatistics = requireNonNull(subsidyCalculatedBasedOnStatistics);
        this.subsidyGrantedLastYear = requireNonNull(subsidyGrantedLastYear);
        this.subsidyLowerLimitBasedOnLastYear = requireNonNull(subsidyLowerLimitBasedOnLastYear);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof SubsidyComparisonToLastYearDTO)) {
            return false;
        } else {
            final SubsidyComparisonToLastYearDTO that = (SubsidyComparisonToLastYearDTO) o;

            return Objects.equals(this.subsidyCalculatedBasedOnStatistics, that.subsidyCalculatedBasedOnStatistics)
                    && Objects.equals(this.subsidyGrantedLastYear, that.subsidyGrantedLastYear)
                    && Objects.equals(this.subsidyLowerLimitBasedOnLastYear, that.subsidyLowerLimitBasedOnLastYear);
        }
    }

    @Override
    public int hashCode() {
        return hash(subsidyCalculatedBasedOnStatistics, subsidyGrantedLastYear, subsidyLowerLimitBasedOnLastYear);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public BigDecimal computeDifferenceOfCalculatedStatisticsToLowerLimit() {
        return subsidyCalculatedBasedOnStatistics.subtract(subsidyLowerLimitBasedOnLastYear);
    }

    public boolean isCalculatedSubsidyBelowLowerLimit() {
        return isNegative(computeDifferenceOfCalculatedStatisticsToLowerLimit());
    }

    // Accessors -->

    public BigDecimal getSubsidyCalculatedBasedOnStatistics() {
        return subsidyCalculatedBasedOnStatistics;
    }

    public BigDecimal getSubsidyGrantedLastYear() {
        return subsidyGrantedLastYear;
    }

    public BigDecimal getSubsidyLowerLimitBasedOnLastYear() {
        return subsidyLowerLimitBasedOnLastYear;
    }
}
