package fi.riista.feature.permit.decision.derogation;

import fi.riista.feature.permit.decision.PermitDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermitDecisionProtectedAreaTypeRepository extends JpaRepository<PermitDecisionProtectedAreaType, Long> {

    public List<PermitDecisionProtectedAreaType> findByPermitDecision(PermitDecision decision);
}
