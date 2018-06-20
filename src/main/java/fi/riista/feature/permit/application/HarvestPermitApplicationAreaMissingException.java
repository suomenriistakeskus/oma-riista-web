package fi.riista.feature.permit.application;

public class HarvestPermitApplicationAreaMissingException extends IllegalStateException {
    public HarvestPermitApplicationAreaMissingException(final long applicationId) {
        super(String.format("HarvestPermitApplication id:%d does not have area defined", applicationId));
    }

}
