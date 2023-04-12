package fi.riista.api.moderator;

import fi.riista.feature.dashboard.DashboardAnnouncementsDTO;
import fi.riista.feature.dashboard.DashboardClubsDTO;
import fi.riista.feature.dashboard.DashboardFeature;
import fi.riista.feature.dashboard.DashboardHarvestReportDTO;
import fi.riista.feature.dashboard.DashboardHarvestReportExcelView;
import fi.riista.feature.dashboard.DashboardHarvestsObservationsDTO;
import fi.riista.feature.dashboard.DashboardMooseHuntingDTO;
import fi.riista.feature.dashboard.DashboardMooselikeEndOfHuntingExcelFeature;
import fi.riista.feature.dashboard.DashboardPOIsDTO;
import fi.riista.feature.dashboard.DashboardPdfDTO;
import fi.riista.feature.dashboard.DashboardRhyEditDTO;
import fi.riista.feature.dashboard.DashboardRhyEditExcelView;
import fi.riista.feature.dashboard.DashboardShootingTestDTO;
import fi.riista.feature.dashboard.DashboardSrvaDTO;
import fi.riista.feature.dashboard.DashboardUsersDTO;
import fi.riista.feature.dashboard.EventSearchConditionDTO;
import fi.riista.feature.dashboard.EventSearchExcelFeature;
import fi.riista.feature.gamediary.summary.AdminGameDiarySummaryExcelFeature;
import fi.riista.feature.gamediary.summary.AdminGameDiarySummaryRequestDTO;
import fi.riista.feature.harvestpermit.HarvestPermitPublicPdfDownloadRepository;
import fi.riista.feature.harvestpermit.HarvestPermitPublicPdfDownloadStatisticsDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.util.DateUtil;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/dashboard")
public class DashboardController {

    @Resource
    private DashboardFeature dashboardFeature;

    @Resource
    private AdminGameDiarySummaryExcelFeature adminGameDiarySummaryExcelFeature;

    @Resource
    private DashboardMooselikeEndOfHuntingExcelFeature dashboardMooselikeEndOfHuntingExcelFeature;

    @Resource
    private EventSearchExcelFeature eventSearchExcelFeature;

    @Resource
    private HarvestPermitPublicPdfDownloadRepository downloadRepository;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("users")
    public DashboardUsersDTO getUsersMetrics() {
        return dashboardFeature.getMetricsUsers();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("pdf")
    public DashboardPdfDTO getPdfMetrics() {
        return dashboardFeature.getMetricsPdf();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("mobile")
    public Map<String, Object> getMobileLogins() {
        return dashboardFeature.getMobileLogins();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("clubs")
    public DashboardClubsDTO getClubsMetrics() {
        return dashboardFeature.getMetricsClubs();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("pois")
    public DashboardPOIsDTO getPOIMetrics() {
        return dashboardFeature.getMetricsPOIs();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("moosehunting")
    public DashboardMooseHuntingDTO getMooseHuntingMetrics() {
        return dashboardFeature.getMetricsMooseHunting();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("harvests_observations")
    public DashboardHarvestsObservationsDTO getHarvestsObservationsMetrics() {
        return dashboardFeature.getMetricsHarvestsObservations();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("announcement")
    public DashboardAnnouncementsDTO getAnnouncementMetrics() {
        return dashboardFeature.getAnnouncementMetrics();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("shootingtest")
    public DashboardShootingTestDTO getShootingTestMetrics() {
        return dashboardFeature.getShootingTestMetrics();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("srva")
    public DashboardSrvaDTO get() {
        return dashboardFeature.getMetricsSrva();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("harvestreport")
    public List<DashboardHarvestReportDTO> getHarvestReportMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        return dashboardFeature.getHarvestReportMetrics(beginToDate(begin), endToDate(end));
    }

    @PostMapping("harvestreport/excel")
    public ModelAndView searchRhysExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<DashboardHarvestReportDTO> results =
                dashboardFeature.getHarvestReportMetrics(beginToDate(begin), endToDate(end));
        return new ModelAndView(new DashboardHarvestReportExcelView(results));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("rhyedit")
    public List<DashboardRhyEditDTO> getRhyEditMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        return dashboardFeature.getRhyEditMetrics(beginToDate(begin), endToDate(end));
    }

    @PostMapping("rhyedit/excel")
    public ModelAndView getRhyEditMetricsExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        final List<DashboardRhyEditDTO> results =
                dashboardFeature.getRhyEditMetrics(beginToDate(begin), endToDate(end));
        return new ModelAndView(new DashboardRhyEditExcelView(results));
    }

    private static Date beginToDate(LocalDate begin) {
        if (begin == null) {
            begin = new LocalDate(2014, 8, 1);
        }
        return begin.toDate();
    }

    private static Date endToDate(LocalDate end) {
        if (end == null) {
            end = DateUtil.today().plusDays(1);
        }
        return end.toDate();
    }

    @PostMapping(value = "/harvestSummary",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView summaryExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate beginDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate,
            @RequestParam final boolean harvestReportOnly,
            @RequestParam final boolean officialHarvestOnly,
            @RequestParam(required = false) final Integer speciesCode,
            @RequestParam(required = false) final OrganisationType organisationType,
            @RequestParam(required = false) final String officialCode) {
        final AdminGameDiarySummaryRequestDTO dto = new AdminGameDiarySummaryRequestDTO();
        dto.setBeginDate(beginDate);
        dto.setEndDate(endDate);
        dto.setHarvestReportOnly(harvestReportOnly);
        dto.setOfficialHarvestOnly(officialHarvestOnly);
        dto.setSpeciesCode(speciesCode);
        dto.setOfficialCode(officialCode);
        dto.setOrganisationType(organisationType);

        return new ModelAndView(adminGameDiarySummaryExcelFeature.export(dto));
    }

    @PostMapping("/mooselike/endofhunting/excel/{huntingYear:\\d+}/{speciesCode:\\d+}")
    public ModelAndView exportMooselikePartnerEndOfHuntingReports(@PathVariable final int huntingYear,
                                                                  @PathVariable final int speciesCode,
                                                                  final Locale locale) {
        return new ModelAndView(dashboardMooselikeEndOfHuntingExcelFeature.exportMooselikeHuntingSummaries(speciesCode, huntingYear, locale));

    }

    @PostMapping(value = "/events", produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView exportEvents(@Valid @RequestBody final EventSearchConditionDTO searchCondition,
                                     final Locale locale) {
        return new ModelAndView(eventSearchExcelFeature.export(searchCondition, locale));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/carnivore/downloads")
    public HarvestPermitPublicPdfDownloadStatisticsDTO getCarnivorePublicPdfDownloadStatistics() {
        return downloadRepository.getStatistics();
    }
}
