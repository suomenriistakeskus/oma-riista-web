package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;

import java.util.List;
import java.util.Optional;

public interface ObservationRepository extends
        BaseRepository<Observation, Long>,
        ObservationRepositoryCustom {

    List<Observation> findByHuntingDayOfGroup(GroupHuntingDay day);

    List<Observation> findByObserver(Person person);

    Optional<Observation> findByAuthorAndMobileClientRefId(Person author, Long refId);

}
