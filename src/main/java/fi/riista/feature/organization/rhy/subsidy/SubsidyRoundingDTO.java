package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.NumberUtils.nullableSum;
import static fi.riista.util.NumberUtils.sum;
import static java.util.Objects.requireNonNull;

public class SubsidyRoundingDTO {

    private final BigDecimal subsidyBeforeRounding;
    private final BigDecimal subsidyAfterRounding;
    private final int givenRemainderEuros;

    public static SubsidyRoundingDTO aggregate(@Nonnull final Iterable<SubsidyRoundingDTO> roundings) {
        requireNonNull(roundings);

        final BigDecimal sumSubsidiesBeforeRounding =
                nullableSum(roundings, SubsidyRoundingDTO::getSubsidyBeforeRounding);

        final BigDecimal sumSubsidiesAfterRounding =
                nullableSum(roundings, SubsidyRoundingDTO::getSubsidyAfterRounding);

        final int sumOfRemainderEuros = sum(roundings, SubsidyRoundingDTO::getGivenRemainderEuros);

        return new SubsidyRoundingDTO(sumSubsidiesBeforeRounding, sumSubsidiesAfterRounding, sumOfRemainderEuros);
    }

    public SubsidyRoundingDTO(@Nonnull final BigDecimal subsidyBeforeRounding,
                              @Nonnull final BigDecimal subsidyAfterRounding,
                              final int givenRemainderEuros) {

        this.subsidyBeforeRounding = requireNonNull(subsidyBeforeRounding);
        this.subsidyAfterRounding = requireNonNull(subsidyAfterRounding);

        checkArgument(givenRemainderEuros >= 0, "Remainder euros must not be negative");

        this.givenRemainderEuros = givenRemainderEuros;
    }

    public BigDecimal getSubsidyBeforeRounding() {
        return subsidyBeforeRounding;
    }

    public BigDecimal getSubsidyAfterRounding() {
        return subsidyAfterRounding;
    }

    public int getGivenRemainderEuros() {
        return givenRemainderEuros;
    }
}
