package fi.riista.feature.permit.application.archive;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

import java.util.List;

public interface PermitApplicationArchiveRepository extends BaseRepository<PermitApplicationArchive, Long> {
    List<PermitApplicationArchive> findByHarvestPermitApplication(HarvestPermitApplication application);
}
