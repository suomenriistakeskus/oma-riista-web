package fi.riista.feature.common.decision.nomination.actions;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionAction;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionDTO;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionFeature;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionActionRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.common.decision.DecisionActionCommunicationType.EMAIL;
import static fi.riista.feature.common.decision.DecisionActionType.KUULEMINEN;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Component
public class NominationDecisionActionFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private NominationDecisionActionFeature feature;

    @Resource
    private NominationDecisionActionRepository repository;

    private Riistanhoitoyhdistys rhy;
    private Person coordinator;
    private NominationDecision decision;
    private SystemUser moderator;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        coordinator = model().newPersonWithAddress();
        final DeliveryAddress deliveryAddress = DeliveryAddress.create(rhy.getNameFinnish(), coordinator.getAddress());
        model().newOccupation(rhy, coordinator, OccupationType.TOIMINNANOHJAAJA);
        moderator = createNewModerator();
        decision = model().newNominationDecision(rhy, OccupationType.METSASTYKSENVALVOJA, coordinator, deliveryAddress);
        decision.setHandler(moderator);

    }

    @Test
    public void testCreateAction_smoke() {
        final DateTime now = DateUtil.now();

        onSavedAndAuthenticated(moderator, () -> {
            final NominationDecisionActionDTO dto = new NominationDecisionActionDTO();
            dto.setActionType(KUULEMINEN);
            dto.setCommunicationType(EMAIL);
            dto.setText("text");
            dto.setDecisionText("decision text");
            dto.setPointOfTime(now.toLocalDateTime());

            feature.create(decision.getId(), dto);
        });

        runInTransaction(() -> {
            final List<NominationDecisionAction> all = repository.findAll();
            assertThat(all, hasSize(1));
            final NominationDecisionAction decisionAction = all.get(0);

            assertThat(decisionAction.getActionType(), equalTo(KUULEMINEN));
            assertThat(decisionAction.getCommunicationType(), equalTo(EMAIL));
            assertThat(decisionAction.getText(), equalTo("text"));
            assertThat(decisionAction.getDecisionText(), equalTo("decision text"));
            assertThat(decisionAction.getPointOfTime(), equalTo(now));
        });
    }
}
