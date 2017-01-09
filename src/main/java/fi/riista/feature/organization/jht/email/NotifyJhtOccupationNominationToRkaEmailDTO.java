package fi.riista.feature.organization.jht.email;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import java.util.Objects;

public class NotifyJhtOccupationNominationToRkaEmailDTO {

    private final OccupationType occupationType;

    private final String rhyOfficialCode;

    private final LocalisedString rhyName;

    private final String rkaEmail;

    public NotifyJhtOccupationNominationToRkaEmailDTO(
            final OccupationType occupationType,
            final String rhyOfficialCode,
            final LocalisedString rhyName,
            final String rkaEmail) {
        this.occupationType = Objects.requireNonNull(occupationType, "occupationType is null");
        this.rhyOfficialCode = Objects.requireNonNull(rhyOfficialCode, "rhyOfficialCode is null");
        this.rhyName = Objects.requireNonNull(rhyName, "rhyName is null");
        this.rkaEmail = Objects.requireNonNull(rkaEmail, "rkaEmail is null");
    }

    @Nonnull
    public OccupationType getOccupationType() {
        return occupationType;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    @Nonnull
    public LocalisedString getRhyName() {
        return rhyName;
    }

    @Nonnull
    public String getRkaEmail() {
        return rkaEmail;
    }
}
