package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface DogEventApplicationRepository extends BaseRepository<DogEventApplication, Long> {

        DogEventApplication findByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

        void deleteByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

}
