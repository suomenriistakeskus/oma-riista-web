package fi.riista.feature.permit.area.verotuslohko;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import org.springframework.data.jpa.repository.Modifying;

public interface HarvestPermitAreaVerotusLohkoRepository extends BaseRepository<HarvestPermitAreaVerotusLohko, Long> {
    @Modifying
    void deleteByHarvestPermitArea(HarvestPermitArea harvestPermitArea);
}
