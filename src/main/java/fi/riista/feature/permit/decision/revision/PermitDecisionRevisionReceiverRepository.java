package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.UUID;

public interface PermitDecisionRevisionReceiverRepository extends BaseRepository<PermitDecisionRevisionReceiver, Long> {

    PermitDecisionRevisionReceiver findByUuid(final UUID uuid);
}
