package fi.riista.feature.permit.decision.methods;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.decision.PermitDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PermitDecisionForbiddenMethodRepository extends JpaRepository<PermitDecisionForbiddenMethod, Long> {

    List<PermitDecisionForbiddenMethod> findByPermitDecision(final PermitDecision decision);

    @Query("SELECT f FROM PermitDecisionForbiddenMethod f WHERE f.permitDecision = ?1 and f.gameSpecies = ?2")
    List<PermitDecisionForbiddenMethod> findByDecisionAndSpecies(PermitDecision permitDecision, GameSpecies gameSpecies);

}
