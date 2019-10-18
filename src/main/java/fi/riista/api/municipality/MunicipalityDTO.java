package fi.riista.api.municipality;

import fi.riista.feature.common.entity.Municipality;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class MunicipalityDTO {

    public static MunicipalityDTO from(@Nonnull final Municipality municipality) {
        requireNonNull(municipality);
        return new MunicipalityDTO(municipality.getOfficialCode(), municipality.getNameLocalisation());
    }

    private final String officialCode;
    private final LocalisedString name;

    public String getOfficialCode() {
        return officialCode;
    }

    public LocalisedString getName() {
        return name;
    }

    private MunicipalityDTO(final String officialCode, final LocalisedString name) {
        this.officialCode = officialCode;
        this.name = name;
    }
}
