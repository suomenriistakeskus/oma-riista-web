package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.mail.queue.MailMessageRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class PermitDecisionAnnualRenewalFeatureTest extends EmbeddedDatabaseTest {

    private SystemUser moderator;
    private Riistanhoitoyhdistys rhy;
    private PermitDecision decision;
    private HarvestPermit permit;

    @Resource
    private PermitDecisionAnnualRenewalFeature feature;

    @Resource
    private MailMessageRepository mailMessageRepository;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        moderator = createNewModerator();
        decision = model().newPermitDecision(rhy);
        decision.setPermitTypeCode(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);
        decision.setHandler(moderator);
        model().newPermitDecisionSpeciesAmount(decision, model().newGameSpecies(), 10);

        final PermitDecisionRevision revision = model().newPermitDecisionRevision(decision);
        revision.setPublishDate(DateTime.now());
        permit = model().newHarvestPermit(
                rhy, decision.createPermitNumber(decision.getDecisionYear()),
                PermitTypeCode.ANNUAL_UNPROTECTED_BIRD, decision);
    }

    // IS RENEWABLE

    @Test
    public void testIsRenewable_latestPermitNotFinished() {
        onSavedAndAuthenticated(moderator, () -> {
            assertThat(feature.isRenewable(decision.getId()), is(false));
        });
    }

    @Test
    public void testIsRenewable_latestPermitFinished() {
        setPermitHuntingFinished();
        onSavedAndAuthenticated(moderator, () -> {
            assertThat(feature.isRenewable(decision.getId()), is(true));
        });
    }

    @Test
    public void testIsRenewable_otherModerator() {
        setPermitHuntingFinished();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            assertThat(feature.isRenewable(decision.getId()), is(false));
        });
    }

    @Test
    public void testIsRenewable_noHandler() {
        setPermitHuntingFinished();
        decision.setHandler(null);

        onSavedAndAuthenticated(moderator, () -> {
            assertThat(feature.isRenewable(decision.getId()), is(false));
        });
    }

    // CREATE NEXT PERMIT

    @Test
    public void testCreateNextPermit() {
        setPermitHuntingFinished();

        doTestCreateNextPermitWithEmailCount(1);
    }

    @Test
    public void testCreateNextPermit_contactPersonEmailIsNull() {
        setPermitHuntingFinished();
        decision.getContactPerson().setEmail(null);

        doTestCreateNextPermitWithEmailCount(0);
    }

    @Test
    public void testCreateNextPermit_contactPersonEmailIsEmpty() {
        setPermitHuntingFinished();
        decision.getContactPerson().setEmail("");

        doTestCreateNextPermitWithEmailCount(0);
    }

    private void doTestCreateNextPermitWithEmailCount(final int expectedCreatedEmailsCount) {
        onSavedAndAuthenticated(moderator, () -> {
            final long expectedEmailCountInTheEnd = mailMessageRepository.count() + expectedCreatedEmailsCount;
            final HarvestPermitDTO dto = feature.createNextAnnualPermit(decision.getId(), permit.getPermitYear() + 1);

            assertThat(dto.getPermitNumber(), equalTo(decision.createPermitNumber(permit.getPermitYear() + 1)));
            assertThat(mailMessageRepository.count(), equalTo(expectedEmailCountInTheEnd));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNextPermit_huntingNotFinished() {
        onSavedAndAuthenticated(moderator, () -> {
            feature.createNextAnnualPermit(decision.getId(), permit.getPermitYear() + 1);
            fail("Should have thrown an exception");
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNextPermit_otherHandler() {
        setPermitHuntingFinished();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.createNextAnnualPermit(decision.getId(), permit.getPermitYear() + 1);
            fail("Should have thrown an exception");
        });
    }

    private void setPermitHuntingFinished() {
        permit.setHarvestReportState(HarvestReportState.APPROVED);
        permit.setHarvestReportAuthor(permit.getOriginalContactPerson());
        permit.setHarvestReportDate(DateTime.now());
        permit.setHarvestReportModeratorOverride(true);
    }
}
