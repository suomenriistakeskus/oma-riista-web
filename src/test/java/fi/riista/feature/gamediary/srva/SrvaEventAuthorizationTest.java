package fi.riista.feature.gamediary.srva;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.security.EntityPermission;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

            onSavedAndAuthenticated(createUser(person), tx(() -> {
                assertHasPermissions(srvaEvent, getAuthorPermissions());
                assertNoPermissions(otherSrvaEvent, getAuthorPermissions());
            }));
        });
    }

    @Test
    public void testAdmin() {
        final SrvaEvent srvaEvent = model().newSrvaEvent(newRhy());
        onSavedAndAuthenticated(createNewAdmin(), tx(() -> assertHasPermissions(srvaEvent, getAuthorPermissions())));
    }

    @Test
    public void testModerator() {
        final SrvaEvent srvaEvent = model().newSrvaEvent(newRhy());
        onSavedAndAuthenticated(
                createNewModerator(), tx(() -> assertHasPermissions(srvaEvent, getAuthorPermissions())));
    }

    @Test
    public void testNormalUser() {
        final SrvaEvent srvaEvent = model().newSrvaEvent(newRhy());
        onSavedAndAuthenticated(
                createUserWithPerson(), tx(() -> assertNoPermissions(srvaEvent, getAuthorPermissions())));

    }

    private static List<EntityPermission> getAuthorPermissions() {
        return Stream.of(EntityPermission.values())
                .filter(entityPermission -> !Objects.equals(entityPermission, EntityPermission.NONE))
                .collect(Collectors.toList());
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

            onSavedAndAuthenticated(createUser(person), tx(() -> {
                assertHasPermissions(srvaEventOfRhy, getCoordinatorOrSrvaPersonPermissions());
                assertNoPermissions(srvaEventOfOtherRhy, getCoordinatorOrSrvaPersonPermissions());
            }));
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

            onSavedAndAuthenticated(createUser(person), tx(() -> {
                assertHasPermissions(srvaEventAccident, getCoordinatorOrSrvaPersonPermissions());
                assertNoPermissions(srvaEventDeportation, getCoordinatorOrSrvaPersonPermissions());
                assertNoPermissions(srvaEventInjured, getCoordinatorOrSrvaPersonPermissions());
            }));
        });
    }

    private SrvaEvent getSrvaEventWithNewPersonAndNewRhy(final SrvaEventNameEnum eventName) {
        final SrvaEvent srvaEvent = model().newSrvaEvent(model().newPerson(), newRhy());
        srvaEvent.setEventName(eventName);
        return srvaEvent;
    }

    private static List<EntityPermission> getCoordinatorOrSrvaPersonPermissions() {
        return Stream.of(EntityPermission.values())
                .filter(entityPermission -> !Objects.equals(entityPermission, EntityPermission.NONE)
                        && !Objects.equals(entityPermission, EntityPermission.CREATE))
                .collect(Collectors.toList());
    }

    private Riistanhoitoyhdistys newRhy() {
        return model().newRiistanhoitoyhdistys(this.rka);
    }

}
