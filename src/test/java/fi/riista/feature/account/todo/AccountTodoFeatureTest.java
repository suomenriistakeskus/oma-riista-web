package fi.riista.feature.account.todo;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.HarvestReportRequirementsService;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;

public class AccountTodoFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private AccountTodoFeature accountTodoFeature;

    @Resource
    private HarvestReportRequirementsService harvestReportRequirementsService;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    private Riistanhoitoyhdistys rhy;

    @Before
    public void initRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
    }

    @Test
    public void testTodoCountWithNothing() {
        persistAndAuthenticateWithNewUser(true);
        assertCount(0, 0, accountTodoFeature.todoCount());
    }

    @Test
    public void testCountSeasonAndPermitHarvests() {
        withPerson(person -> {

            HarvestSeason season = model().newHarvestSeason(today());
            Harvest seasonHarvest = model().newHarvest(season.getFields().getSpecies(), person);

            HarvestPermit permit = model().newHarvestPermit(this.rhy);
            Harvest permitHarvest = model().newHarvest(person);
            model().newHarvestReportFields(permitHarvest.getSpecies(), true);
            permitHarvest.setHarvestPermit(permit);
            permitHarvest.setRhy(permit.getRhy());

            onSavedAndAuthenticated(createUser(person), () -> {
                updateHarvestReportRequired(seasonHarvest, permitHarvest);
                assertCount(2, 0, accountTodoFeature.todoCount());
            });
        });
    }

    @Test
    public void testPermitEndOfHuntingReportNotDone() {
        withPerson(person -> {
            final LocalDate today = today();

            createPermit(person, today.plusDays(1), null); // not counted
            createPermit(person, today, null);

            onSavedAndAuthenticated(createUser(person), () -> assertCount(0, 1, accountTodoFeature.todoCount()));
        });
    }

    @Test
    public void testPermitEndOfHuntingReportNotDoneMooselikePermit() {
        withPerson(person -> {
            final LocalDate today = today();

            HarvestPermit p1 = createPermit(person, today.plusDays(1), null);
            HarvestPermit p2 = createPermit(person, today, null);

            // because both permits are to mooselike, neither of them are counted
            p1.setPermitTypeCode(HarvestPermit.MOOSELIKE_PERMIT_TYPE);
            p1.setPermitAreaSize(123);
            p2.setPermitTypeCode(HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);

            onSavedAndAuthenticated(createUser(person), () -> assertCount(0, 0, accountTodoFeature.todoCount()));
        });
    }

    @Test
    public void testPermitEndOfHuntingReportDone() {
        withPerson(person -> {
            final LocalDate today = today();

            createPermit(person, today.plusDays(1), null); // not counted
            final HarvestPermit permit = createPermit(person, today, null);

            permit.setEndOfHuntingReport(
                    model().newHarvestReport_endOfHunting(permit, HarvestReport.State.SENT_FOR_APPROVAL));

            onSavedAndAuthenticated(createUser(person), () -> assertCount(0, 0, accountTodoFeature.todoCount()));
        });
    }

    @Test
    public void testPermitQuotaUsed() {
        withPerson(person -> {

            final HarvestPermit permit = createPermit(person, today(), null);

            onSavedAndAuthenticated(createUser(person), tx(() -> {
                HarvestPermit reloadedPermit = harvestPermitRepository.getOne(permit.getId());
                HarvestPermitSpeciesAmount amount = reloadedPermit.getSpeciesAmounts().get(0);
                HarvestPermitSpeciesAmount amount2 = reloadedPermit.getSpeciesAmounts().get(1);

                model().newHarvestReport(model().newHarvest(reloadedPermit, amount.getGameSpecies()), HarvestReport.State.SENT_FOR_APPROVAL);
                model().newHarvestReport(model().newHarvest(reloadedPermit, amount.getGameSpecies()), HarvestReport.State.SENT_FOR_APPROVAL);
                model().newHarvestReport(model().newHarvest(reloadedPermit, amount2.getGameSpecies()), HarvestReport.State.APPROVED);
                model().newHarvestReport(model().newHarvest(reloadedPermit, amount2.getGameSpecies()), HarvestReport.State.APPROVED);

                persistInCurrentlyOpenTransaction();

                assertCount(0, 0, accountTodoFeature.todoCount());
            }));
        });
    }

    @Test
    public void testPermitQuotaUnUsed() {
        withPerson(person -> {

            final HarvestPermit permit = createPermit(person, today().minusDays(1), null);

            onSavedAndAuthenticated(createUser(person), tx(() -> {
                HarvestPermit reloadedPermit = harvestPermitRepository.getOne(permit.getId());
                HarvestPermitSpeciesAmount amount = reloadedPermit.getSpeciesAmounts().get(0);
                HarvestPermitSpeciesAmount amount2 = reloadedPermit.getSpeciesAmounts().get(1);

                model().newHarvestReport(model().newHarvest(reloadedPermit, amount.getGameSpecies()), HarvestReport.State.SENT_FOR_APPROVAL);
                model().newHarvestReport(model().newHarvest(reloadedPermit, amount.getGameSpecies()), HarvestReport.State.DELETED);
                model().newHarvestReport(model().newHarvest(reloadedPermit, amount2.getGameSpecies()), HarvestReport.State.REJECTED);
                model().newHarvestReport(model().newHarvest(reloadedPermit, amount2.getGameSpecies()), HarvestReport.State.DELETED);

                persistInCurrentlyOpenTransaction();

                assertCount(0, 1, accountTodoFeature.todoCount());
            }));
        });
    }

    private HarvestPermit createPermit(Person person, LocalDate beginDate, LocalDate beginDate2) {
        final HarvestPermit permit = model().newHarvestPermit(this.rhy);
        permit.setOriginalContactPerson(person);

        HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies(), 2.0f);
        spa.setBeginDate(beginDate);
        spa.setBeginDate2(beginDate2);

        HarvestPermitSpeciesAmount spa2 = model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies(), 2.0f);
        spa2.setBeginDate(beginDate);
        spa2.setBeginDate2(beginDate2);

        return permit;
    }

    @Test
    public void testHarvestProposedForPermit() {
        doTestHarvestForPermit(Harvest.StateAcceptedToHarvestPermit.PROPOSED, 0);
    }

    @Test
    public void testHarvestAcceptedForPermit() {
        doTestHarvestForPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED, 0);
    }

    @Test
    public void testHarvestRejectedForPermit() {
        doTestHarvestForPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED, 1);
    }

    private void doTestHarvestForPermit(Harvest.StateAcceptedToHarvestPermit state, int expectedTodoCount) {
        SystemUser user = createUserWithPerson();

        HarvestPermit permit = model().newHarvestPermit(this.rhy, true);

        Harvest permitHarvest = model().newHarvest(user.getPerson());
        permitHarvest.setHarvestPermit(permit);
        permitHarvest.setStateAcceptedToHarvestPermit(state);
        permitHarvest.setRhy(permit.getRhy());

        persistInNewTransaction();

        updateHarvestReportRequired(permitHarvest);

        authenticate(user);

        assertCount(expectedTodoCount, 0, accountTodoFeature.todoCount());
    }

    private void updateHarvestReportRequired(final Harvest... harvests) {
        runInTransaction(() -> {
            for (final Harvest h : harvestRepository.findAll(F.getUniqueIds(harvests))) {
                h.setHarvestReportRequired(harvestReportRequirementsService.isHarvestReportRequired(
                        h.getSpecies(),
                        DateUtil.toLocalDateNullSafe(h.getPointOfTime()),
                        h.getGeoLocation(),
                        h.getHarvestPermit()));
            }
            persistInCurrentlyOpenTransaction();
        });
    }

    private static void assertCount(int harvestCount, int permitCount, AccountTodoCountDTO dto) {
        assertEquals("Harvest todo count does not match expected", harvestCount, dto.getHarvests());
        assertEquals("Permit todo count does not match expected", permitCount, dto.getPermits());
        assertEquals("harvest+permit todo sum does not match", harvestCount + permitCount, dto.getHarvestsAndPermitsTotal());
    }

}
