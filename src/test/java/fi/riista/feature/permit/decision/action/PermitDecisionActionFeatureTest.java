package fi.riista.feature.permit.decision.action;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.DecisionActionCommunicationType;
import fi.riista.feature.common.decision.DecisionActionType;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class PermitDecisionActionFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PermitDecisionActionFeature feature;

    @Resource
    private PermitDecisionRepository decisionRepository;

    @Test
    public void testCreateActions() {
        final SystemUser moderator = createNewModerator();
        final PermitDecision decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        decision.setHandler(moderator);

        onSavedAndAuthenticated(moderator, () -> {
            final PermitDecisionActionDTO actionDTO = createActionDTO();
            feature.createActions(decision.getId(), Collections.singletonList(actionDTO));

            runInTransaction(() -> {
                final PermitDecision updatedDecision = decisionRepository.getOne(decision.getId());
                final List<PermitDecisionAction> actionList = updatedDecision.getActions();
                assertThat(actionList, hasSize(1));

                final PermitDecisionAction action = actionList.get(0);
                assertAction(action, actionDTO);
                assertThat(updatedDecision.getDocument().getProcessing(), is(equalTo(actionDTO.getDecisionText())));
            });
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateActions_notHandler() {
        final SystemUser moderator = createNewModerator();
        final PermitDecision decision = model().newPermitDecision(model().newRiistanhoitoyhdistys());
        decision.setHandler(moderator);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final PermitDecisionActionDTO actionDTO = createActionDTO();
            feature.createActions(decision.getId(), Collections.singletonList(actionDTO));
        });
    }

    private PermitDecisionActionDTO createActionDTO() {
        final PermitDecisionActionDTO actionDTO = new PermitDecisionActionDTO();
        actionDTO.setPointOfTime(DateUtil.localDateTime());
        actionDTO.setActionType(some(DecisionActionType.class));
        actionDTO.setCommunicationType(some(DecisionActionCommunicationType.class));
        actionDTO.setText("Text");
        actionDTO.setDecisionText("Decision text");

        return actionDTO;
    }

    private void assertAction(final PermitDecisionAction result, final PermitDecisionActionDTO expected) {
        assertThat(result.getPointOfTime(), is(equalTo(DateUtil.toDateTimeNullSafe(expected.getPointOfTime()))));
        assertThat(result.getActionType(), is(equalTo(expected.getActionType())));
        assertThat(result.getCommunicationType(), is(equalTo(expected.getCommunicationType())));
        assertThat(result.getText(), is(equalTo(expected.getText())));
        assertThat(result.getDecisionText(), is(equalTo(expected.getDecisionText())));
    }
}
