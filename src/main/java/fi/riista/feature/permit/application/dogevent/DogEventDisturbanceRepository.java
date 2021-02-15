package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

import java.util.List;

public interface DogEventDisturbanceRepository extends BaseRepository<DogEventDisturbance, Long> {

    List<DogEventDisturbance> findAllByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);


    DogEventDisturbance findByHarvestPermitApplicationAndEventType(final HarvestPermitApplication harvestPermitApplication,
                                                                   final DogEventType eventType);

    void deleteAllByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

}
