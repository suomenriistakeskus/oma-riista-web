package fi.riista.feature.permit.application.dogevent.unleash;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.dogevent.DogEventUnleash;
import fi.riista.feature.permit.application.dogevent.DogEventUnleashRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class DogEventUnleashFeature {

    @Resource
    private DogEventUnleashRepository dogEventUnleashRepository;

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Transactional(readOnly = true)
    public List<DogEventUnleashDTO> getEvents(final long applicationId) {
        final HarvestPermitApplication harvestPermitApplication =
                harvestPermitApplicationAuthorizationService.readApplication(applicationId);
        return dogEventUnleashRepository.findAllByHarvestPermitApplication(harvestPermitApplication)
                .stream()
                .map(DogEventUnleashDTO::createFrom)
                .collect(toList());
    }

    @Transactional
    public DogEventUnleashDTO updateEvent(
            final long applicationId,
            final DogEventUnleashDTO event) {

        final HarvestPermitApplication harvestPermitApplication =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);
        final DogEventUnleashDTO savedEvent;

        if (event.getId() == null) {
            savedEvent = addEvent(harvestPermitApplication, event);
        } else {
            updateEvent(event);
            savedEvent = event;
        }

        return savedEvent;
    }

    @Transactional
    public void deleteEvent(final long applicationId, final long eventId) {
        // Check first that can user update the application
        harvestPermitApplicationAuthorizationService.updateApplication(applicationId);
        dogEventUnleashRepository.deleteById(eventId);
    }

    private DogEventUnleashDTO addEvent(
            final HarvestPermitApplication harvestPermitApplication,
            final DogEventUnleashDTO dto) {

        final DogEventUnleash entity = new DogEventUnleash();
        entity.setHarvestPermitApplication(harvestPermitApplication);
        updateMutableEventValues(entity, dto);
        dogEventUnleashRepository.save(entity);
        return DogEventUnleashDTO.createFrom(entity);
    }

    private void updateEvent(final DogEventUnleashDTO dto) {
        dogEventUnleashRepository.findById(dto.getId())
                .ifPresent(entity -> updateMutableEventValues(entity, dto));
    }

    private void updateMutableEventValues(final DogEventUnleash entity,
                                          final DogEventUnleashDTO dto) {

        entity.setEventType(dto.getEventType());
        entity.setBeginDate(dto.getBeginDate());
        entity.setEndDate(dto.getEndDate());
        entity.setDogsAmount(dto.getDogsAmount());
        entity.setNaturaArea(dto.getNaturaArea());
        entity.setEventDescription(dto.getEventDescription());
        entity.setLocationDescription(dto.getLocationDescription());
        entity.setContactName(dto.getContactName());
        entity.setContactMail(dto.getContactMail());
        entity.setContactPhone(dto.getContactPhone());
        entity.setAdditionalInfo(dto.getAdditionalInfo());
        entity.setGeoLocation(dto.getGeoLocation());
    }
}
