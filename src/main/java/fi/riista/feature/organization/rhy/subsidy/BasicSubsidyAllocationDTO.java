package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.OrganisationNameDTO;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class BasicSubsidyAllocationDTO {

    private final OrganisationNameDTO rhy;
    private final OrganisationNameDTO rka;

    private final StatisticsBasedSubsidyShareDTO calculatedShares;
    private final BigDecimal totalRoundedShare;

    private final int givenRemainderEuros;

    public BasicSubsidyAllocationDTO(@Nonnull final OrganisationNameDTO rhy,
                                     @Nonnull final OrganisationNameDTO rka,
                                     @Nonnull final StatisticsBasedSubsidyShareDTO calculatedShares,
                                     @Nonnull final BigDecimal totalRoundedShare,
                                     final int givenRemainderEuros) {

        this.rhy = requireNonNull(rhy);
        this.rka = requireNonNull(rka);

        this.calculatedShares = requireNonNull(calculatedShares);
        this.totalRoundedShare = requireNonNull(totalRoundedShare);

        checkArgument(givenRemainderEuros >= 0, "Remainder euros must not be negative");

        this.givenRemainderEuros = givenRemainderEuros;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public String getRhyCode() {
        return rhy.getOfficialCode();
    }

    // Accessors -->

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public OrganisationNameDTO getRka() {
        return rka;
    }

    public StatisticsBasedSubsidyShareDTO getCalculatedShares() {
        return calculatedShares;
    }

    public BigDecimal getTotalRoundedShare() {
        return totalRoundedShare;
    }

    public int getGivenRemainderEuros() {
        return givenRemainderEuros;
    }
}
