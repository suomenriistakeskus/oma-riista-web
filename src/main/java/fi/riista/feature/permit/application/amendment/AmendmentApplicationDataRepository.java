package fi.riista.feature.permit.application.amendment;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface AmendmentApplicationDataRepository extends BaseRepository<AmendmentApplicationData, Long> {

    AmendmentApplicationData getByApplication(final HarvestPermitApplication application);

    void deleteByApplication(HarvestPermitApplication harvestPermitApplication);
}
