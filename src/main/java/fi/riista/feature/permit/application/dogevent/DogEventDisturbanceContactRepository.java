package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface DogEventDisturbanceContactRepository extends BaseRepository<DogEventDisturbanceContact, Long> {

    List<DogEventDisturbanceContact> findAllByEvent(final DogEventDisturbance event);

    void deleteAllByEvent(final DogEventDisturbance event);
}
