package fi.riista.feature.common.decision.nomination.action;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionTextService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class NominationDecisionActionFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private NominationDecisionActionRepository actionRepository;

    @Resource
    private NominationDecisionActionDTOTransformer dtoTransformer;

    @Resource
    private NominationDecisionTextService nominationDecisionTextService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public List<NominationDecisionActionDTO> listActions(final long decisionId) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(decisionId, EntityPermission.READ);

        final List<NominationDecisionAction> actions =
                actionRepository.findAllByNominationDecisionOrderByPointOfTimeDesc(decision);

        return dtoTransformer.transform(actions);
    }

    @Transactional
    public void create(final long decisionId, final NominationDecisionActionDTO dto) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final NominationDecisionAction action = new NominationDecisionAction();
        action.setNominationDecision(decision);
        updateEntity(dto, action);
        actionRepository.save(action);

        updateDecisionText(decision);
    }

    @Transactional
    public void update(final long decisionId, final NominationDecisionActionDTO dto) {
        final NominationDecision decision =
                requireEntityService.requireNominationDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final NominationDecisionAction action = actionRepository.getOne(dto.getId());
        Preconditions.checkArgument(decision.equals(action.getNominationDecision()));

        updateEntity(dto, action);
        updateDecisionText(decision);
    }

    @Transactional
    public void delete(final long decisionId, final long intermediateActionId) {
        final NominationDecision decision = requireEntityService.requireNominationDecision(decisionId, EntityPermission.UPDATE);
        decision.assertHandler(activeUserService.requireActiveUser());

        final NominationDecisionAction action = actionRepository.getOne(intermediateActionId);
        Preconditions.checkArgument(decision.equals(action.getNominationDecision()));

        action.setNominationDecision(null);
        actionRepository.delete(action);

        updateDecisionText(decision);
    }

    private static void updateEntity(final NominationDecisionActionDTO dto, final NominationDecisionAction action) {
        action.setPointOfTime(DateUtil.toDateTimeNullSafe(dto.getPointOfTime().toDate()));
        action.setActionType(dto.getActionType());
        action.setCommunicationType(dto.getCommunicationType());
        action.setText(dto.getText());
        action.setDecisionText(dto.getDecisionText());
    }

    private void updateDecisionText(final NominationDecision decision) {
        final NominationDecisionDocument document = decision.getDocument();
        final String processingBody = nominationDecisionTextService.generateProcessing(decision);
        document.setProcessing(processingBody);
    }
}
