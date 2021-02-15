package fi.riista.feature.organization.rhy.subsidy.compensation;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.currencySum;
import static java.util.Objects.requireNonNull;

public class SubsidyCompensationPartition {

    private final List<SubsidyCompensationInputDTO> needingCompensation;
    private final List<SubsidyCompensationInputDTO> keptUnchanged;
    private final List<SubsidyCompensationInputDTO> downscaled;

    public static SubsidyCompensationPartition partitionByCompensationNeed(
            @Nonnull final List<SubsidyCompensationInputDTO> inputs) {

        checkArgument(!F.isNullOrEmpty(inputs), "inputs must not be null or empty");

        final ArrayList<SubsidyCompensationInputDTO> needingCompensation = new ArrayList<>();
        final ArrayList<SubsidyCompensationInputDTO> keepUnchanged = new ArrayList<>();
        final ArrayList<SubsidyCompensationInputDTO> toBeDownscaled = new ArrayList<>();

        inputs.forEach(input -> {
            if (input.isCompensationNeeded()) {
                needingCompensation.add(input);
            } else if (input.isAlreadyCompensated() || input.isExactlyAtLowerLimit()) {
                keepUnchanged.add(input);
            } else {
                toBeDownscaled.add(input);
            }
        });

        return new SubsidyCompensationPartition(needingCompensation, keepUnchanged, toBeDownscaled);
    }

    private SubsidyCompensationPartition(@Nonnull final List<SubsidyCompensationInputDTO> needingCompensation,
                                         @Nonnull final List<SubsidyCompensationInputDTO> keptUnchanged,
                                         @Nonnull final List<SubsidyCompensationInputDTO> downscaled) {

        requireNonNull(needingCompensation);
        requireNonNull(keptUnchanged);
        requireNonNull(downscaled);

        this.needingCompensation = ImmutableList.copyOf(needingCompensation);
        this.keptUnchanged = ImmutableList.copyOf(keptUnchanged);
        this.downscaled = ImmutableList.copyOf(downscaled);
    }

    // Produces a coefficient that is used to scale down subsidies above lower limit (and having
    // non-negative amount calculated for second batch) to compensate for subsidies falling below.
    public SubsidyAllocationCompensationBasis calculateCompensationBasis() {
        final BigDecimal totalCompensationNeed =
                currencySum(needingCompensation, SubsidyCompensationInputDTO::countAmountOfCompensationNeed);

        final BigDecimal sumOfSubsidiesAboveLowerLimit =
                currencySum(downscaled, SubsidyCompensationInputDTO::getCalculatedSubsidy);

        final BigDecimal decrementCoefficient =
                SubsidyCalculation.divide(totalCompensationNeed, sumOfSubsidiesAboveLowerLimit);

        return new SubsidyAllocationCompensationBasis(
                totalCompensationNeed, sumOfSubsidiesAboveLowerLimit, decrementCoefficient);
    }

    // Accessors -->

    public List<SubsidyCompensationInputDTO> getNeedingCompensation() {
        return needingCompensation;
    }

    public List<SubsidyCompensationInputDTO> getKeptUnchanged() {
        return keptUnchanged;
    }

    public List<SubsidyCompensationInputDTO> getDownscaled() {
        return downscaled;
    }
}
