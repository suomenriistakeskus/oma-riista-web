package fi.riista.feature.organization.rhy.subsidy.compensation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.isPositive;
import static java.util.Objects.requireNonNull;

public class SubsidyCompensationOutputDTO implements SubsidyCompensationNeed {

    private final String rhyCode;

    private final BigDecimal subsidyAfterCompensation;

    private final BigDecimal subsidyLowerLimitBasedOnLastYear;

    private final BigDecimal decrement;

    private final SubsidyCompensationType compensationType;

    public static SubsidyCompensationOutputDTO keepSubsidyUnchanged(@Nonnull final SubsidyCompensationInputDTO input) {
        requireNonNull(input);

        return new SubsidyCompensationOutputDTO(
                input.getRhyCode(),
                input.getCalculatedSubsidy(),
                input.getSubsidyLowerLimitBasedOnLastYear(),
                input.isAlreadyCompensated());
    }

    public static SubsidyCompensationOutputDTO increased(@Nonnull final SubsidyCompensationInputDTO input,
                                                         @Nonnull final BigDecimal increment) {
        requireNonNull(input);
        requireNonNull(increment);

        checkArgument(isPositive(increment), "Increment must be positive");

        final BigDecimal increasedSubsidy = input.getCalculatedSubsidy().add(increment);

        return new SubsidyCompensationOutputDTO(
                input.getRhyCode(), increasedSubsidy, input.getSubsidyLowerLimitBasedOnLastYear(), true);
    }

    public static SubsidyCompensationOutputDTO decreased(@Nonnull final SubsidyCompensationInputDTO input,
                                                         @Nonnull final BigDecimal decrement) {
        requireNonNull(input);
        requireNonNull(decrement);

        final BigDecimal decreasedSubsidy = input.getCalculatedSubsidy().subtract(decrement);

        return new SubsidyCompensationOutputDTO(
                input.getRhyCode(), decreasedSubsidy, input.getSubsidyLowerLimitBasedOnLastYear(), decrement);
    }

    // Exposed as package-private for tests.
    SubsidyCompensationOutputDTO(@Nonnull final String rhyCode,
                                 @Nonnull final BigDecimal subsidyAfterCompensation,
                                 @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                 final boolean compensated) {
        this(rhyCode,
                subsidyAfterCompensation,
                subsidyLowerLimitBasedOnLastYear,
                null,
                compensated ? SubsidyCompensationType.COMPENSATED : SubsidyCompensationType.UNCHANGED);
    }

    // Exposed as package-private for tests.
    SubsidyCompensationOutputDTO(@Nonnull final String rhyCode,
                                 @Nonnull final BigDecimal subsidyAfterCompensation,
                                 @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                 @Nonnull final BigDecimal decrement) {
        this(rhyCode,
                subsidyAfterCompensation,
                subsidyLowerLimitBasedOnLastYear,
                requireNonNull(decrement),
                SubsidyCompensationType.DECREASED);
    }

    private SubsidyCompensationOutputDTO(@Nonnull final String rhyCode,
                                         @Nonnull final BigDecimal subsidyAfterCompensation,
                                         @Nonnull final BigDecimal subsidyLowerLimitBasedOnLastYear,
                                         @Nullable final BigDecimal decrement,
                                         @Nonnull final SubsidyCompensationType compensationType) {

        this.rhyCode = requireNonNull(rhyCode);
        this.subsidyAfterCompensation = requireNonNull(subsidyAfterCompensation);
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
    public BigDecimal getCalculatedSubsidy() {
        return subsidyAfterCompensation;
    }

    @Override
    public BigDecimal countDifferenceOfCalculatedSubsidyToLowerLimit() {
        return subsidyAfterCompensation.subtract(subsidyLowerLimitBasedOnLastYear);
    }

    public boolean isDownscaled() {
        return compensationType == SubsidyCompensationType.DECREASED;
    }

    public SubsidyCompensationInputDTO toInputForAnotherCompensationRound() {
        return new SubsidyCompensationInputDTO(
                rhyCode,
                subsidyAfterCompensation,
                subsidyLowerLimitBasedOnLastYear,
                compensationType == SubsidyCompensationType.COMPENSATED);
    }

    // Accessors -->

    public String getRhyCode() {
        return rhyCode;
    }

    public BigDecimal getSubsidyAfterCompensation() {
        return subsidyAfterCompensation;
    }

    public BigDecimal getSubsidyLowerLimitBasedOnLastYear() {
        return subsidyLowerLimitBasedOnLastYear;
    }

    public BigDecimal getDecrement() {
        return decrement;
    }
}
