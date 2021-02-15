package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationNeed;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyCompensationType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.isPositive;
import static java.util.Objects.requireNonNull;

public class SubsidyCompensation2019OutputDTO implements SubsidyCompensationNeed {

    private final String rhyCode;

    private final BigDecimal totalSubsidyAfterCompensation;
    private final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear;

    private final BigDecimal subsidyLowerLimitBasedOnLastYear;

    private final BigDecimal decrement;

    private final SubsidyCompensationType compensationType;

    public static SubsidyCompensation2019OutputDTO keepSubsidyUnchanged(@Nonnull final SubsidyCompensation2019InputDTO input) {
        requireNonNull(input);

        return new SubsidyCompensation2019OutputDTO(
                input.getRhyCode(),
                input.getTotalSubsidyCalculatedForCurrentYear(),
                input.getSubsidyGrantedInFirstBatchOfCurrentYear(),
                input.getSubsidyLowerLimitBasedOnLastYear(),
                input.isAlreadyCompensated());
    }

    public static SubsidyCompensation2019OutputDTO increased(@Nonnull final SubsidyCompensation2019InputDTO input,
                                                             @Nonnull final BigDecimal increment) {
        requireNonNull(input);
        requireNonNull(increment);

        checkArgument(isPositive(increment), "Increment must be positive");

        final BigDecimal increasedSubsidyForCurrentYear =
                input.getTotalSubsidyCalculatedForCurrentYear().add(increment);

        return new SubsidyCompensation2019OutputDTO(
                input.getRhyCode(),
                increasedSubsidyForCurrentYear,
                input.getSubsidyGrantedInFirstBatchOfCurrentYear(),
                input.getSubsidyLowerLimitBasedOnLastYear(),
                true);
    }

    public static SubsidyCompensation2019OutputDTO decreased(@Nonnull final SubsidyCompensation2019InputDTO input,
                                                             @Nonnull final BigDecimal decrement) {
        requireNonNull(input);
        requireNonNull(decrement);

        final BigDecimal decreasedSubsidyForCurrentYear =
                input.getTotalSubsidyCalculatedForCurrentYear().subtract(decrement);

        return new SubsidyCompensation2019OutputDTO(
                input.getRhyCode(),
                decreasedSubsidyForCurrentYear,
                input.getSubsidyGrantedInFirstBatchOfCurrentYear(),
                input.getSubsidyLowerLimitBasedOnLastYear(),
                decrement);
    }

    // Exposed as package-private for tests.
    SubsidyCompensation2019OutputDTO(@Nonnull final String rhyCode,
                                     @Nonnull final BigDecimal totalSubsidyAfterCompensation,
                                     @Nonnull final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear,
                                     @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                     final boolean compensated) {
        this(rhyCode,
                totalSubsidyAfterCompensation,
                subsidyGrantedInFirstBatchOfCurrentYear,
                subsidyLowerLimitBasedOnLastYear,
                null,
                compensated ? SubsidyCompensationType.COMPENSATED : SubsidyCompensationType.UNCHANGED);
    }

    // Exposed as package-private for tests.
    SubsidyCompensation2019OutputDTO(@Nonnull final String rhyCode,
                                     @Nonnull final BigDecimal totalSubsidyAfterCompensation,
                                     @Nonnull final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear,
                                     @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                     @Nonnull final BigDecimal decrement) {
        this(rhyCode,
                totalSubsidyAfterCompensation,
                subsidyGrantedInFirstBatchOfCurrentYear,
                subsidyLowerLimitBasedOnLastYear,
                requireNonNull(decrement),
                SubsidyCompensationType.DECREASED);
    }

    private SubsidyCompensation2019OutputDTO(@Nonnull final String rhyCode,
                                             @Nonnull final BigDecimal totalSubsidyAfterCompensation,
                                             @Nonnull final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear,
                                             @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                             @Nullable final BigDecimal decrement,
                                             @Nonnull final SubsidyCompensationType compensationType) {

        this.rhyCode = requireNonNull(rhyCode);
        this.totalSubsidyAfterCompensation = requireNonNull(totalSubsidyAfterCompensation);
        this.subsidyGrantedInFirstBatchOfCurrentYear = requireNonNull(subsidyGrantedInFirstBatchOfCurrentYear);
        this.subsidyLowerLimitBasedOnLastYear = requireNonNull(subsidyLowerLimitBasedOnLastYear);
        this.compensationType = requireNonNull(compensationType);
        this.decrement = decrement;

        if (decrement != null) {
            checkArgument(isPositive(decrement), "Decrement must be positive");
            checkArgument(compensationType == SubsidyCompensationType.DECREASED);
        } else {
            checkArgument(compensationType != SubsidyCompensationType.DECREASED);
        }
    }

    @Override
    public BigDecimal countDifferenceOfCalculatedSubsidyToLowerLimit() {
        return totalSubsidyAfterCompensation.subtract(subsidyLowerLimitBasedOnLastYear);
    }

    @Override
    public BigDecimal getCalculatedSubsidy() {
        return totalSubsidyAfterCompensation.subtract(subsidyGrantedInFirstBatchOfCurrentYear);
    }

    public boolean isDownscaled() {
        return compensationType == SubsidyCompensationType.DECREASED;
    }

    public SubsidyCompensation2019InputDTO toInputForAnotherCompensationRound() {
        return new SubsidyCompensation2019InputDTO(
                rhyCode,
                totalSubsidyAfterCompensation,
                subsidyGrantedInFirstBatchOfCurrentYear,
                subsidyLowerLimitBasedOnLastYear,
                compensationType == SubsidyCompensationType.COMPENSATED);
    }

    // Accessors -->

    public String getRhyCode() {
        return rhyCode;
    }

    public BigDecimal getTotalSubsidyAfterCompensation() {
        return totalSubsidyAfterCompensation;
    }

    public BigDecimal getSubsidyGrantedInFirstBatchOfCurrentYear() {
        return subsidyGrantedInFirstBatchOfCurrentYear;
    }

    public BigDecimal getSubsidyLowerLimitBasedOnLastYear() {
        return subsidyLowerLimitBasedOnLastYear;
    }

    public BigDecimal getDecrement() {
        return decrement;
    }
}
