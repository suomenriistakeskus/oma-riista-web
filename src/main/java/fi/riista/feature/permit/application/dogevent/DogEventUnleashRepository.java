package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

import java.util.List;

public interface DogEventUnleashRepository extends BaseRepository<DogEventUnleash, Long> {

    List<DogEventUnleash> findAllByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

    List<DogEventUnleash> findAllByHarvestPermitApplicationOrderByBeginDate(final HarvestPermitApplication harvestPermitApplication);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

}
