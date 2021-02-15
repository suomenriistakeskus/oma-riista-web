package fi.riista.feature.permit.decision.document;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionCompleteStatus;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.PermitDecisionPaymentAmount;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Component
public class PermitDecisionDocumentFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public PermitDecisionDocument getDecisionDocument(final long id) {
        return requireEntityService.requirePermitDecision(id, EntityPermission.READ).getDocument();
    }

    @Transactional
    public void updateDecisionDocument(final long id, final PermitDecisionDocumentSectionDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        decision.getDocument().updateContent(dto.getSectionId(), dto.getContent());
    }

    @Transactional(readOnly = true)
    public PermitDecisionCompleteStatus getCompleteStatus(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.UPDATE);
        return decision.getCompleteStatus();
    }

    @Transactional
    public void setSectionCompletionStatus(final long id, final PermitDecisionDocumentSectionDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        decision.getCompleteStatus().updateStatus(dto.getSectionId(), dto.getComplete());
    }

    @Transactional(readOnly = true)
    public String generate(final long id, final PermitDecisionSectionIdentifier sectionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        decision.assertHandler(activeUserService.requireActiveUser());

        switch (sectionId) {
            case APPLICATION:
                return permitDecisionTextService.generateApplicationSummary(decision);
            case APPLICATION_REASONING:
                return permitDecisionTextService.generateApplicationReasoning(decision);
            case DECISION:
                return permitDecisionTextService.generateDecision(decision);
            case RESTRICTION:
                return permitDecisionTextService.generateRestriction(decision);
            case PROCESSING:
                return permitDecisionTextService.generateProcessing(decision);
            case DECISION_REASONING:
                return permitDecisionTextService.generateDecisionReasoning(decision);
            case LEGAL_ADVICE:
                return permitDecisionTextService.generateLegalAdvice(decision);
            case NOTIFICATION_OBLIGATION:
                return permitDecisionTextService.generateNotificationObligation(decision);
            case APPEAL:
                return permitDecisionTextService.generateAppeal(decision);
            case ADDITIONAL_INFO:
                return permitDecisionTextService.generateAdditionalInfo(decision);
            case DELIVERY:
                return permitDecisionTextService.generateDelivery(decision);
            case DECISION_EXTRA:
                return permitDecisionTextService.generateDecisionExtra(decision);
            default:
                return "";
        }
    }

    @Transactional(readOnly = true)
    public String generateAreaActionText(final long id){
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        decision.assertHandler(activeUserService.requireActiveUser());
        return permitDecisionTextService.generateAdjustedAreaSizeText(decision);
    }

    @Transactional(readOnly = true)
    public List<BigDecimal> getPaymentOptions(long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        return PermitDecisionPaymentAmount.getPaymentOptionsFor(decision);
    }

    @Transactional
    public void updatePayment(final UpdateDecisionPaymentDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getId(), EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        decision.setPaymentAmount(dto.getPaymentAmount());
        decision.getDocument().setPayment(permitDecisionTextService.generatePayment(decision));
    }
}
