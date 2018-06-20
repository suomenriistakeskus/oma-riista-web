package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestReportingTypeChangeForbiddenException;

public enum HarvestReportingType {
    PERMIT,
    HUNTING_DAY,
    SEASON,
    BASIC;

    public void assertValidReportingType(final HarvestReportingType previousReportingType,
                                         final HarvestMutationRole mutationRole) {
        if (!isValidReportingTypeFor(mutationRole)) {
            throw new HarvestReportingTypeChangeForbiddenException(previousReportingType, this);
        }
    }

    public boolean isHarvestReportRequired() {
        return this == PERMIT || this == SEASON;
    }

    public boolean isValidReportingTypeFor(final HarvestMutationRole mutationRole) {
        switch (mutationRole) {
            case MODERATOR:
                // Moderator should not be able to create or update harvest resulting in basic diary entry
                return this != BASIC;

            case AUTHOR_OR_ACTOR:
                return true;

            case PERMIT_CONTACT_PERSON:
                return this == PERMIT;

            case OTHER:
                return this == HUNTING_DAY;
        }

        return false;
    }
}
