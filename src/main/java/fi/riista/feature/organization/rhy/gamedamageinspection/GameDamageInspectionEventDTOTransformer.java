package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;

@Component
public class GameDamageInspectionEventDTOTransformer extends ListTransformer<GameDamageInspectionEvent, GameDamageInspectionEventDTO> {
    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Override
    protected List<GameDamageInspectionEventDTO> transform(@Nonnull final List<GameDamageInspectionEvent> events) {
        final Function<GameDamageInspectionEvent, Riistanhoitoyhdistys> eventToRhy = getEventToRhyMapping(events);

        return events.stream()
                .map(event -> {
                    final Riistanhoitoyhdistys rhy = eventToRhy.apply(event);

                    return GameDamageInspectionEventDTO.create(event, rhy);
                })
                .collect(Collectors.toList());
    }

    @Nonnull
    private Function<GameDamageInspectionEvent, Riistanhoitoyhdistys> getEventToRhyMapping(final Iterable<GameDamageInspectionEvent> events) {
        return singleQueryFunction(events, GameDamageInspectionEvent::getRhy, rhyRepository, true);
    }
}
