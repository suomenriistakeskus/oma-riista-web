package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.violation.AmendmentPermitDoesNotMatchHarvestCountException;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.support.HuntingClubTestDataHelper;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoice;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoiceRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import static fi.riista.feature.permit.invoice.InvoiceState.DELIVERED;
import static fi.riista.feature.permit.invoice.InvoiceState.PAID;
import static fi.riista.feature.permit.invoice.InvoiceState.REMINDER;
import static fi.riista.feature.permit.invoice.InvoiceState.VOID;
import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EndOfMooselikePermitHuntingFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private EndOfMooselikePermitHuntingFeature endOfMooselikePermitHuntingFeature;

    @Resource
    private PermitHarvestInvoiceRepository permitHarvestInvoiceRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository speciesAmountRepository;

    private final HuntingClubTestDataHelper helper = new HuntingClubTestDataHelper() {
        @Override
        protected EntitySupplier model() {
            return EndOfMooselikePermitHuntingFeatureTest.this.model();
        }
    };

    private void withHarvestPermitFixture(final Consumer<Fixture> consumer) {
        final Fixture f = new Fixture();
        f.rhy = model().newRiistanhoitoyhdistys();
        f.mooseSpecies = model().newGameSpeciesMoose();

        f.application = model().newHarvestPermitApplication(f.rhy, null, f.mooseSpecies);
        f.application.setApplicationYear(f.huntingYear);

        f.permitDecision = model().newPermitDecision(f.application);

        f.permit = model().newMooselikePermit(f.permitDecision);
        f.speciesAmount = model().newHarvestPermitSpeciesAmount(f.permit, f.mooseSpecies);

        f.club = model().newHuntingClub(f.rhy);
        f.permit.getPermitPartners().add(f.club);

        f.group = model().newHuntingClubGroup(f.club, f.speciesAmount);

        consumer.accept(f);
    }

    private static class Fixture {
        final int huntingYear = huntingYear();
        Riistanhoitoyhdistys rhy;
        GameSpecies mooseSpecies;
        HarvestPermitApplication application;
        PermitDecision permitDecision;
        HarvestPermit permit;
        HarvestPermitSpeciesAmount speciesAmount;
        HuntingClub club;
        HuntingClubGroup group;
    }

    // HAPPY cases

    @Test
    public void testPermitDecisionNotPresent() {
        withHarvestPermitFixture(f -> {

            f.permit.setPermitDecision(null);

            persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);

            assertMooseHuntingEndStatus(f.speciesAmount.getId(), true);
            assertHarvestInvoiceNotCreated();
        });
    }

    @Test
    public void testAllPartnersFinishedHuntingWithoutHarvest() {
        withHarvestPermitFixture(f -> {
            persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);

            runInTransaction(() -> {
                final Invoice invoice = getHarvestInvoiceAndAssertOnlyOneExists().getInvoice();
                assertPaymentTerms(invoice, today(), VOID, new BigDecimal("0.00"), null);
            });
        });
    }

    @Test
    public void testAllPartnersFinishedHuntingWithHarvest() {
        withHarvestPermitFixture(f -> {
            helper.createHarvestsForHuntingGroup(f.group, model().newPerson(), HasHarvestCountsForPermit.of(
                    6, 5, 4, 3,
                    0, 0));

            persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);

            runInTransaction(() -> {
                final Invoice invoice = getHarvestInvoiceAndAssertOnlyOneExists().getInvoice();

                // (6 + 5) * 120 + (4 + 3) * 50 = 11 * 120 + 7 * 50 = 1670
                final BigDecimal expectedAmount = new BigDecimal("1670.00");

                assertPaymentTerms(invoice, today(), DELIVERED, expectedAmount, null);
            });
        });
    }

    @Test
    public void testAllPartnersFinishedHuntingWithHarvestWithAmendmentPermits() {
        withHarvestPermitFixture(f -> {

            final HarvestPermit amendmentPermit1 = model().newHarvestPermit(f.permit);
            model().newHarvestPermitSpeciesAmount(amendmentPermit1, f.mooseSpecies, 1f);

            final HarvestPermit amendmentPermit2 = model().newHarvestPermit(f.permit);
            model().newHarvestPermitSpeciesAmount(amendmentPermit2, f.mooseSpecies, 1.5f);

            helper.createHarvestsForHuntingGroup(f.group, model().newPerson(), HasHarvestCountsForPermit.of(
                    6, 5, 4, 3,
                    2, 1));

            persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);

            runInTransaction(() -> {
                final Invoice invoice = getHarvestInvoiceAndAssertOnlyOneExists().getInvoice();

                // (6 + 5 - 2) * 120 + (4 + 3 - 1) * 50 = 9 * 120 + 6 * 50 = 1380
                final BigDecimal expectedAmount = new BigDecimal("1380.00");

                assertPaymentTerms(invoice, today(), DELIVERED, expectedAmount, null);
            });
        });
    }

    @Test
    public void testCallingAgainEndMooseHunting_whenInvoiceWasNotPaidBeforeCancellingEndOfMooseHunting() {
        final LocalDate today = today();

        withHarvestPermitFixture(f -> {
            final PermitHarvestInvoice harvestInvoice =
                    model().newPermitHarvestInvoice(f.speciesAmount, today.minusDays(1));

            final Invoice invoice = harvestInvoice.getInvoice();
            invoice.setState(InvoiceState.VOID); // resulting state after cancelling invoice in CREATED/DELIVERED state
            invoice.setAmount(new BigDecimal("120.00")); // original amount

            helper.createHarvestsForHuntingGroup(f.group, model().newPerson(), HasHarvestCountsForPermit.of(
                    1, 1, 0, 0,
                    0, 0));

            persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);

            runInTransaction(() -> {
                final PermitHarvestInvoice updatedHarvestInvoice = getHarvestInvoiceAndAssertOnlyOneExists();
                assertEquals(harvestInvoice.getId(), updatedHarvestInvoice.getId());

                final Invoice updatedInvoice = updatedHarvestInvoice.getInvoice();

                // 1 * 120 + 1 * 120 = 240
                final BigDecimal expectedAmount = new BigDecimal("240.00");

                assertPaymentTerms(updatedInvoice, today, DELIVERED, expectedAmount, null);
            });
        });
    }

    @Test
    public void testCallingAgainEndMooseHunting_whenInvoiceWasAlreadyPaidBeforeCancellingEndOfMooseHunting() {
        final LocalDate originalInvoiceDate = today().minusDays(1);

        withHarvestPermitFixture(f -> {
            final PermitHarvestInvoice harvestInvoice =
                    model().newPermitHarvestInvoice(f.speciesAmount, originalInvoiceDate);

            final Invoice invoice = harvestInvoice.getInvoice();
            invoice.setPaid(today());

            final BigDecimal paidAmount = new BigDecimal("120.00");
            invoice.setAmount(paidAmount);

            helper.createHarvestsForHuntingGroup(f.group, model().newPerson(), HasHarvestCountsForPermit.of(
                    1, 1, 0, 0,
                    0, 0));

            persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);

            runInTransaction(() -> {
                final PermitHarvestInvoice updatedHarvestInvoice = getHarvestInvoiceAndAssertOnlyOneExists();
                assertEquals(harvestInvoice.getId(), updatedHarvestInvoice.getId());

                final Invoice updatedInvoice = updatedHarvestInvoice.getInvoice();

                // 1 * 120 + 1 * 120 = 240
                final BigDecimal expectedCorrectedAmount = new BigDecimal("240.00");

                assertPaymentTerms(updatedInvoice, originalInvoiceDate, PAID, paidAmount, expectedCorrectedAmount);
            });
        });
    }

    @Test
    public void testCallingAgainEndMooseHunting_whenElectronicInvoicingWasDisabledBeforeCancellingEndOfMooseHunting() {
        final LocalDate originalInvoiceDate = today().minusDays(1);

        withHarvestPermitFixture(f -> {
            final PermitHarvestInvoice harvestInvoice =
                    model().newPermitHarvestInvoice(f.speciesAmount, originalInvoiceDate);

            final Invoice invoice = harvestInvoice.getInvoice();
            invoice.disableElectronicInvoicing();
            invoice.setState(InvoiceState.REMINDER);

            final BigDecimal originalAmount = new BigDecimal("120.00");
            invoice.setAmount(originalAmount);

            helper.createHarvestsForHuntingGroup(f.group, model().newPerson(), HasHarvestCountsForPermit.of(
                    1, 1, 0, 0,
                    0, 0));

            persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);

            runInTransaction(() -> {
                final PermitHarvestInvoice updatedHarvestInvoice = getHarvestInvoiceAndAssertOnlyOneExists();
                assertEquals(harvestInvoice.getId(), updatedHarvestInvoice.getId());

                final Invoice updatedInvoice = updatedHarvestInvoice.getInvoice();

                // 1 * 120 + 1 * 120 = 240
                final BigDecimal expectedCorrectedAmount = new BigDecimal("240.00");

                assertPaymentTerms(
                        updatedInvoice, originalInvoiceDate, REMINDER, originalAmount, expectedCorrectedAmount);
            });
        });
    }

    // This test models the case where end of mooselike hunting has been cancelled twice.
    // On the third time of ending mooselike hunting harvest amount is reverted back to
    // original setting. It needs to be asserted that corrected amount is nulled.
    @Test
    public void testCallingAgainEndMooseHunting_correctedAmountShouldBeNulledIfItMatchesWithOriginalAmount() {
        final LocalDate originalInvoiceDate = today().minusDays(1);

        withHarvestPermitFixture(f -> {
            final PermitHarvestInvoice harvestInvoice =
                    model().newPermitHarvestInvoice(f.speciesAmount, originalInvoiceDate);

            final Invoice invoice = harvestInvoice.getInvoice();

            // Original payment amount is based on one adult moose.
            final BigDecimal paidAmount = new BigDecimal("120.00");
            invoice.setAmount(paidAmount);
            invoice.setPaid(today());

            // On the second time of ending hunting, harvest count was increased by one adult and
            // hence invoice was also updated with a corrected amount.
            invoice.setCorrectedAmount(new BigDecimal("240.00"));

            // On the third time of ending hunting, harvest count is reverted back to original.
            helper.createHarvestsForHuntingGroup(f.group, model().newPerson(), HasHarvestCountsForPermit.of(
                    1, 0, 0, 0,
                    0, 0));

            persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);

            runInTransaction(() -> {
                final PermitHarvestInvoice updatedHarvestInvoice = getHarvestInvoiceAndAssertOnlyOneExists();
                assertEquals(harvestInvoice.getId(), updatedHarvestInvoice.getId());

                final Invoice updatedInvoice = updatedHarvestInvoice.getInvoice();
                assertPaymentTerms(updatedInvoice, originalInvoiceDate, PAID, paidAmount, null);
            });
        });
    }

    // NOT HAPPY cases

    @Test
    public void testNotAllPartnersFinishedHunting() {
        withHarvestPermitFixture(f -> {
            try {
                persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, false);
                fail("Expected " + AllPartnersMustFinnishHuntingException.class.getSimpleName());

            } catch (final AllPartnersMustFinnishHuntingException e) {
                assertMooseHuntingEndStatus(f.speciesAmount.getId(), false);
                assertHarvestInvoiceNotCreated();
            }
        });
    }

    @Test
    public void testAmendmentPermitsDoNotMatchHarvest() {
        withHarvestPermitFixture(f -> {

            final HarvestPermit amendmentPermit = model().newHarvestPermit(f.permit);
            model().newHarvestPermitSpeciesAmount(amendmentPermit, f.mooseSpecies, 1f);

            helper.createHarvestsForHuntingGroup(f.group, model().newPerson(), HasHarvestCountsForPermit.of(
                    6, 5, 4, 3,
                    2, 1));

            try {
                persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);
                fail("Expected " + AmendmentPermitDoesNotMatchHarvestCountException.class.getSimpleName());

            } catch (final AmendmentPermitDoesNotMatchHarvestCountException e) {
                assertMooseHuntingEndStatus(f.speciesAmount.getId(), false);
                assertHarvestInvoiceNotCreated();
            }
        });
    }

    @Test
    public void testPermitContactPersonDoesNotHaveAddress() {
        withHarvestPermitFixture(f -> {

            final Person permitContactPerson = f.permit.getOriginalContactPerson();
            permitContactPerson.setMrAddress(null);
            permitContactPerson.setOtherAddress(null);

            try {
                persistAndCallEndMooselikeHunting(f.speciesAmount, f.club, true);
                fail("Expected " + IllegalStateException.class.getSimpleName());

            } catch (final IllegalStateException e) {
                assertMooseHuntingEndStatus(f.speciesAmount.getId(), false);
                assertHarvestInvoiceNotCreated();
            }
        });
    }

    @Test
    public void testNonMooselikePermit() {
        withRhy(rhy -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit();
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club = model().newHuntingClub(rhy);
            permit.getPermitPartners().add(club);

            try {
                persistAndCallEndMooselikeHunting(speciesAmount, club, true);
                fail("Expected " + IllegalStateException.class.getSimpleName());

            } catch (final IllegalStateException e) {
                assertMooseHuntingEndStatus(speciesAmount.getId(), false);
                assertHarvestInvoiceNotCreated();
            }
        });
    }

    private void createMooseHuntingSummary(final HarvestPermit permit, final HuntingClub club, final boolean finished) {
        persistInNewTransaction();
        model().newMooseHuntingSummary(permit, club, finished);
    }

    private void persistAndCallEndMooselikeHunting(final HarvestPermitSpeciesAmount speciesAmount,
                                                   final HuntingClub huntingPermitPartner,
                                                   final boolean partnerFinishedHunting) {

        // Assert test invariant.
        speciesAmount.assertMooselikeHuntingNotFinished();

        createMooseHuntingSummary(speciesAmount.getHarvestPermit(), huntingPermitPartner, partnerFinishedHunting);

        onSavedAndAuthenticated(createUser(speciesAmount.getHarvestPermit().getOriginalContactPerson()), () -> {
            try {
                endOfMooselikePermitHuntingFeature.endMooselikeHunting(
                        speciesAmount.getHarvestPermit().getId(),
                        speciesAmount.getGameSpecies().getOfficialCode());
            } catch (final IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
    }

    private PermitHarvestInvoice getHarvestInvoiceAndAssertOnlyOneExists() {
        final List<PermitHarvestInvoice> harvestInvoices = permitHarvestInvoiceRepository.findAll();
        assertEquals(1, harvestInvoices.size());
        final PermitHarvestInvoice harvestInvoice = harvestInvoices.get(0);
        assertTrue(harvestInvoice.getSpeciesAmount().isMooselikeHuntingFinished());
        return harvestInvoice;
    }

    private void assertMooseHuntingEndStatus(final long speciesAmountId, final boolean expectFinished) {
        assertEquals(expectFinished, speciesAmountRepository.findOne(speciesAmountId).isMooselikeHuntingFinished());
    }

    private void assertHarvestInvoiceNotCreated() {
        assertEquals(0, permitHarvestInvoiceRepository.count());
    }

    private static void assertPaymentTerms(final Invoice invoice,
                                           final LocalDate expectedInvoiceDate,
                                           final InvoiceState expectedState,
                                           final BigDecimal expectedAmount,
                                           final BigDecimal expectedCorrectedAmount) {

        assertEquals(expectedInvoiceDate, invoice.getInvoiceDate());
        assertEquals(expectedState, invoice.getState());
        assertEquals(expectedAmount, invoice.getAmount());
        assertEquals(expectedCorrectedAmount, invoice.getCorrectedAmount());
    }
}
