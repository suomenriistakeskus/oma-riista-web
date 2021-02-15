package fi.riista.feature.permit.application.deportation;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface DeportationPermitApplicationRepository extends BaseRepository<DeportationPermitApplication, Long> {

    DeportationPermitApplication findByHarvestPermitApplication(final HarvestPermitApplication application);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication application);
}
