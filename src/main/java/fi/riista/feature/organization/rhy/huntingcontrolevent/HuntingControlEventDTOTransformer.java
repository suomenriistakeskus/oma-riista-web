package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.stream.Collectors.*;

@Component
public class HuntingControlEventDTOTransformer extends ListTransformer<HuntingControlEvent, HuntingControlEventDTO> {
    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource HuntingControlAttachmentRepository attachmentRepository;

    @Nonnull
    @Override
    protected List<HuntingControlEventDTO> transform(@Nonnull List<HuntingControlEvent> events) {
        final Function<HuntingControlEvent, Riistanhoitoyhdistys> eventToRhy = getEventToRhyMapping(events);
        final Map<HuntingControlEvent, List<HuntingControlAttachmentDTO>> eventToAttachment =
                getEventToAttachmentDtoMapping(events);

        return events.stream()
                .map(event -> {
                    final Riistanhoitoyhdistys rhy = eventToRhy.apply(event);

                    return HuntingControlEventDTO.create(event, rhy, eventToAttachment.get(event));
                })
                .collect(toList());
    }

    @Nonnull
    private Function<HuntingControlEvent, Riistanhoitoyhdistys> getEventToRhyMapping(final Iterable<HuntingControlEvent> events) {
        return singleQueryFunction(events, HuntingControlEvent::getRhy, rhyRepository, true);
    }

    private Map<HuntingControlEvent, List<HuntingControlAttachmentDTO>> getEventToAttachmentDtoMapping(final List<HuntingControlEvent> events) {
        return attachmentRepository.findByHuntingControlEventIn(events).stream()
                .collect(groupingBy(HuntingControlAttachment::getHuntingControlEvent,
                        mapping(HuntingControlAttachmentDTO::create, toList())));
    }
}
