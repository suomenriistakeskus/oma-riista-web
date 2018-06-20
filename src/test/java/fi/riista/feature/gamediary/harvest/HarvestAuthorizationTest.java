package fi.riista.feature.gamediary.harvest;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.GameDiaryEntryAuthorizationTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.junit.Test;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static fi.riista.feature.gamediary.harvest.HarvestAuthorization.Permission.LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class HarvestAuthorizationTest extends GameDiaryEntryAuthorizationTest<Harvest> {

    private static final ImmutableSet<Enum<?>> ALL_PERMS =
            Stream.of(CREATE, READ, UPDATE, DELETE, LINK_HARVEST_TO_HUNTING_DAY_OF_GROUP).collect(toImmutableSet());

    @Test
    public void testActorPermissions() {
        withPerson(author -> {
            final Harvest harvest = create();
            harvest.setActualShooter(author);

            onSavedAndAuthenticated(createUser(author), () -> assertPermissions(harvest, EntityPermission.crud()));
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
