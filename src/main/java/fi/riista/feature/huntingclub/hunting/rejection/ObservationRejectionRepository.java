package fi.riista.feature.huntingclub.hunting.rejection;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ObservationRejectionRepository extends BaseRepository<ObservationRejection, Long> {

    @Modifying
    @Query("DELETE FROM ObservationRejection r WHERE r.group = ?1 and r.observation = ?2")
    void deleteByGroup(HuntingClubGroup group, Observation observation);

    Optional<ObservationRejection> findByGroupAndObservation(HuntingClubGroup group, Observation observation);

    List<ObservationRejection> findByGroup(HuntingClubGroup group);
}
