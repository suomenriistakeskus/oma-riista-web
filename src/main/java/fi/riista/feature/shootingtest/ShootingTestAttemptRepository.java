package fi.riista.feature.shootingtest;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShootingTestAttemptRepository extends BaseRepository<ShootingTestAttempt, Long> {

    List<ShootingTestAttempt> findByParticipant(ShootingTestParticipant participant);

    @Query("SELECT a FROM #{#entityName} a WHERE a.participant = :participant AND a.result != 'REBATED'")
    List<ShootingTestAttempt> findChargeableByParticipant(@Param("participant") ShootingTestParticipant participant);

}
