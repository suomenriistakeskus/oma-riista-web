package fi.riista.api;

import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.permit.decision.CreatePermitDecisionDTO;
import fi.riista.feature.permit.decision.PermitDecisionCompleteStatus;
import fi.riista.feature.permit.decision.PermitDecisionDTO;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.PermitDecisionFeature;
import fi.riista.feature.permit.decision.PermitDecisionPublishSettingsDTO;
import fi.riista.feature.permit.decision.PermitDecisionUnlockDTO;
import fi.riista.feature.permit.decision.action.PermitDecisionActionDTO;
import fi.riista.feature.permit.decision.action.PermitDecisionActionFeature;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentDTO;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentFeature;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentUploadDTO;
import fi.riista.feature.permit.decision.authority.PermitDecisionAuthoritiesDTO;
import fi.riista.feature.permit.decision.authority.PermitDecisionAuthorityFeature;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryDTO;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryFeature;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryUpdateDTO;
import fi.riista.feature.permit.decision.document.PermitDecisionDocumentFeature;
import fi.riista.feature.permit.decision.document.PermitDecisionDocumentSectionDTO;
import fi.riista.feature.permit.decision.document.PermitDecisionSectionIdentifier;
import fi.riista.feature.permit.decision.document.UpdateDecisionPaymentDTO;
import fi.riista.feature.permit.decision.reference.PermitDecisionReferenceDTO;
import fi.riista.feature.permit.decision.reference.PermitDecisionReferenceFeature;
import fi.riista.feature.permit.decision.reference.UpdatePermitDecisionReferenceDTO;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionDTO;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionDownloadFeature;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionFeature;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountDTO;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountFeature;
import fi.riista.feature.permit.invoice.pdf.PermitDecisionInvoicePdfFeature;
import fi.riista.feature.permit.invoice.pdf.PermitHarvestInvoicePdfFeature;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/decision")
public class PermitDecisionApiResource {

    @Resource
    private PermitDecisionFeature permitDecisionFeature;

    @Resource
    private PermitDecisionSpeciesAmountFeature permitDecisionSpeciesAmountFeature;

    @Resource
    private PermitDecisionDocumentFeature permitDecisionDocumentFeature;

    @Resource
    private PermitDecisionRevisionFeature permitDecisionRevisionFeature;

    @Resource
    private PermitDecisionRevisionDownloadFeature permitDecisionRevisionDownloadFeature;

    @Resource
    private PermitDecisionAttachmentFeature permitDecisionAttachmentFeature;

    @Resource
    private PermitDecisionReferenceFeature permitDecisionReferenceFeature;

    @Resource
    private PermitDecisionActionFeature permitDecisionActionFeature;

    @Resource
    private PermitDecisionDeliveryFeature permitDecisionDeliveryFeature;

    @Resource
    private PermitDecisionAuthorityFeature permitDecisionAuthorityFeature;

    @Resource
    private PermitDecisionInvoicePdfFeature permitDecisionInvoicePdfFeature;

    @Resource
    private PermitHarvestInvoicePdfFeature permitHarvestInvoiceFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitDecisionDTO getDecision(final @PathVariable long decisionId) {
        return permitDecisionFeature.getDecision(decisionId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> getOrCreateDecision(final @RequestBody @Valid CreatePermitDecisionDTO dto) {
        final Long decisionId = permitDecisionFeature.getOrCreateDecisionForApplication(dto);
        return Collections.singletonMap("id", decisionId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/application", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Long> getApplication(final @PathVariable long decisionId) {
        return Collections.singletonMap("id", permitDecisionFeature.getDecisionApplicationId(decisionId));
    }

    // INVOICE

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/invoice/processing", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getProcessingInvoice(final @PathVariable long decisionId) throws IOException {
        return permitDecisionInvoicePdfFeature.getProcessingInvoicePdfFile(decisionId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/invoice/harvest/{gameSpeciesCode:\\d+}", produces = MediaTypeExtras.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getHarvestInvoice(final @PathVariable long decisionId,
                                                    final @PathVariable int gameSpeciesCode) throws IOException {
        return permitHarvestInvoiceFeature.getHarvestInvoicePdfFile(decisionId, gameSpeciesCode);
    }

    // SPECIES

    @GetMapping(value = "{decisionId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionSpeciesAmountDTO> listSpecies(final @PathVariable long decisionId) {
        return permitDecisionSpeciesAmountFeature.getSpeciesAmounts(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/species", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateSpecies(final @PathVariable long decisionId,
                              final @Valid @RequestBody PermitDecisionSpeciesAmountDTO dto) {
        permitDecisionSpeciesAmountFeature.saveSpeciesAmounts(decisionId, dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "{decisionId:\\d+}/species/{id:\\d+}")
    public void deleteSpecies(final @PathVariable long decisionId,
                              final @PathVariable long id) {
        permitDecisionSpeciesAmountFeature.deleteSpeciesAmounts(decisionId, id);
    }

    // DOCUMENT

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/document", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitDecisionDocument getDecisionDocument(final @PathVariable long decisionId) {
        return permitDecisionDocumentFeature.getDecisionDocument(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/document")
    public void updateDecisionDocument(final @PathVariable long decisionId,
                                       final @Validated(PermitDecisionDocumentSectionDTO.ContentValidation.class)
                                       @RequestBody PermitDecisionDocumentSectionDTO dto) {
        permitDecisionDocumentFeature.updateDecisionDocument(decisionId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/generate/{sectionId:\\w+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, String> generateTextContent(final @PathVariable long decisionId,
                                                   final @PathVariable PermitDecisionSectionIdentifier sectionId) {
        return Collections.singletonMap("content", permitDecisionDocumentFeature.generate(decisionId, sectionId));
    }

    // COMPLETE STATUS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/complete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitDecisionCompleteStatus getCompletetStatus(final @PathVariable long decisionId) {
        return permitDecisionDocumentFeature.getCompleteStatus(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/complete")
    public void updateCompleteStatus(final @PathVariable long decisionId,
                                     final @Validated(PermitDecisionDocumentSectionDTO.CompleteValidation.class)
                                     @RequestBody PermitDecisionDocumentSectionDTO dto) {
        permitDecisionDocumentFeature.setSectionCompletionStatus(decisionId, dto);
    }

    // PAYMENT

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/payment")
    public void updatePayment(final @Valid @RequestBody UpdateDecisionPaymentDTO dto) {
        permitDecisionDocumentFeature.updatePayment(dto);
    }

    // PUBLISH DATE

    @ResponseBody
    @GetMapping(value = "{decisionId:\\d+}/publish-settings")
    public PermitDecisionPublishSettingsDTO getPublishDate(final @PathVariable long decisionId) {
        return permitDecisionFeature.getPublishSettings(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/publish-settings")
    public void updatePublishDate(final @PathVariable long decisionId,
                                  final @Valid @RequestBody PermitDecisionPublishSettingsDTO dto) {
        dto.setDecisionId(decisionId);
        permitDecisionFeature.updatePublishSettings(dto);
    }

    // MODERATOR TOOLS

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/assign")
    public void assignDecision(final @PathVariable long decisionId) {
        permitDecisionFeature.assignApplication(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/lock")
    public void lockDecision(final @PathVariable long decisionId) {
        permitDecisionRevisionFeature.lockDecision(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/unlock")
    public void unlockDecision(final @PathVariable long decisionId,
                               final @RequestBody @Valid PermitDecisionUnlockDTO dto) {
        dto.setId(decisionId);
        permitDecisionRevisionFeature.unlockDecision(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/revisions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionRevisionDTO> getRevisions(final @PathVariable long decisionId) {
        return permitDecisionRevisionFeature.listRevisions(decisionId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{decisionId:\\d+}/revisions/{revisionId:\\d+}/pdf")
    public void getRevisionPdf(@PathVariable final long decisionId,
                               @PathVariable final long revisionId,
                               final HttpServletResponse response) throws IOException {
        permitDecisionRevisionDownloadFeature.downloadPdf(decisionId, revisionId, response);
    }

    @PostMapping(value = "/{decisionId:\\d+}/revisions/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getRevisionAttachment(@PathVariable final long attachmentId) throws IOException {
        return permitDecisionRevisionFeature.getAttachment(attachmentId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{decisionId:\\d+}/revisions/{revisionId:\\d+}/posted")
    public PermitDecisionRevisionDTO updatePosted(@PathVariable final long decisionId,
                                                  @PathVariable final long revisionId) {
        return permitDecisionRevisionFeature.updatePosted(decisionId, revisionId, true);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{decisionId:\\d+}/revisions/{revisionId:\\d+}/notposted")
    public PermitDecisionRevisionDTO updateNotPosted(@PathVariable final long decisionId,
                                                     @PathVariable final long revisionId) {
        return permitDecisionRevisionFeature.updatePosted(decisionId, revisionId, false);
    }

    // REFERENCE DECISION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/reference")
    public PermitDecisionReferenceDTO getReference(final @PathVariable long decisionId) {
        return permitDecisionReferenceFeature.getReference(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/reference")
    public void updateReference(final @RequestBody @Valid UpdatePermitDecisionReferenceDTO dto) {
        permitDecisionReferenceFeature.updateReference(dto);
    }

    @PostMapping(value = "/search/references", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionReferenceDTO> searchReferences(
            final @RequestBody @Valid HarvestPermitApplicationSearchDTO dto) {
        return permitDecisionReferenceFeature.searchReferences(dto);
    }

    // DECISION ACTIONS

    @GetMapping(value = "{decisionId:\\d+}/actions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionActionDTO> listActions(final @PathVariable long decisionId) {
        return permitDecisionActionFeature.listActions(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/action", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createAction(final @PathVariable long decisionId,
                             final @Valid @RequestBody PermitDecisionActionDTO dto) {
        permitDecisionActionFeature.create(decisionId, dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/action/{actionId:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateAction(final @PathVariable long decisionId,
                             final @PathVariable long actionId,
                             final @Valid @RequestBody PermitDecisionActionDTO dto) {
        dto.setId(actionId);
        permitDecisionActionFeature.update(decisionId, dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "{decisionId:\\d+}/action/{actionId:\\d+}")
    public void deleteAction(final @PathVariable long decisionId,
                             final @PathVariable long actionId) {
        permitDecisionActionFeature.delete(decisionId, actionId);
    }

    // ATTACHMENTS

    @GetMapping(value = "/{decisionId:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @DeleteMapping(value = "/{decisionId:\\d+}/attachment/{attachmentId:\\d+}")
    public void deleteAttachment(@PathVariable final long attachmentId) {
        permitDecisionAttachmentFeature.deleteAttachment(attachmentId);
    }

    @PostMapping(value = "/{decisionId:\\d+}/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable final long attachmentId) throws IOException {
        return permitDecisionAttachmentFeature.getAttachment(attachmentId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/attachment-order")
    public void updateAttachmentOrder(final @PathVariable long decisionId,
                                      final @RequestBody @Valid List<Long> ordering) {
        permitDecisionAttachmentFeature.updateAttachmentOrder(decisionId, ordering);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/moose-attachment", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addDefaultMooseAttachment(final @PathVariable long decisionId) throws IOException {
        permitDecisionAttachmentFeature.addDefaultMooseAttachment(decisionId);
    }

    // DELIVERY
    @GetMapping(value = "{decisionId:\\d+}/delivery", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionDeliveryDTO> getDelivery(final @PathVariable long decisionId) {
        return permitDecisionDeliveryFeature.getDelivery(decisionId);
    }

    @PostMapping(value = "{decisionId:\\d+}/delivery", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateDelivery(final @PathVariable long decisionId,
                               final @Valid @RequestBody PermitDecisionDeliveryUpdateDTO deliveries) {
        permitDecisionDeliveryFeature.updateDelivery(decisionId, deliveries);
    }

    // AUTHORITIES
    @GetMapping(value = "{decisionId:\\d+}/authorities", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitDecisionAuthoritiesDTO getAuthorities(final @PathVariable long decisionId) {
        return permitDecisionAuthorityFeature.getAuthorities(decisionId);
    }

    @PostMapping(value = "{decisionId:\\d+}/authorities", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateAuthorities(final @PathVariable long decisionId,
                                  final @Valid @RequestBody PermitDecisionAuthoritiesDTO authorities) {
        permitDecisionAuthorityFeature.updateAuthorities(decisionId, authorities);
    }
}
