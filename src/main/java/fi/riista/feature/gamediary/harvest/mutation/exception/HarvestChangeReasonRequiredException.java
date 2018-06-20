package fi.riista.feature.gamediary.harvest.mutation.exception;

import fi.riista.feature.account.user.SystemUser;

public class HarvestChangeReasonRequiredException extends IllegalStateException {
    public HarvestChangeReasonRequiredException(final SystemUser.Role activeUserRole) {
        super("Reason is required for role " + activeUserRole);
    }
}
