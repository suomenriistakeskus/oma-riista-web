package fi.riista.feature.permit.decision;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PermitDecisionRepository extends BaseRepository<PermitDecision, Long>, PermitDecisionRepositoryCustom {

    @Query("SELECT d FROM PermitDecision d WHERE d.application = ?1")
    PermitDecision findOneByApplication(final HarvestPermitApplication application);

    @Query("SELECT d FROM PermitDecision d WHERE d.application in (?1)")
    List<PermitDecision> findApplications(final List<HarvestPermitApplication> applications);

    @Query("SELECT d FROM HarvestPermit h JOIN PermitDecision d ON h.permitDecision.id = d.id WHERE h in (?1)")
    List<PermitDecision> findByHarvestPermitIn(final Collection<HarvestPermit> permits);

    @Query("SELECT d FROM PermitDecision d WHERE d.decisionNumber = ?1")
    PermitDecision findByDecisionNumber(final int decisionNumber);
}
