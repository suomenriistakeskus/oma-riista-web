package fi.riista.feature.common.decision.nomination.attachment;

import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.revision.NominationDecisionRevision;

import java.util.List;

public interface NominationDecisionAttachmentRepositoryCustom {

    List<NominationDecisionAttachment> findOrderedByNominationDecision(final NominationDecision decision);

    List<NominationDecisionAttachment> findAllByNominationDecision(final NominationDecision decision);

    List<NominationDecisionAttachment> findListedAttachmentsByNominationDecisionRevision(final NominationDecisionRevision revision);

}
