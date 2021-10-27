package fi.riista.feature.huntingclub.area;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HuntingClubAreaRepository extends BaseRepository<HuntingClubArea, Long>, HuntingClubAreaRepositoryCustom {
    @Query("select distinct o.huntingYear FROM #{#entityName} o WHERE o.club= ?1")
    List<Integer> listHuntingYears(HuntingClub club);

    Optional<HuntingClubArea> findByExternalId(String externalId);

    // Simple workaround as Hibernate does not like void as return value.
    @Query(value = "SELECT COUNT(*) FROM (SELECT calculate_zone_changes(zone_id) FROM hunting_club_area WHERE hunting_club_area_id = :id) AS tmp", nativeQuery = true)
    int calculateZoneChanges(@Param("id") long clubAreaId);
}