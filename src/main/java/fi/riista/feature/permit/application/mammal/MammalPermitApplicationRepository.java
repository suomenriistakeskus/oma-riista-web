package fi.riista.feature.permit.application.mammal;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface MammalPermitApplicationRepository extends BaseRepository<MammalPermitApplication, Long> {

    MammalPermitApplication findByHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication);

    void deleteByHarvestPermitApplication(HarvestPermitApplication harvestPermitApplication);

}
