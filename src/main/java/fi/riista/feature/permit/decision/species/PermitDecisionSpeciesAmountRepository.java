package fi.riista.feature.permit.decision.species;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.decision.PermitDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PermitDecisionSpeciesAmountRepository extends JpaRepository<PermitDecisionSpeciesAmount, Long> {

    List<PermitDecisionSpeciesAmount> findByPermitDecision(final PermitDecision decision);

    @Modifying
    void deleteByPermitDecision(PermitDecision permitDecision);

    @Modifying
    @Query("UPDATE PermitDecisionSpeciesAmount spa SET spa.forbiddenMethodComplete = TRUE WHERE spa.permitDecision = ?1 AND spa.gameSpecies = ?2")
    void setForbiddenMethodComplete(PermitDecision permitDecision, GameSpecies species);
}
