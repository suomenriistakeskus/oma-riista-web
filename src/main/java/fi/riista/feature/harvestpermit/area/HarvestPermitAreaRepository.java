package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Optional;

public interface HarvestPermitAreaRepository extends BaseRepository<HarvestPermitArea, Long> {
    Optional<HarvestPermitArea> findByExternalId(final String externalId);
}
