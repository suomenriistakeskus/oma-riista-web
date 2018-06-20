package fi.riista.feature.huntingclub;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoosePermitTodoFeatureTest extends EmbeddedDatabaseTest {

    private Riistanhoitoyhdistys rhy;

    @Resource
    private MoosePermitTodoFeature feature;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Before
    public void createRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
    }

    @Before
    public void disablePermitLockByDate() {
        harvestPermitLockedByDateService.disableLockingForTests();
    }

    @After
    public void enablePermitLockByDate() {
        harvestPermitLockedByDateService.normalLocking();
    }

    // area

    @Test
    public void testPermitTodo_AreaMissing() {
        runTest(conf -> conf.hasArea = false, this::permitTodoTest);
    }

    @Test
    public void testClubTodo__AreaMissing() {
        runTest(conf -> conf.hasArea = false, this::clubTodoTest);
    }

    @Test
    public void testPermitTodo_AreaInactive() {
        runTest(conf -> conf.areaActive = false, this::permitTodoTest);
    }

    @Test
    public void testClubTodo_AreaInactive() {
        runTest(conf -> conf.areaActive = false, this::clubTodoTest);
    }

    @Test
    public void testPermitTodo_AreaYearNotMatches() {
        runTest(conf -> conf.areaYearMatches = false, this::permitTodoTest);
    }

    @Test
    public void testClubTodo_AreaYearNotMatches() {
        runTest(conf -> conf.areaYearMatches = false, this::clubTodoTest);
    }

    // group

    @Test
    public void testPermitTodo_GroupMissing() {
        runTest(conf -> conf.hasGroup = false, this::permitTodoTest);
    }

    @Test
    public void testClubTodo_GroupMissing() {
        runTest(conf -> conf.hasGroup = false, this::clubTodoTest);
    }

    @Test
    public void testPermitTodo_GroupNotLinkedToPermit() {
        runTest(conf -> conf.groupPermit = false, this::permitTodoTest);

    }

    @Test
    public void testClubTodo_GroupNotLinkedToPermit() {
        runTest(conf -> conf.groupPermit = false, this::clubTodoTest);
    }

    @Test
    public void testPermitTodo_GroupSpeciesNotMatching() {
        runTest(conf -> conf.groupSpecies = false, this::permitTodoTest);
    }

    @Ignore("Enable when groups species can be other than moose")
    @Test
    public void testClubTodo_GroupSpeciesNotMatching() {
        runTest(conf -> conf.groupSpecies = false, this::clubTodoTest);
    }

    // occupation

    @Test
    public void testPermitTodo_NoOccupation() {
        runTest(conf -> conf.hasGroupOccupation = false, this::permitTodoTest);
    }

    @Test
    public void testClubTodo_NoOccupation() {
        runTest(conf -> conf.hasGroupOccupation = false, this::clubTodoTest);
    }

    @Test
    public void testPermitTodo_OccupationMember() {
        runTest(conf -> conf.occupationLeader = false, this::permitTodoTest);
    }

    @Test
    public void testClubTodo_OccupationMember() {
        runTest(conf -> conf.occupationLeader = false, this::clubTodoTest);
    }

    @Test
    public void testPermitTodo_OccupationDeleted() {
        runTest(conf -> conf.occupationNotDeleted = false, this::permitTodoTest);
    }

    @Test
    public void testClubTodo_OccupationDeleted() {
        runTest(conf -> conf.occupationNotDeleted = false, this::clubTodoTest);
    }

    private static void runTest(Consumer<Conf> confMutate, Consumer<Conf> confRunner) {
        confMutate.andThen(confRunner).accept(Conf.allTrue());
    }

    private void permitTodoTest(Conf conf) {
        final HarvestPermit permit = model().newMooselikePermit(this.rhy);
        final GameSpecies species = model().newGameSpecies();
        model().newHarvestPermitSpeciesAmount(permit, species);

        final HuntingClub clubToTest = createData(conf, permit, species);
        final HuntingClub clubOk = createData(Conf.allTrue(), permit, species);
        final HuntingClub clubAllTodos = createData(Conf.allFalse(), permit, species);

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final Map<Long, MoosePermitTodoFeature.TodoDto> todos = feature.listTodos(permit.getId(), species.getOfficialCode());
            assertEquals(3, todos.size());

            final MoosePermitTodoFeature.TodoDto result = todos.get(clubToTest.getId());
            assertTrue(result.isTodo());
            assertEquals(!(conf.hasArea && conf.areaActive && conf.areaYearMatches), result.isAreaMissing());
            assertEquals(!(conf.hasGroup && conf.groupSpecies), result.isGroupMissing());
            assertEquals(!(conf.hasGroup && conf.groupSpecies && conf.groupPermit), result.isGroupPermitMissing());
            assertEquals(!(conf.hasGroup && conf.groupSpecies && conf.hasGroupOccupation && conf.occupationLeader && conf.occupationNotDeleted), result.isGroupLeaderMissing());

            assertTodo(todos.get(clubOk.getId()), false);
            assertTodo(todos.get(clubAllTodos.getId()), true);
        });
    }

    private void clubTodoTest(Conf conf) {
        final HarvestPermit permit = model().newMooselikePermit(this.rhy);
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermitSpeciesAmount hpsa = model().newHarvestPermitSpeciesAmount(permit, species);

        final HuntingClub clubToTest = createData(conf, permit, species);
        final HuntingClub clubOk = createData(Conf.allTrue(), permit, species);
        final HuntingClub clubAllTodos = createData(Conf.allFalse(), permit, species);

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final MoosePermitTodoFeature.TodoDto result =
                    feature.listTodosForClub(clubToTest.getId(), hpsa.resolveHuntingYear());

            assertTrue(result.isTodo());
            assertEquals(!(conf.hasArea && conf.areaActive && conf.areaYearMatches), result.isAreaMissing());
            assertEquals(!(conf.hasGroup && conf.groupSpecies), result.isGroupMissing());
            assertEquals(!(conf.hasGroup && conf.groupPermit), result.isGroupPermitMissing());
            assertEquals(!(conf.hasGroup && conf.hasGroupOccupation && conf.occupationLeader && conf.occupationNotDeleted), result.isGroupLeaderMissing());
        });
    }

    private static void assertTodo(MoosePermitTodoFeature.TodoDto clubOkResult, boolean expected) {
        assertEquals(expected, clubOkResult.isTodo());
        assertEquals(expected, clubOkResult.isAreaMissing());
        assertEquals(expected, clubOkResult.isGroupMissing());
        assertEquals(expected, clubOkResult.isGroupPermitMissing());
        assertEquals(expected, clubOkResult.isGroupLeaderMissing());
    }

    private HuntingClub createData(final Conf conf, final HarvestPermit permit, final GameSpecies species) {
        final HuntingClub club = model().newHuntingClub(this.rhy);
        permit.getPermitPartners().add(club);

        if (conf.hasArea) {
            final HuntingClubArea area = model().newHuntingClubArea(club);
            area.setActive(conf.areaActive);
            if (!conf.areaYearMatches) {
                area.setHuntingYear(area.getHuntingYear() - 1);
            }
        }

        if (conf.hasGroup) {
            final HuntingClubGroup group = model().newHuntingClubGroup(club);
            if (conf.groupSpecies) {
                group.setSpecies(species);
            }
            if (conf.groupPermit) {
                group.updateHarvestPermit(permit);
            }
            if (conf.hasGroupOccupation) {
                Occupation member = model().newHuntingClubGroupMember(group,
                        conf.occupationLeader ? OccupationType.RYHMAN_METSASTYKSENJOHTAJA : OccupationType.RYHMAN_JASEN);

                if (!conf.occupationNotDeleted) {
                    member.softDelete();
                }
            }
        }
        return club;
    }

    private static class Conf {
        boolean hasArea;
        boolean areaActive;
        boolean areaYearMatches;

        boolean hasGroup;
        boolean groupSpecies;
        boolean groupPermit;

        boolean hasGroupOccupation;
        boolean occupationLeader;
        boolean occupationNotDeleted;

        Conf(boolean hasArea, boolean areaActive, boolean areaYearMatches,
             boolean hasGroup, boolean groupSpecies, boolean groupPermit,
             boolean hasGroupOccupation, boolean occupationLeader, boolean occupationNotDeleted) {

            this.hasArea = hasArea;
            this.areaActive = areaActive;
            this.areaYearMatches = areaYearMatches;

            this.hasGroup = hasGroup;
            this.groupSpecies = groupSpecies;
            this.groupPermit = groupPermit;

            this.hasGroupOccupation = hasGroupOccupation;
            this.occupationLeader = occupationLeader;
            this.occupationNotDeleted = occupationNotDeleted;
        }

        private static Conf allTrue() {
            return new Conf(true, true, true, true, true, true, true, true, true);
        }

        private static Conf allFalse() {
            return new Conf(false, false, false, false, false, false, false, false, false);
        }
    }
}
