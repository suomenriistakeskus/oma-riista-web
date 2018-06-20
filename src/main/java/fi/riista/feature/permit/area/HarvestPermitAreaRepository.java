package fi.riista.feature.permit.area;

import fi.riista.feature.common.repository.BaseRepository;
import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HarvestPermitAreaRepository extends BaseRepository<HarvestPermitArea, Long>, HarvestPermitAreaRepositoryCustom {

    Optional<HarvestPermitArea> findByExternalId(String externalId);

    @Query("SELECT a.id FROM #{#entityName} a WHERE a.status = 'PENDING' OR (a.status = 'PROCESSING' AND a.statusTime < ?1)")
    List<Long> findInStatusPendingOrProcessingTooLong(DateTime statusTime, Pageable pageRequest);
}
