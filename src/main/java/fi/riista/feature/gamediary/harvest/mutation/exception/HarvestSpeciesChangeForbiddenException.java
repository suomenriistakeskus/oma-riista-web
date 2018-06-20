package fi.riista.feature.gamediary.harvest.mutation.exception;

import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;

public class HarvestSpeciesChangeForbiddenException extends IllegalArgumentException {
    public HarvestSpeciesChangeForbiddenException(final HarvestMutationRole mutationRole) {
        super("Species change is not allowed for role " + mutationRole);
    }
}
