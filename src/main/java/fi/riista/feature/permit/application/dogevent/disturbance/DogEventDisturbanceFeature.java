package fi.riista.feature.permit.application.dogevent.disturbance;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbance;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContact;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContactRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceRepository;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceValidator;
import fi.riista.feature.permit.application.dogevent.DogEventType;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
public class DogEventDisturbanceFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService authorizationService;

    @Resource
    private DogEventDisturbanceRepository eventRepository;

    @Resource
    private DogEventDisturbanceContactRepository eventContactRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Transactional(readOnly = true)
    public DogEventDisturbanceDTO getEvent(final long applicationId, final DogEventType eventType) {

        final HarvestPermitApplication application = authorizationService.readApplication(applicationId);
        return Optional.ofNullable(eventRepository.findByHarvestPermitApplicationAndEventType(application, eventType))
                .map(event -> DogEventDisturbanceDTO.createFrom(
                        event,
                        eventContactRepository.findAllByEvent(event),
                        Optional.ofNullable(event.getGameSpecies()).map(GameSpecies::getOfficialCode).orElse(null)))
                .orElse(null);
    }

    @Transactional
    public void updateEvent(final long applicationId,
                            final DogEventDisturbanceDTO eventDto) {

        final HarvestPermitApplication application = authorizationService.updateApplication(applicationId);
        if (eventDto.getId() == null) {
            addEvent(application, eventDto);
        } else {
            updateEvent(eventDto);
        }
    }

    private void addEvent(final HarvestPermitApplication application,
                          final DogEventDisturbanceDTO dto) {

        final DogEventDisturbance entity = new DogEventDisturbance();
        entity.setHarvestPermitApplication(application);
        updateMutableEventValues(entity, dto);
        eventRepository.save(entity);
        Optional.ofNullable(dto.getContacts()).ifPresent(contacts ->
                contacts.forEach(contact -> addContact(entity, contact))
        );
    }

    private void updateMutableEventValues(final DogEventDisturbance entity,
                                          final DogEventDisturbanceDTO dto) {

        entity.setEventType(dto.getEventType());
        entity.setSkipped(dto.isSkipped());
        entity.setGameSpecies(F.mapNullable(dto.getSpeciesCode(), gameSpeciesService::requireByOfficialCode));
        entity.setDogsAmount(dto.getDogsAmount());
        entity.setBeginDate(dto.getBeginDate());
        entity.setEndDate(dto.getEndDate());
        entity.setEventDescription(dto.getEventDescription());
        DogEventDisturbanceValidator.validateContent(entity);
    }

    private void addContact(final DogEventDisturbance event,
                            final DogEventDisturbanceContactDTO dto) {

        final DogEventDisturbanceContact entity = new DogEventDisturbanceContact();
        entity.setEvent(event);
        updateMutableContactValues(entity, dto);
        eventContactRepository.save(entity);
    }

    private void updateMutableContactValues(final DogEventDisturbanceContact entity,
                                            final DogEventDisturbanceContactDTO dto) {

        entity.setContactName(dto.getName());
        entity.setContactMail(dto.getMail());
        entity.setContactPhone(dto.getPhone());
    }

    private void updateEvent(final DogEventDisturbanceDTO dto) {

        final DogEventDisturbance entity = eventRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("DogEventCarnivore not found, id:" + dto.getId()));
        updateMutableEventValues(entity, dto);
        updateContacts(entity, Optional.ofNullable(dto.getContacts()).orElse(emptyList()));
    }

    private void updateContacts(final DogEventDisturbance event,
                                @Nonnull final List<DogEventDisturbanceContactDTO> updatedContactsDto) {

        // Read all contacts at once
        final Map<Long, DogEventDisturbanceContact> existingContactsById =
                F.indexById(eventContactRepository.findAllByEvent(event));

        final List<Long> updatedContactIds = F.mapNonNullsToList(updatedContactsDto, DogEventDisturbanceContactDTO::getId);

        // Delete removed contacts. Do this before creating new ones.
        existingContactsById.forEach((id, entity) -> {
            if (!updatedContactIds.contains(id)) {
                eventContactRepository.deleteById(id);
            }
        });

        // Add new contacts ...
        updatedContactsDto.forEach(dto -> {
            if (dto.getId() == null) {
                addContact(event, dto);
            } else {
                // ... and update existing contacts.
                final DogEventDisturbanceContact entity = Optional.ofNullable(existingContactsById.get(dto.getId()))
                        .orElseThrow(() -> new IllegalArgumentException("DogEventCarnivoreContact not found, id:" + dto.getId()));
                updateMutableContactValues(entity, dto);
            }
        });

    }

}
