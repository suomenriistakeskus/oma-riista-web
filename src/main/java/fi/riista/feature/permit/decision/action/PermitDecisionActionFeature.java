package fi.riista.feature.permit.decision.action;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class PermitDecisionActionFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionActionRepository permitDecisionActionRepository;

    @Resource
    private PermitDecisionActionDTOTransformer permitDecisionActionDTOTransformer;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public List<PermitDecisionActionDTO> listActions(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);

        return permitDecisionActionDTOTransformer.transform(decision.getActions()).stream()
                .sorted(Comparator.comparing(PermitDecisionActionDTO::getPointOfTime).reversed())
                .collect(toList());
    }

    @Transactional
    public void create(final long decisionId, final PermitDecisionActionDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final PermitDecisionAction action = new PermitDecisionAction();
        action.setPermitDecision(decision);
        updateEntity(dto, action);
        permitDecisionActionRepository.save(action);

        updateDecisionText(decision);
    }

    @Transactional
    public void createActions(final long decisionId, final List<PermitDecisionActionDTO> list) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final List<PermitDecisionAction> actions = list.stream()
                .map(dto -> {
                    final PermitDecisionAction action = new PermitDecisionAction();
                    action.setPermitDecision(decision);
                    updateEntity(dto, action);
                    return action;
                })
                .collect(toList());

        permitDecisionActionRepository.saveAll(actions);
        updateDecisionText(decision);
    }

    @Transactional
    public void update(final long decisionId, final PermitDecisionActionDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final PermitDecisionAction action = permitDecisionActionRepository.getOne(dto.getId());
        Preconditions.checkArgument(decision.equals(action.getPermitDecision()));

        updateEntity(dto, action);
        updateDecisionText(decision);
    }

    @Transactional
    public void delete(final long decisionId, final long intermediateActionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final PermitDecisionAction action = permitDecisionActionRepository.getOne(intermediateActionId);
        Preconditions.checkArgument(decision.equals(action.getPermitDecision()));

        permitDecisionActionRepository.delete(action);
        decision.getActions().remove(action);

        updateDecisionText(decision);
    }

    private static void updateEntity(final PermitDecisionActionDTO dto, final PermitDecisionAction action) {
        action.setPointOfTime(DateUtil.toDateTimeNullSafe(dto.getPointOfTime().toDate()));
        action.setActionType(dto.getActionType());
        action.setCommunicationType(dto.getCommunicationType());
        action.setText(dto.getText());
        action.setDecisionText(dto.getDecisionText());
    }

    private void updateDecisionText(final PermitDecision decision) {
        final PermitDecisionDocument document = decision.getDocument();
        document.setProcessing(permitDecisionTextService.generateProcessing(decision));
    }
}
