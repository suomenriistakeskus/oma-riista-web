package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;
import java.util.UUID;

public interface NominationDecisionRevisionReceiverRepository extends BaseRepository<NominationDecisionRevisionReceiver, Long> {

    NominationDecisionRevisionReceiver findByUuid(final UUID uuid);

    List<NominationDecisionRevisionReceiver> findAllByDecisionRevision(final NominationDecisionRevision revision);
}
