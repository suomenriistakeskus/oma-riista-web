package fi.riista.feature.harvestpermit;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface HarvestPermitSpeciesAmountRepositoryCustom {

    Map<Integer, HarvestPermitSpeciesAmount> findSpeciesCodeToSpeciesAmountByHarvestPermit(final HarvestPermit permit);

    Map<Long, Set<HarvestPermitSpeciesAmount>> findAllByPermitId(Collection<HarvestPermit> harvestPermits);
}
