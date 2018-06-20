package fi.riista.feature.huntingclub.members.club;

import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContactPersonCanExitClubServiceTest extends EmbeddedDatabaseTest {
    @Resource
    private ContactPersonCanExitClubService service;

    private void assertContactPersonIsLocked(final Occupation occupation,
                                             final String errorMessage) {
        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();
            assertTrue(errorMessage, service.isContactPersonLocked(occupation));
        });
    }

    private void assertContactPersonNotLocked(final Occupation occupation,
                                              final String errorMessage) {
        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();
            assertFalse(errorMessage, service.isContactPersonLocked(occupation));
        });
    }

    @Test
    public void testClubHasActiveMember() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation member = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_JASEN);
        final Occupation contactPerson = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);

        assertContactPersonIsLocked(contactPerson, "should NOT ALLOW delete contact person when club has active member");
    }

    @Test
    public void testAnotherContactPerson_NotExists_EmptyClub() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation occupation = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);

        assertContactPersonNotLocked(occupation, "should ALLOW delete contact person for club without data");
    }

    @Test
    public void testAnotherContactPerson_Exists_NotEmptyClub() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation member = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_JASEN);
        final Occupation contactPerson1 = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        final Occupation contactPerson2 = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);

        assertContactPersonNotLocked(contactPerson1, "should ALLOW delete contact person when another contact person exists");
    }

    @Test
    public void testAnotherContactPerson_OccupationNotActive_EmptyClub() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation contactPerson1 = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        final Occupation contactPerson2 = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        contactPerson2.setEndDate(DateUtil.today().minusDays(1));

        assertContactPersonNotLocked(contactPerson1, "should ALLOW delete contact person when club has members and other contact person is not valid");
    }

    @Test
    public void testAnotherContactPerson_OccupationNotActive_NotEmptyClub() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation member = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_JASEN);
        final Occupation contactPerson1 = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        final Occupation contactPerson2 = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        contactPerson2.setEndDate(DateUtil.today().minusDays(1));

        assertContactPersonIsLocked(contactPerson1, "should NOT ALLOW delete contact person when club has members and other contact person is not valid");
    }

    @Test
    public void testClubHasActiveAreas() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation contactPerson = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        model().newHuntingClubArea(club);

        assertContactPersonIsLocked(contactPerson, "should NOT ALLOW delete contact person when club has active area");
    }

    @Test
    public void testClubHasDeactiveAreas() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation contactPerson = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        final HuntingClubArea area = model().newHuntingClubArea(club);
        area.setActive(false);

        assertContactPersonNotLocked(contactPerson, "should ALLOW delete contact person when club has only deactive area");
    }

    @Test
    public void testClubHasPendingInvitations() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation contactPerson = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        model().newHuntingClubInvitation(club);

        assertContactPersonIsLocked(contactPerson, "should NOT ALLOW delete contact person when club has pending invitation");
    }

    @Test
    public void testClubHasHuntingDaysForMooseGroup() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation contactPerson = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        final HuntingClubGroup group = model().newHuntingClubGroup(club, model().newGameSpeciesMoose());
        model().newGroupHuntingDay(group, DateUtil.today());

        assertContactPersonIsLocked(contactPerson, "should NOT ALLOW delete contact person when club has hunting days for moose group");
    }

    @Test
    public void testClubHasHarvestLinkedToHuntingDay() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation contactPerson = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, DateUtil.today());
        final Harvest harvest = model().newHarvest();
        harvest.updateHuntingDayOfGroup(huntingDay, null);

        assertContactPersonIsLocked(contactPerson, "should NOT ALLOW delete contact person when club has harvest linked to hunting day");
    }

    @Test
    public void testClubHasObservationLinkedToHuntingDay() {
        final HuntingClub club = model().newHuntingClub();
        final Occupation contactPerson = model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, DateUtil.today());
        final Observation observation = model().newObservation();
        observation.updateHuntingDayOfGroup(huntingDay, null);

        assertContactPersonIsLocked(contactPerson, "should NOT ALLOW delete contact person when club has observation linked to hunting day");
    }
}
