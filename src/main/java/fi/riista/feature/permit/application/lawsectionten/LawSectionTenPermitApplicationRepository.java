package fi.riista.feature.permit.application.lawsectionten;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface LawSectionTenPermitApplicationRepository extends BaseRepository<LawSectionTenPermitApplication, Long> {

    LawSectionTenPermitApplication findByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication);

}
