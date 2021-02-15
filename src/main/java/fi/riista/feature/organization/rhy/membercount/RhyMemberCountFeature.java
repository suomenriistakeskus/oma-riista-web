package fi.riista.feature.organization.rhy.membercount;

import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsRepository;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static fi.riista.util.Collect.indexingByIdOf;
import static fi.riista.util.DateUtil.today;

@Service
public class RhyMemberCountFeature {

    private static final Logger LOG = LoggerFactory.getLogger(RhyMemberCountFeature.class);

    @Resource
    private RhyAnnualStatisticsRepository statisticsRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void updateMemberCounts() {
        final LocalDate today = today();

        // Only update member counts at the beginning of the year
        if (today.getMonthOfYear() == 1 && today.getDayOfMonth() <= 16) {
            LOG.info("Updating member counts for rhy annual statistics...");
            final int statisticsYear = today.getYear() - 1;

            final Map<Long, RhyAnnualStatistics> statsIndexByRhyId = statisticsRepository
                    .findByYear(statisticsYear)
                    .stream()
                    .collect(indexingByIdOf(RhyAnnualStatistics::getRhy));

            final Map<Long, Integer> memberCountsByRhyId =
                    rhyRepository.calculateMemberCountsForStatistics(statisticsYear);

            final HashSet<RhyAnnualStatistics> addedStatistics = new HashSet<>();
            final AtomicInteger updateCount = new AtomicInteger(0);

            rhyRepository.findAll().forEach(rhy -> {
                final int memberCount = memberCountsByRhyId.getOrDefault(rhy.getId(), 0);
                RhyAnnualStatistics statistics = statsIndexByRhyId.get(rhy.getId());

                if (statistics == null && memberCount > 0) {
                    statistics = new RhyAnnualStatistics(rhy, statisticsYear);
                    addedStatistics.add(statistics);
                }

                if (statistics != null || memberCount > 0) {
                    statistics.getOrCreateBasicInfo().setRhyMembers(memberCount);
                    updateCount.getAndIncrement();
                }
            });

            statisticsRepository.saveAll(addedStatistics);

            LOG.info("Updated member counts for {} rhys. Created new statistics for {} rhys.",
                    updateCount.get(),
                    addedStatistics.size());
        } else {
            LOG.info("Not running member count update on {}.", today);
        }
    }
}
