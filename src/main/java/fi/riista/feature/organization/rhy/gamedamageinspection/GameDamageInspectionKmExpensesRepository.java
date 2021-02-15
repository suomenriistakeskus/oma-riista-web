package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Collection;
import java.util.List;

public interface GameDamageInspectionKmExpensesRepository extends BaseRepository<GameDamageInspectionKmExpense, Long> {

    List<GameDamageInspectionKmExpense> findByGameDamageInspectionEventIn(final Collection<GameDamageInspectionEvent> events);
    void deleteByGameDamageInspectionEvent(final GameDamageInspectionEvent event);
}
