package fi.riista.feature.permit.area.mml;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface HarvestPermitAreaMmlRepository extends BaseRepository<HarvestPermitAreaMml, Long> {

    @Modifying
    @Query(value = "DELETE FROM HarvestPermitAreaMml mml WHERE mml.harvestPermitArea = ?1")
    void deleteByHarvestPermitArea(HarvestPermitArea harvestPermitArea);
}
