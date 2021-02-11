package fi.riista.feature.common.decision.nomination.attachment;

import fi.riista.feature.common.repository.BaseRepository;

public interface NominationDecisionAttachmentRepository extends
        BaseRepository<NominationDecisionAttachment, Long>,
        NominationDecisionAttachmentRepositoryCustom {
}
