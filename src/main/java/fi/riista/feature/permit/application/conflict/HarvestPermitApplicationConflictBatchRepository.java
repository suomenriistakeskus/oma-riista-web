package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;

public interface HarvestPermitApplicationConflictBatchRepository extends BaseRepository<HarvestPermitApplicationConflictBatch, Long> {

    @Query("SELECT MAX(b.id) FROM HarvestPermitApplicationConflictBatch b WHERE b.completedAt IS NOT NULL")
    Long findLatestCompletedBatchId();

}
