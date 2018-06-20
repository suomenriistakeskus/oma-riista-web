package fi.riista.feature.gamediary.harvest.mutation.exception;

import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;

public class HarvestPermitChangeForbiddenException extends IllegalArgumentException {
    public HarvestPermitChangeForbiddenException(final HarvestMutationRole mutationRole) {
        super("Permit change is not allowed for role " + mutationRole);
    }
}
