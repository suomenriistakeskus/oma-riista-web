package fi.riista.api.decision.permit;

import fi.riista.feature.common.decision.authority.DecisionAuthoritiesDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.permit.decision.CreatePermitDecisionDTO;
import fi.riista.feature.permit.decision.DecisionAppealSettingsDTO;
import fi.riista.feature.permit.decision.DecisionInformationPublishingDTO;
import fi.riista.feature.permit.decision.DecisionPublishSettingsDTO;
import fi.riista.feature.permit.decision.DecisionUnlockDTO;
import fi.riista.feature.permit.decision.PermitDecisionCompleteStatus;
import fi.riista.feature.permit.decision.PermitDecisionCreateFeature;
import fi.riista.feature.permit.decision.PermitDecisionDTO;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.PermitDecisionDocumentSettingsDTO;
import fi.riista.feature.permit.decision.PermitDecisionFeature;
import fi.riista.feature.permit.decision.PermitDecisionGrantStatusDTO;
import fi.riista.feature.permit.decision.PublishDecisionInformationDTO;
import fi.riista.feature.permit.decision.action.PermitDecisionActionDTO;
import fi.riista.feature.permit.decision.action.PermitDecisionActionFeature;
import fi.riista.feature.permit.decision.authority.PermitDecisionAuthorityFeature;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryDTO;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryFeature;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryUpdateDTO;
import fi.riista.feature.permit.decision.document.PermitDecisionDocumentFeature;
import fi.riista.feature.permit.decision.document.PermitDecisionDocumentSectionDTO;
import fi.riista.feature.permit.decision.document.PermitDecisionSectionIdentifier;
import fi.riista.feature.permit.decision.document.UpdateDecisionPaymentDTO;
import fi.riista.feature.permit.decision.informationrequest.PermitDecisionInformationRequestFeature;
import fi.riista.feature.permit.decision.legal.PermitDecisionLegalFieldsDTO;
import fi.riista.feature.permit.decision.legal.PermitDecisionLegalFieldsFeature;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethodDTO;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethodFeature;
import fi.riista.feature.permit.decision.publish.AdminPermitDecisionInvoiceCreateFeature;
import fi.riista.feature.permit.decision.reference.PermitDecisionReferenceDTO;
import fi.riista.feature.permit.decision.reference.PermitDecisionReferenceFeature;
import fi.riista.feature.permit.decision.reference.UpdatePermitDecisionReferenceDTO;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionDTO;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionDownloadFeature;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionFeature;
import fi.riista.feature.permit.decision.settings.PermitDecisionSettingsFeature;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountDTO;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountFeature;
import fi.riista.feature.permit.invoice.pdf.PermitDecisionInvoicePdfFeature;
import fi.riista.feature.permit.invoice.pdf.PermitHarvestInvoicePdfFeature;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Slice;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/decision")
public class PermitDecisionApiResource {

    @Resource
    private PermitDecisionFeature permitDecisionFeature;

    @Resource
    private PermitDecisionCreateFeature permitDecisionCreateFeature;

    @Resource
    private PermitDecisionSettingsFeature permitDecisionSettingsFeature;

    @Resource
    private PermitDecisionSpeciesAmountFeature permitDecisionSpeciesAmountFeature;

    @Resource
    private PermitDecisionForbiddenMethodFeature permitDecisionForbiddenMethodFeature;

    @Resource
    private PermitDecisionLegalFieldsFeature permitDecisionLegalFieldsFeature;

    @Resource
    private PermitDecisionDocumentFeature permitDecisionDocumentFeature;

    @Resource
    private PermitDecisionRevisionFeature permitDecisionRevisionFeature;

    @Resource
    private PermitDecisionRevisionDownloadFeature permitDecisionRevisionDownloadFeature;

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
    private PermitHarvestInvoicePdfFeature permitHarvestInvoicePdfFeature;

    @Resource
    private AdminPermitDecisionInvoiceCreateFeature adminPermitDecisionInvoiceCreateFeature;

    @Resource
    private PermitDecisionInformationRequestFeature permitDecisionInformationRequestFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitDecisionDTO getDecision(final @PathVariable long decisionId) {
        return permitDecisionFeature.getDecision(decisionId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getOrCreateDecision(final @RequestBody @Valid CreatePermitDecisionDTO dto) {
        final Long decisionId = permitDecisionCreateFeature.getOrCreateDecisionForApplication(dto);
        return Collections.singletonMap("id", decisionId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/application", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> getApplication(final @PathVariable long decisionId) {
        return Collections.singletonMap("id", permitDecisionFeature.getDecisionApplicationId(decisionId));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/hasarea", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Boolean> hasArea(final @PathVariable long decisionId) {
        return Collections.singletonMap("hasArea", permitDecisionFeature.hasArea(decisionId));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/hasnatura", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Boolean> hasNatura(final @PathVariable long decisionId) {
        return Collections.singletonMap("hasNatura", permitDecisionFeature.hasNatura(decisionId));
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
        return permitHarvestInvoicePdfFeature.getHarvestInvoicePdfFile(decisionId, gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "admin/invoice/processing/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void adminCreateProcessingInvoicePdf(final @RequestParam List<Long> ids) throws IOException {
        adminPermitDecisionInvoiceCreateFeature.createInvoicePdfs(ids);
    }

    // SPECIES

    static class AmountList {

        @NotEmpty
        @Valid
        public List<PermitDecisionSpeciesAmountDTO> list;

        public List<PermitDecisionSpeciesAmountDTO> getList() {
            return list;
        }

        public void setList(final List<PermitDecisionSpeciesAmountDTO> list) {
            this.list = list;
        }
    }

    @GetMapping(value = "{decisionId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PermitDecisionSpeciesAmountDTO> listSpecies(final @PathVariable long decisionId) {
        return permitDecisionSpeciesAmountFeature.getSpeciesAmounts(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/species", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateSpecies(final @PathVariable long decisionId,
                              final @Valid @RequestBody AmountList request) {
        permitDecisionSpeciesAmountFeature.saveSpeciesAmounts(decisionId, request.list);
    }

    // FORBIDDEN METHODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/methods/{gameSpeciesCode:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitDecisionForbiddenMethodDTO getForbiddenMethods(final @PathVariable long decisionId,
                                                                final @PathVariable int gameSpeciesCode) {
        return permitDecisionForbiddenMethodFeature.getForbiddenMethods(decisionId, gameSpeciesCode);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/methods/{gameSpeciesCode:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateForbiddenMethods(final @PathVariable long decisionId,
                                       final @PathVariable int gameSpeciesCode,
                                       final @RequestBody @Valid PermitDecisionForbiddenMethodDTO dto) {
        permitDecisionForbiddenMethodFeature.updateForbiddenMethods(decisionId, gameSpeciesCode, dto);
    }

    // DEVIATE LEGAL SECTION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/legal", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitDecisionLegalFieldsDTO getLegalFields(final @PathVariable long decisionId) {
        return permitDecisionLegalFieldsFeature.getFields(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/legal", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateLegalFields(final @PathVariable long decisionId,
                                  final @RequestBody @Valid PermitDecisionLegalFieldsDTO dto) {
        permitDecisionLegalFieldsFeature.updateFields(decisionId, dto);
    }

    // DOCUMENT

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/document", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @GetMapping(value = "{decisionId:\\d+}/generate/{sectionId:\\w+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> generateTextContent(final @PathVariable long decisionId,
                                                   final @PathVariable PermitDecisionSectionIdentifier sectionId) {
        return Collections.singletonMap("content", permitDecisionDocumentFeature.generate(decisionId, sectionId));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/generate-area-action", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> generateAreaAction(final @PathVariable long decisionId) {
        return Collections.singletonMap("content", permitDecisionDocumentFeature.generateAreaActionText(decisionId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/generate/{sectionId:\\w+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void generateAndPersistTextContent(final @PathVariable long decisionId,
                                              final @PathVariable PermitDecisionSectionIdentifier sectionId) {
        final String content = permitDecisionDocumentFeature.generate(decisionId, sectionId);
        if (StringUtils.isNotBlank(content)) {
            final PermitDecisionDocumentSectionDTO dto = new PermitDecisionDocumentSectionDTO();
            dto.setSectionId(sectionId);
            dto.setContent(content);
            permitDecisionDocumentFeature.updateDecisionDocument(decisionId, dto);
        }
    }

    // COMPLETE STATUS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
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


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/payment", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BigDecimal> getPayment(final @PathVariable long decisionId) {
        return permitDecisionDocumentFeature.getPaymentOptions(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/payment")
    public void updatePayment(final @Valid @RequestBody UpdateDecisionPaymentDTO dto) {
        permitDecisionDocumentFeature.updatePayment(dto);
    }

    // DOCUMENT SETTINGS

    @ResponseBody
    @GetMapping(value = "{decisionId:\\d+}/document-settings")
    public PermitDecisionDocumentSettingsDTO getDocumentSettings(final @PathVariable long decisionId) {
        return permitDecisionSettingsFeature.getDocumentSettings(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/document-settings")
    public void updateDocumentSettings(final @PathVariable long decisionId,
                                       final @Valid @RequestBody PermitDecisionDocumentSettingsDTO dto) {
        dto.setDecisionId(decisionId);
        permitDecisionSettingsFeature.updateDocumentSettings(dto);
    }

    // PUBLISH SETTINGS

    @ResponseBody
    @GetMapping(value = "{decisionId:\\d+}/publish-settings")
    public DecisionPublishSettingsDTO getPublishSettings(final @PathVariable long decisionId) {
        return permitDecisionSettingsFeature.getPublishSettings(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/publish-settings")
    public void updatePublishSettings(final @PathVariable long decisionId,
                                      final @Valid @RequestBody DecisionPublishSettingsDTO dto) {
        dto.setDecisionId(decisionId);
        permitDecisionSettingsFeature.updatePublishSettings(dto);
    }

    // APPEAL SETTINGS

    @ResponseBody
    @GetMapping(value = "{decisionId:\\d+}/appeal-settings")
    public DecisionAppealSettingsDTO getAppealSettings(final @PathVariable long decisionId) {
        return permitDecisionSettingsFeature.getAppealSettings(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/appeal-settings")
    public void updateAppealSettings(final @PathVariable long decisionId,
                                     final @Valid @RequestBody DecisionAppealSettingsDTO dto) {
        dto.setDecisionId(decisionId);
        permitDecisionSettingsFeature.updateAppealSettings(dto);
    }

    // MODERATOR TOOLS

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/assign")
    public void assignDecision(final @PathVariable long decisionId) {
        permitDecisionFeature.assignApplication(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/unassign")
    public void unassignDecision(final @PathVariable long decisionId) {
        permitDecisionFeature.unassignApplication(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/set-forbidden-methods")
    public void setForbiddenMethods(final @PathVariable long decisionId, final @RequestParam boolean forbiddenMethodsOnly) {
        permitDecisionFeature.updatePermitType(decisionId, forbiddenMethodsOnly);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/automatic-delivery-deduction")
    public void updateAutomaticDeliveryDeduction(final @PathVariable long decisionId, final @RequestParam boolean enabled) {
        permitDecisionFeature.updateAutomaticDeliveryDeduction(decisionId, enabled);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/lock")
    public void lockDecision(final @PathVariable long decisionId) {
        permitDecisionRevisionFeature.lockDecision(decisionId);

        final String htmlPath = PermitDecisionPdfController.getHtmlPath(decisionId);
        final String publicHtmlPath = PermitDecisionPdfController.getPublicHtmlPath(decisionId);

        // Create revision in separate transaction for pdf generation by external process
        permitDecisionRevisionFeature.createDecisionRevision(decisionId, htmlPath, publicHtmlPath);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/unlock")
    public void unlockDecision(final @PathVariable long decisionId,
                               final @RequestBody @Valid DecisionUnlockDTO dto) {
        dto.setId(decisionId);
        permitDecisionRevisionFeature.unlockDecision(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
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
    public ResponseEntity<byte[]> getRevisionAttachment(@PathVariable final long decisionId,
                                                        @PathVariable final long attachmentId) throws IOException {
        return permitDecisionRevisionFeature.getAttachment(decisionId, attachmentId);
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
    public ResponseEntity<?> getReference(final @PathVariable long decisionId) {
        final PermitDecisionReferenceDTO dto = permitDecisionReferenceFeature.getReference(decisionId);

        return ResponseEntity.ok().body(Collections.singletonMap("reference", dto));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/reference")
    public void updateReference(final @RequestBody @Valid UpdatePermitDecisionReferenceDTO dto) {
        permitDecisionReferenceFeature.updateReference(dto);
    }

    @PostMapping(value = "/search/references", produces = MediaType.APPLICATION_JSON_VALUE)
    public Slice<PermitDecisionReferenceDTO> searchReferences(
            final @RequestBody @Valid HarvestPermitApplicationSearchDTO dto) {
        return permitDecisionReferenceFeature.searchReferences(dto);
    }

    // DECISION ACTIONS

    @GetMapping(value = "{decisionId:\\d+}/action", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(value = "{decisionId:\\d+}/action/copy-actions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createActions(final @PathVariable long decisionId,
                              final @Valid @RequestBody List<PermitDecisionActionDTO> list) {
        permitDecisionActionFeature.createActions(decisionId, list);
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


    // DELIVERY
    @GetMapping(value = "{decisionId:\\d+}/delivery", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PermitDecisionDeliveryDTO> getDelivery(final @PathVariable long decisionId) {
        return permitDecisionDeliveryFeature.getDelivery(decisionId);
    }

    @PostMapping(value = "{decisionId:\\d+}/delivery", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateDelivery(final @PathVariable long decisionId,
                               final @Valid @RequestBody PermitDecisionDeliveryUpdateDTO deliveries) {
        permitDecisionDeliveryFeature.updateDelivery(decisionId, deliveries);
    }

    // AUTHORITIES
    @GetMapping(value = "{decisionId:\\d+}/authorities", produces = MediaType.APPLICATION_JSON_VALUE)
    public DecisionAuthoritiesDTO getAuthorities(final @PathVariable long decisionId) {
        return permitDecisionAuthorityFeature.getAuthorities(decisionId);
    }

    @PostMapping(value = "{decisionId:\\d+}/authorities", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateAuthorities(final @PathVariable long decisionId,
                                  final @Valid @RequestBody DecisionAuthoritiesDTO authorities) {
        permitDecisionAuthorityFeature.updateAuthorities(decisionId, authorities);
    }

    // GRANT STATUS
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/grantstatus")
    public void updateGrantStatus(final @PathVariable long decisionId,
                                  final @Valid @RequestBody PermitDecisionGrantStatusDTO dto) {
        permitDecisionFeature.updateGrantStatus(decisionId, dto);
    }

    // INFORMATION REQUESTS
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/inforequests")
    public void createInformationRequestLink(final @PathVariable long decisionId,
                                             final @RequestBody @Valid PublishDecisionInformationDTO dto) {
        dto.setId(decisionId);
        permitDecisionInformationRequestFeature.createAndSendInformationRequestLink(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/inforequests", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DecisionInformationPublishingDTO> listInformationRequestLinks(final @PathVariable long decisionId) {
        return permitDecisionInformationRequestFeature.getInformationRequestsStatistics(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "{decisionId:\\d+}/inforequests/{linkId}")
    public void deleteInformationRequestLink(final @PathVariable long decisionId,
                                             final @PathVariable long linkId) {
        permitDecisionInformationRequestFeature.invalidateInformationRequestLink(decisionId, linkId);
    }
}
