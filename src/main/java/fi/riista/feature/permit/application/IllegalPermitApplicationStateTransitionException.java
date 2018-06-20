package fi.riista.feature.permit.application;

public class IllegalPermitApplicationStateTransitionException extends IllegalStateException {
    public IllegalPermitApplicationStateTransitionException(final long applicationId) {
        super(String.format("HarvestPermitApplication id:%d is read only", applicationId));
    }
}
