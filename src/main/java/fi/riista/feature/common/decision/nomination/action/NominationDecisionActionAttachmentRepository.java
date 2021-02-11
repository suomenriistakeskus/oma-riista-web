package fi.riista.feature.common.decision.nomination.action;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface NominationDecisionActionAttachmentRepository extends BaseRepository<NominationDecisionActionAttachment, Long> {

    List<NominationDecisionActionAttachment> findAllByNominationDecisionAction(NominationDecisionAction decisionAction);
}
