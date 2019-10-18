package fi.riista.feature.harvestpermit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.harvestpermit.endofhunting.EndOfHuntingReportExistsException;
import fi.riista.feature.harvestpermit.report.HarvestReportExistsException;
import fi.riista.feature.harvestpermit.report.HarvestReportNotSupportedException;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.BiConsumer;

import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.ACCEPTED;
import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.PROPOSED;
import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.REJECTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HarvestPermitAcceptHarvestFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitAcceptHarvestFeature harvestPermitAcceptHarvestFeature;

    @Resource
    private HarvestRepository harvestRepository;

    private void withActivePermit(final BiConsumer<Harvest, SystemUser> consumer) {
        withTestContext(false, consumer);
    }

    private void withEndOfHuntingReport(final BiConsumer<Harvest, SystemUser> consumer) {
        withTestContext(true, consumer);
    }

    private void withTestContext(final boolean endOfHuntingReportExists,
                                 final BiConsumer<Harvest, SystemUser> consumer) {
        final SystemUser user = createUserWithPerson();
        final HarvestPermit permit = model().newHarvestPermit(user.getPerson(), true);
        permit.setPermitTypeCode("201");
        permit.setPermitAreaSize(123);

        final Harvest harvest = model().newHarvest(user.getPerson());
        harvest.setHarvestPermit(permit);
        harvest.setRhy(permit.getRhy());

        if (endOfHuntingReportExists) {
            permit.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            permit.setHarvestReportAuthor(harvest.getAuthor());
            permit.setHarvestReportDate(DateUtil.now());
            permit.setHarvestReportModeratorOverride(false);
        }

        consumer.accept(harvest, user);
    }

    private void changeHarvestState(final Harvest harvest, final Harvest.StateAcceptedToHarvestPermit toState) {
        final HarvestPermitAcceptHarvestDTO dto = new HarvestPermitAcceptHarvestDTO();
        dto.setHarvestId(harvest.getId());
        dto.setHarvestRev(harvest.getConsistencyVersion());
        dto.setToState(toState);
        harvestPermitAcceptHarvestFeature.changeAcceptedToPermit(dto);
    }

    private void assertCorrectHarvestState(final Harvest.StateAcceptedToHarvestPermit expectedState) {
        runInTransaction(() -> {
            final List<Harvest> all = harvestRepository.findAll();
            assertEquals(1, all.size());

            final Harvest harvest = all.iterator().next();
            assertNotNull(harvest.getHarvestPermit());
            assertEquals(expectedState, harvest.getStateAcceptedToHarvestPermit());

            if (expectedState == ACCEPTED) {
                assertEquals(HarvestReportState.SENT_FOR_APPROVAL, harvest.getHarvestReportState());
                assertEquals(harvest.getAuthor(), harvest.getHarvestReportAuthor());
                assertNotNull(harvest.getHarvestReportDate());
            } else {
                assertNull(harvest.getHarvestReportState());
                assertNull(harvest.getHarvestReportAuthor());
                assertNull(harvest.getHarvestReportDate());
            }
        });
    }

    // WITHOUT END OF HUNTING REPORT FOR PERMIT

    @Test
    public void testProposedToAccepted() {
        withActivePermit((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, ACCEPTED));
            assertCorrectHarvestState(ACCEPTED);
        });
    }

    @Test
    public void testProposedToRejected() {
        withActivePermit((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, REJECTED));
            assertCorrectHarvestState(REJECTED);
        });
    }


    @Test
    public void testAcceptedToRejected() {
        withActivePermit((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportAuthor(harvest.getAuthor());

            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, REJECTED));
            assertCorrectHarvestState(REJECTED);
        });
    }

    @Test
    public void testRejectedToAccepted() {
        withActivePermit((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(REJECTED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, PROPOSED));
            assertCorrectHarvestState(PROPOSED);
        });
    }

    @Test
    public void testRejectedToProposed() {
        withActivePermit((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(REJECTED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, PROPOSED));
            assertCorrectHarvestState(PROPOSED);
        });
    }

    // WITH HARVEST REPORT

    @Test
    public void testAcceptedToProposed_harvestReportSentForApproval() {
        withActivePermit((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportAuthor(harvest.getAuthor());

            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, PROPOSED));
            assertCorrectHarvestState(PROPOSED);
        });
    }

    @Test(expected = HarvestReportExistsException.class)
    public void testAcceptedToProposed_harvestReportApproved() {
        withActivePermit((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setHarvestReportState(HarvestReportState.APPROVED);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportAuthor(harvest.getAuthor());

            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, PROPOSED));
        });
    }

    @Test(expected = HarvestReportExistsException.class)
    public void testAcceptedToProposed_harvestReportRejected() {
        withActivePermit((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            harvest.setHarvestReportState(HarvestReportState.REJECTED);
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportAuthor(harvest.getAuthor());

            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, PROPOSED));
        });
    }

    // WITH END OF HUNTING REPORT

    @Test(expected = EndOfHuntingReportExistsException.class)
    public void testProposedToRejected_endOfHuntingReport() {
        withEndOfHuntingReport((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, REJECTED));
        });
    }

    @Test(expected = EndOfHuntingReportExistsException.class)
    public void testProposedToAccepted_endOfHuntingReport() {
        withEndOfHuntingReport((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, ACCEPTED));
        });
    }

    @Test(expected = EndOfHuntingReportExistsException.class)
    public void testRejectedToAccepted_endOfHuntingReport() {
        withEndOfHuntingReport((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(REJECTED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, ACCEPTED));
        });
    }

    @Test(expected = EndOfHuntingReportExistsException.class)
    public void testRejectedToProposed_endOfHuntingReport() {
        withEndOfHuntingReport((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(REJECTED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, PROPOSED));
        });
    }

    @Test(expected = EndOfHuntingReportExistsException.class)
    public void testAcceptedToRejected_endOfHuntingReport() {
        withEndOfHuntingReport((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, REJECTED));
        });
    }

    @Test(expected = EndOfHuntingReportExistsException.class)
    public void testAcceptedToProposed_endOfHuntingReport() {
        withEndOfHuntingReport((harvest, user) -> {
            harvest.setStateAcceptedToHarvestPermit(ACCEPTED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, PROPOSED));
        });
    }

    // WITH MOOSE PERMIT

    @Test(expected = HarvestReportNotSupportedException.class)
    public void testAcceptHarvestCantBeCalledForMooselikePermit() {
        withActivePermit((harvest, user) -> {
            harvest.getHarvestPermit().setPermitTypeCode(PermitTypeCode.MOOSELIKE);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, ACCEPTED));
        });
    }

    @Test(expected = HarvestReportNotSupportedException.class)
    public void testAcceptHarvestCantBeCalledForAmendmentPermit() {
        withActivePermit((harvest, user) -> {
            harvest.getHarvestPermit().setPermitTypeCode(PermitTypeCode.MOOSELIKE_AMENDMENT);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            onSavedAndAuthenticated(user, () -> changeHarvestState(harvest, ACCEPTED));
        });
    }
}
