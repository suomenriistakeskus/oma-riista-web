package fi.riista.feature.organization.rhy.annualreport;

import fi.riista.feature.common.EnumLocaliser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;

@Component
public class RhyAnnualReportFeature {

    @Resource
    private RhyAnnualReportService reportService;

    public byte[] createAnnualReport(final long rhyId, final int year, final Locale locale, final EnumLocaliser localiser) {
        final RhyAnnualReportDTO reportDTO = reportService.getOrCreateRhyAnnualReport(rhyId, year, locale);
        return reportService.createAnnualReport(reportDTO, locale, localiser);
    }

}
