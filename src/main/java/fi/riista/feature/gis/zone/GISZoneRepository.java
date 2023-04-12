package fi.riista.feature.gis.zone;

import fi.riista.feature.common.repository.BaseRepository;
import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface GISZoneRepository extends BaseRepository<GISZone, Long>, GISZoneRepositoryCustom {

    @Modifying
    @Query("UPDATE #{#entityName} a SET a.status = :newStatus  WHERE a.status = 'PROCESSING' AND a.statusTime < :processingSince")
    void updateTooLongProcessedZonesStatus(
            @Param("processingSince") DateTime processingSince,
            @Param("newStatus") GISZone.StatusCode statusCode);


    @Query("SELECT a.id FROM #{#entityName} a WHERE a.status = 'PENDING'")
    List<Long> findInStatusPending();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("SELECT a FROM #{#entityName} a where a.id = :zoneId")
    Optional<GISZone> findOneWithLock(@Param("zoneId") final long zoneId);

}
