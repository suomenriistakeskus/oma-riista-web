package fi.riista.feature.permit.decision.settings;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.permit.decision.DecisionAppealSettingsDTO;
import fi.riista.feature.permit.decision.DecisionPublishSettingsDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocumentSettingsDTO;
import fi.riista.feature.permit.decision.PermitDecisionPaymentAmount;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Component
public class PermitDecisionSettingsFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    // DOCUMENT SETTINGS

    @Transactional(readOnly = true)
    public PermitDecisionDocumentSettingsDTO getDocumentSettings(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        final PermitDecisionDocumentSettingsDTO dto = new PermitDecisionDocumentSettingsDTO();
        dto.setLocale(decision.getLocale());
        dto.setDecisionType(decision.getDecisionType());

        return dto;
    }

    @Transactional
    public void updateDocumentSettings(final PermitDecisionDocumentSettingsDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());
        
        final boolean localeChanged = !Objects.equals(decision.getLocale(), dto.getLocale());
        final boolean decisionTypeChanged = !Objects.equals(decision.getDecisionType(), dto.getDecisionType());

        decision.setLocale(dto.getLocale());
        decision.setDecisionType(dto.getDecisionType());

        if (localeChanged || decisionTypeChanged) {
            if (decision.getDecisionType() != PermitDecision.DecisionType.HARVEST_PERMIT) {
                decision.setPaymentAmount(PermitDecisionPaymentAmount.getDefaultPaymentAmount(decision));
            }

            permitDecisionTextService.generateDefaultTextSections(decision, localeChanged);
        }
    }

    // PUBLISH SETTINGS

    @Transactional(readOnly = true)
    public DecisionPublishSettingsDTO getPublishSettings(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        final DecisionPublishSettingsDTO dto = new DecisionPublishSettingsDTO();
        dto.setAppealStatus(decision.getAppealStatus());

        if (decision.getPublishDate() != null) {
            dto.setPublishDate(decision.getPublishDate().toLocalDate());
            dto.setPublishTime(decision.getPublishDate().toLocalTime());
        }

        return dto;
    }

    @Transactional
    public void updatePublishSettings(final DecisionPublishSettingsDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        decision.setPublishDate(dto.getPublishDate().toDateTime(dto.getPublishTime()));

        if (decision.getAppealStatus() != null) {
            // Appeal status options are not available in UI if appeal status is missing
            decision.setAppealStatus(dto.getAppealStatus());
        }
    }

    // APPEAL SETTINGS

    @Transactional(readOnly = true)
    public DecisionAppealSettingsDTO getAppealSettings(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        final DecisionAppealSettingsDTO dto = new DecisionAppealSettingsDTO();
        dto.setAppealStatus(decision.getAppealStatus());

        return dto;
    }

    @Transactional
    public void updateAppealSettings(final DecisionAppealSettingsDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());
        decision.setAppealStatus(dto.getAppealStatus());
    }
}
