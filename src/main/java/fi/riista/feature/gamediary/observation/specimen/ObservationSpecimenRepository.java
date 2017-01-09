package fi.riista.feature.gamediary.observation.specimen;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.observation.Observation;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ObservationSpecimenRepository extends BaseRepository<ObservationSpecimen, Long> {

    List<ObservationSpecimen> findByObservation(Observation observation);

    List<ObservationSpecimen> findByObservation(Observation observation, Sort sort);

}
