package fi.riista.feature.permit.application.gamemanagement;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface GameManagementPermitApplicationRepository extends BaseRepository<GameManagementPermitApplication, Long> {

    GameManagementPermitApplication findByHarvestPermitApplication(final HarvestPermitApplication application);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication application);

}
