package fi.riista.feature.permit.application.derogation.reasons;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DerogationPermitApplicationReasonRepository
        extends JpaRepository<DerogationPermitApplicationReason, Long> {

    List<DerogationPermitApplicationReason> findByHarvestPermitApplication(HarvestPermitApplication application);

    void deleteByHarvestPermitApplication(HarvestPermitApplication application);
}
