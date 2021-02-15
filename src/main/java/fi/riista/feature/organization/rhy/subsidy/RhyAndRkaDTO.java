package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.OrganisationNameDTO;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class RhyAndRkaDTO {

    private final OrganisationNameDTO rhy;
    private final OrganisationNameDTO rka;

    public RhyAndRkaDTO(@Nonnull final OrganisationNameDTO rhy, @Nonnull final OrganisationNameDTO rka) {
        this.rhy = requireNonNull(rhy);
        this.rka = requireNonNull(rka);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof RhyAndRkaDTO)) {
            return false;
        } else {
            final RhyAndRkaDTO that = (RhyAndRkaDTO) o;

            return Objects.equals(this.rhy, that.rhy) && Objects.equals(this.rka, that.rka);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(rhy, rka);
    }

    @Override
    public String toString() {
        return String.format("{ rhy: { %s }, rka: { %s } }", rhy, rka);
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    public OrganisationNameDTO getRka() {
        return rka;
    }
}
