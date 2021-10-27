package fi.riista.feature.huntingclub.hunting;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GroupHuntingDiaryFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private GroupHuntingDiaryFeature feature;

    @Test(expected = NotFoundException.class)
    public void testGetHarvestsOfGroupMembers_whenGroupNotExisting() {
        feature.getHarvestsOfGroupMembers(123456789);
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenHarvestAuthorIsGroupMember() {
        testGetHarvestsOfGroupMembers_forHarvestAuthorAndShooterRestriction(true, RYHMAN_JASEN);
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenHarvestAuthorIsGroupLeader() {
        testGetHarvestsOfGroupMembers_forHarvestAuthorAndShooterRestriction(true, RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenHarvestShooterIsGroupMember() {
        testGetHarvestsOfGroupMembers_forHarvestAuthorAndShooterRestriction(false, RYHMAN_JASEN);
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenHarvestShooterIsGroupLeader() {
        testGetHarvestsOfGroupMembers_forHarvestAuthorAndShooterRestriction(false, RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenHarvestAuthorHasMultipleOccupationsForGroup() {
        testGetHarvestsOfGroupMembers_forHarvestAuthorAndShooterRestriction(true, RYHMAN_JASEN, RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenHarvestShooterHasMultipleOccupationsForGroup() {
        testGetHarvestsOfGroupMembers_forHarvestAuthorAndShooterRestriction(false, RYHMAN_JASEN, RYHMAN_METSASTYKSENJOHTAJA);
    }

    private void testGetHarvestsOfGroupMembers_forHarvestAuthorAndShooterRestriction(
            final boolean isAuthor, final OccupationType... groupOccupationTypes) {

        withPerson(groupMember -> {
            final GeoLocation location = geoLocation();

            final HuntingClub club = model().newHuntingClub();
            final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(club, location);
            model().newHarvestPermitForHuntingGroup(group);

            model().newOccupation(club, groupMember, SEURAN_JASEN);
            Stream.of(groupOccupationTypes).forEach(occType -> model().newOccupation(group, groupMember, occType));

            final Person nonGroupMember = model().newPerson();
            final Person harvestAuthor = isAuthor ? groupMember : nonGroupMember;
            final Person harvestShooter = isAuthor ? nonGroupMember : groupMember;

            final DateTime now = DateUtil.now();
            final Harvest harvest1 = newHarvest(group.getSpecies(), harvestAuthor, harvestShooter, location);
            harvest1.setPointOfTime(now);
            final Harvest harvest2 = newHarvest(group.getSpecies(), harvestAuthor, harvestShooter, location);
            harvest2.setPointOfTime(now.minusMinutes(1));

            // This harvest should not exist in the results because author/shooter is not group member.
            newHarvest(group.getSpecies(), model().newPerson(), location);

            onSavedAndAuthenticated(createUser(groupMember), () -> assertGroupMemberHarvests(group, harvest1, harvest2));
        });
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenShooterNotMemberOfClub() {
        // person is invited to club but has not yet accepted the invitation, also person is added to be member of a group
        final GeoLocation location = geoLocation();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(location);
        model().newHarvestPermitForHuntingGroup(group);

        final Person groupMemberPerson = newHuntingClubGroupMember(group);
        newHarvest(group.getSpecies(), groupMemberPerson, location);

        onSavedAndAuthenticated(createUser(groupMemberPerson), () -> assertGroupMemberHarvests(group));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenShooterMembershipOfClubIsSoftDeleted() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = model().newHuntingClub();
        final Occupation clubMemberOccupation = model().newHuntingClubMember(club, SEURAN_JASEN);
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(club, location);
        model().newHarvestPermitForHuntingGroup(group);

        final Occupation groupMemberOccupation = model().newHuntingClubGroupMember(clubMemberOccupation.getPerson(), group);
        newHarvest(group.getSpecies(), groupMemberOccupation.getPerson(), location);

        clubMemberOccupation.softDelete();
        groupMemberOccupation.softDelete();

        onSavedAndAuthenticated(
                createUser(newHuntingClubGroupLeader(group)), () -> assertGroupMemberHarvests(group));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenShooterMembershipOfClubHasBeginAndEndDates() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = model().newHuntingClub();
        final Occupation clubMemberOccupation = model().newHuntingClubMember(club, SEURAN_JASEN);
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(club, location);
        model().newHarvestPermitForHuntingGroup(group);

        final Occupation groupMemberOccupation = model().newHuntingClubGroupMember(clubMemberOccupation.getPerson(), group);
        final Harvest harvest = newHarvest(group.getSpecies(), groupMemberOccupation.getPerson(), location);

        final LocalDate dayBeforeHarvest = harvest.getPointOfTime().toLocalDate().minusDays(1);
        clubMemberOccupation.setEndDate(dayBeforeHarvest);
        groupMemberOccupation.setEndDate(dayBeforeHarvest);

        onSavedAndAuthenticated(
                createUser(newHuntingClubGroupLeader(group)), () -> assertGroupMemberHarvests(group));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenAuthorIsAlsoShooter() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = model().newHuntingClub();
        final Occupation clubMemberOccupation = model().newHuntingClubMember(club, SEURAN_JASEN);
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(club, location);
        model().newHarvestPermitForHuntingGroup(group);

        final Person clubMemberPerson = newHuntingClubGroupMember(group, clubMemberOccupation.getPerson());
        final Harvest harvest = newHarvest(group.getSpecies(), clubMemberPerson, location);

        onSavedAndAuthenticated(createUser(clubMemberPerson), () -> assertGroupMemberHarvests(group, harvest));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_whenUserIsContactPersonInSameClubAsHarvestAuthorOrShooter() {
        final HuntingClub club = model().newHuntingClub();
        final GeoLocation location = geoLocation();
        final HuntingClubGroup group =
                model().newHuntingClubGroupWithAreaContaining(location, club, model().newGameSpecies());
        model().newHarvestPermitForHuntingGroup(group);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, SEURAN_JASEN);
        model().newHuntingClubGroupMember(hunter, group);

        final Harvest harvest = newHarvest(group.getSpecies(), hunter, location);

        final Person clubContact =
                model().newHuntingClubMember(club, SEURAN_YHDYSHENKILO).getPerson();

        onSavedAndAuthenticated(createUser(clubContact), () -> assertGroupMemberHarvests(group, harvest));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_forSpeciesRestriction() {
        final GeoLocation location = geoLocation();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(location);
        model().newHarvestPermitForHuntingGroup(group);

        model().newHarvest(newHuntingClubGroupLeader(group), location);
        model().newHarvest(newHuntingClubGroupMember(group), location);

        onSavedAndAuthenticated(createUser(newHuntingClubGroupMember(group)), () -> assertGroupMemberHarvests(group));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_forHuntingYearRestriction() {
        final GeoLocation location = geoLocation();
        final GameSpecies species = model().newGameSpecies();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(location, species);
        model().newHarvestPermitForHuntingGroup(group);

        final Person groupLeader = newHuntingClubGroupLeader(group);
        final Person groupMember = newHuntingClubGroupMember(group);

        final int currentHuntingYear = DateUtil.huntingYear();
        final LocalDate lastDayOfPreviousHuntingYear = DateUtil.huntingYearEndDate(currentHuntingYear - 1);

        newHarvest(species, groupLeader, lastDayOfPreviousHuntingYear, location);
        newHarvest(species, groupMember, lastDayOfPreviousHuntingYear, location);

        final LocalDate firstDayOfNextHuntingYear = DateUtil.huntingYearBeginDate(currentHuntingYear + 1);

        newHarvest(species, groupLeader, firstDayOfNextHuntingYear, location);
        newHarvest(species, groupMember, firstDayOfNextHuntingYear, location);

        onSavedAndAuthenticated(createUser(newHuntingClubGroupMember(group)), () -> assertGroupMemberHarvests(group));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_forAreaRestriction() {
        final GeoLocation location = geoLocation();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaNotContaining(location);
        model().newHarvestPermitForHuntingGroup(group);

        newHarvest(group.getSpecies(), newHuntingClubGroupLeader(group), location);
        newHarvest(group.getSpecies(), newHuntingClubGroupMember(group), location);

        onSavedAndAuthenticated(createUser(newHuntingClubGroupMember(group)), () -> assertGroupMemberHarvests(group));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_forAreaRestriction_whenAreaNotSpecified() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        model().newHarvestPermitForHuntingGroup(group);

        model().newHarvest(group.getSpecies(), newHuntingClubGroupLeader(group));
        model().newHarvest(group.getSpecies(), newHuntingClubGroupMember(group));

        onSavedAndAuthenticated(createUser(newHuntingClubGroupMember(group)), () -> assertGroupMemberHarvests(group));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_forHuntingDayRestriction() {
        final GeoLocation location = geoLocation();
        final GameSpecies species = model().newGameSpecies();

        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(location, club, species);
        model().newHarvestPermitForHuntingGroup(group);

        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, today());

        newHarvest(species, newHuntingClubGroupLeader(group), huntingDay, location);
        newHarvest(species, newHuntingClubGroupMember(group), huntingDay, location);

        final HuntingClubGroup group2 = model().newHuntingClubGroupWithAreaContaining(location, club, species);
        final Person group2Leader = newHuntingClubGroupLeader(group2);

        onSavedAndAuthenticated(createUser(group2Leader), () -> assertGroupMemberHarvests(group2));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_huntingDayAssociationOverridesOtherRestrictionsExceptRejections() {
        final GeoLocation location = geoLocation();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(location);
        model().newHarvestPermitForHuntingGroup(group);

        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, today());

        // (1) Differing species, (2) non-group-member as author/hunter
        final Harvest harvest = newHarvest(model().newGameSpecies(), model().newPerson(), huntingDay, location);

        onSavedAndAuthenticated(
                createUser(newHuntingClubGroupMember(group)),
                () -> assertGroupMemberHarvests(group, harvest));
    }

    @Test
    public void testGetHarvestsOfGroupMembers_forRejectedHarvests() {
        withMooseHuntingGroupFixture(fixture -> {
            // location is not in the group's area, rejected should still be found
            final GeoLocation location = geoLocation();

            final Harvest harvest1 = newHarvest(fixture.species, fixture.groupLeader, location);
            final Harvest harvest2 = newHarvest(fixture.species, fixture.groupMember, location);
            model().newHarvestRejection(fixture.group, harvest1);
            model().newHarvestRejection(fixture.group, harvest2);

            onSavedAndAuthenticated(createUser(fixture.groupMember), () -> assertGroupMemberHarvests(fixture.group, harvest1, harvest2));
        });
    }

    @Test
    public void testGetObservationsOfGroupMembers_whenNoObservations() {
        final GameSpecies groupSpecies = model().newGameSpeciesMoose();
        doTestGetObservationsOfGroupMembers(groupSpecies, null);
    }

    @Test
    public void testGetObservationsOfGroupMembers_whenGroupIsMoose() {
        final GameSpecies groupSpecies = model().newGameSpeciesMoose();
        final GameSpecies observationSpecies = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        doTestGetObservationsOfGroupMembers(groupSpecies, observationSpecies);
    }

    @Test
    public void testGetObservationsOfGroupMembers_whenGroupIsOtherThanMoose() {
        final GameSpecies groupSpecies = model().newDeerSubjectToClubHunting();
        final GameSpecies observationSpecies = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        doTestGetObservationsOfGroupMembers(groupSpecies, observationSpecies);
    }

    private void doTestGetObservationsOfGroupMembers(GameSpecies groupSpecies, GameSpecies observationSpecies) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        model().newHarvestPermitSpeciesAmount(permit, groupSpecies);

        final GeoLocation location = geoLocation();

        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(location, club, groupSpecies);
        group.updateHarvestPermit(permit);

        final Person groupMember = newHuntingClubGroupMember(group);
        // sql fetching observations requires that club membership exists too
        model().newOccupation(group.getParentOrganisation(), groupMember, SEURAN_JASEN);
        final Observation observation = observationSpecies == null ? null : newObservation(observationSpecies, location, groupMember, MOOSE_HUNTING);

        onSavedAndAuthenticated(
                createUser(groupMember),
                () -> assertGroupMemberObservations(group, groupSpecies.isMoose() ? observation : null));
    }

    @Test
    public void testGetObservationsOfGroupMembers_forRejectedObservations() {
        withMooseHuntingGroupFixture(fixture -> {
            // location is not in the group's area, rejected should still be found
            final GeoLocation location = geoLocation();

            final Observation observation1 = newObservation(fixture.species, location, fixture.groupMember, MOOSE_HUNTING);
            final Observation observation2 = newObservation(fixture.species, location, fixture.groupLeader, MOOSE_HUNTING);
            model().newObservationRejection(fixture.group, observation1);
            model().newObservationRejection(fixture.group, observation2);

            onSavedAndAuthenticated(createUser(fixture.groupMember), () -> assertGroupMemberObservations(fixture.group, observation1, observation2));
        });
    }

    // ****** DEER HUNTING ******

    @Test
    public void testGetObservationsOfGroupMembers_deerHunting_deerHuntingObservationsAreShown() {
        withDeerHuntingGroupFixture(fixture -> {
            final Observation observation = newObservation(fixture.species, fixture.zoneCentroid, fixture.groupMember, DEER_HUNTING);
            linkObservationToHuntingDayOfGroup(observation, fixture.group);
            onSavedAndAuthenticated(
                    createUser(fixture.groupMember),
                    () -> assertGroupMemberObservations(fixture.group, observation));
        });
    }

    @Test
    public void testGetObservationsOfGroupMembers_deerHunting_suggestedDeerHuntingObservationsAreHidden() {
        // Suggested observations has no groupHuntingDay, or they are not rejected.
        // This appears when observations within deer hunting are done before person belongs to the group.
        withDeerHuntingGroupFixture(fixture -> {
            newObservation(fixture.species, fixture.zoneCentroid, fixture.groupMember, DEER_HUNTING);
            onSavedAndAuthenticated(
                    createUser(fixture.groupMember),
                    () -> assertTrue(feature.getObservationsOfGroupMembers(fixture.group.getId()).isEmpty()));
        });
    }

    @Test
    public void testGetObservationsOfGroupMembers_deerHunting_normalObservationsAreHidden() {
        withDeerHuntingGroupFixture(fixture -> {
            newObservation(fixture.species, fixture.zoneCentroid, fixture.groupMember, NORMAL);

            onSavedAndAuthenticated(
                    createUser(fixture.groupMember),
                    () -> assertGroupMemberObservations(fixture.group));
        });
    }

    @Test
    public void testGetObservationsOfGroupMembers_deerHunting_rejectedObservationsAreShown() {
        withDeerHuntingGroupFixture(fixture -> {
            final Observation observation = newObservation(fixture.species, fixture.zoneCentroid, fixture.groupMember, DEER_HUNTING);
            model().newObservationRejection(fixture.group, observation);

            onSavedAndAuthenticated(
                    createUser(fixture.groupMember),
                    () -> assertGroupMemberObservations(fixture.group, observation));
        });
    }

    @Test
    public void testGetObservationsOfGroupMembers_deerHunting_observationsNotInGroupAreaAreHidden() {
        withDeerHuntingGroupFixture(fixture -> {
            final GeoLocation location = geoLocation();
            newObservation(fixture.species, location, fixture.groupMember, DEER_HUNTING);

            onSavedAndAuthenticated(
                    createUser(fixture.groupMember),
                    () -> assertGroupMemberObservations(fixture.group));
        });
    }

    private Person newHuntingClubGroupLeader(final HuntingClubGroup group) {
        return newHuntingClubGroupMember(group, RYHMAN_METSASTYKSENJOHTAJA);
    }

    private Person newHuntingClubGroupMember(final HuntingClubGroup group) {
        return newHuntingClubGroupMember(group, RYHMAN_JASEN);
    }

    private Person newHuntingClubGroupMember(final HuntingClubGroup group, final OccupationType type) {
        return model().newHuntingClubGroupMember(group, type).getPerson();
    }

    private Person newHuntingClubGroupMember(final HuntingClubGroup group, final Person person) {
        return model().newHuntingClubGroupMember(person, group).getPerson();
    }

    private Harvest newHarvest(final GameSpecies species, final Person hunter, final GeoLocation location) {
        return newHarvest(species, hunter, hunter, location);
    }

    private Harvest newHarvest(
            final GameSpecies species, final Person author, final Person hunter, final GeoLocation location) {

        final Harvest harvest = model().newHarvest(species, author, hunter);
        harvest.setGeoLocation(location);
        return harvest;
    }

    private Harvest newHarvest(
            final GameSpecies species, final Person hunter, final LocalDate pointOfTime, final GeoLocation location) {

        final Harvest harvest = model().newHarvest(species, hunter, pointOfTime);
        harvest.setGeoLocation(location);
        return harvest;
    }

    private Harvest newHarvest(final GameSpecies species,
                               final Person hunter,
                               final GroupHuntingDay huntingDay,
                               final GeoLocation location) {

        final Harvest harvest = model().newHarvest(species, hunter, huntingDay);
        harvest.setGeoLocation(location);
        return harvest;
    }

    private Observation newObservation(final GameSpecies species,
                                       final GeoLocation location,
                                       final Person member,
                                       final ObservationCategory observationCategory) {

        final Observation observation = model().newObservation(species, member, observationCategory);
        observation.setGeoLocation(location);
        return observation;
    }

    private void linkObservationToHuntingDayOfGroup(final Observation observation,
                                                    final HuntingClubGroup group) {

        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, observation.getPointOfTimeAsLocalDate());
        observation.updateHuntingDayOfGroup(huntingDay, observation.getAuthor());
    }

    private void assertGroupMemberHarvests(final HuntingClubGroup clubGroup, final Harvest... expectedHarvests) {
        final List<HarvestDTO> harvests = feature.getHarvestsOfGroupMembers(clubGroup.getId());
        assertNotNull(harvests);
        Collections.sort(harvests, Comparator.comparing(HarvestDTO::getId));
        assertEquals(F.getNonNullIds(expectedHarvests), F.getNonNullIds(harvests));
    }

    private void assertGroupMemberObservations(final HuntingClubGroup clubGroup, final Observation... expectedObservations) {
        final List<ObservationDTO> observations = feature.getObservationsOfGroupMembers(clubGroup.getId());
        assertNotNull(observations);
        assertEquals(F.getUniqueIds(expectedObservations), F.getUniqueIds(observations));
    }

}
