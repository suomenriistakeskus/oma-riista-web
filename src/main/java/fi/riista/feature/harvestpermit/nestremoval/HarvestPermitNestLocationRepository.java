package fi.riista.feature.harvestpermit.nestremoval;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Collection;

public interface HarvestPermitNestLocationRepository extends BaseRepository<HarvestPermitNestLocation, Long> {

    void deleteByHarvestPermitNestRemovalUsageIn(final Collection<HarvestPermitNestRemovalUsage> usages);
}
