package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class HarvestPermitListFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitListFeature harvestPermitListFeature;

    private RiistakeskuksenAlue rka;

    @Before
    public void initRka() {
        this.rka = model().newRiistakeskuksenAlue();
    }

    @Test
    public void testListMyPermits() {
        doTestListPermits(createUserWithPerson("user1"), null);
    }

    @Test
    public void testListMyPermitsAsModerator() {
        doTestListPermits(createNewModerator(), model().newPerson());
    }

    private void doTestListPermits(final SystemUser callingUser, Person personViewed) {
        withRhy(rhy -> {
            final Person permitContact = personViewed != null ? personViewed : callingUser.getPerson();

            final HarvestPermit permit = model().newHarvestPermit(rhy, permitContact, true);

            final HarvestPermit amendmentPermit = model().newHarvestPermit(permit, permitContact, true);
            amendmentPermit.setPermitTypeCode(HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);

            // Permit for which the user is not contact person, should not be included in the results.
            model().newHarvestPermit(rhy, model().newPerson(), true);

            onSavedAndAuthenticated(callingUser, () -> {
                final List<HarvestPermitListDTO> results = harvestPermitListFeature.listPermitsForPerson(F.getId(personViewed));

                // Very limited testing - does not test other content of DTOs yet.
                assertEquals(F.getUniqueIds(permit), F.getUniqueIds(results));
            });
        });
    }

    @Test
    public void testPreloadPermits() {
        withRhy(rhy -> withPerson(person -> {

            GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();

            HarvestPermit permit = model().newHarvestPermit(rhy);
            HarvestPermit listPermit = model().newHarvestPermit(rhy, true);

            createHarvestAndReport(person, species, permit, null);
            createHarvest(person, species, listPermit);

            testAndAssert(createUser(person), permit, listPermit);
        }));
    }

    @Test
    public void testPreloadPermitsForPermitContactPerson() {
        withRhy(rhy -> withPerson(person -> {

            GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();

            HarvestPermit permit = model().newHarvestPermit(rhy, person);
            HarvestPermit listPermit = model().newHarvestPermit(rhy, person, true);

            createHarvestAndReport(person, species, permit, null);
            createHarvest(person, species, listPermit);

            testAndAssert(createUser(person), permit, listPermit);
        }));
    }

    @Test
    public void testPreloadPermitsWhenHarvestReportsDone() {
        withRhy(rhy -> withPerson(person -> {

            GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();

            HarvestPermit permit = model().newHarvestPermit(rhy);
            HarvestPermit listPermit = model().newHarvestPermit(rhy, true);

            createHarvestAndReport(person, species, permit, HarvestReportState.APPROVED);
            createHarvest(person, species, listPermit);
            createEndOfHuntingReport(person, listPermit, HarvestReportState.APPROVED);

            testAndAssert(createUser(person), permit);
        }));
    }

    @Test
    public void testPreloadPermitsWhenHarvestReportsDoneForPermitContactPerson() {
        withPerson(person -> withRhy(rhy -> {

            GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();

            HarvestPermit permit = model().newHarvestPermit(rhy, person);
            HarvestPermit listPermit = model().newHarvestPermit(rhy, person, true);

            createHarvestAndReport(person, species, permit, HarvestReportState.APPROVED);
            createHarvest(person, species, listPermit);
            createEndOfHuntingReport(person, listPermit, HarvestReportState.APPROVED);

            testAndAssert(createUser(person), permit);
        }));
    }

    @Test
    public void testPreloadPermitsForAmendmentPermits() {
        withPerson(person -> withRhy(rhy -> {

            GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();

            HarvestPermit originalSingularPermit = model().newHarvestPermit(rhy, model().newPerson());
            HarvestPermit amendmentForSingularPermit = model().newHarvestPermit(originalSingularPermit, person);
            amendmentForSingularPermit.setPermitTypeCode(HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);

            createHarvestAndReport(person, species, amendmentForSingularPermit, null);

            testAndAssert(createUser(person));
        }));
    }

    private void createHarvestAndReport(Person author,
                                        GameSpecies species,
                                        HarvestPermit permit,
                                        HarvestReportState state) {

        Harvest harvest = model().newHarvest(species, author, author);
        harvest.setHarvestPermit(permit);
        harvest.setRhy(permit.getRhy());

        if (state != null) {
            harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
            harvest.setHarvestReportState(state);
            harvest.setHarvestReportAuthor(author);
            harvest.setHarvestReportDate(DateUtil.now());
        } else {
            harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.PROPOSED);
        }
    }

    private Harvest createHarvest(Person author,
                                  GameSpecies species,
                                  HarvestPermit permit) {
        return model().newHarvest(author, permit, species);
    }

    private void createEndOfHuntingReport(Person author,
                                          HarvestPermit permit,
                                          HarvestReportState state) {
        permit.setHarvestReportState(state);
        permit.setHarvestReportAuthor(author);
        permit.setHarvestReportDate(DateUtil.now());
        permit.setHarvestReportModeratorOverride(false);
    }

    private void testAndAssert(SystemUser user, HarvestPermit... expectedPermits) {
        onSavedAndAuthenticated(user, () -> assertEquals(
                F.getUniqueIds(expectedPermits),
                F.getUniqueIds(harvestPermitListFeature.preloadNonMoosePermits())));
    }


    @Test
    public void testMooselikePermits() {
        runTest((species, rhy) -> newHarvestPermit(rhy, species));
    }

    @Test
    public void testMooselikePermits_relatedRhy() {
        runTest((species, rhy) -> {
            final HarvestPermit p = newHarvestPermit(model().newRiistanhoitoyhdistys(this.rka), species);
            p.setRelatedRhys(Collections.singleton(rhy));
            return p;
        });
    }

    private HarvestPermit newHarvestPermit(Riistanhoitoyhdistys rhy, GameSpecies species) {
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        permit.setPermitHolder(model().newHuntingClub(rhy));

        model().newHarvestPermitSpeciesAmount(permit, species).setCreditorReference(creditorReference());

        return permit;
    }

    private void runTest(BiFunction<GameSpecies, Riistanhoitoyhdistys, HarvestPermit> permitCreator) {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(this.rka);
        final Riistanhoitoyhdistys otherRhy = model().newRiistanhoitoyhdistys(this.rka);

        final GameSpecies moose = model().newGameSpeciesMoose();
        model().newMooselikePrice(
                DateUtil.huntingYear(),
                moose,
                BigDecimal.valueOf(120),
                BigDecimal.valueOf(50)
        );

        final HarvestPermit permit = permitCreator.apply(moose, rhy);
        permitCreator.apply(moose, otherRhy);

        onSavedAndAuthenticated(createNewAdmin(), tx(() -> {
            final List<MooselikeHuntingYearDTO> years = harvestPermitListFeature.listRhyMooselikeHuntingYears(rhy.getId());
            assertEquals(1, years.size());

            final Integer year = DateUtil.huntingYear();
            final List<MooselikePermitListDTO> permits = harvestPermitListFeature.listRhyMooselikePermits(rhy.getId(), year, GameSpecies.OFFICIAL_CODE_MOOSE, null);
            assertEquals(1, permits.size());
            assertEquals((long) permit.getId(), permits.get(0).getId());
        }));
    }
}
