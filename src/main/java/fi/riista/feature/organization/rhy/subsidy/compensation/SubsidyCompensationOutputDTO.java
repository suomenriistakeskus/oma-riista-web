package fi.riista.feature.organization.rhy.subsidy.compensation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.isPositive;
import static java.util.Objects.requireNonNull;

public class SubsidyCompensationOutputDTO implements SubsidyCompensationNeed {

    private enum CompensationType {
        COMPENSATED,
        DECREASED,
        UNCHANGED
    }

    private final String rhyCode;

    private final BigDecimal totalSubsidyAfterCompensation;
    private final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear;

    private final BigDecimal subsidyLowerLimitBasedOnLastYear;

    private final BigDecimal decrement;

    private final CompensationType compensationType;

    public static SubsidyCompensationOutputDTO keepSubsidyUnchanged(@Nonnull final SubsidyCompensationInputDTO input) {
        requireNonNull(input);

        return new SubsidyCompensationOutputDTO(
                input.getRhyCode(),
                input.getTotalSubsidyCalculatedForCurrentYear(),
                input.getSubsidyGrantedInFirstBatchOfCurrentYear(),
                input.getSubsidyLowerLimitBasedOnLastYear(),
                input.isAlreadyCompensated());
    }

    public static SubsidyCompensationOutputDTO increased(@Nonnull final SubsidyCompensationInputDTO input,
                                                         @Nonnull final BigDecimal increment) {
        requireNonNull(input);
        requireNonNull(increment);

        checkArgument(isPositive(increment), "Increment must be positive");

        final BigDecimal increasedSubsidyForCurrentYear =
                input.getTotalSubsidyCalculatedForCurrentYear().add(increment);

        return new SubsidyCompensationOutputDTO(
                input.getRhyCode(),
                increasedSubsidyForCurrentYear,
                input.getSubsidyGrantedInFirstBatchOfCurrentYear(),
                input.getSubsidyLowerLimitBasedOnLastYear(),
                true);
    }

    public static SubsidyCompensationOutputDTO decreased(@Nonnull final SubsidyCompensationInputDTO input,
                                                         @Nonnull final BigDecimal decrement) {
        requireNonNull(input);
        requireNonNull(decrement);

        final BigDecimal decreasedSubsidyForCurrentYear =
                input.getTotalSubsidyCalculatedForCurrentYear().subtract(decrement);

        return new SubsidyCompensationOutputDTO(
                input.getRhyCode(),
                decreasedSubsidyForCurrentYear,
                input.getSubsidyGrantedInFirstBatchOfCurrentYear(),
                input.getSubsidyLowerLimitBasedOnLastYear(),
                decrement);
    }

    // Exposed as package-private for tests.
    SubsidyCompensationOutputDTO(@Nonnull final String rhyCode,
                                 @Nonnull final BigDecimal totalSubsidyAfterCompensation,
                                 @Nonnull final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear,
                                 @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                 final boolean compensated) {
        this(rhyCode,
                totalSubsidyAfterCompensation,
                subsidyGrantedInFirstBatchOfCurrentYear,
                subsidyLowerLimitBasedOnLastYear,
                null,
                compensated ? CompensationType.COMPENSATED : CompensationType.UNCHANGED);
    }

    // Exposed as package-private for tests.
    SubsidyCompensationOutputDTO(@Nonnull final String rhyCode,
                                 @Nonnull final BigDecimal totalSubsidyAfterCompensation,
                                 @Nonnull final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear,
                                 @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                 @Nonnull final BigDecimal decrement) {
        this(rhyCode,
                totalSubsidyAfterCompensation,
                subsidyGrantedInFirstBatchOfCurrentYear,
                subsidyLowerLimitBasedOnLastYear,
                requireNonNull(decrement),
                CompensationType.DECREASED);
    }

    private SubsidyCompensationOutputDTO(@Nonnull final String rhyCode,
                                         @Nonnull final BigDecimal totalSubsidyAfterCompensation,
                                         @Nonnull final BigDecimal subsidyGrantedInFirstBatchOfCurrentYear,
                                         @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                         @Nullable final BigDecimal decrement,
                                         @Nonnull final CompensationType compensationType) {

        this.rhyCode = requireNonNull(rhyCode);
        this.totalSubsidyAfterCompensation = requireNonNull(totalSubsidyAfterCompensation);
        this.subsidyGrantedInFirstBatchOfCurrentYear = requireNonNull(subsidyGrantedInFirstBatchOfCurrentYear);
        this.subsidyLowerLimitBasedOnLastYear = requireNonNull(subsidyLowerLimitBasedOnLastYear);
        this.compensationType = requireNonNull(compensationType);
        this.decrement = decrement;

        if (decrement != null) {
            checkArgument(isPositive(decrement), "Decrement must be positive");
            checkArgument(compensationType == CompensationType.DECREASED);
        } else {
            checkArgument(compensationType != CompensationType.DECREASED);
        }
    }

    @Override
    public BigDecimal countDifferenceOfTotalCalculatedSubsidyToLowerLimit() {
        return totalSubsidyAfterCompensation.subtract(subsidyLowerLimitBasedOnLastYear);
    }

    @Override
    public BigDecimal getCalculatedSubsidyForSecondBatch() {
        return totalSubsidyAfterCompensation.subtract(subsidyGrantedInFirstBatchOfCurrentYear);
    }

    public boolean isDownscaled() {
        return compensationType == CompensationType.DECREASED;
    }

    public SubsidyCompensationInputDTO toInputForAnotherCompensationRound() {
        return new SubsidyCompensationInputDTO(
                rhyCode,
                totalSubsidyAfterCompensation,
                subsidyGrantedInFirstBatchOfCurrentYear,
                subsidyLowerLimitBasedOnLastYear,
                compensationType == CompensationType.COMPENSATED);
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
