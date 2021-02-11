package fi.riista.feature.permit.decision.attachment;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PermitDecisionAttachmentFeatureTest extends EmbeddedDatabaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Resource
    private PermitDecisionAttachmentFeature feature;

    @Resource
    private PermitDecisionAttachmentRepository permitDecisionAttachmentRepository;

    private PermitDecision decision;
    private PermitDecisionAttachment attachmentOnDecision;
    private PermitDecisionAttachment otherDecisionAttachment;
    private PermitDecision otherDecision;
    private SystemUser moderator;

    @Before
    public void setup() throws Exception {
        moderator = createNewModerator();
        decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        decision.setHandler(moderator);

        otherDecision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        otherDecision.setHandler(moderator);

        attachmentOnDecision = model().newPermitDecisionAttachment(decision);
        attachmentOnDecision.setOrderingNumber(1);

        final File file = folder.newFile("temp.txt");
        final PersistentFileMetadata attachmentMetadata = attachmentOnDecision.getAttachmentMetadata();
        attachmentMetadata.setResourceUrl(file.toURI().toURL());
        attachmentMetadata.setContentType("text/plain");
        attachmentMetadata.setOriginalFilename(file.getName());

        otherDecisionAttachment = model().newPermitDecisionAttachment(otherDecision);

        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization() throws IOException {
        feature.getAttachment(decision.getId(), attachmentOnDecision.getId());

        fail("Should throw an exception");
    }


    @Test
    public void testDownloadingAttachment() throws IOException {
        authenticate(moderator);

        final ResponseEntity<byte[]> responseEntity = feature.getAttachment(decision.getId(),
                attachmentOnDecision.getId());

        assertEquals(HttpServletResponse.SC_OK, responseEntity.getStatusCodeValue());
        assertEquals(0, responseEntity.getHeaders().getContentLength());
        assertThat(
                responseEntity.getHeaders().getFirst("Content-Disposition"),
                Matchers.containsString(attachmentOnDecision.getAttachmentMetadata().getOriginalFilename()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDownloadingAttachment_invalidAttachmentId() throws IOException {
        authenticate(moderator);

        feature.getAttachment(decision.getId(), otherDecisionAttachment.getId());

        fail("Should throw IllegalArgumentException");
    }

    @Test
    public void testAddAdditionalAttachment_otherModerator() {

        final MockMultipartFile multipartFile = new MockMultipartFile(
                "additional.txt", "additional.txt", "text/plain", new byte[0]);
        final PermitDecisionAttachmentUploadDTO uploadDTO =
                new PermitDecisionAttachmentUploadDTO();
        uploadDTO.setDecisionId(decision.getId());
        uploadDTO.setDescription("Additional attachment");
        uploadDTO.setFile(multipartFile);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.addAdditionalAttachment(uploadDTO);
        });

        runInTransaction(() -> {
            final List<PermitDecisionAttachment> all = permitDecisionAttachmentRepository.findAll();

            // Two from setup + new one
            assertThat(all, hasSize(3));
            all.stream()
                    .filter(attachment ->
                            !F.getUniqueIds(attachmentOnDecision, otherDecisionAttachment)
                                    .contains(attachment.getId()))
                    .forEach(attachment -> {
                        assertEquals(decision, attachment.getPermitDecision());
                        assertEquals(uploadDTO.getDescription(), attachment.getDescription());
                        assertEquals(multipartFile.getName(), attachment.getAttachmentMetadata().getOriginalFilename());
                        assertNull(attachment.getOrderingNumber());
                    });
        });
    }

    @Test
    public void testDeleteAttachment() {
        authenticate(moderator);

        feature.deleteAttachment(decision.getId(), attachmentOnDecision.getId());

        runInTransaction(() -> {
            final PermitDecisionAttachment attachment =
                    permitDecisionAttachmentRepository.getOne(attachmentOnDecision.getId());
            assertTrue(attachment.isDeleted());
        });
    }

    @Test
    public void testDeleteAttachment_otherModerator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.deleteAttachment(decision.getId(), attachmentOnDecision.getId());
        });

        runInTransaction(() -> {
            final PermitDecisionAttachment attachment =
                    permitDecisionAttachmentRepository.getOne(attachmentOnDecision.getId());
            assertTrue(attachment.isDeleted());
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAttachment_invalidAttachmentId() {
        authenticate(moderator);

        feature.deleteAttachment(decision.getId(), otherDecisionAttachment.getId());
        fail("Should throw IllegalArgumentException");
    }

    @Test
    public void testDeleteAttachmentReordersOnlyOrderedAttachments() {
        final List<PermitDecisionAttachment> attachmentList =
                IntStream.rangeClosed(1, 5)
                        .mapToObj(notUsed -> createAdditionalAttachment(decision))
                        .collect(Collectors.toList());

        onSavedAndAuthenticated(createNewModerator(), () ->
                feature.deleteAttachment(decision.getId(), attachmentOnDecision.getId()));

        runInTransaction(() -> {
            final List<PermitDecisionAttachment> attachments =
                    permitDecisionAttachmentRepository.findAllByPermitDecision(decision);
            assertThat(attachments, hasSize(6));
            attachments
                    .stream()
                    .forEach(attachment -> assertNull(attachment.getOrderingNumber()));
        });
    }

    public PermitDecisionAttachment createAdditionalAttachment(final PermitDecision decision) {
        final PermitDecisionAttachment attachment = model().newPermitDecisionAttachment(decision);
        attachment.setOrderingNumber(null);
        return attachment;
    }
}
