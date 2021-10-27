package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.OrganisationNameDTO;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class HuntingControlEventExportDTO {

    private final OrganisationNameDTO rkaName;
    private final OrganisationNameDTO rhyName;
    private final List<HuntingControlEventDTO> events;

    public HuntingControlEventExportDTO(final @Nonnull OrganisationNameDTO rkaName,
                                        final @Nonnull OrganisationNameDTO rhyName,
                                        final @Nonnull List<HuntingControlEventDTO> events) {
        this.rkaName = Objects.requireNonNull(rkaName);
        this.rhyName = Objects.requireNonNull(rhyName);
        this.events = Objects.requireNonNull(events);
    }

    public OrganisationNameDTO getRkaName() {
        return rkaName;
    }

    public OrganisationNameDTO getRhyName() {
        return rhyName;
    }

    public List<HuntingControlEventDTO> getEvents() {
        return events;
    }
}
