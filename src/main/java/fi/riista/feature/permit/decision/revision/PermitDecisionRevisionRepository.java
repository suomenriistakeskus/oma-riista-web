package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.decision.PermitDecision;

import java.util.List;

public interface PermitDecisionRevisionRepository extends BaseRepository<PermitDecisionRevision, Long> {

    List<PermitDecisionRevision> findByPermitDecision(final PermitDecision decision);
}
