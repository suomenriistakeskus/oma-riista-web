package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachment;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThrows;


public class NominationDecisionRevisionFeatureTest extends EmbeddedDatabaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Resource
    private NominationDecisionRevisionFeature feature;

    private NominationDecision decision;
    private NominationDecisionRevision revision;
    private NominationDecisionRevisionAttachment revisionAttachment;

    private NominationDecisionAttachment attachmentOnDecision;

    private NominationDecision otherDecision;
    private NominationDecisionAttachment otherDecisionAttachment;
    private NominationDecisionRevision otherDecisionRevision;
    private NominationDecisionRevisionAttachment otherDecisionRevisionAttachment;

    private SystemUser moderator;

    @Before
    public void setup() throws Exception {
        moderator = createNewModerator();
        final Person coordinator = model().newPersonWithAddress();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        model().newOccupation(rhy, coordinator, OccupationType.TOIMINNANOHJAAJA);
        final DeliveryAddress deliveryAddress = DeliveryAddress.create(rhy.getNameFinnish(), coordinator.getAddress());

        decision = model().newNominationDecision(rhy, OccupationType.METSASTYKSENVALVOJA, coordinator, deliveryAddress);
        decision.setHandler(moderator);

        attachmentOnDecision = model().newNominationDecisionAttachment(decision);
        attachmentOnDecision.setOrderingNumber(1);

        final File file = folder.newFile("temp.txt");
        final PersistentFileMetadata attachmentMetadata = attachmentOnDecision.getAttachmentMetadata();
        attachmentMetadata.setResourceUrl(file.toURI().toURL());
        attachmentMetadata.setContentType("text/plain");
        attachmentMetadata.setOriginalFilename(file.getName());

        revision = model().newNominationDecisionRevision(decision);
        revisionAttachment = model().newNominationDecisionRevisionAttachment(revision, attachmentOnDecision);
        model().newNominationDecisionReceiverForContactPerson(revision);

        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final Person coordinator2 = model().newPersonWithAddress();
        final DeliveryAddress deliveryAddress2 = DeliveryAddress.create(rhy2.getNameFinnish(), coordinator2.getAddress());
        otherDecision = model().newNominationDecision(
                rhy2,
                OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA,
                coordinator2,
                deliveryAddress2);
        otherDecision.setHandler(moderator);

        otherDecisionAttachment = model().newNominationDecisionAttachment(otherDecision);
        otherDecisionRevision = model().newNominationDecisionRevision(decision);
        otherDecisionRevisionAttachment = model().newNominationDecisionRevisionAttachment(revision,
                otherDecisionAttachment);
        model().newNominationDecisionReceiverForContactPerson(otherDecisionRevision);

        persistInNewTransaction();
    }

    @Test
    public void testAuthorization() {
        assertThrows(AccessDeniedException.class, () ->
                feature.getAttachment(decision.getId(), revisionAttachment.getId()));

    }


    @Test
    public void testDownloadingAttachment() throws IOException {
        authenticate(moderator);

        final ResponseEntity<byte[]> responseEntity = feature.getAttachment(decision.getId(),
                revisionAttachment.getId());

        assertThat(responseEntity.getStatusCodeValue(), equalTo(HttpServletResponse.SC_OK));
        assertThat(responseEntity.getHeaders().getContentLength(), equalTo(0L));
        assertThat(
                responseEntity.getHeaders().getFirst("Content-Disposition"),
                containsString(attachmentOnDecision.getAttachmentMetadata().getOriginalFilename()));

    }

    @Test
    public void testDownloadingAttachment_invalidAttachmentId() {
        authenticate(moderator);

        assertThrows(IllegalArgumentException.class, () ->
                feature.getAttachment(decision.getId(), otherDecisionRevisionAttachment.getId()));

    }
}
