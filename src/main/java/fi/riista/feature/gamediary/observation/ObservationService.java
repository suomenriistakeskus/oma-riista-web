package fi.riista.feature.gamediary.observation;

import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class ObservationService {

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private ObservationSpecimenService observationSpecimenService;

    @Resource
    private ObservationUpdateService observationUpdateService;

    @Resource
    private GameDiaryImageService gameDiaryImageService;



    @Resource
    private DeletedObservationRepository deletedObservationRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteObservation(final Observation observation) {

        final ObservationLockInfo lockInfo =
                observationUpdateService.getObservationLockInfo(observation, ObservationSpecVersion.MOST_RECENT);

        final DeletedObservation deletedObservation =
                new DeletedObservation(F.getId(observation), F.getId(observation.getAuthor()), F.getId(observation.getObserver()));
        deletedObservationRepository.save(deletedObservation);

        observationSpecimenService.deleteAllSpecimens(observation);
        gameDiaryImageService.deleteGameDiaryImages(observation);
        observationUpdateService.deleteObservation(observation, lockInfo);
    }
}
