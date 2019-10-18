package fi.riista.feature.organization.rhy.annualstats.statechange;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;

import java.util.Optional;

public interface RhyAnnualStatisticsStateChangeEventRepository
        extends BaseRepository<RhyAnnualStatisticsStateChangeEvent, Long> {

    Optional<RhyAnnualStatisticsStateChangeEvent> findFirstByStatisticsAndStateOrderByEventTimeDesc(RhyAnnualStatistics statistics,
                                                                                                    RhyAnnualStatisticsState state);

}
