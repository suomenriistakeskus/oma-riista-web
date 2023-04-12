package fi.riista.feature.common.decision.nomination;

import com.google.common.base.Preconditions;
import fi.riista.api.decision.nomination.NominationDecisionHandlingStatisticsDTO;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.DecisionHandlerDTO;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionAction;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionAttachment;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionAttachmentRepository;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionRepository;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachment;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachmentRepository;
import fi.riista.feature.common.decision.nomination.authority.NominationDecisionAuthority;
import fi.riista.feature.common.decision.nomination.authority.NominationDecisionAuthorityRepository;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDelivery;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDeliveryRepository;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.common.decision.nomination.revision.NominationDecisionRevision;
import fi.riista.feature.common.decision.nomination.revision.NominationDecisionRevisionRepository;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.security.EntityPermission;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NominationDecisionFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private NominationDecisionRepository nominationDecisionRepository;

    @Resource
    private NominationDecisionRevisionRepository nominationDecisionRevisionRepository;

    @Resource
    private NominationDecisionActionRepository nominationDecisionActionRepository;

    @Resource
    private NominationDecisionActionAttachmentRepository nominationDecisionActionAttachmentRepository;

    @Resource
    private NominationDecisionAttachmentRepository nominationDecisionAttachmentRepository;

    @Resource
    private NominationDecisionAuthorityRepository nominationDecisionAuthorityRepository;

    @Resource
    private NominationDecisionDeliveryRepository nominationDecisionDeliveryRepository;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private NominationDecisionDTOTransformer nominationDecisionDTOTransformer;

    @Transactional(readOnly = true)
    public NominationDecisionDTO getDecision(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.READ);

        final NominationDecisionDocument htmlDocument =
                NominationDecisionDocumentTransformer.MARKDOWN_TO_HTML.copy(decision.getDocument());

        return NominationDecisionDTO.create(decision, htmlDocument, decision.isHandler(activeUserService.requireActiveUser()), canDelete(decision));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    public Slice<NominationDecisionDTO> search(final NominationDecisionSearchDTO dto) {

        final Slice<NominationDecision> slice = nominationDecisionRepository.search(dto, dto.asPageRequest());

        return nominationDecisionDTOTransformer.apply(slice, dto.asPageRequest());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<DecisionHandlerDTO> listHandlers() {
        return nominationDecisionRepository.listHandlers().stream()
                .map(u -> new DecisionHandlerDTO(u.getId(), u.getFullName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignDecision(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.UPDATE);

        decision.setHandler(activeUserService.requireActiveUser());
    }


    @Transactional
    public void unassignDecision(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.UPDATE);

        decision.assertEditableBy(activeUserService.requireActiveUser());
        decision.setHandler(null);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<NominationDecisionHandlingStatisticsDTO> getStatistics(final int year) {
        return nominationDecisionRepository.getStatistics(year);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional
    public void delete(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.DELETE);
        Preconditions.checkArgument(canDelete(decision), "Cannot delete nomination decision with published revision");
        Preconditions.checkArgument(decision.isHandler(activeUserService.requireActiveUser()), "Only handler can delete nomination decision");

        deleteActions(decision);
        deleteAttachments(decision);
        deleteAuthorities(decision);
        deleteDeliveries(decision);
        unlinkReferences(decision);

        nominationDecisionRepository.delete(decision);
    }

    private boolean canDelete(final NominationDecision decision) {
        final List<NominationDecisionRevision> revisions =
                nominationDecisionRevisionRepository.findByNominationDecision(decision);
        return (revisions == null || revisions.isEmpty()) && decision.isHandler(activeUserService.requireActiveUser());
    }

    private void deleteActions(final NominationDecision decision) {
        final List<NominationDecisionAction> actions =
                nominationDecisionActionRepository.findAllByNominationDecisionOrderByPointOfTimeAsc(decision);

        if (actions != null && !actions.isEmpty()) {
            actions.forEach(action -> {
                final List<NominationDecisionActionAttachment> attachments =
                        nominationDecisionActionAttachmentRepository.findAllByNominationDecisionAction(action);
                attachments.forEach(attachment -> {
                    final UUID metadataId = attachment.getAttachmentMetadata().getId();
                    nominationDecisionActionAttachmentRepository.delete(attachment);
                    fileStorageService.remove(metadataId);
                });
            });

            nominationDecisionActionRepository.deleteAll(actions);
        }
    }

    private void deleteAttachments(final NominationDecision decision) {
        final List<NominationDecisionAttachment> attachments =
                nominationDecisionAttachmentRepository.findAllByNominationDecision(decision);
        attachments.forEach(attachment -> {
            final UUID metadataId = attachment.getAttachmentMetadata().getId();
            nominationDecisionAttachmentRepository.delete(attachment);
            fileStorageService.remove(metadataId);
        });
    }

    private void deleteAuthorities(final NominationDecision decision) {
        final NominationDecisionAuthority presenter = decision.getPresenter();
        final NominationDecisionAuthority decisionMaker = decision.getDecisionMaker();

        if (presenter != null) {
            nominationDecisionAuthorityRepository.delete(presenter);
        }

        if (decisionMaker != null) {
            nominationDecisionAuthorityRepository.delete(decisionMaker);
        }
    }

    private void deleteDeliveries(final NominationDecision decision) {
        final List<NominationDecisionDelivery> deliveries =
                nominationDecisionDeliveryRepository.findAllByNominationDecisionOrderById(decision);
        if (deliveries != null && !deliveries.isEmpty()) {
            nominationDecisionDeliveryRepository.deleteAll(deliveries);
        }
    }

    private void unlinkReferences(final NominationDecision decision) {
        final List<NominationDecision> referencing = nominationDecisionRepository.findByReference(decision);
        referencing.forEach(d -> d.setReference(null));
    }
}
