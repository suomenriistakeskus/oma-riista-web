package fi.riista.feature.permit.application.disability;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface DisabilityPermitApplicationRepository extends BaseRepository<DisabilityPermitApplication, Long> {

    DisabilityPermitApplication findByHarvestPermitApplication(final HarvestPermitApplication application);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication application);
}
