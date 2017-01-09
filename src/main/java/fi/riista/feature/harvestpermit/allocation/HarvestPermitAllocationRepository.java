package fi.riista.feature.harvestpermit.allocation;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import org.springframework.data.jpa.repository.Modifying;

public interface HarvestPermitAllocationRepository extends BaseRepository<HarvestPermitAllocation, Long>,
        HarvestPermitAllocationRepositoryCustom {

    @Modifying
    void deleteByHarvestPermitAndGameSpecies(HarvestPermit permit, GameSpecies species);
}
