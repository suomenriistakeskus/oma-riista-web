package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Component
public class GameDamageInspectionEventDTOTransformer extends ListTransformer<GameDamageInspectionEvent, GameDamageInspectionEventDTO> {
    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private GameDamageInspectionKmExpensesRepository gameDamageInspectionKmExpensesRepository;

    @Override
    protected List<GameDamageInspectionEventDTO> transform(@Nonnull final List<GameDamageInspectionEvent> events) {
        final Function<GameDamageInspectionEvent, Riistanhoitoyhdistys> eventToRhy = getEventToRhyMapping(events);
        final Map<Long, List<GameDamageInspectionKmExpenseDTO>> eventIdToKmExpenses = getEventIdToKmExpensesMapping(events);

        return events.stream()
                .map(event -> {
                    final Riistanhoitoyhdistys rhy = eventToRhy.apply(event);

                    return GameDamageInspectionEventDTO.create(event, rhy, eventIdToKmExpenses.get(event.getId()));
                })
                .collect(toList());
    }

    @Nonnull
    private Function<GameDamageInspectionEvent, Riistanhoitoyhdistys> getEventToRhyMapping(final Iterable<GameDamageInspectionEvent> events) {
        return singleQueryFunction(events, GameDamageInspectionEvent::getRhy, rhyRepository, true);
    }

    private Map<Long, List<GameDamageInspectionKmExpenseDTO>> getEventIdToKmExpensesMapping(final List<GameDamageInspectionEvent> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<GameDamageInspectionKmExpense> expenses =
                gameDamageInspectionKmExpensesRepository.findByGameDamageInspectionEventIn(events);

        return expenses.stream()
                .collect(groupingBy(e -> e.getGameDamageInspectionEvent().getId(),
                        mapping(e -> GameDamageInspectionKmExpenseDTO.create(e), toList())));
    }
}
