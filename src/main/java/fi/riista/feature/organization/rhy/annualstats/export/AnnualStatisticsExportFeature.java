package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.config.Constants;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsRepository;
import fi.riista.util.ContentDispositionUtil;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.feature.organization.RiistakeskusAuthorization.Permission.LIST_ANNUAL_STATISTICS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExcelLayout.NORMAL;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExcelLayout.TRANSPOSED_WITH_MULTIPLE_SHEETS;
import static fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExcelLayout.WITH_RKA_GROUPING;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.util.Collect.indexingBy;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.MediaTypeExtras.APPLICATION_PDF;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Service
public class AnnualStatisticsExportFeature {

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private RhyAnnualStatisticsRepository annualStatisticsRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private AnnualStatisticItemGroupFactory statisticsGroupFactory;

    @Resource
    private AnnualStatisticsPdfCreator pdfCreator;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public AnnualStatisticsExcelView exportAnnualStatisticsToExcel(final long annualStatisticsId, final Locale locale) {
        final RhyAnnualStatistics annualStatistics =
                requireEntityService.requireRhyAnnualStatistics(annualStatisticsId, READ);
        final Riistanhoitoyhdistys rhy = annualStatistics.getRhy();

        final AnnualStatisticsExportItemDTO dto =
                AnnualStatisticsExportItemDTO.create(rhy, rhy.getParentOrganisation(), annualStatistics);

        return createExcelView(annualStatistics.getYear(), singletonList(dto), TRANSPOSED_WITH_MULTIPLE_SHEETS, locale);
    }

    @Transactional(readOnly = true)
    public AnnualStatisticsExcelView exportAllAnnualStatistics(final int year,
                                                               final Locale locale,
                                                               final boolean groupByRka) {

        requireEntityService.requireRiistakeskus(LIST_ANNUAL_STATISTICS);

        final Map<Riistanhoitoyhdistys, RhyAnnualStatistics> indexByRhy =
                annualStatisticsRepository.findByYear(year).stream().collect(indexingBy(a -> a.getRhy()));

        final List<AnnualStatisticsExportItemDTO> list = rhyRepository
                .findAll(fetch(Organisation_.parentOrganisation))
                .stream()
                .map(rhy -> {
                    return AnnualStatisticsExportItemDTO.create(
                            rhy,
                            rhy.getParentOrganisation(),
                            indexByRhy.computeIfAbsent(rhy, r -> new RhyAnnualStatistics(rhy, year)));
                })
                .sorted(comparing(AnnualStatisticsExportItemDTO::getOrganisationCode))
                .collect(toList());

        return createExcelView(year, list, groupByRka ? WITH_RKA_GROUPING : NORMAL, locale);
    }

    private AnnualStatisticsExcelView createExcelView(final int year,
                                                      final List<AnnualStatisticsExportItemDTO> list,
                                                      final AnnualStatisticsExcelLayout layout,
                                                      final Locale locale) {

        final boolean includeIban = layout == AnnualStatisticsExcelLayout.TRANSPOSED_WITH_MULTIPLE_SHEETS;

        return new AnnualStatisticsExcelView(
                year, list, statisticsGroupFactory.getAllGroups(includeIban), getLocaliser(locale), layout);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportAnnualStatisticsToPdf(final long annualStatisticsId, final Locale locale) {
        final RhyAnnualStatistics annualStatistics =
                requireEntityService.requireRhyAnnualStatistics(annualStatisticsId, READ);
        final Riistanhoitoyhdistys rhy = annualStatistics.getRhy();
        final int year = annualStatistics.getYear();

        final AnnualStatisticsExportItemDTO dto =
                AnnualStatisticsExportItemDTO.create(rhy, rhy.getParentOrganisation(), annualStatistics);

        final String filename = getPdfFilename(rhy, year, locale);
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
