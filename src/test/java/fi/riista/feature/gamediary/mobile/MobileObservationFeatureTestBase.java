package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.GameDiaryEntryFeatureTest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen_;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Resource;
import java.util.List;

public class MobileObservationFeatureTestBase extends GameDiaryEntryFeatureTest {

    @Resource
    protected MobileObservationFeature feature;

    @Resource
    protected ObservationRepository observationRepo;

    @Resource
    protected ObservationSpecimenRepository observationSpecimenRepo;

    protected MobileObservationDTO invokeCreateObservation(final MobileObservationDTO input) {
        return withVersionChecked(feature.createObservation(input));
    }

    protected MobileObservationDTO invokeUpdateObservation(final MobileObservationDTO input) {
        return withVersionChecked(feature.updateObservation(input));
    }

    protected MobileObservationDTO withVersionChecked(final MobileObservationDTO dto) {
        return checkDtoVersionAgainstEntity(dto, Observation.class);
    }

    protected List<ObservationSpecimen> findSpecimens(final Observation observation) {
        return observationSpecimenRepo.findByObservation(observation, JpaSort.of(ObservationSpecimen_.id));
    }
}
