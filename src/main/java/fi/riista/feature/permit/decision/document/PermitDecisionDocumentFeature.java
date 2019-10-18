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
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

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
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        decision.getCompleteStatus().updateStatus(dto.getSectionId(), dto.getComplete());
    }

    @Transactional(readOnly = true)
    public String generate(final long id, final PermitDecisionSectionIdentifier sectionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        decision.assertHandler(activeUserService.requireActiveUser());

        switch (sectionId) {
            case application:
                return permitDecisionTextService.generateApplicationSummary(decision);
            case applicationReasoning:
                return permitDecisionTextService.generateApplicationReasoning(decision);
            case decision:
                return permitDecisionTextService.generateDecision(decision);
            case restriction:
                return permitDecisionTextService.generateRestriction(decision);
            case processing:
                return permitDecisionTextService.generateProcessing(decision);
            case decisionReasoning:
                return permitDecisionTextService.generateDecisionReasoning(decision);
            case legalAdvice:
                return permitDecisionTextService.generateLegalAdvice(decision);
            case notificationObligation:
                return permitDecisionTextService.generateNotificationObligation(decision);
            case appeal:
                return permitDecisionTextService.generateAppeal(decision);
            case additionalInfo:
                return permitDecisionTextService.generateAdditionalInfo(decision);
            case delivery:
                return permitDecisionTextService.generateDelivery(decision);
            case adjustedAreaSizeAction:
                return permitDecisionTextService.generateAdjustedAreaSizeText(decision);
            default:
                return "";
        }
    }

    @Transactional(readOnly = true)
    public List<BigDecimal> getPaymentOptions(long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        return PermitDecisionPaymentAmount.getPaymentOptionsFor(decision);
    }

    @Transactional
    public void updatePayment(final UpdateDecisionPaymentDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getId(), EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        decision.setPaymentAmount(dto.getPaymentAmount());
        decision.getDocument().setPayment(permitDecisionTextService.generatePayment(decision));
    }
}
