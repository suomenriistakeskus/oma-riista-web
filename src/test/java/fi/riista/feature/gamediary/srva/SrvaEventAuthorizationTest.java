package fi.riista.feature.gamediary.srva;

import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;

public class SrvaEventAuthorizationTest extends EmbeddedDatabaseTest {

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Test
    public void testAuthor() {
        withPerson(person -> {
            final Riistanhoitoyhdistys rhy = newRhy();
            final SrvaEvent srvaEvent = model().newSrvaEvent(person, rhy);
            final SrvaEvent otherSrvaEvent = model().newSrvaEvent(model().newPerson(), rhy);

            onSavedAndAuthenticated(createUser(person), () -> {
                assertHasPermissions(srvaEvent, EntityPermission.crud());
                assertNoPermissions(otherSrvaEvent, EntityPermission.crud());
            });
        });
    }

    @Test
    public void testAdmin() {
        final SrvaEvent srvaEvent = model().newSrvaEvent(newRhy());
        onSavedAndAuthenticated(createNewAdmin(), () -> assertHasPermissions(srvaEvent, EntityPermission.crud()));
    }

    @Test
    public void testModerator() {
        final SrvaEvent srvaEvent = model().newSrvaEvent(newRhy());
        onSavedAndAuthenticated(createNewModerator(), () -> assertHasPermissions(srvaEvent, EntityPermission.crud()));
    }

    @Test
    public void testNormalUser() {
        final SrvaEvent srvaEvent = model().newSrvaEvent(newRhy());
        onSavedAndAuthenticated(createUserWithPerson(), () -> assertNoPermissions(srvaEvent, EntityPermission.crud()));
    }

    @Test
    public void testCoordinatorOfRhy() {
        _testCoordinatorOrSrvaPersonOfRhy(OccupationType.TOIMINNANOHJAAJA);
    }

    @Test
    public void testSrvaPersonOfRhy() {
        _testCoordinatorOrSrvaPersonOfRhy(OccupationType.SRVA_YHTEYSHENKILO);
    }

    private void _testCoordinatorOrSrvaPersonOfRhy(final OccupationType occupationType) {
        withPerson(person -> {
            final Riistanhoitoyhdistys rhyOfEvent = newRhy();
            model().newOccupation(rhyOfEvent, person, occupationType);
            final SrvaEvent srvaEventOfRhy = model().newSrvaEvent(model().newPerson(), rhyOfEvent);

            //Using DEPORTATION since ACCIDENTs can be accessed by any coordinator or SRVA contact person.
            final SrvaEvent srvaEventOfOtherRhy = getSrvaEventWithNewPersonAndNewRhy(SrvaEventNameEnum.DEPORTATION);

            onSavedAndAuthenticated(createUser(person), () -> {
                assertHasPermissions(srvaEventOfRhy, getCoordinatorOrSrvaPersonPermissions());
                assertNoPermissions(srvaEventOfOtherRhy, getCoordinatorOrSrvaPersonPermissions());
            });
        });
    }

    @Test
    public void testCoordinatorOfAnyRhy() {
        _testCoordinatorOrSrvaPersonOfAnyRhy(OccupationType.TOIMINNANOHJAAJA);
    }

    @Test
    public void testSrvaPersonOfAnyRhy() {
        _testCoordinatorOrSrvaPersonOfAnyRhy(OccupationType.SRVA_YHTEYSHENKILO);
    }

    private void _testCoordinatorOrSrvaPersonOfAnyRhy(final OccupationType occupationType) {
        withPerson(person -> {
            model().newOccupation(newRhy(), person, occupationType);

            final SrvaEvent srvaEventAccident = getSrvaEventWithNewPersonAndNewRhy(SrvaEventNameEnum.ACCIDENT);
            final SrvaEvent srvaEventDeportation = getSrvaEventWithNewPersonAndNewRhy(SrvaEventNameEnum.DEPORTATION);
            final SrvaEvent srvaEventInjured = getSrvaEventWithNewPersonAndNewRhy(SrvaEventNameEnum.INJURED_ANIMAL);

            onSavedAndAuthenticated(createUser(person), () -> {
                assertHasPermissions(srvaEventAccident, getCoordinatorOrSrvaPersonPermissions());
                assertNoPermissions(srvaEventDeportation, getCoordinatorOrSrvaPersonPermissions());
                assertNoPermissions(srvaEventInjured, getCoordinatorOrSrvaPersonPermissions());
            });
        });
    }

    private SrvaEvent getSrvaEventWithNewPersonAndNewRhy(final SrvaEventNameEnum eventName) {
        final SrvaEvent srvaEvent = model().newSrvaEvent(model().newPerson(), newRhy());
        srvaEvent.setEventName(eventName);
        srvaEvent.setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(eventName)));
        return srvaEvent;
    }

    private static EnumSet<EntityPermission> getCoordinatorOrSrvaPersonPermissions() {
        return F.filterToEnumSet(EntityPermission.class, perm -> {
            return perm != EntityPermission.CREATE && perm != EntityPermission.NONE;
        });
    }

    private Riistanhoitoyhdistys newRhy() {
        return model().newRiistanhoitoyhdistys(this.rka);
    }
}
