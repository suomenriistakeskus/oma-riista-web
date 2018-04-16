package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface HarvestPermitAreaHtaRepository extends BaseRepository<HarvestPermitAreaHta, Long> {
    @Modifying
    void deleteByHarvestPermitArea(HarvestPermitArea harvestPermitArea);
}
