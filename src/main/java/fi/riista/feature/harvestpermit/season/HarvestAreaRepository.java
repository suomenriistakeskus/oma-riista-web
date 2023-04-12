package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.season.HarvestArea;

import java.util.Collection;
import java.util.List;

public interface HarvestAreaRepository extends BaseRepository<HarvestArea, Long> {

    List<HarvestArea> findByTypeIn(Collection<HarvestArea.HarvestAreaType> types);
    HarvestArea findByTypeAndOfficialCode(HarvestArea.HarvestAreaType type, String officialCode);
}
