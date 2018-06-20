package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import java.util.List;
import java.util.Optional;

public interface RhyAnnualStatisticsRepository extends BaseRepository<RhyAnnualStatistics, Long> {

    Optional<RhyAnnualStatistics> findByRhyAndYear(Riistanhoitoyhdistys rhy, int calendarYear);

    List<RhyAnnualStatistics> findByYear(int calendarYear);

}
