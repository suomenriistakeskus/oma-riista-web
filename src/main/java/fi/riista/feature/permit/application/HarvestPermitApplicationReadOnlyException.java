package fi.riista.feature.permit.application;

public class HarvestPermitApplicationReadOnlyException extends IllegalStateException {
    public HarvestPermitApplicationReadOnlyException(final long applicationId) {
        super(String.format("HarvestPermitApplication id:%d is read only", applicationId));
    }
}
