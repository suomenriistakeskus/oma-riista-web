package fi.riista.feature.harvestpermit.attachment;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentDTO;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class HarvestPermitAttachmentFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitAttachmentFeature feature;

    @Test
    public void testUnorderedAttachmentsNotListedThroughPermit() {
        withRhy(rhy-> {
            final PermitDecision decision = model().newPermitDecision(rhy);

            final PermitDecisionRevision revision = model().newPermitDecisionRevision(decision);
            revision.setPublishDate(DateTime.now());

            final PermitDecisionAttachment orderedAttachment = model().newPermitDecisionAttachment(decision);
            orderedAttachment.setOrderingNumber(1);
            model().newPermitDecisionRevisionAttachment(revision, orderedAttachment);
            final PermitDecisionAttachment unorderedAttachment = model().newPermitDecisionAttachment(decision);
            model().newPermitDecisionRevisionAttachment(revision, unorderedAttachment);

            final HarvestPermit permit = model().newHarvestPermit();
            permit.setPermitDecision(decision);

            onSavedAndAuthenticated(createUser(permit.getOriginalContactPerson()), () ->{
                final List<PermitDecisionAttachmentDTO> permitAttachments = feature.listAttachments(permit.getId());
                assertThat(permitAttachments, hasSize(1));

                final PermitDecisionAttachmentDTO attachmentDTO = permitAttachments.get(0);
                // DTO's id is from PermitDecisionAttachment
                assertThat(attachmentDTO.getId(), equalTo(orderedAttachment.getId()));
            });
        });
    }
}
