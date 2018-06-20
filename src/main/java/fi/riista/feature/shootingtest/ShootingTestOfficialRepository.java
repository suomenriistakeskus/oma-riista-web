package fi.riista.feature.shootingtest;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShootingTestOfficialRepository extends BaseRepository<ShootingTestOfficial, Long> {

    @Query("select o from #{#entityName} o" +
            " inner join fetch o.occupation occ" +
            " where o.shootingTestEvent = :event")
    List<ShootingTestOfficial> findByShootingTestEvent(@Param("event") ShootingTestEvent event);

    @Query("select o from #{#entityName} o" +
            " inner join fetch o.occupation occ" +
            " inner join fetch occ.person p" +
            " where o.shootingTestEvent IN (:events)" +
            " order by o.id asc")
    List<ShootingTestOfficial> findByShootingTestEventIn(@Param("events") Collection<ShootingTestEvent> events);

}
