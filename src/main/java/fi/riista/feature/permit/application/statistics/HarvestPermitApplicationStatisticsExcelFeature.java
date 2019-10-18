package fi.riista.feature.permit.application.statistics;

import fi.riista.feature.common.EnumLocaliser;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Component
public class HarvestPermitApplicationStatisticsExcelFeature {

    @Resource
    private MessageSource messageSource;

    @Resource
    private HarvestPermitApplicationStatisticsFeature harvestPermitApplicationStatisticsFeature;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public HarvestPermitApplicationStatisticsExcelView export(int year) {
        final Locale locale = LocaleContextHolder.getLocale();
        final List<HarvestPermitApplicationStatusTableDTO> harvestPermitApplicationStatusTableDTOS =
                harvestPermitApplicationStatisticsFeature.statusTable(year);

        return new HarvestPermitApplicationStatisticsExcelView(
                new EnumLocaliser(messageSource, locale),
                harvestPermitApplicationStatusTableDTOS);

    }
}
