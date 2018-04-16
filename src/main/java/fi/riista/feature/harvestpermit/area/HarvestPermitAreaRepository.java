package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HarvestPermitAreaRepository extends BaseRepository<HarvestPermitArea, Long> {

    Optional<HarvestPermitArea> findByExternalId(String externalId);

    // TODO: Uncomment commented lines to enable hunting years for partners as well (not only owner).
    @Query("SELECT DISTINCT area.huntingYear " +
            "FROM #{#entityName} area " +
            //"LEFT JOIN area.partners p " +
            //"LEFT JOIN p.sourceArea ps " +
            "WHERE area.club = :club " +
            //"OR ps.club = :club " +
            "ORDER BY area.huntingYear ASC")
    List<Integer> listHuntingYears(@Param("club") HuntingClub club);

    @Query("SELECT a.id FROM #{#entityName} a WHERE a.status = 'PENDING' OR (a.status = 'PROCESSING' AND a.statusTime < ?1)")
    List<Long> findInStatusPendingOrProcessingTooLong(DateTime statusTime, Pageable pageRequest);
}
