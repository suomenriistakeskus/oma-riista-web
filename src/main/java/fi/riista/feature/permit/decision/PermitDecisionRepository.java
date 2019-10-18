package fi.riista.feature.permit.decision;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PermitDecisionRepository extends BaseRepository<PermitDecision, Long>, PermitDecisionRepositoryCustom {

    @Query("SELECT d FROM PermitDecision d WHERE d.application = ?1")
    PermitDecision findOneByApplication(final HarvestPermitApplication application);

    @Query("SELECT d FROM PermitDecision d WHERE d.application in (?1)")
    List<PermitDecision> findApplications(final List<HarvestPermitApplication> applications);
}
