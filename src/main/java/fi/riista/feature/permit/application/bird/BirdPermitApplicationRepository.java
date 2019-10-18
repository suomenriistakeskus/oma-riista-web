package fi.riista.feature.permit.application.bird;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface BirdPermitApplicationRepository extends BaseRepository<BirdPermitApplication, Long> {

    BirdPermitApplication findByHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication);

    void deleteByHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication);

}

