package fi.riista.feature.common.decision.nomination.action;

import fi.riista.feature.common.decision.nomination.NominationDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NominationDecisionActionRepository extends JpaRepository<NominationDecisionAction, Long> {

    List<NominationDecisionAction> findAllByNominationDecisionOrderByPointOfTimeDesc(NominationDecision nominationDecision);
    List<NominationDecisionAction> findAllByNominationDecisionOrderByPointOfTimeAsc(NominationDecision nominationDecision);
}
