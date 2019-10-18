package fi.riista.feature.permit.application.archive;

import fi.riista.feature.permit.application.HarvestPermitApplication;

import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class PermitApplicationArchiveDTO {
    public static PermitApplicationArchiveDTO create(final HarvestPermitApplication application) {
        return new PermitApplicationArchiveDTO(application.getId(),
                application.getApplicationNumber(),
                application.getArea() != null,
                application.getLocale());
    }

    private final long id;
    private final int applicationNumber;
    private final boolean hasPermitArea;
    private final Locale locale;

    public PermitApplicationArchiveDTO(final long id, final Integer applicationNumber, final boolean hasPermitArea, final Locale locale) {
        this.id = id;
        this.applicationNumber = requireNonNull(applicationNumber);
        this.hasPermitArea = hasPermitArea;
        this.locale = locale;
    }

    public long getId() {
        return id;
    }

    public int getApplicationNumber() {
        return applicationNumber;
    }

    public boolean isHasPermitArea() {
        return hasPermitArea;
    }

    public Locale getLocale() {
        return locale;
    }
}
