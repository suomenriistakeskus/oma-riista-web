package fi.riista.feature.permit.decision.legal;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class PermitDecisionLegalFieldsFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public PermitDecisionLegalFieldsDTO getFields(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);

        final PermitDecisionLegalFieldsDTO dto = new PermitDecisionLegalFieldsDTO();
        dto.setLegalSection32(decision.isLegalSection32());
        dto.setLegalSection33(decision.isLegalSection33());
        dto.setLegalSection34(decision.isLegalSection34());
        dto.setLegalSection35(decision.isLegalSection35());
        dto.setLegalSection51(decision.isLegalSection51());

        return dto;
    }

    @Transactional
    public void updateFields(final long decisionId, final PermitDecisionLegalFieldsDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);

        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        decision.setLegalSection32(dto.isLegalSection32());
        decision.setLegalSection33(dto.isLegalSection33());
        decision.setLegalSection34(dto.isLegalSection34());
        decision.setLegalSection35(dto.isLegalSection35());
        decision.setLegalSection51(dto.isLegalSection51());
    }
}
