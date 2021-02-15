package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NominationDecisionRepository extends BaseRepository<NominationDecision, Long>, NominationDecisionRepositoryCustom{

}
