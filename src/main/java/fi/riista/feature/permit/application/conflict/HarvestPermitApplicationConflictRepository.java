package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.common.repository.BaseRepository;

public interface HarvestPermitApplicationConflictRepository extends BaseRepository<HarvestPermitApplicationConflict, Long>,
        HarvestPermitApplicationConflictRepositoryCustom {
}
