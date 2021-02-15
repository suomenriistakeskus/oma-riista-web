package fi.riista.api.decision.nomination;


import fi.riista.common.LocalDateDTO;
import fi.riista.feature.common.decision.authority.DecisionAuthoritiesDTO;
import fi.riista.feature.common.decision.nomination.CreateNominationDecisionDTO;
import fi.riista.feature.common.decision.nomination.NominationDecisionCreateFeature;
import fi.riista.feature.common.decision.nomination.NominationDecisionDTO;
import fi.riista.feature.common.decision.nomination.NominationDecisionFeature;
import fi.riista.feature.common.decision.nomination.NominationDecisionSearchDTO;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionDTO;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionFeature;
import fi.riista.feature.common.decision.nomination.authority.NominationDecisionAuthorityFeature;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDeliveryDTO;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDeliveryFeature;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDeliveryUpdateDTO;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionCompleteStatus;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocumentFeature;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocumentSectionDTO;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionSectionIdentifier;
import fi.riista.feature.common.decision.nomination.reference.NominationDecisionReferenceDTO;
import fi.riista.feature.common.decision.nomination.reference.NominationDecisionReferenceFeature;
import fi.riista.feature.common.decision.nomination.reference.UpdateNominationDecisionReferenceDTO;
import fi.riista.feature.common.decision.nomination.revision.NominationDecisionRevisionDTO;
import fi.riista.feature.common.decision.nomination.revision.NominationDecisionRevisionDownloadFeature;
import fi.riista.feature.common.decision.nomination.revision.NominationDecisionRevisionFeature;
import fi.riista.feature.common.decision.nomination.settings.NominationDecisionDocumentSettingsDTO;
import fi.riista.feature.common.decision.nomination.settings.NominationDecisionSettingsFeature;
import fi.riista.feature.permit.decision.DecisionAppealSettingsDTO;
import fi.riista.feature.permit.decision.DecisionPublishSettingsDTO;
import fi.riista.feature.permit.decision.DecisionUnlockDTO;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/nominationdecision")
public class NominationDecisionApiResource {

    @Resource
    private NominationDecisionFeature nominationDecisionFeature;

    @Resource
    private NominationDecisionCreateFeature nominationDecisionCreateFeature;

    @Resource
    private NominationDecisionDocumentFeature nominationDecisionDocumentFeature;

    @Resource
    private NominationDecisionActionFeature nominationDecisionActionFeature;

    @Resource
    private NominationDecisionReferenceFeature nominationDecisionReferenceFeature;

    @Resource
    private NominationDecisionAuthorityFeature nominationDecisionAuthorityFeature;

    @Resource
    private NominationDecisionDeliveryFeature nominationDecisionDeliveryFeature;

    @Resource
    private NominationDecisionRevisionFeature nominationDecisionRevisionFeature;

    @Resource
    private NominationDecisionSettingsFeature nominationDecisionSettingsFeature;

    @Resource
    private NominationDecisionRevisionDownloadFeature nominationDecisionRevisionDownloadFeature;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> createDecision(final @RequestBody @Valid CreateNominationDecisionDTO dto) {
        final long decisionId = nominationDecisionCreateFeature.createNominationDecision(dto);
        return Collections.singletonMap("id", decisionId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NominationDecisionDTO getDecision(final @PathVariable long decisionId) {
        return nominationDecisionFeature.getDecision(decisionId);
    }

    // DOCUMENT

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/document", produces = MediaType.APPLICATION_JSON_VALUE)
    public NominationDecisionDocument getDecisionDocument(final @PathVariable long decisionId) {
        return nominationDecisionDocumentFeature.getDecisionDocument(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/document")
    public void updateDecisionDocument(final @PathVariable long decisionId,
                                       final @Validated(NominationDecisionDocumentSectionDTO.ContentValidation.class)
                                       @RequestBody NominationDecisionDocumentSectionDTO dto) {
        nominationDecisionDocumentFeature.updateDecisionDocument(decisionId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/generate/{sectionId:\\w+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> generateTextContent(final @PathVariable long decisionId,
                                                   final @PathVariable NominationDecisionSectionIdentifier sectionId) {
        return Collections.singletonMap("content", nominationDecisionDocumentFeature.generate(decisionId, sectionId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/generate/{sectionId:\\w+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void generateAndPersistTextContent(final @PathVariable long decisionId,
                                              final @PathVariable NominationDecisionSectionIdentifier sectionId) {
        final String content = nominationDecisionDocumentFeature.generate(decisionId, sectionId);
        if (StringUtils.isNotBlank(content)) {
            final NominationDecisionDocumentSectionDTO dto = new NominationDecisionDocumentSectionDTO();
            dto.setSectionId(sectionId);
            dto.setContent(content);
            nominationDecisionDocumentFeature.updateDecisionDocument(decisionId, dto);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/proposal-date", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateProposalDate(final @PathVariable long decisionId,
                                   final @Valid @RequestBody LocalDateDTO dto) {

        nominationDecisionDocumentFeature.updateProposalDate(decisionId, dto.getDate());
    }

    // DOCUMENT SETTINGS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/document-settings")
    public NominationDecisionDocumentSettingsDTO getDocumentSettings(final @PathVariable long decisionId) {
        return nominationDecisionSettingsFeature.getDocumentSettings(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/document-settings")
    public void updateDocumentSettings(final @PathVariable long decisionId,
                                       final @Valid @RequestBody NominationDecisionDocumentSettingsDTO dto) {
        dto.setDecisionId(decisionId);
        nominationDecisionSettingsFeature.updateDocumentSettings(dto);
    }

    // PUBLISH SETTINGS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/publish-settings")
    public DecisionPublishSettingsDTO getPublishSettings(final @PathVariable long decisionId) {
        return nominationDecisionSettingsFeature.getPublishSettings(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/publish-settings")
    public void updatePublishSettings(final @PathVariable long decisionId,
                                      final @Valid @RequestBody DecisionPublishSettingsDTO dto) {
        dto.setDecisionId(decisionId);
        nominationDecisionSettingsFeature.updatePublishSettings(dto);
    }


    // APPEAL SETTINGS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/appeal-settings")
    public DecisionAppealSettingsDTO getAppealSettings(final @PathVariable long decisionId) {
        return nominationDecisionSettingsFeature.getAppealSettings(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/appeal-settings")
    public void updateAppealSettings(final @PathVariable long decisionId,
                                     final @Valid @RequestBody DecisionAppealSettingsDTO dto) {
        dto.setDecisionId(decisionId);
        nominationDecisionSettingsFeature.updateAppealSettings(dto);
    }

    // MODERATOR TOOLS

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/assign")
    public void assignDecision(final @PathVariable long decisionId) {
        nominationDecisionFeature.assignDecision(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/unassign")
    public void unassignDecision(final @PathVariable long decisionId) {
        nominationDecisionFeature.unassignDecision(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/lock")
    public void lockAndPublishDecision(final @PathVariable long decisionId) {
        nominationDecisionRevisionFeature.lockDecision(decisionId);

        // Create and publish revision in separate transaction for pdf generation by external process
        nominationDecisionRevisionFeature.createAndPublishDecisionRevision(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/unlock")
    public void unlockDecision(final @PathVariable long decisionId,
                               final @RequestBody @Valid DecisionUnlockDTO dto) {
        dto.setId(decisionId);
        nominationDecisionRevisionFeature.unlockDecision(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NominationDecisionRevisionDTO> listRevisions(final @PathVariable long decisionId) {
        return nominationDecisionRevisionFeature.listRevisions(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{decisionId:\\d+}/revisions/{revisionId:\\d+}/pdf")
    public void getRevisionPdf(@PathVariable final long decisionId,
                               @PathVariable final long revisionId,
                               final HttpServletResponse response) throws IOException {
        nominationDecisionRevisionDownloadFeature.downloadPdf(decisionId, revisionId, response);
    }

    @PostMapping(value = "/{decisionId:\\d+}/revisions/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getRevisionAttachment(@PathVariable final long decisionId,
                                                        @PathVariable final long attachmentId) throws IOException {
        return nominationDecisionRevisionFeature.getAttachment(decisionId, attachmentId);
    }

    @PostMapping(value = "/{decisionId:\\d+}/revisions/{revisionId:\\d+}/posted")
    public NominationDecisionRevisionDTO updatePosted(@PathVariable final long decisionId,
                                                      @PathVariable final long revisionId) {
        return nominationDecisionRevisionFeature.updatePosted(decisionId, revisionId, true);
    }

    @PostMapping(value = "/{decisionId:\\d+}/revisions/{revisionId:\\d+}/notposted")
    public NominationDecisionRevisionDTO updateNotPosted(@PathVariable final long decisionId,
                                                         @PathVariable final long revisionId) {
        return nominationDecisionRevisionFeature.updatePosted(decisionId, revisionId, false);
    }


    // COMPLETE STATUS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public NominationDecisionCompleteStatus getCompletetStatus(final @PathVariable long decisionId) {
        return nominationDecisionDocumentFeature.getCompleteStatus(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/complete")
    public void updateCompleteStatus(final @PathVariable long decisionId,
                                     final @Validated(NominationDecisionDocumentSectionDTO.CompleteValidation.class)
                                     @RequestBody NominationDecisionDocumentSectionDTO dto) {
        nominationDecisionDocumentFeature.setSectionCompletionStatus(decisionId, dto);
    }

    // REFERENCE DECISION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/reference")
    public ResponseEntity<?> getReference(final @PathVariable long decisionId) {
        final NominationDecisionReferenceDTO dto = nominationDecisionReferenceFeature.getReference(decisionId);

        return ResponseEntity.ok().body(Collections.singletonMap("reference", dto));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/reference")
    public void updateReference(final @RequestBody @Valid UpdateNominationDecisionReferenceDTO dto) {
        nominationDecisionReferenceFeature.updateReference(dto);
    }

    @PostMapping(value = "/search/references", produces = MediaType.APPLICATION_JSON_VALUE)
    public Slice<NominationDecisionReferenceDTO> searchReferences(
            final @RequestBody @Valid NominationDecisionSearchDTO dto) {
        return nominationDecisionReferenceFeature.searchReferences(dto);
    }

    // DECISION ACTIONS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/action", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NominationDecisionActionDTO> listActions(final @PathVariable long decisionId) {
        return nominationDecisionActionFeature.listActions(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/action", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createAction(final @PathVariable long decisionId,
                             final @Valid @RequestBody NominationDecisionActionDTO dto) {
        nominationDecisionActionFeature.create(decisionId, dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{decisionId:\\d+}/action/{actionId:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateAction(final @PathVariable long decisionId,
                             final @PathVariable long actionId,
                             final @Valid @RequestBody NominationDecisionActionDTO dto) {
        dto.setId(actionId);
        nominationDecisionActionFeature.update(decisionId, dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "{decisionId:\\d+}/action/{actionId:\\d+}")
    public void deleteAction(final @PathVariable long decisionId,
                             final @PathVariable long actionId) {
        nominationDecisionActionFeature.delete(decisionId, actionId);
    }

    // DELIVERY

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/delivery", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NominationDecisionDeliveryDTO> getDelivery(final @PathVariable long decisionId) {
        return nominationDecisionDeliveryFeature.getDelivery(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/delivery", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateDelivery(final @PathVariable long decisionId,
                               final @Valid @RequestBody NominationDecisionDeliveryUpdateDTO deliveries) {
        nominationDecisionDeliveryFeature.updateDelivery(decisionId, deliveries);
    }

    // AUTHORITIES

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/authorities", produces = MediaType.APPLICATION_JSON_VALUE)
    public DecisionAuthoritiesDTO getAuthorities(final @PathVariable long decisionId) {
        return nominationDecisionAuthorityFeature.getAuthorities(decisionId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{decisionId:\\d+}/authorities", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateAuthorities(final @PathVariable long decisionId,
                                  final @Valid @RequestBody DecisionAuthoritiesDTO authorities) {
        nominationDecisionAuthorityFeature.updateAuthorities(decisionId, authorities);
    }
}
