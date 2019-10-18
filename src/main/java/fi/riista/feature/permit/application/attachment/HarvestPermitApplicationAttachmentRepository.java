package fi.riista.feature.permit.application.attachment;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

import java.util.List;

public interface HarvestPermitApplicationAttachmentRepository extends BaseRepository<HarvestPermitApplicationAttachment, Long> {
    List<HarvestPermitApplicationAttachment> findByHarvestPermitApplication(HarvestPermitApplication entity);

    void deleteByHarvestPermitApplication(HarvestPermitApplication entity);
}
