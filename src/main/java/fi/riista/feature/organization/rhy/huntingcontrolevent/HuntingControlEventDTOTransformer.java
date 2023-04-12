package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Component
public class HuntingControlEventDTOTransformer extends ListTransformer<HuntingControlEvent, HuntingControlEventDTO> {

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private HuntingControlEventDTOTransformerHelper helper;

    @Nonnull
    @Override
    protected List<HuntingControlEventDTO> transform(@Nonnull List<HuntingControlEvent> events) {

        requireNonNull(events);

        if (events.isEmpty()) {
            return emptyList();
        }

        final Function<HuntingControlEvent, Riistanhoitoyhdistys> rhyMapper = getEventToRhyMapping(events);
        final Map<HuntingControlEvent, Set<Person>> inspectors = helper.getEventToInspectorsMap(events);
        final Map<HuntingControlEvent, Set<HuntingControlCooperationType>> cooperations = helper.getEventToCooperationsMap(events);
        final Map<HuntingControlEvent, List<HuntingControlAttachmentDTO>> attachments = getEventToAttachmentMapping(events);
        final Map<HuntingControlEvent, List<ChangeHistoryDTO>> changes = getEventToChangesMapping(events);

        return events.stream()
                .map(event -> HuntingControlEventDTO.create(event,
                                                            rhyMapper.apply(event),
                                                            inspectors.get(event),
                                                            cooperations.get(event),
                                                            attachments.get(event),
                                                            changes.get(event)))
                .collect(toList());
    }

    @Nonnull
    private Function<HuntingControlEvent, Riistanhoitoyhdistys> getEventToRhyMapping(final Iterable<HuntingControlEvent> events) {
        return singleQueryFunction(events, HuntingControlEvent::getRhy, rhyRepository, true);
    }

    private Map<HuntingControlEvent, List<HuntingControlAttachmentDTO>> getEventToAttachmentMapping(final List<HuntingControlEvent> events) {
        return helper.listAttachments(events).stream()
                .collect(groupingBy(HuntingControlAttachment::getHuntingControlEvent,
                        mapping(HuntingControlAttachmentDTO::create, toList())));
    }

    private  Map<HuntingControlEvent, List<ChangeHistoryDTO>> getEventToChangesMapping(final List<HuntingControlEvent> events) {
        final List<HuntingControlEventChange> changes = helper.listChanges(events);
        final Map<Long, SystemUser> userMap = helper.getChangeUserIdToUserMap(changes);

        return changes.stream().collect(groupingBy(
                HuntingControlEventChange::getHuntingControlEvent,
                mapping(change -> ChangeHistoryDTO.create(change, userMap.get(change.getChangeHistory().getUserId())),toList())));
    }

}
