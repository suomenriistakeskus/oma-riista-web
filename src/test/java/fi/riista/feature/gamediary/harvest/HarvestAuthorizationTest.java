package fi.riista.feature.gamediary.harvest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import fi.riista.feature.gamediary.GameDiaryEntryAuthorizationTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestAuthorization.HarvestPermission;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.person.Person;
import org.junit.Test;

import java.util.EnumSet;

import static fi.riista.feature.gamediary.harvest.HarvestAuthorization.HarvestPermission.LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class HarvestAuthorizationTest extends GameDiaryEntryAuthorizationTest<Harvest> {

    private static final ImmutableSet<Enum<?>> ALL_PERMS = Sets.union(
            EnumSet.allOf(HarvestPermission.class), ImmutableSet.of(CREATE, READ, UPDATE, DELETE))
            .immutableCopy();

    @Test
    public void testActorPermissions() {
        withPerson(author -> {
            final Harvest harvest = create();
            harvest.setActualShooter(author);

            onSavedAndAuthenticated(
                    createUser(author),
                    tx(() -> assertPermissions(harvest, EnumSet.of(CREATE, READ, UPDATE, DELETE))));
        });
    }

    @Override
    protected ImmutableSet<Enum<?>> getAllPermissions() {
        return ALL_PERMS;
    }

    @Override
    protected Enum<?> getPermissionForLinkingDiaryEntryToGroupHuntingDay() {
        return LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP;
    }

    @Override
    protected Harvest create() {
        return model().newHarvest();
    }

    @Override
    protected Harvest create(final GameSpecies species, final Person author) {
        return model().newHarvest(species, author);
    }

    @Override
    protected void addGroupRejection(final HuntingClubGroup group, final Harvest harvest) {
        model().newHarvestRejection(group, harvest);
    }

}
