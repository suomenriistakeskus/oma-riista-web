package fi.riista.api.decision.nomination;

import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionAttachmentDTO;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionAttachmentFeature;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachmentDTO;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachmentFeature;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachmentUploadDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/nominationdecision")
public class NominationDecisionAttachmentApiResource {

    @Resource
    private NominationDecisionAttachmentFeature nominationDecisionAttachmentFeature;

    @Resource
    private NominationDecisionActionAttachmentFeature nominationDecisionActionAttachmentFeature;

    // ATTACHMENTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{decisionId:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NominationDecisionAttachmentDTO> listAttachments(final @PathVariable long decisionId) {
        return nominationDecisionAttachmentFeature.listAttachments(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addAttachment(final @PathVariable long decisionId,
                              final @RequestParam("file") MultipartFile file,
                              final @RequestParam(value = "description", required = false) String description) {
        final NominationDecisionAttachmentUploadDTO dto =
                new NominationDecisionAttachmentUploadDTO(decisionId, file, description);

        nominationDecisionAttachmentFeature.addAttachment(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/additional-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addAdditionalAttachment(final @PathVariable long decisionId,
                                        final @RequestParam("file") MultipartFile file,
                                        final @RequestParam(value = "description", required = false) String description) {
        final NominationDecisionAttachmentUploadDTO dto =
                new NominationDecisionAttachmentUploadDTO(decisionId, file, description);

        nominationDecisionAttachmentFeature.addAdditionalAttachment(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{decisionId:\\d+}/attachment/{attachmentId:\\d+}")
    public void deleteAttachment(final @PathVariable long decisionId, final @PathVariable long attachmentId) {
        nominationDecisionAttachmentFeature.deleteAttachment(decisionId, attachmentId);
    }

    @PostMapping(value = "/{decisionId:\\d+}/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getAttachment(final @PathVariable long decisionId,
                                                final @PathVariable long attachmentId) throws IOException {
        return nominationDecisionAttachmentFeature.getAttachment(decisionId, attachmentId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/attachment-order")
    public void updateAttachmentOrder(final @PathVariable long decisionId,
                                      final @RequestBody @Valid List<Long> ordering) {
        nominationDecisionAttachmentFeature.updateAttachmentOrder(decisionId, ordering);
    }

    // ACTION ATTACHMENT

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{decisionId:\\d+}/action/{actionId:\\d+}/attachment", produces =
            MediaType.APPLICATION_JSON_VALUE)
    public List<NominationDecisionActionAttachmentDTO> listActionAttachments(final @PathVariable long decisionId,
                                                                             final @PathVariable long actionId) {
        return nominationDecisionActionAttachmentFeature.listAttachments(decisionId, actionId);
    }

    @PostMapping(value = "/{decisionId:\\d+}/action/{actionId:\\d+}/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getActionAttachment(final @PathVariable long decisionId,
                                                      final @PathVariable long attachmentId) throws IOException {
        return nominationDecisionActionAttachmentFeature.getAttachment(decisionId, attachmentId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/action/{actionId:\\d+}/attachment", consumes =
            MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addActionAttachment(final @PathVariable long decisionId,
                                    final @PathVariable long actionId,
                                    final @RequestParam("file") MultipartFile file) throws IOException {
        nominationDecisionActionAttachmentFeature.addAttachment(decisionId, actionId, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{decisionId:\\d+}/action/{actionId:\\d+}/attachment/{attachmentId:\\d+}")
    public void deleteActionAttachment(final @PathVariable long decisionId,
                                       final @PathVariable long attachmentId) {
        nominationDecisionActionAttachmentFeature.deleteAttachment(decisionId, attachmentId);
    }

}
