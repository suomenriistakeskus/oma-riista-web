package fi.riista.feature.permit.decision.publish;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.mail.queue.MailMessage;
import fi.riista.feature.mail.queue.MailMessageRepository;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionReceiver;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

public class PermitDecisionNotificationServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitDecisionNotificationService service;

    @Resource
    private MailMessageRepository mailMessageRepository;

    private PermitDecision decision;
    private PermitDecisionRevision revision;
    private PermitDecisionRevisionReceiver contactPersonReceiver;
    private PermitDecisionRevisionReceiver rhyReceiver;


    @Test
    public void testCancellation() {
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        decision.setDecisionType(PermitDecision.DecisionType.CANCEL_APPLICATION);
        revision = model().newPermitDecisionRevision(decision);
        contactPersonReceiver = model().newPermitDecisionReceiverForContactPerson(revision);
        rhyReceiver = model().newPermitDecisionReceiverForInformedParty(revision, "rhy@invalid", "rhy");
        persistInNewTransaction();

        runInTransaction(() -> service.emailRevisionReceivers(decision, revision.getId()));

        runInTransaction(() -> {
            final List<MailMessage> all = mailMessageRepository.findAll();
            assertThat(all, hasSize(2));

            final MailMessage contactPersonMail = getContactPersonMail(all);
            assertMailBodyForAnonymousDownloading(contactPersonMail);

            final MailMessage informedPartyMail = getInformedPartyMail(all);
            assertMailBodyForAnonymousDownloading(informedPartyMail);
        });
    }

    @Test
    public void testIgnore() {
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        decision.setDecisionType(PermitDecision.DecisionType.IGNORE_APPLICATION);
        revision = model().newPermitDecisionRevision(decision);
        contactPersonReceiver = model().newPermitDecisionReceiverForContactPerson(revision);
        rhyReceiver = model().newPermitDecisionReceiverForInformedParty(revision, "rhy@invalid", "rhy");
        persistInNewTransaction();

        runInTransaction(() -> service.emailRevisionReceivers(decision, revision.getId()));

        runInTransaction(() -> {
            final List<MailMessage> all = mailMessageRepository.findAll();
            assertThat(all, hasSize(2));

            final MailMessage contactPersonMail = getContactPersonMail(all);
            assertMailBodyForAnonymousDownloading(contactPersonMail);

            final MailMessage informedPartyMail = getInformedPartyMail(all);
            assertMailBodyForAnonymousDownloading(informedPartyMail);
        });
    }

    @Test
    public void testHarvestPermitApproval_unchanged() {
        final GameSpecies gameSpecies = model().newGameSpecies();
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys(), gameSpecies);
        model().newPermitDecisionSpeciesAmount(decision, gameSpecies, 5f);
        decision.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
        decision.setGrantStatus(GrantStatus.UNCHANGED);
        revision = model().newPermitDecisionRevision(decision);

        final HarvestPermit harvestPermit = model().newHarvestPermit();
        harvestPermit.setPermitDecision(decision);
        model().newHarvestPermitSpeciesAmount(harvestPermit, gameSpecies, 5f);
        contactPersonReceiver = model().newPermitDecisionReceiverForContactPerson(revision);
        rhyReceiver = model().newPermitDecisionReceiverForInformedParty(revision, "rhy@invalid", "rhy");
        persistInNewTransaction();

        runInTransaction(() -> service.emailRevisionReceivers(decision, revision.getId()));

        runInTransaction(() -> {
            final List<MailMessage> all = mailMessageRepository.findAll();
            assertThat(all, hasSize(2));

            final MailMessage contactPersonMail = getContactPersonMail(all);
            assertMailBodyForPermitManagement(contactPersonMail);

            final MailMessage informedPartyMail = getInformedPartyMail(all);
            assertMailBodyForAnonymousDownloading(informedPartyMail);
        });
    }

    @Test
    public void testHarvestPermitApproval_restricted() {
        final GameSpecies gameSpecies = model().newGameSpecies();
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys(), gameSpecies);
        model().newPermitDecisionSpeciesAmount(decision, gameSpecies, 2f);
        decision.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
        decision.setGrantStatus(GrantStatus.RESTRICTED);
        revision = model().newPermitDecisionRevision(decision);

        final HarvestPermit harvestPermit = model().newHarvestPermit();
        harvestPermit.setPermitDecision(decision);
        model().newHarvestPermitSpeciesAmount(harvestPermit, gameSpecies, 2f);
        contactPersonReceiver = model().newPermitDecisionReceiverForContactPerson(revision);
        rhyReceiver = model().newPermitDecisionReceiverForInformedParty(revision, "rhy@invalid", "rhy");
        persistInNewTransaction();

        runInTransaction(() -> service.emailRevisionReceivers(decision, revision.getId()));

        runInTransaction(() -> {
            final List<MailMessage> all = mailMessageRepository.findAll();
            assertThat(all, hasSize(2));

            final MailMessage contactPersonMail = getContactPersonMail(all);
            assertMailBodyForPermitManagement(contactPersonMail);

            final MailMessage informedPartyMail = getInformedPartyMail(all);
            assertMailBodyForAnonymousDownloading(informedPartyMail);
        });
    }

    @Test
    public void testHarvestPermitApproval_rejected() {
        final GameSpecies gameSpecies = model().newGameSpecies();
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys(), gameSpecies);
        model().newPermitDecisionSpeciesAmount(decision, gameSpecies, 0f);
        decision.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
        decision.setGrantStatus(GrantStatus.REJECTED);
        revision = model().newPermitDecisionRevision(decision);

        final HarvestPermit harvestPermit = model().newHarvestPermit();
        harvestPermit.setPermitDecision(decision);
        model().newHarvestPermitSpeciesAmount(harvestPermit, gameSpecies, 0f);
        contactPersonReceiver = model().newPermitDecisionReceiverForContactPerson(revision);
        rhyReceiver = model().newPermitDecisionReceiverForInformedParty(revision, "rhy@invalid", "rhy");
        persistInNewTransaction();

        runInTransaction(() -> service.emailRevisionReceivers(decision, revision.getId()));

        runInTransaction(() -> {
            final List<MailMessage> all = mailMessageRepository.findAll();
            assertThat(all, hasSize(2));

            final MailMessage contactPersonMail = getContactPersonMail(all);
            assertMailBodyForPermitManagement(contactPersonMail);

            final MailMessage informedPartyMail = getInformedPartyMail(all);
            assertMailBodyForAnonymousDownloading(informedPartyMail);
        });
    }

    @Test
    public void testHarvestPermitRenewal() {
        final GameSpecies gameSpecies = model().newGameSpecies();
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys(), gameSpecies);
        model().newPermitDecisionSpeciesAmount(decision, gameSpecies, 5f);
        decision.setDecisionType(PermitDecision.DecisionType.HARVEST_PERMIT);
        decision.setPermitTypeCode(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);
        revision = model().newPermitDecisionRevision(decision);
        contactPersonReceiver = model().newPermitDecisionReceiverForContactPerson(revision);
        rhyReceiver = model().newPermitDecisionReceiverForInformedParty(revision, "rhy@invalid", "rhy");

        final HarvestPermit harvestPermit2019 = model().newHarvestPermit(permitNumber(2019, 1));
        harvestPermit2019.setPermitDecision(decision);
        model().newHarvestPermitSpeciesAmount(harvestPermit2019, gameSpecies, 5f);

        final HarvestPermit harvestPermit2020 = model().newHarvestPermit(permitNumber(2020, 1));
        harvestPermit2020.setPermitDecision(decision);
        model().newHarvestPermitSpeciesAmount(harvestPermit2020, gameSpecies, 5f);

        persistInNewTransaction();

        runInTransaction(() -> service.emailRevisionReceivers(decision, revision.getId()));

        runInTransaction(() -> {
            final List<MailMessage> all = mailMessageRepository.findAll();
            assertThat(all, hasSize(1));

            final MailMessage contactPersonMail = getContactPersonMail(all);
            assertMailBodyForPermitManagementForRenewal(contactPersonMail);

        });
    }

    private static MailMessage getContactPersonMail(final List<MailMessage> all) {
        return all.stream().filter(m -> m.getSubject().equals(PermitDecisionNotificationService.SUBJECT_CONTACT_PERSON)).findFirst().get();
    }

    private static MailMessage getInformedPartyMail(final List<MailMessage> all) {
        return all.stream().filter(m -> m.getSubject().equals(PermitDecisionNotificationService.SUBJECT_OTHERS)).findFirst().get();
    }

    private static void assertMailBodyForPermitManagement(final MailMessage mailMessage) {
        assertTrue(mailMessage.getBody().contains("Päätös liitteineen on maksettavissa ja noudettavissa oheisesta " +
                "linkistä"));
        assertTrue(mailMessage.getBody().contains("Beslut med tillhörande bilagor kan betalas och hämtas via " +
                "webb-länken nedan"));
    }

    private static void assertMailBodyForPermitManagementForRenewal(final MailMessage mailMessage) {
        assertTrue(mailMessage.getBody().contains(
                "Suomen riistakeskus on arvioinut ja tehnyt päätöksen ilmoitusmenettelyänne koskien."));
        assertTrue(mailMessage.getBody().contains(
                "Finlands viltcentral har utvärderat och gjort beslut beträffande ert anmälningsförfarande."));
    }

    private static void assertMailBodyForAnonymousDownloading(final MailMessage mailMessage) {
        assertTrue(mailMessage.getBody().contains("Päätös on noudettavissa oheisesta linkistä"));
        assertTrue(mailMessage.getBody().contains("Beslutet kan hämtas via Webb-länken nedan"));
    }
}
