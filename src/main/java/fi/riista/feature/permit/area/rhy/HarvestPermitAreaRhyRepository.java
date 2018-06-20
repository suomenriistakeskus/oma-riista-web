package fi.riista.feature.permit.area.rhy;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import org.springframework.data.jpa.repository.Modifying;

public interface HarvestPermitAreaRhyRepository extends BaseRepository<HarvestPermitAreaRhy, Long> {
    @Modifying
    void deleteByHarvestPermitArea(HarvestPermitArea harvestPermitArea);
}
