package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface GameDamageInspectionEventRepository extends BaseRepository<GameDamageInspectionEvent, Long> {
    List<GameDamageInspectionEvent> findByRhyIdAndDateBetweenOrderByDateDesc(final Long orgId, final Date startDate, final Date endDate);

    List<GameDamageInspectionEvent> findByRhyIdAndDateBetweenAndGameSpeciesOfficialCodeIn(final Long rhyId, final Date startDate, final Date endDate, final Set<Integer> officialCodes);

    List<GameDamageInspectionEvent> findByDateBetweenAndGameSpeciesOfficialCodeIn(final Date startDate, final Date endDate, final Set<Integer> officialCodes);
}
