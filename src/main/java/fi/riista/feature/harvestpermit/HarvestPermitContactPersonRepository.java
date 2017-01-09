package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import java.util.List;

public interface HarvestPermitContactPersonRepository extends BaseRepository<HarvestPermitContactPerson, Long> {

    List<HarvestPermitContactPerson> findByHarvestPermit(HarvestPermit permit);

}
