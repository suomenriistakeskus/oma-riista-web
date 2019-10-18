package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.permit.decision.PermitDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermitDecisionDerogationReasonRepository extends JpaRepository<PermitDecisionDerogationReason, Long> {

    public List<PermitDecisionDerogationReason> findByPermitDecision(PermitDecision decision);
}
