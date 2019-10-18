package fi.riista.feature.permit.decision.settings;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionAppealSettingsDTO;
import fi.riista.feature.permit.decision.PermitDecisionDocumentSettingsDTO;
import fi.riista.feature.permit.decision.PermitDecisionPaymentAmount;
import fi.riista.feature.permit.decision.PermitDecisionPublishSettingsDTO;
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
        dto.setAppealStatus(decision.getAppealStatus());
        dto.setDecisionType(decision.getDecisionType());

        return dto;
    }

    @Transactional
    public void updateDocumentSettings(final PermitDecisionDocumentSettingsDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());
        
        final boolean localeChanged = !Objects.equals(decision.getLocale(), dto.getLocale());
        final boolean decisionTypeChanged = !Objects.equals(decision.getDecisionType(), dto.getDecisionType());

        decision.setLocale(dto.getLocale());
        decision.setDecisionType(dto.getDecisionType());

        updateAppealStatus(dto, decision);

        if (localeChanged || decisionTypeChanged) {
            if (decision.getDecisionType() != PermitDecision.DecisionType.HARVEST_PERMIT) {
                decision.setPaymentAmount(PermitDecisionPaymentAmount.getDefaultPaymentAmount(decision));
            }

            permitDecisionTextService.generateDefaultTextSections(decision, localeChanged);
        }
    }

    private static void updateAppealStatus(final PermitDecisionDocumentSettingsDTO dto, final PermitDecision decision) {
        // Settings UI only allows toggling appeal initiated state on/off
        final boolean appealNotResolved = decision.getAppealStatus() == null ||
                decision.getAppealStatus() == PermitDecision.AppealStatus.INITIATED;

        if (appealNotResolved) {
            final boolean shouldInitiateAppeal = dto.getAppealStatus() == PermitDecision.AppealStatus.INITIATED;

            // Settings UI should not send other appeal states, but make sure...
            decision.setAppealStatus(shouldInitiateAppeal ? PermitDecision.AppealStatus.INITIATED : null);
        }
    }

    // PUBLISH SETTINGS

    @Transactional(readOnly = true)
    public PermitDecisionPublishSettingsDTO getPublishSettings(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        final PermitDecisionPublishSettingsDTO dto = new PermitDecisionPublishSettingsDTO();
        dto.setAppealStatus(decision.getAppealStatus());

        if (decision.getPublishDate() != null) {
            dto.setPublishDate(decision.getPublishDate().toLocalDate());
            dto.setPublishTime(decision.getPublishDate().toLocalTime());
        }

        return dto;
    }

    @Transactional
    public void updatePublishSettings(final PermitDecisionPublishSettingsDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        decision.setPublishDate(dto.getPublishDate().toDateTime(dto.getPublishTime()));

        if (decision.getAppealStatus() != null) {
            // Appeal status options are not available in UI if appeal status is missing
            decision.setAppealStatus(dto.getAppealStatus());
        }
    }

    // APPEAL SETTINGS

    @Transactional(readOnly = true)
    public PermitDecisionAppealSettingsDTO getAppealSettings(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        final PermitDecisionAppealSettingsDTO dto = new PermitDecisionAppealSettingsDTO();
        dto.setAppealStatus(decision.getAppealStatus());

        return dto;
    }

    @Transactional
    public void updateAppealSettings(final PermitDecisionAppealSettingsDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());
        decision.setAppealStatus(dto.getAppealStatus());
    }
}
