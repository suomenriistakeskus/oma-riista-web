package fi.riista.feature.permit.application.dogevent.summary;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.dogevent.DogEventApplication;
import fi.riista.feature.permit.application.dogevent.DogEventApplicationRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbance;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContactRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceRepository;
import fi.riista.feature.permit.application.dogevent.DogEventUnleash;
import fi.riista.feature.permit.application.dogevent.DogEventUnleashRepository;
import fi.riista.feature.permit.application.dogevent.disturbance.DogEventDisturbanceDTO;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TEST;
import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TRAINING;

@Service
public class DogEventSummaryFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService authorizationService;

    @Resource
    private DogEventApplicationRepository dogEventApplicationRepository;

    @Resource
    private DogEventUnleashRepository unleashRepository;

    @Resource
    private DogEventDisturbanceRepository disturbanceRepository;

    @Resource
    private DogEventDisturbanceContactRepository disturbanceContactRepository;

    @Transactional(readOnly = true)
    public DogEventUnleashSummaryDTO readUnleashDetails(final long applicationId, final Locale locale) {

        final HarvestPermitApplication application = authorizationService.readApplication(applicationId);
        final DogEventApplication dogEventApplication = dogEventApplicationRepository.findByHarvestPermitApplication(application);
        final List<DogEventUnleash> dogEvents = unleashRepository.findAllByHarvestPermitApplication(application);

        return DogEventUnleashSummaryDTO.create(application, dogEventApplication, dogEvents);
    }

    @Transactional(readOnly = true)
    public DogEventDisturbanceSummaryDTO readDisturbanceDetails(final long applicationId, final Locale locale) {

        final HarvestPermitApplication application = authorizationService.readApplication(applicationId);
        final DogEventApplication dogEventApplication = dogEventApplicationRepository.findByHarvestPermitApplication(application);
        final DogEventDisturbance trainingEvent = disturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TRAINING);
        final DogEventDisturbance testEvent = disturbanceRepository.findByHarvestPermitApplicationAndEventType(application, DOG_TEST);

        return DogEventDisturbanceSummaryDTO.create(
                application,
                dogEventApplication,
                createDtoFromEvent(trainingEvent),
                createDtoFromEvent(testEvent));
    }

    private DogEventDisturbanceDTO createDtoFromEvent(final DogEventDisturbance event) {
        return DogEventDisturbanceDTO.createFrom(
                event,
                disturbanceContactRepository.findAllByEvent(event),
                F.mapNullable(event.getGameSpecies(), GameSpecies::getOfficialCode));
    }

}
