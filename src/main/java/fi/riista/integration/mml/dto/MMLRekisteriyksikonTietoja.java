package fi.riista.integration.mml.dto;

import java.util.Objects;

public class MMLRekisteriyksikonTietoja {
    private final String propertyIdentifier;
    private final String municipalityCode;

    public MMLRekisteriyksikonTietoja(final String propertyIdentifier, final String municipalityCode) {
        this.propertyIdentifier = Objects.requireNonNull(propertyIdentifier);
        this.municipalityCode = municipalityCode;
    }

    public String getPropertyIdentifier() {
        return propertyIdentifier;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MMLRekisteriyksikonTietoja that = (MMLRekisteriyksikonTietoja) o;
        return Objects.equals(propertyIdentifier, that.propertyIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyIdentifier);
    }
}
