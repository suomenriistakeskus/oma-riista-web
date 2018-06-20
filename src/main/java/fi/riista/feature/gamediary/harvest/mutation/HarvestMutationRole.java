package fi.riista.feature.gamediary.harvest.mutation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;

import javax.annotation.Nonnull;

// Role used to determine which mutations are valid for active user
public enum HarvestMutationRole {
    AUTHOR_OR_ACTOR,
    PERMIT_CONTACT_PERSON,
    MODERATOR,
    // Leader in hunting group
    OTHER;

    @Nonnull
    public static HarvestMutationRole getMutationRoleForHarvest(final Harvest harvest, final SystemUser activeUser) {
        if (activeUser.isModeratorOrAdmin()) {
            return HarvestMutationRole.MODERATOR;

        } else if (harvest.isNew() || harvest.isAuthorOrActor(activeUser)) {
            return HarvestMutationRole.AUTHOR_OR_ACTOR;

        } else if (activeUser.getPerson() != null
                && harvest.getHarvestPermit() != null
                && harvest.getHarvestPermit().hasContactPerson(activeUser.getPerson())) {
            return HarvestMutationRole.PERMIT_CONTACT_PERSON;
        }

        return HarvestMutationRole.OTHER;
    }
}
