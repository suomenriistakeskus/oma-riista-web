package fi.riista.feature.permit.area;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface HarvestPermitAreaEventRepository extends BaseRepository<HarvestPermitAreaEvent, Long> {

    List<HarvestPermitAreaEvent> findByHarvestPermitArea(HarvestPermitArea area);

}
