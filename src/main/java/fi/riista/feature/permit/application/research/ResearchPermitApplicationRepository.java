package fi.riista.feature.permit.application.research;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface ResearchPermitApplicationRepository extends BaseRepository<ResearchPermitApplication, Long> {

    ResearchPermitApplication findByHarvestPermitApplication(final HarvestPermitApplication application);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication application);
}
