package fi.riista.feature.permit.application.mooselike;

public class MooselikePermitApplicationAreaMissingException extends IllegalStateException {
    public MooselikePermitApplicationAreaMissingException(final long applicationId) {
        super(String.format("HarvestPermitApplication id:%d does not have area defined", applicationId));
    }

}
