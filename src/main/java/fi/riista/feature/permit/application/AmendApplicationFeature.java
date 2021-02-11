package fi.riista.feature.permit.application;

import fi.riista.feature.permit.application.validation.HarvestPermitApplicationValidationService;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionAmendUpdater;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.common.decision.DecisionActionType;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.action.PermitDecisionActionRepository;
import fi.riista.util.DateUtil;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Component
public class AmendApplicationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationValidationService harvestPermitApplicationValidationService;

    @Resource
    private PermitDecisionAmendUpdater permitDecisionAmendUpdater;

    @Resource
    private PermitDecisionActionRepository permitDecisionActionRepository;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Transactional
    public void startAmendApplication(final long applicationId) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.amendApplication(applicationId);
        final PermitDecision permitDecision = requireDecision(application);
        permitDecision.assertStatus(DecisionStatus.DRAFT);

        application.startAmending();

        if (application.getArea() != null && application.getArea().getStatus() == HarvestPermitArea.StatusCode.LOCKED) {
            application.getArea().setStatusUnlocked();
        }
    }

    @Transactional
    public void stopAmendApplication(final HarvestPermitApplicationAmendDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.amendApplication(dto.getId());
        final PermitDecision permitDecision = requireDecision(application);

        harvestPermitApplicationValidationService.validateApplicationForAmend(application);

        application.stopAmending();

        if (application.getArea() != null) {
            application.getArea().setStatusLocked();
        }

        if (dto.getSubmitDate() != null) {
            final LocalTime originalLocalTime = application.getSubmitDate().toLocalTime();
            application.setSubmitDate(dto.getSubmitDate().toDateTime(originalLocalTime));
        }

        final PermitDecisionAction action = new PermitDecisionAction();
        action.setPermitDecision(permitDecision);
        action.setPointOfTime(DateUtil.now());
        action.setText(dto.getChangeReason());
        action.setActionType(DecisionActionType.TAYDENNYS);

        permitDecisionActionRepository.save(action);

        permitDecisionAmendUpdater.updateDecision(permitDecision);
    }

    private PermitDecision requireDecision(final HarvestPermitApplication application) {
        final PermitDecision decision = permitDecisionRepository.findOneByApplication(application);

        return Objects.requireNonNull(decision, "Decision not available for application");
    }
}
