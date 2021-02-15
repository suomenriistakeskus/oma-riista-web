package fi.riista.feature.common.decision.nomination.attachment;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
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

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

public class NominationDecisionAttachmentFeatureTest extends EmbeddedDatabaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Resource
    private NominationDecisionAttachmentFeature feature;

    @Resource
    private NominationDecisionAttachmentRepository nominationDecisionAttachmentRepository;

    private NominationDecision decision;
    private NominationDecisionAttachment attachmentOnDecision;
    private NominationDecisionAttachment otherDecisionAttachment;
    private NominationDecision otherDecision;
    private SystemUser moderator;

    @Before
    public void setup() throws Exception {
        moderator = createNewModerator();
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Person coordinator1 = model().newPersonWithAddress();
        final DeliveryAddress deliveryAddress1 = DeliveryAddress.create(rhy1.getNameFinnish(), coordinator1.getAddress());
        decision = model().newNominationDecision(rhy1, METSASTYKSENVALVOJA, coordinator1, deliveryAddress1);
        decision.setHandler(moderator);

        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final Person coordinator2 = model().newPersonWithAddress();
        final DeliveryAddress deliveryAddress2 = DeliveryAddress.create(rhy2.getNameFinnish(), coordinator2.getAddress());
        otherDecision = model().newNominationDecision(rhy2, AMPUMAKOKEEN_VASTAANOTTAJA, coordinator2, deliveryAddress2);
        otherDecision.setHandler(moderator);

        attachmentOnDecision = model().newNominationDecisionAttachment(decision);
        attachmentOnDecision.setOrderingNumber(1);

        final File file = folder.newFile("temp.txt");
        final PersistentFileMetadata attachmentMetadata = attachmentOnDecision.getAttachmentMetadata();
        attachmentMetadata.setResourceUrl(file.toURI().toURL());
        attachmentMetadata.setContentType("text/plain");
        attachmentMetadata.setOriginalFilename(file.getName());

        otherDecisionAttachment = model().newNominationDecisionAttachment(otherDecision);

        persistInNewTransaction();
    }

    @Test
    public void testAuthorization() throws IOException {
        assertThrows(AccessDeniedException.class, ()->
                feature.getAttachment(decision.getId(), attachmentOnDecision.getId()));
    }


    @Test
    public void testDownloadingAttachment() throws IOException {
        authenticate(moderator);

        final ResponseEntity<byte[]> responseEntity = feature.getAttachment(decision.getId(),
                attachmentOnDecision.getId());

        assertThat(responseEntity.getStatusCodeValue(), equalTo(HttpServletResponse.SC_OK));
        assertThat(responseEntity.getHeaders().getContentLength(), equalTo(0L));
        assertThat(
                responseEntity.getHeaders().getFirst("Content-Disposition"),
                Matchers.containsString(attachmentOnDecision.getAttachmentMetadata().getOriginalFilename()));
    }

    @Test
    public void testDownloadingAttachment_invalidAttachmentId() throws IOException {
        authenticate(moderator);

        assertThrows(IllegalArgumentException.class, ()->
                feature.getAttachment(decision.getId(), otherDecisionAttachment.getId()));
    }

    @Test
    public void testAddAdditionalAttachment_otherModerator() {

        final MockMultipartFile multipartFile = new MockMultipartFile(
                "additional.txt", "additional.txt", "text/plain", new byte[0]);
        final NominationDecisionAttachmentUploadDTO uploadDTO =
                new NominationDecisionAttachmentUploadDTO(decision.getId(), multipartFile, "Additional attachment");

        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.addAdditionalAttachment(uploadDTO);
        });

        runInTransaction(() -> {
            final List<NominationDecisionAttachment> all = nominationDecisionAttachmentRepository.findAll();

            // Two from setup + new one
            assertThat(all, hasSize(3));
            all.stream()
                    .filter(attachment ->
                            !F.getUniqueIds(attachmentOnDecision, otherDecisionAttachment)
                                    .contains(attachment.getId()))
                    .forEach(attachment -> {
                        assertThat(attachment.getNominationDecision(), equalTo(decision));
                        assertThat(attachment.getDescription(), equalTo(uploadDTO.getDescription()));
                        assertThat(attachment.getAttachmentMetadata().getOriginalFilename(), equalTo(multipartFile.getName()));
                        assertThat(attachment.getOrderingNumber(), is(nullValue()));
                    });
        });
    }

    @Test
    public void testDeleteAttachment() {
        authenticate(moderator);

        feature.deleteAttachment(decision.getId(), attachmentOnDecision.getId());

        runInTransaction(() -> {
            final NominationDecisionAttachment attachment =
                    nominationDecisionAttachmentRepository.getOne(attachmentOnDecision.getId());
            assertThat(attachment.isDeleted(), is(true));
        });
    }

    @Test
    public void testDeleteAttachment_otherModerator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.deleteAttachment(decision.getId(), attachmentOnDecision.getId());
        });

        runInTransaction(() -> {
            final NominationDecisionAttachment attachment =
                    nominationDecisionAttachmentRepository.getOne(attachmentOnDecision.getId());

            assertThat(attachment.isDeleted(), is(true));
        });
    }

    @Test
    public void testDeleteAttachment_invalidAttachmentId() {
        authenticate(moderator);
        assertThrows(IllegalArgumentException.class, ()->
                feature.deleteAttachment(decision.getId(), otherDecisionAttachment.getId()));
    }

}
