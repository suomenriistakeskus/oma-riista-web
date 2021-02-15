package fi.riista.feature.permit.application.importing;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface ImportingPermitApplicationRepository extends BaseRepository<ImportingPermitApplication, Long> {

    ImportingPermitApplication findByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

}
