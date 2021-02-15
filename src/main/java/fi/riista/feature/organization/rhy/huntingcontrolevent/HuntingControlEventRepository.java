package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.Organisation;
import org.joda.time.LocalDate;

import java.util.List;

public interface HuntingControlEventRepository extends BaseRepository<HuntingControlEvent, Long> {
    List<HuntingControlEvent> findByRhyIdAndDateBetweenOrderByDateDesc(final Long orgId, final LocalDate startDate, final LocalDate endDate);
    List<HuntingControlEvent> findByRhyAndDateBetweenOrderByDateDesc(final Organisation org, final LocalDate startDate, final LocalDate endDate);
}
