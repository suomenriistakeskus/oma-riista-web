package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class HuntingControlEventDTOTransformerHelper {

    @Resource
    private HuntingControlAttachmentRepository attachmentRepository;

    @Resource
    private HuntingControlEventRepository eventRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private HuntingControlEventChangeRepository changeRepository;

    @Resource
    private UserRepository userRepository;

    public Map<HuntingControlEvent, Set<Person>> getEventToInspectorsMap(
            @Nonnull final List<HuntingControlEvent> events) {

        requireNonNull(events);
        final Map<Long, Set<Long>> eventToInspectors = eventRepository.mapInspectorPersonIdsByEventId(events);
        final Set<Long> uniquePersonIds = eventToInspectors.values()
                .stream()
                .flatMap(Set::stream)
                .collect(toSet());
        final Map<Long, Person> persons = F.indexById(personRepository.findAllById(uniquePersonIds));

        return events.stream()
                .collect(toMap(
                        event -> event,
                        event -> eventToInspectors.getOrDefault(event.getId(), Collections.emptySet())
                                .stream()
                                .map(personId -> persons.get(personId))
                                .collect(toSet())));
    }

    public Map<HuntingControlEvent, Set<HuntingControlCooperationType>> getEventToCooperationsMap(
            @Nonnull final List<HuntingControlEvent> events) {

        requireNonNull(events);
        final Map<Long, Set<HuntingControlCooperationType>> eventToCooperations =
                eventRepository.mapCooperationTypesByEventId(events);

        return events.stream()
                .collect(toMap(
                        event -> event,
                        event -> eventToCooperations.getOrDefault(event.getId(), Collections.emptySet())));
    }

    public List<HuntingControlEventChange> listChanges(@Nonnull final List<HuntingControlEvent> events) {
        requireNonNull(events);
        return changeRepository.findAllByHuntingControlEventIn(events);
    }

    public Map<Long, SystemUser> getChangeUserIdToUserMap(@Nonnull final List<HuntingControlEventChange> changes) {
        requireNonNull(changes);

        final Set<Long> uniqueModifiers = changes.stream()
                .map(HuntingControlEventChange::getChangeHistory)
                .filter(Objects::nonNull)
                .map(ChangeHistory::getUserId)
                .collect(toSet());

        return F.indexById(userRepository.findAllById(uniqueModifiers));
    }

    public List<HuntingControlAttachment> listAttachments(@Nonnull final List<HuntingControlEvent> events) {
        requireNonNull(events);
        return attachmentRepository.findByHuntingControlEventIn(events);
    }

    public Map<HuntingControlEvent, List<HuntingControlAttachment>> getEventToAttachmentsMap(
            @Nonnull final Collection<HuntingControlEvent> events) {

        requireNonNull(events);
        return attachmentRepository.findByHuntingControlEventIn(events).stream()
                .collect(groupingBy(HuntingControlAttachment::getHuntingControlEvent,
                                    mapping(attachment -> attachment, toList())));
    }
}
