package fi.riista.feature.gamediary.observation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameDiaryEntryAuthorizationTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.ObservationAuthorization.Permission;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.junit.Test;

import java.util.EnumSet;

import static fi.riista.feature.gamediary.observation.ObservationAuthorization.Permission.LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class ObservationAuthorizationTest extends GameDiaryEntryAuthorizationTest<Observation> {

    private static final ImmutableSet<Enum<?>> ALL_PERMS = Sets.union(
            EnumSet.allOf(Permission.class), ImmutableSet.of(CREATE, READ, UPDATE, DELETE))
            .immutableCopy();

    @Test
    public void testActorPermissions() {
        withPerson(author -> {
            final Observation observation = create();
            observation.setObserver(author);

            onSavedAndAuthenticated(
                    createUser(author),
                    tx(() -> assertPermissions(observation, EnumSet.of(CREATE, READ, UPDATE, DELETE))));
        });
    }

    @Test
    public void testHuntingClubGroupBasedPermissions_whenUnlinkedObservationWithinGroupAreaButDifferentSpecies() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(club, location);
        model().newHarvestPermitForHuntingGroup(group);

        final Occupation clubOccupation = model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN);
        final Occupation groupOccupation = model().newHuntingClubGroupMember(clubOccupation.getPerson(), group);
        final Person author = groupOccupation.getPerson();

        // Observation species is not the one assigned to the group but
        // observation is done within moose hunting which makes a match.
        final Observation observation = create(model().newGameSpecies(), author);
        observation.setGeoLocation(location);

        final Enum<?> linkPerm = getPermissionForLinkingDiaryEntryToGroupHuntingDay();

        assertPermissions(
                observation,
                group,
                ImmutableSet.of(CREATE, READ, UPDATE, linkPerm),
                ImmutableSet.of(CREATE, READ),
                ImmutableSet.of(CREATE, READ, UPDATE, linkPerm),
                ImmutableSet.of(CREATE, READ));
    }

    @Test
    public void testHuntingClubGroupBasedPermissions_whenNonShareableObservationWithinGroupArea() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(club, location);
        model().newHarvestPermitForHuntingGroup(group);

        final Occupation clubOccupation = model().newHuntingClubMember(club, OccupationType.SEURAN_JASEN);
        final Occupation groupOccupation = model().newHuntingClubGroupMember(clubOccupation.getPerson(), group);
        final Person author = groupOccupation.getPerson();

        final Observation observation = createNonShareableForClub(group.getSpecies(), author);
        observation.setGeoLocation(location);

        assertPermissions(observation, group, NO_PERMS, NO_PERMS, NO_PERMS, NO_PERMS);
    }

    @Override
    protected ImmutableSet<Enum<?>> getAllPermissions() {
        return ALL_PERMS;
    }

    @Override
    protected Enum<?> getPermissionForLinkingDiaryEntryToGroupHuntingDay() {
        return LINK_OBSERVATION_TO_HUNTING_DAY_OF_GROUP;
    }

    @Override
    protected void addGroupRejection(final HuntingClubGroup group, final Observation observation) {
        model().newObservationRejection(group, observation);
    }

    @Override
    protected Observation create() {
        return model().newObservation(true);
    }

    @Override
    protected Observation create(final GameSpecies species, final Person author) {
        return model().newObservation(species, author, true);
    }

    protected Observation createNonShareableForClub(final GameSpecies species, final Person author) {
        return model().newObservation(species, author, false);
    }

}
