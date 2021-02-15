package fi.riista.feature.gamediary.observation;

import fi.riista.feature.gamediary.GameDiaryEntryFeatureTest;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Resource;
import java.util.List;

public abstract class ObservationFeatureTestBase extends GameDiaryEntryFeatureTest {

    @Resource
    protected ObservationFeature feature;

    @Resource
    protected ObservationSpecimenRepository observationSpecimenRepo;

    protected ObservationDTO invokeCreateObservation(final ObservationDTO input) {
        return withVersionChecked(feature.createObservation(input));
    }

    protected ObservationDTO invokeUpdateObservation(final ObservationDTO input) {
        return withVersionChecked(feature.updateObservation(input));
    }

    protected ObservationDTO withVersionChecked(final ObservationDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Observation.class);
    }

    protected List<ObservationSpecimen> findSpecimens(final Observation observation) {
        return observationSpecimenRepo.findByObservation(observation, JpaSort.of(ObservationSpecimen_.id));
    }
}
