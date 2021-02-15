package fi.riista.feature.common.decision.nomination.delivery;

import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface NominationDecisionDeliveryRepository extends BaseRepository<NominationDecisionDelivery, Long> {

    List<NominationDecisionDelivery> findAllByNominationDecisionOrderById(final NominationDecision decision);
}
