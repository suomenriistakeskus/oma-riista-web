package fi.riista.feature.common.decision.nomination;

import fi.riista.api.decision.nomination.NominationDecisionHandlingStatisticsDTO;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.DecisionHandlerDTO;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.security.EntityPermission;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
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
    private NominationDecisionDTOTransformer nominationDecisionDTOTransformer;

    @Transactional(readOnly = true)
    public NominationDecisionDTO getDecision(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.READ);

        final NominationDecisionDocument htmlDocument =
                NominationDecisionDocumentTransformer.MARKDOWN_TO_HTML.copy(decision.getDocument());

        return NominationDecisionDTO.create(decision, htmlDocument, decision.isHandler(activeUserService.requireActiveUser()));
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
}
