package fi.riista.feature.gamediary.harvest.mutation.exception;

import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;

public class HarvestSeasonChangeForbiddenException extends IllegalArgumentException {
    public HarvestSeasonChangeForbiddenException(final HarvestMutationRole mutationRole) {
        super("Season can not be changed by role " + mutationRole);
    }
}
