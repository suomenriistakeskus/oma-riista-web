package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NominationDecisionRepository extends BaseRepository<NominationDecision, Long>, NominationDecisionRepositoryCustom {
    List<NominationDecision> findByReference(NominationDecision reference);
}
