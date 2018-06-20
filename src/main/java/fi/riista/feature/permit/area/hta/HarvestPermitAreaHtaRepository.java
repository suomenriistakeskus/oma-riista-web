package fi.riista.feature.permit.area.hta;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import org.springframework.data.jpa.repository.Modifying;

public interface HarvestPermitAreaHtaRepository extends BaseRepository<HarvestPermitAreaHta, Long> {
    @Modifying
    void deleteByHarvestPermitArea(HarvestPermitArea harvestPermitArea);
}
