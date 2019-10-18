package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.config.Constants;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.util.ContentDispositionUtil;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static fi.riista.feature.organization.RiistakeskusAuthorization.Permission.LIST_ANNUAL_STATISTICS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExcelLayout.NORMAL;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExcelLayout.TRANSPOSED;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExcelLayout.WITH_RKA_GROUPING;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.MediaTypeExtras.APPLICATION_PDF;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

@Service
public class AnnualStatisticsExportFeature {

    @Resource
    private AnnualStatisticsExportService exportService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private AnnualStatisticsPdfCreator pdfCreator;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public AnnualStatisticsExcelView exportAnnualStatisticsToExcel(final long annualStatisticsId, final Locale locale) {
        final RhyAnnualStatistics annualStatistics =
                requireEntityService.requireRhyAnnualStatistics(annualStatisticsId, READ);

        final AnnualStatisticsExportDTO dto = exportService.export(annualStatistics);

        return createExcelView(annualStatistics.getYear(), singletonList(dto), TRANSPOSED, locale);
    }

    @Transactional(readOnly = true)
    public AnnualStatisticsExcelView exportAllAnnualStatistics(final int year,
                                                               final Locale locale,
                                                               final boolean groupByRka) {

        requireEntityService.requireRiistakeskus(LIST_ANNUAL_STATISTICS);

        final List<AnnualStatisticsExportDTO> exportedStatistics = exportService.exportAnnualStatistics(year);
        final AnnualStatisticsExcelLayout layout = groupByRka ? WITH_RKA_GROUPING : NORMAL;

        return createExcelView(year, exportedStatistics, layout, locale);
    }

    private AnnualStatisticsExcelView createExcelView(final int year,
                                                      final List<AnnualStatisticsExportDTO> list,
                                                      final AnnualStatisticsExcelLayout layout,
                                                      final Locale locale) {

        return new AnnualStatisticsExcelView(year, list, getLocaliser(locale), layout);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportAnnualStatisticsToPdf(final long annualStatisticsId, final Locale locale) {
        final RhyAnnualStatistics annualStatistics =
                requireEntityService.requireRhyAnnualStatistics(annualStatisticsId, READ);

        final AnnualStatisticsExportDTO dto = exportService.export(annualStatistics);
        final int year = annualStatistics.getYear();

        final String filename = getPdfFilename(annualStatistics.getRhy(), year, locale);
        final byte[] pdfBytes = pdfCreator.create(year, dto, locale);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(APPLICATION_PDF)
                .headers(ContentDispositionUtil.header(filename))
                .body(pdfBytes);
    }

    private String getPdfFilename(final Riistanhoitoyhdistys rhy, final int year, final Locale locale) {
        return format("%s-%d-%s-%s.pdf",
                getLocaliser(locale).getTranslation("annualStatistics"),
                year,
                rhy.getNameLocalisation().getAnyTranslation(locale).replaceAll(" ", "_"),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private EnumLocaliser getLocaliser(final Locale locale) {
        return new EnumLocaliser(messageSource, locale);
    }
}
