package fi.riista.feature.permit.application.carnivore;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface CarnivorePermitApplicationRepository extends BaseRepository<CarnivorePermitApplication, Long> {

    CarnivorePermitApplication findByHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication);

    void deleteByHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication);

}
