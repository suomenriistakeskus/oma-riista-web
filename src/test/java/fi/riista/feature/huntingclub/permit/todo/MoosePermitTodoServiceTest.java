package fi.riista.feature.huntingclub.permit.todo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Resource;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MoosePermitTodoServiceTest extends EmbeddedDatabaseTest {

    private Riistanhoitoyhdistys rhy;

    @Resource
    private MoosePermitTodoService moosePermitTodoService;

    @Before
    public void createRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
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

    // partner summary
    @Test
    public void testPartnerTodo_huntingNotFinished_datesValid() {
        runTest(conf -> {
            conf.harvestPermitSpeciesAmountDatesValid = true;
            conf.partnerHuntingSummaryHasHuntingFinished = false;
        }, this::partnerTodoTest);
    }

    @Test
    public void testPartnerTodo_huntingNotFinished_datesExpired() {
        runTest(conf -> {
            conf.harvestPermitSpeciesAmountDatesValid = false;
            conf.partnerHuntingSummaryHasHuntingFinished = false;
        }, this::partnerTodoTest);
    }

    @Test
    public void testPartnerTodo_noSummary_datesValid() {
        runTest(conf -> {
            conf.harvestPermitSpeciesAmountDatesValid = true;
            conf.partnerHasHuntingSummary = false;
        }, this::partnerTodoTest);
    }

    @Test
    public void testPartnerTodo_noSummary_datesExpired() {
        runTest(conf -> {
            conf.harvestPermitSpeciesAmountDatesValid = false;
            conf.partnerHasHuntingSummary = false;
        }, this::partnerTodoTest);
    }

    private static void runTest(Consumer<Conf> confMutate, Consumer<Conf> confRunner) {
        confMutate.andThen(confRunner).accept(Conf.allTrue());
    }

    private void permitTodoTest(Conf conf) {
        final HarvestPermit permit = model().newMooselikePermit(this.rhy);
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermitSpeciesAmount hpsa = model().newHarvestPermitSpeciesAmount(permit, species);

        final HuntingClub clubToTest = createData(conf, permit, hpsa);
        final HuntingClub clubOk = createData(Conf.allTrue(), permit, hpsa);
        final HuntingClub clubAllTodos = createData(Conf.allFalse(), permit, hpsa);

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final Map<Long, MoosePermitTodoDTO> todos = moosePermitTodoService.listTodos(permit, species);
            assertEquals(3, todos.size());

            final MoosePermitTodoDTO result = todos.get(clubToTest.getId());
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

        final HuntingClub clubToTest = createData(conf, permit, hpsa);
        final HuntingClub clubOk = createData(Conf.allTrue(), permit, hpsa);
        final HuntingClub clubAllTodos = createData(Conf.allFalse(), permit, hpsa);

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final MoosePermitTodoDTO result =
                    moosePermitTodoService.listTodosForClub(clubToTest, hpsa.resolveHuntingYear());

            assertTrue(result.isTodo());
            assertEquals(!(conf.hasArea && conf.areaActive && conf.areaYearMatches), result.isAreaMissing());
            assertEquals(!(conf.hasGroup && conf.groupSpecies), result.isGroupMissing());
            assertEquals(!(conf.hasGroup && conf.groupPermit), result.isGroupPermitMissing());
            assertEquals(!(conf.hasGroup && conf.hasGroupOccupation && conf.occupationLeader && conf.occupationNotDeleted), result.isGroupLeaderMissing());
        });
    }

    private void partnerTodoTest(final Conf conf) {
        final GameSpecies species = model().newGameSpecies();

        final HarvestPermit permit = model().newMooselikePermit(this.rhy);
        final HarvestPermitSpeciesAmount hpsa = model().newHarvestPermitSpeciesAmount(permit, species);
        if (!conf.harvestPermitSpeciesAmountDatesValid) {
            // this fails tests on the first day of hunting year?
            hpsa.setEndDate(LocalDate.now().minusDays(1));
        }

        final HarvestPermit prevYearPermit = model().newMooselikePermit(this.rhy);
        final HarvestPermitSpeciesAmount prevYearHpsa = model().newHarvestPermitSpeciesAmount(prevYearPermit, species);
        if (!conf.harvestPermitSpeciesAmountDatesValid) {
            prevYearHpsa.setBeginDate(prevYearHpsa.getBeginDate().minusYears(1));
            prevYearHpsa.setEndDate(prevYearHpsa.getEndDate().minusYears(1));
        }

        final HuntingClub prevYear = createData(conf, prevYearPermit, prevYearHpsa);
        final HuntingClub clubToTest = createData(conf, permit, hpsa);
        final HuntingClub clubOk = createData(Conf.allTrue(), permit, hpsa);
        final HuntingClub clubAllTodos = createData(Conf.allFalse(), permit, hpsa);

        persistInNewTransaction();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            // permit for previous year, but asking for current year
            assertFalse(moosePermitTodoService.listTodosForClub(prevYear, hpsa.resolveHuntingYear()).isPartnerHuntingSummaryMissing());

            final MoosePermitTodoDTO result =
                    moosePermitTodoService.listTodosForClub(clubToTest, hpsa.resolveHuntingYear());

            assertFalse(result.isTodo());

            final boolean datesNotValid = !conf.harvestPermitSpeciesAmountDatesValid;
            final boolean noSummary = !conf.partnerHasHuntingSummary;
            final boolean notFinished = !conf.partnerHuntingSummaryHasHuntingFinished;
            final boolean expected = datesNotValid && (noSummary || notFinished);
            assertEquals(expected, result.isPartnerHuntingSummaryMissing());
        });
    }

    private static void assertTodo(MoosePermitTodoDTO clubOkResult, boolean expected) {
        assertEquals(expected, clubOkResult.isTodo());
        assertEquals(expected, clubOkResult.isAreaMissing());
        assertEquals(expected, clubOkResult.isGroupMissing());
        assertEquals(expected, clubOkResult.isGroupPermitMissing());
        assertEquals(expected, clubOkResult.isGroupLeaderMissing());
    }

    private HuntingClub createData(final Conf conf, final HarvestPermit permit, final HarvestPermitSpeciesAmount hpsa) {
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
                group.setSpecies(hpsa.getGameSpecies());
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
        if (conf.partnerHasHuntingSummary) {
            model().newBasicHuntingSummary(hpsa, club, conf.partnerHuntingSummaryHasHuntingFinished);
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

        boolean partnerHasHuntingSummary;
        boolean partnerHuntingSummaryHasHuntingFinished;
        boolean harvestPermitSpeciesAmountDatesValid;

        Conf(boolean hasArea, boolean areaActive, boolean areaYearMatches,
             boolean hasGroup, boolean groupSpecies, boolean groupPermit,
             boolean hasGroupOccupation, boolean occupationLeader, boolean occupationNotDeleted,
             boolean partnerHasHuntingSummary, boolean partnerHuntingSummaryHasHuntingFinished,
             boolean harvestPermitSpeciesAmountDatesValid) {

            this.hasArea = hasArea;
            this.areaActive = areaActive;
            this.areaYearMatches = areaYearMatches;

            this.hasGroup = hasGroup;
            this.groupSpecies = groupSpecies;
            this.groupPermit = groupPermit;

            this.hasGroupOccupation = hasGroupOccupation;
            this.occupationLeader = occupationLeader;
            this.occupationNotDeleted = occupationNotDeleted;

            this.partnerHasHuntingSummary = partnerHasHuntingSummary;
            this.partnerHuntingSummaryHasHuntingFinished = partnerHuntingSummaryHasHuntingFinished;
            this.harvestPermitSpeciesAmountDatesValid = harvestPermitSpeciesAmountDatesValid;
        }

        private static Conf allTrue() {
            return new Conf(true, true, true, true, true, true, true, true, true, true, true, true);
        }

        private static Conf allFalse() {
            return new Conf(false, false, false, false, false, false, false, false, false, false, false, true);
        }
    }
}
