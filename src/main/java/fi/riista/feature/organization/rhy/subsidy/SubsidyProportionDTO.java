package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.config.Constants;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.NumberUtils.nullableIntSum;
import static fi.riista.util.NumberUtils.nullableSum;
import static java.util.Objects.requireNonNull;

/**
 * Holds a numeric quantity and a money share that is calculated by multiplying
 * a total subsidy amount by the percentage share of the quantity in proportion
 * to total sum of all quantities.
 */
public class SubsidyProportionDTO {

    private final Integer quantity;
    private final BigDecimal calculatedAmount;

    @Nullable
    public static final SubsidyProportionDTO reduce(@Nullable final SubsidyProportionDTO a,
                                                    @Nullable final SubsidyProportionDTO b) {
        if (a == null) {
            return b;
        }

        final Integer sumOfQuantities = nullableIntSum(a, b, SubsidyProportionDTO::getQuantity);
        final BigDecimal sumOfCalculatedAmounts = nullableSum(a, b, SubsidyProportionDTO::getCalculatedAmount);

        return new SubsidyProportionDTO(sumOfQuantities, sumOfCalculatedAmounts);
    }

    @Nonnull
    public static SubsidyProportionDTO reduce(@Nonnull final Stream<SubsidyProportionDTO> items) {
        requireNonNull(items);
        return items.reduce(new SubsidyProportionDTO(0, Constants.ZERO_MONETARY_AMOUNT), SubsidyProportionDTO::reduce);
    }

    @Nonnull
    public static <T> SubsidyProportionDTO reduce(@Nonnull final Iterable<? extends T> items,
                                                  @Nonnull final Function<? super T, SubsidyProportionDTO> extractor) {

        requireNonNull(extractor);
        return reduce(F.stream(items).map(extractor));
    }

    public SubsidyProportionDTO(@Nullable final Integer quantity, @Nonnull final BigDecimal calculatedAmount) {
        this.quantity = quantity;
        this.calculatedAmount = requireNonNull(calculatedAmount);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof SubsidyProportionDTO)) {
            return false;
        } else {
            final SubsidyProportionDTO that = (SubsidyProportionDTO) o;

            return Objects.equals(this.quantity, that.quantity)
                    && Objects.equals(this.calculatedAmount, that.calculatedAmount);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, calculatedAmount);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", quantity, calculatedAmount);
    }

    // Accessors -->

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }
}
