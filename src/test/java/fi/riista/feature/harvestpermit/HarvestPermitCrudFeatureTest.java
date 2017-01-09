package fi.riista.feature.harvestpermit;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.ACCEPTED;
import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.PROPOSED;
import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.REJECTED;
import static fi.riista.util.Asserts.assertEmpty;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

public class HarvestPermitCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitCrudFeature harvestPermitCrudFeature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitContactPersonRepository contactPersonRepository;

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
                final List<HarvestPermitDTO> results = harvestPermitCrudFeature.listMyPermits(F.getId(personViewed));

                // Very limited testing - does not test other content of DTOs yet.
                assertEquals(F.getUniqueIds(permit), F.getUniqueIds(results));
            });
        });
    }

    @Test
    public void testProposedToAccepted() {
        testStateChange(PROPOSED, ACCEPTED, false);
    }

    @Test
    public void testProposedToRejected() {
        testStateChange(PROPOSED, REJECTED, false);
    }

    @Test
    public void testProposedToRejectedWhenHarvestReportExists() {
        testStateChange(PROPOSED, REJECTED, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testProposedToAcceptedWhenHarvestReportExists() {
        testStateChange(PROPOSED, ACCEPTED, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testRejectedToAcceptedWhenHarvestReportExists() {
        testStateChange(REJECTED, ACCEPTED, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testAcceptedToRejectedWhenHarvestReportExists() {
        testStateChange(ACCEPTED, REJECTED, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testAccepHarvestCantBeCalledForMooselikePermit() {
        testStateChange(PROPOSED, ACCEPTED, false, HarvestPermit.MOOSELIKE_PERMIT_TYPE);
    }

    @Test(expected = IllegalStateException.class)
    public void testAccepHarvestCantBeCalledForAmendmentPermit() {
        testStateChange(PROPOSED, ACCEPTED, false, HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);
    }

    private void testStateChange(final Harvest.StateAcceptedToHarvestPermit from,
                                 final Harvest.StateAcceptedToHarvestPermit to,
                                 final boolean createHarvestReport) {
        testStateChange(from, to, createHarvestReport, "201");
    }

    private void testStateChange(final Harvest.StateAcceptedToHarvestPermit from,
                                 final Harvest.StateAcceptedToHarvestPermit to,
                                 final boolean createHarvestReport,
                                 final String permitTypeCde) {
        final SystemUser user = createUserWithPerson();

        final HarvestPermit permit = model().newHarvestPermit(user.getPerson(), true);
        permit.setPermitTypeCode(permitTypeCde);
        final Harvest harvest = createProposedHarvestForPermit(user, permit, from);
        if (createHarvestReport) {
            createHarvestReport(permit, harvest);
        }

        persistInNewTransaction();

        doTestStateChangeAndAsserts(user, harvest, to);
    }

    private HarvestReport createHarvestReport(HarvestPermit permit, Harvest harvest) {
        HarvestReport harvestReport = model().newHarvestReport(harvest, HarvestReport.State.SENT_FOR_APPROVAL);
        harvestReport.setHarvestPermit(permit);
        harvest.setHarvestReport(harvestReport);
        return harvestReport;
    }

    private void doTestStateChangeAndAsserts(
            final SystemUser user, final Harvest harvest, final Harvest.StateAcceptedToHarvestPermit toState) {

        authenticate(user);

        final HarvestPermit permit = Objects.requireNonNull(harvest.getHarvestPermit());

        harvestPermitCrudFeature.acceptHarvest(harvest.getId(), harvest.getConsistencyVersion(), toState);

        runInTransaction(() -> {
            HarvestPermit updatedPermit = harvestPermitRepository.getOne(permit.getId());

            assertEquals(1, updatedPermit.getHarvests().size());

            Harvest h = updatedPermit.getHarvests().iterator().next();
            assertEquals(harvest.getId(), h.getId());
            assertEquals(toState, h.getStateAcceptedToHarvestPermit());
        });
    }

    private Harvest createProposedHarvestForPermit(
            final SystemUser user, final HarvestPermit permit, final Harvest.StateAcceptedToHarvestPermit state) {

        final Harvest harvest = model().newHarvest(user.getPerson());
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(state);
        harvest.setRhy(permit.getRhy());
        return harvest;
    }

    @Test
    public void testPreloadPermits() {
        withRhy(rhy -> withPerson(person -> {

            HarvestPermit permit = model().newHarvestPermit(rhy);
            HarvestPermit listPermit = model().newHarvestPermit(rhy, true);

            createHarvestAndReport(person, permit, null);
            createHarvestAndReport(person, listPermit, null);

            testAndAssert(createUser(person), permit, listPermit);
        }));
    }

    @Test
    public void testPreloadPermitsForPermitContactPerson() {
        withRhy(rhy -> withPerson(person -> {

            HarvestPermit permit = model().newHarvestPermit(rhy, person);
            HarvestPermit listPermit = model().newHarvestPermit(rhy, person, true);

            createHarvestAndReport(person, permit, null);
            createHarvestAndReport(person, listPermit, null);

            testAndAssert(createUser(person), permit, listPermit);
        }));
    }

    @Test
    public void testPreloadPermitsWhenHarvestReportsDone() {
        withRhy(rhy -> withPerson(person -> {

            HarvestPermit permit = model().newHarvestPermit(rhy);
            HarvestPermit listPermit = model().newHarvestPermit(rhy, true);

            createHarvestAndReport(person, permit, HarvestReport.State.APPROVED);
            createHarvestAndReport(person, listPermit, HarvestReport.State.APPROVED);

            testAndAssert(createUser(person), permit);
        }));
    }

    @Test
    public void testPreloadPermitsWhenHarvestReportsDoneForPermitContactPerson() {
        withPerson(person -> withRhy(rhy -> {

            HarvestPermit permit = model().newHarvestPermit(rhy, person);
            HarvestPermit listPermit = model().newHarvestPermit(rhy, person, true);

            createHarvestAndReport(person, permit, HarvestReport.State.APPROVED);
            createHarvestAndReport(person, listPermit, HarvestReport.State.APPROVED);

            testAndAssert(createUser(person), permit);
        }));
    }

    @Test
    public void testPreloadPermitsForAmendmentPermits() {
        withPerson(person -> withRhy(rhy -> {

            HarvestPermit originalSingularPermit = model().newHarvestPermit(rhy, model().newPerson());
            HarvestPermit amendmentForSingularPermit = model().newHarvestPermit(originalSingularPermit, person);
            amendmentForSingularPermit.setPermitTypeCode(HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);

            HarvestPermit originalListPermit = model().newHarvestPermit(rhy, model().newPerson(), true);
            HarvestPermit amendmentForListPermit = model().newHarvestPermit(originalListPermit, person, true);
            amendmentForListPermit.setPermitTypeCode(HarvestPermit.MOOSELIKE_AMENDMENT_PERMIT_TYPE);

            createHarvestAndReport(person, amendmentForSingularPermit, null);
            createHarvestAndReport(person, amendmentForListPermit, null);

            testAndAssert(createUser(person));
        }));
    }

    private void createHarvestAndReport(Person author, HarvestPermit permit, HarvestReport.State state) {
        Harvest harvest = model().newHarvest(permit);
        harvest.setAuthor(author);
        if (state != null) {
            model().newHarvestReport(harvest, state);
        }
    }

    private void testAndAssert(SystemUser user, HarvestPermit... expectedPermits) {
        onSavedAndAuthenticated(user, () -> assertEquals(
                F.getUniqueIds(expectedPermits),
                F.getUniqueIds(harvestPermitCrudFeature.preloadPermits())));
    }

    @Test
    public void testUpdateContactPersons() {
        withRhy(rhy -> {
            final Person person1 = model().newPerson();
            final Person person2 = model().newPerson();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final Person originalContactPerson = permit.getOriginalContactPerson();

            final HarvestPermitContactPerson oldContactPerson1 = model().newHarvestPermitContactPerson(permit, person1);
            final HarvestPermitContactPerson oldContactPerson2 = model().newHarvestPermitContactPerson(permit, person2);

            final HarvestPermit permit2 = model().newHarvestPermit(rhy);
            final HarvestPermitContactPerson permit2ContactPerson =
                    model().newHarvestPermitContactPerson(permit2, person1);

            final Person person3 = model().newPerson();
            final Person person4 = model().newPerson();

            onSavedAndAuthenticated(createNewModerator(), () -> {

                harvestPermitCrudFeature.updateContactPersons(permit.getId(), Arrays.asList(
                        HarvestPermitContactPersonDTO.create(person3, false),
                        HarvestPermitContactPersonDTO.create(person4, false),
                        // should be ignored
                        HarvestPermitContactPersonDTO.create(originalContactPerson, false)));

                assertEmpty(contactPersonRepository.findAll(F.getUniqueIds(oldContactPerson1, oldContactPerson2)));

                final List<HarvestPermitContactPerson> updatedContactPersons =
                        contactPersonRepository.findByHarvestPermit(permit);
                assertEquals(2, updatedContactPersons.size());

                assertEquals(
                        F.getUniqueIds(person3, person4),
                        updatedContactPersons.stream().map(cp -> cp.getContactPerson().getId()).collect(toSet()));

                // Assert that permit2 is left intact.
                assertEquals(
                        F.getUniqueIds(permit2ContactPerson),
                        F.getUniqueIds(contactPersonRepository.findByHarvestPermit(permit2)));
            });
        });
    }

}
