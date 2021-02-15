package fi.riista.feature.harvestpermit.nestremoval;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;

import java.util.Collection;
import java.util.List;

public interface HarvestPermitNestRemovalUsageRepository extends BaseRepository<HarvestPermitNestRemovalUsage, Long> {

    List<HarvestPermitNestRemovalUsage> findByHarvestPermitSpeciesAmount(final HarvestPermitSpeciesAmount speciesAmount);
    List<HarvestPermitNestRemovalUsage> findByHarvestPermitSpeciesAmountIn(final Collection<HarvestPermitSpeciesAmount> speciesAmounts);
}
