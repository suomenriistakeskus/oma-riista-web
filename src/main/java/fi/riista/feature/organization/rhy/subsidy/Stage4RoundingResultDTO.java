package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class Stage4RoundingResultDTO {

    private final String rhyCode;
    private final BigDecimal subsidyBeforeRounding;
    private final BigDecimal subsidyAfterRounding;
    private final int givenRemainderEuros;

    public Stage4RoundingResultDTO(@Nonnull final String rhyCode,
                                   @Nonnull final BigDecimal subsidyBeforeRounding,
                                   @Nonnull final BigDecimal subsidyAfterRounding,
                                   final int givenRemainderEuros) {

        this.rhyCode = requireNonNull(rhyCode);
        this.subsidyBeforeRounding = requireNonNull(subsidyBeforeRounding);
        this.subsidyAfterRounding = requireNonNull(subsidyAfterRounding);

        checkArgument(givenRemainderEuros >= 0, "Remainder euros must not be negative");

        this.givenRemainderEuros = givenRemainderEuros;
    }

    public String getRhyCode() {
        return rhyCode;
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
