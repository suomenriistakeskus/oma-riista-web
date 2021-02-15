package fi.riista.feature.organization.rhy.subsidy2019.compensation;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation;
import fi.riista.feature.organization.rhy.subsidy.compensation.SubsidyAllocationCompensationBasis;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.currencySum;
import static java.util.Objects.requireNonNull;

public class SubsidyCompensation2019Partition {

    private final List<SubsidyCompensation2019InputDTO> needingCompensation;
    private final List<SubsidyCompensation2019InputDTO> keptUnchanged;
    private final List<SubsidyCompensation2019InputDTO> downscaled;

    public static SubsidyCompensation2019Partition partitionByCompensationNeed(
            @Nonnull final List<SubsidyCompensation2019InputDTO> inputs) {

        checkArgument(!F.isNullOrEmpty(inputs), "inputs must not be null or empty");

        final ArrayList<SubsidyCompensation2019InputDTO> needingCompensation = new ArrayList<>();
        final ArrayList<SubsidyCompensation2019InputDTO> keepUnchanged = new ArrayList<>();
        final ArrayList<SubsidyCompensation2019InputDTO> toBeDownscaled = new ArrayList<>();

        inputs.forEach(input -> {
            if (input.isCompensationNeeded()) {
                needingCompensation.add(input);
            } else if (input.isAlreadyCompensated() || input.isExactlyAtLowerLimit()) {
                keepUnchanged.add(input);
            } else {
                toBeDownscaled.add(input);
            }
        });

        return new SubsidyCompensation2019Partition(needingCompensation, keepUnchanged, toBeDownscaled);
    }

    private SubsidyCompensation2019Partition(@Nonnull final List<SubsidyCompensation2019InputDTO> needingCompensation,
                                             @Nonnull final List<SubsidyCompensation2019InputDTO> keptUnchanged,
                                             @Nonnull final List<SubsidyCompensation2019InputDTO> downscaled) {

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
                currencySum(needingCompensation, SubsidyCompensation2019InputDTO::countAmountOfCompensationNeed);

        final BigDecimal sumOfSubsidiesAboveLowerLimit =
                currencySum(downscaled, SubsidyCompensation2019InputDTO::getTotalSubsidyCalculatedForCurrentYear);

        final BigDecimal decrementCoefficient =
                SubsidyCalculation.divide(totalCompensationNeed, sumOfSubsidiesAboveLowerLimit);

        return new SubsidyAllocationCompensationBasis(
                totalCompensationNeed, sumOfSubsidiesAboveLowerLimit, decrementCoefficient);
    }

    // Accessors -->

    public List<SubsidyCompensation2019InputDTO> getNeedingCompensation() {
        return needingCompensation;
    }

    public List<SubsidyCompensation2019InputDTO> getKeptUnchanged() {
        return keptUnchanged;
    }

    public List<SubsidyCompensation2019InputDTO> getDownscaled() {
        return downscaled;
    }
}
