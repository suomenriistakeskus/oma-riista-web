package fi.riista.feature.permit.application.schedule;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;

public interface HarvestPermitApplicationScheduleRepository extends BaseRepository<HarvestPermitApplicationSchedule, Long> {
    HarvestPermitApplicationSchedule findByCategory(final HarvestPermitCategory category);
}
