package fi.riista.api;

import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsCrudFeature;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsDTO;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsFeature;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsProgressDTO;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;
import static fi.riista.util.MediaTypeExtras.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys/annualstatistics", produces = APPLICATION_JSON_UTF8_VALUE)
public class RhyAnnualStatisticsApiResource {

    @Resource
    private RhyAnnualStatisticsFeature annualStatisticsFeature;

    @Resource
    private RhyAnnualStatisticsCrudFeature annualStatisticsCrudFeature;

    @Resource
    private AnnualStatisticsExportFeature annualStatisticsExportFeature;

    @PutMapping(value = "/{annualStatisticsId:\\d+}", consumes = APPLICATION_JSON_UTF8_VALUE)
    public RhyAnnualStatisticsDTO updateStatistics(final @PathVariable long annualStatisticsId,
                                                   final @RequestBody @Valid RhyAnnualStatisticsDTO dto) {

        dto.setId(annualStatisticsId);
        return annualStatisticsCrudFeature.update(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/year/{year:\\d+}/progress")
    public List<RhyAnnualStatisticsProgressDTO> getAnnualStatisticsProgress(final @PathVariable int year) {
        return annualStatisticsFeature.getAnnualStatisticsProgress(year);
    }

    @PostMapping(value = "/{annualStatisticsId:\\d+}/submitforinspection", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void submitForInspection(final @PathVariable long annualStatisticsId,
                                    final @RequestBody @Valid IdRevisionDTO dto) {

        dto.setId(annualStatisticsId);
        annualStatisticsFeature.submitForInspection(dto);
    }

    @PostMapping(value = "/{annualStatisticsId:\\d+}/approve", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void approve(final @PathVariable long annualStatisticsId, final @RequestBody @Valid IdRevisionDTO dto) {
        dto.setId(annualStatisticsId);
        annualStatisticsFeature.approve(dto);
    }

    @PostMapping(value = "/{annualStatisticsId:\\d+}/excel",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportStatisticsToExcel(final @PathVariable long annualStatisticsId, final Locale locale) {
        return new ModelAndView(annualStatisticsExportFeature.exportAnnualStatisticsToExcel(annualStatisticsId, locale));
    }

    @PostMapping(value = "/{annualStatisticsId:\\d+}/pdf",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportStatisticsToPdf(final @PathVariable long annualStatisticsId,
                                                        final Locale locale) {

        return annualStatisticsExportFeature.exportAnnualStatisticsToPdf(annualStatisticsId, locale);
    }

    @PostMapping(value = "/year/{year:\\d+}/excel",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportAllStatisticsToExcel(final @PathVariable int year,
                                                   final @RequestParam boolean groupByRka,
                                                   final Locale locale) {

        return new ModelAndView(annualStatisticsExportFeature.exportAllAnnualStatistics(year, locale, groupByRka));
    }
}
