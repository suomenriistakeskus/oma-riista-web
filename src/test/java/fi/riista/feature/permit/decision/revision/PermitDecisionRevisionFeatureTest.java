package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocumentTransformer;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentRepository;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.hamcrest.Matchers;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class PermitDecisionRevisionFeatureTest extends EmbeddedDatabaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Resource
    private PermitDecisionRevisionFeature feature;

    @Resource
    private PermitDecisionRevisionAttachmentRepository revisionAttachmentRepository;

    private PermitDecision decision;
    private PermitDecisionRevision revision;
    private PermitDecisionRevisionAttachment revisionAttachment;

    private PermitDecisionAttachment attachmentOnDecision;

    private PermitDecision otherDecision;
    private PermitDecisionAttachment otherDecisionAttachment;
    private PermitDecisionRevision otherDecisionRevision;
    private PermitDecisionRevisionAttachment otherDecisionRevisionAttachment;

    private SystemUser moderator;

    @Before
    public void setup() throws Exception {
        moderator = createNewModerator();
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        decision.setHandler(moderator);

        attachmentOnDecision = model().newPermitDecisionAttachment(decision);
        attachmentOnDecision.setOrderingNumber(1);

        final File file = folder.newFile("temp.txt");
        final PersistentFileMetadata attachmentMetadata = attachmentOnDecision.getAttachmentMetadata();
        attachmentMetadata.setResourceUrl(file.toURI().toURL());
        attachmentMetadata.setContentType("text/plain");
        attachmentMetadata.setOriginalFilename(file.getName());

        revision = model().newPermitDecisionRevision(decision);
        revisionAttachment = model().newPermitDecisionRevisionAttachment(revision, attachmentOnDecision);
        model().newPermitDecisionReceiverForContactPerson(revision);

        otherDecision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        otherDecision.setHandler(moderator);

        otherDecisionAttachment = model().newPermitDecisionAttachment(otherDecision);
        otherDecisionRevision = model().newPermitDecisionRevision(decision);
        otherDecisionRevisionAttachment = model().newPermitDecisionRevisionAttachment(revision,
                otherDecisionAttachment);
        model().newPermitDecisionReceiverForContactPerson(otherDecisionRevision);

        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization() throws IOException {
        feature.getAttachment(decision.getId(), revisionAttachment.getId());

        fail("Should throw an exception");
    }


    @Test
    public void testDownloadingAttachment() throws IOException {
        authenticate(moderator);

        final ResponseEntity<byte[]> responseEntity = feature.getAttachment(decision.getId(),
                revisionAttachment.getId());

        assertEquals(HttpServletResponse.SC_OK, responseEntity.getStatusCodeValue());
        assertEquals(0, responseEntity.getHeaders().getContentLength());
        assertThat(
                responseEntity.getHeaders().getFirst("Content-Disposition"),
                Matchers.containsString(attachmentOnDecision.getAttachmentMetadata().getOriginalFilename()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testDownloadingAttachment_invalidAttachmentId() throws IOException {
        authenticate(moderator);

        feature.getAttachment(decision.getId(), otherDecisionRevisionAttachment.getId());

        fail("Should throw an exception");
    }

    @Test
    public void testUnorderedAttachmentNotCopiedIntoRevision() {
        withRhy(rhy -> {

            final PermitDecision permitDecision = model().newPermitDecision(rhy);
            final PermitDecisionAttachment orderedAttachment = model().newPermitDecisionAttachment(permitDecision);
            orderedAttachment.setOrderingNumber(1);
            final PermitDecisionAttachment unorderedAttachment = model().newPermitDecisionAttachment(permitDecision);
            unorderedAttachment.setOrderingNumber(null);

            final PermitDecisionRevision revision = model().newPermitDecisionRevision(decision);
            persistInNewTransaction();

            authenticate(moderator);

            runInTransaction(() ->
                    feature.createAttachments(revision, Arrays.asList(orderedAttachment, unorderedAttachment)));

            runInTransaction(() -> {
                final List<PermitDecisionRevisionAttachment> list =
                        revisionAttachmentRepository.findAllByDecisionRevision(revision);
                assertThat(list, hasSize(1));

                final PermitDecisionRevisionAttachment attachment = list.get(0);
                assertThat(attachment.getDecisionAttachment().getId(), equalTo(orderedAttachment.getId()));
            });
        });
    }

}
