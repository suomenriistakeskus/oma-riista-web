package fi.riista.feature.shootingtest.statistics;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import java.util.Locale;

@Service
public class ShootingTestStatisticsFeature {

    @Resource
    private ShootingTestStatisticsService statisticsService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public View exportStatisticsToExcel(final long rhyId, final int calendarYear, final Locale locale) {
        final Riistanhoitoyhdistys rhy = requireRhy(rhyId);
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final ShootingTestStatisticsDTO dto = statisticsService.calculate(rhy, calendarYear);

        return new ShootingTestExcelView(localiser, calendarYear, rhy.getNameLocalisation(), dto);
    }

    @Transactional(readOnly = true)
    public ShootingTestStatisticsDTO getStatistics(final long rhyId, final int calendarYear) {
        return statisticsService.calculate(requireRhy(rhyId), calendarYear);
    }

    private Riistanhoitoyhdistys requireRhy(final long rhyId) {
        return requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.VIEW_SHOOTING_TEST_EVENTS);
    }
}
