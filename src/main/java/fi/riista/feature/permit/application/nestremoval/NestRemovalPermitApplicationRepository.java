package fi.riista.feature.permit.application.nestremoval;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface NestRemovalPermitApplicationRepository extends BaseRepository<NestRemovalPermitApplication, Long> {

    NestRemovalPermitApplication findByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

}
