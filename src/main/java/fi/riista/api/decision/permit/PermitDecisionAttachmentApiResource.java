package fi.riista.api.decision.permit;

import fi.riista.feature.harvestpermit.report.paper.PermitHarvestReportFeature;
import fi.riista.feature.harvestpermit.report.paper.PermitHarvestReportPdf;
import fi.riista.feature.permit.decision.action.PermitDecisionActionAttachmentDTO;
import fi.riista.feature.permit.decision.action.PermitDecisionActionAttachmentFeature;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentDTO;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentFeature;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentUploadDTO;
import fi.riista.util.MediaTypeExtras;
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
@RequestMapping(value = "/api/v1/decision")
public class PermitDecisionAttachmentApiResource {

    @Resource
    private PermitDecisionAttachmentFeature permitDecisionAttachmentFeature;

    @Resource
    private PermitDecisionActionAttachmentFeature permitDecisionActionAttachmentFeature;

    @Resource
    private PermitHarvestReportFeature permitHarvestReportFeature;

    // ATTACHMENTS

    @GetMapping(value = "/{decisionId:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PermitDecisionAttachmentDTO> listAttachments(final @PathVariable long decisionId) {
        return permitDecisionAttachmentFeature.listAttachments(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addAttachment(final @PathVariable long decisionId,
                              final @RequestParam("file") MultipartFile file,
                              final @RequestParam(value = "description", required = false) String description) {
        final PermitDecisionAttachmentUploadDTO dto = new PermitDecisionAttachmentUploadDTO();
        dto.setDecisionId(decisionId);
        dto.setFile(file);
        dto.setDescription(description);

        permitDecisionAttachmentFeature.addAttachment(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/additional-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addAdditionalAttachment(final @PathVariable long decisionId,
                                        final @RequestParam("file") MultipartFile file,
                                        final @RequestParam(value = "description", required = false) String description) {
        final PermitDecisionAttachmentUploadDTO dto = new PermitDecisionAttachmentUploadDTO();
        dto.setDecisionId(decisionId);
        dto.setFile(file);
        dto.setDescription(description);

        permitDecisionAttachmentFeature.addAdditionalAttachment(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{decisionId:\\d+}/attachment/{attachmentId:\\d+}")
    public void deleteAttachment(final @PathVariable long decisionId, final @PathVariable long attachmentId) {
        permitDecisionAttachmentFeature.deleteAttachment(decisionId, attachmentId);
    }

    @PostMapping(value = "/{decisionId:\\d+}/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getAttachment(final @PathVariable long decisionId,
                                                final @PathVariable long attachmentId) throws IOException {
        return permitDecisionAttachmentFeature.getAttachment(decisionId, attachmentId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/attachment-order")
    public void updateAttachmentOrder(final @PathVariable long decisionId,
                                      final @RequestBody @Valid List<Long> ordering) {
        permitDecisionAttachmentFeature.updateAttachmentOrder(decisionId, ordering);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/moose-attachment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addDefaultMooseAttachment(final @PathVariable long decisionId) throws IOException {
        permitDecisionAttachmentFeature.addDefaultMooseAttachment(decisionId);
    }

    // ACTION ATTACHMENT

    @GetMapping(value = "/{decisionId:\\d+}/action/{actionId:\\d+}/attachment", produces =
            MediaType.APPLICATION_JSON_VALUE)
    public List<PermitDecisionActionAttachmentDTO> listActionAttachments(final @PathVariable long decisionId,
                                                                         final @PathVariable long actionId) {
        return permitDecisionActionAttachmentFeature.listAttachments(actionId);
    }

    @PostMapping(value = "/{decisionId:\\d+}/action/{actionId:\\d+}/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getActionAttachment(final @PathVariable long decisionId,
                                                      final @PathVariable long attachmentId) throws IOException {
        return permitDecisionActionAttachmentFeature.getAttachment(attachmentId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/action/{actionId:\\d+}/attachment", consumes =
            MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addActionAttachment(final @PathVariable long decisionId,
                                    final @PathVariable long actionId,
                                    final @RequestParam("file") MultipartFile file) throws IOException {
        permitDecisionActionAttachmentFeature.addAttachment(actionId, file);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{decisionId:\\d+}/action/{actionId:\\d+}/attachment/{attachmentId:\\d+}")
    public void deleteActionAttachment(final @PathVariable long decisionId,
                                       final @PathVariable long attachmentId) {
        permitDecisionActionAttachmentFeature.deleteAttachment(attachmentId);
    }

    // HARVEST REPORT

    @GetMapping(value = "/{decisionId:\\d+}/permit-harvest-report", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> createBirdHarvestReportPdf(final @PathVariable long decisionId) throws IOException {
        final PermitHarvestReportPdf pdf = permitHarvestReportFeature.getPdf(decisionId);
        return pdf.asResponseEntity();
    }
}
