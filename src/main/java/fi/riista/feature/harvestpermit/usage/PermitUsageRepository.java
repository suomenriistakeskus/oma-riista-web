package fi.riista.feature.harvestpermit.usage;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;

import java.util.Collection;
import java.util.List;

public interface PermitUsageRepository extends BaseRepository<PermitUsage, Long> {

    List<PermitUsage> findByHarvestPermitSpeciesAmountIn(final Collection<HarvestPermitSpeciesAmount> amounts);
}
