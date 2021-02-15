package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.feature.common.repository.BaseRepository;

public interface NominationDecisionRevisionAttachmentRepository extends
        BaseRepository<NominationDecisionRevisionAttachment, Long>,
        NominationDecisionRevisionAttachmentRepositoryCustom {
}
