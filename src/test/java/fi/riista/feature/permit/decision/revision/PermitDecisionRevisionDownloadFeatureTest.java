package fi.riista.feature.permit.decision.revision;

import fi.riista.api.pub.PermitDecisionDownloadDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class PermitDecisionRevisionDownloadFeatureTest extends EmbeddedDatabaseTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Resource
    private PermitDecisionRevisionDownloadFeature feature;

    private PermitDecision decision;
    private PermitDecisionRevision revision;
    private PermitDecisionRevisionReceiver receiver;
    private PermitDecisionAttachment attachmentOnDecision;
    private PermitDecisionAttachment otherAttachment;

    @Before
    public void setup() throws Exception {
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        revision = model().newPermitDecisionRevision(decision);
        attachmentOnDecision = model().newPermitDecisionAttachment(decision);
        attachmentOnDecision.setOrderingNumber(1);
        final File file = folder.newFile("temp.txt");
        attachmentOnDecision.getAttachmentMetadata().setResourceUrl(file.toURI().toURL());
        otherAttachment = model().newPermitDecisionAttachment(decision);
        model().newPermitDecisionRevisionAttachment(revision, attachmentOnDecision);
        model().newPermitDecisionRevisionAttachment(revision, otherAttachment);
        receiver = model().newPermitDecisionReceiverForContactPerson(revision);
        persistInNewTransaction();
    }

    @Test
    public void testAnonymousListingOnlyIncludesListedAttachments() {

        final PermitDecisionDownloadDTO downloadLinks = feature.getDownloadLinks(receiver.getUuid(), revision.getId());

        assertThat(downloadLinks.getAttachmentLinks(), hasSize(1));
        final PermitDecisionDownloadDTO.PermitDecisionLinkDTO decisionLinkDTO =
                downloadLinks.getAttachmentLinks().get(0);
        assertEquals(attachmentOnDecision.getDescription(), decisionLinkDTO.getLinkName());
    }

    @Test
    public void testAnonymousDownloadOfListedAttachment() throws Exception {
        final MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        feature.downloadDecisionAttachmentNoAuthorization(revision.getId(), attachmentOnDecision.getId(),
                httpServletResponse);

        assertEquals(HttpServletResponse.SC_OK, httpServletResponse.getStatus());
    }

    @Test
    public void testAnonymousDownloadDoesNotAllowDownloadingOfUnlistedAttachment() throws Exception {
        final MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        feature.downloadDecisionAttachmentNoAuthorization(revision.getId(), otherAttachment.getId(),
                httpServletResponse);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, httpServletResponse.getStatus());
        assertEquals(0, httpServletResponse.getContentLength());
    }

}
