package fi.riista.feature.harvestpermit.usage;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Collection;
import java.util.List;

public interface PermitUsageLocationRepository extends BaseRepository<PermitUsageLocation, Long> {

    List<PermitUsageLocation> findByPermitUsageIn(final Collection<PermitUsage> usages);
    void deleteByPermitUsage(final PermitUsage usage);
    void deleteByPermitUsageIn(final Collection<PermitUsage> usages);
}
