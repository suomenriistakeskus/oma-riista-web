package fi.riista.feature.permit.application.attachment;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface HarvestPermitApplicationAttachmentRepository extends BaseRepository<HarvestPermitApplicationAttachment, Long> {

    void deleteByHarvestPermitApplication(HarvestPermitApplication entity);
}
