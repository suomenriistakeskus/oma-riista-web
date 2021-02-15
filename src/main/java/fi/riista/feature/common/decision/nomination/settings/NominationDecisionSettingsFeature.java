package fi.riista.feature.common.decision.nomination.settings;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionTextService;
import fi.riista.feature.permit.decision.DecisionAppealSettingsDTO;
import fi.riista.feature.permit.decision.DecisionPublishSettingsDTO;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Component
public class NominationDecisionSettingsFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private NominationDecisionTextService nominationDecisionTextService;

    // DOCUMENT SETTINGS

    @Transactional(readOnly = true)
    public NominationDecisionDocumentSettingsDTO getDocumentSettings(final long decisionId) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(decisionId, EntityPermission.READ);
        final NominationDecisionDocumentSettingsDTO dto = new NominationDecisionDocumentSettingsDTO();
        dto.setLocale(decision.getLocale());

        return dto;
    }

    @Transactional
    public void updateDocumentSettings(final NominationDecisionDocumentSettingsDTO dto) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertEditableBy(activeUserService.requireActiveUser());

        final boolean localeChanged = !Objects.equals(decision.getLocale(), dto.getLocale());

        decision.setLocale(dto.getLocale());

        if (localeChanged) {
            nominationDecisionTextService.generateDefaultTextSections(decision);
        }
    }

    // PUBLISH SETTINGS

    @Transactional(readOnly = true)
    public DecisionPublishSettingsDTO getPublishSettings(final long decisionId) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(decisionId, EntityPermission.READ);
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
        final NominationDecision decision = requireEntityService.requireNominationDecision(dto.getDecisionId(), EntityPermission.UPDATE);
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
        final NominationDecision decision = requireEntityService.requireNominationDecision(decisionId, EntityPermission.READ);
        final DecisionAppealSettingsDTO dto = new DecisionAppealSettingsDTO();
        dto.setAppealStatus(decision.getAppealStatus());

        return dto;
    }

    @Transactional
    public void updateAppealSettings(final DecisionAppealSettingsDTO dto) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());
        decision.setAppealStatus(dto.getAppealStatus());
    }
}
