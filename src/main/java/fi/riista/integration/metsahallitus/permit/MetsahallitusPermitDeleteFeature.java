package fi.riista.integration.metsahallitus.permit;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class MetsahallitusPermitDeleteFeature {

    @Resource
    private MetsahallitusPermitRepository repository;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public long deleteOldPermits() {
        return repository.deleteOldPermits();
    }
}
