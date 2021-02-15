package fi.riista.feature.common.decision.nomination.document;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.security.EntityPermission;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Component
public class NominationDecisionDocumentFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private NominationDecisionTextService nominationDecisionTextService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public NominationDecisionDocument getDecisionDocument(final long id) {
        return requireEntityService.requireNominationDecision(id, EntityPermission.READ).getDocument();
    }

    @Transactional
    public void updateDecisionDocument(final long id, final NominationDecisionDocumentSectionDTO dto) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        decision.getDocument().updateContent(dto.getSectionId(), dto.getContent());
    }

    @Transactional(readOnly = true)
    public NominationDecisionCompleteStatus getCompleteStatus(final long id) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.UPDATE);
        return decision.getCompleteStatus();
    }

    @Transactional
    public void setSectionCompletionStatus(final long id, final NominationDecisionDocumentSectionDTO dto) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        decision.getCompleteStatus().updateStatus(dto.getSectionId(), dto.getComplete());
    }

    @Transactional(readOnly = true)
    public String generate(final long id, final NominationDecisionSectionIdentifier sectionId) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.READ);
        decision.assertHandler(activeUserService.requireActiveUser());
        return nominationDecisionTextService.generateSection(decision, sectionId);
    }

    @Transactional
    public void updateProposalDate(final long id, final LocalDate proposalDate) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(id, EntityPermission.UPDATE);
        decision.setProposalDate(proposalDate);
    }
}
