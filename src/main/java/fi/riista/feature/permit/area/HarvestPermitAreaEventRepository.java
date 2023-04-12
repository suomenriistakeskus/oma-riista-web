package fi.riista.feature.permit.area;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface HarvestPermitAreaEventRepository extends BaseRepository<HarvestPermitAreaEvent, Long> {

    List<HarvestPermitAreaEvent> findByHarvestPermitArea(HarvestPermitArea area);

    @Modifying
    void deleteByHarvestPermitArea(HarvestPermitArea harvestPermitArea);

}
