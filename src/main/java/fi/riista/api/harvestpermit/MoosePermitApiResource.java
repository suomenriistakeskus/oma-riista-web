package fi.riista.api.harvestpermit;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermitDetailsFeature;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationFeature;
import fi.riista.feature.harvestpermit.endofhunting.EndOfMooselikePermitHuntingFeature;
import fi.riista.feature.harvestpermit.endofhunting.excel.UnfinishedMooselikePermitsFeature;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsDTO;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsExcelView;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsFeature;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsGroupBy;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsOrganisationType;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsReportType;
import fi.riista.feature.huntingclub.hunting.overview.SharedPermitMapFeature;
import fi.riista.feature.huntingclub.members.HuntingClubContactDetailDTO;
import fi.riista.feature.huntingclub.members.HuntingClubContactFeature;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingSummaryModerationFeature;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportFeature;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportParams;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportParamsDTO;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportUriBuilder;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.area.pdf.PermitAreaMapPdfFeature;
import fi.riista.feature.permit.invoice.harvest.excel.MooselikeHarvestPaymentSummaryFeature;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/moosepermit")
public class MoosePermitApiResource {

    @Resource
    private HarvestPermitDetailsFeature harvestPermitDetailsFeature;

    @Resource
    private MoosePermitAllocationFeature huntingClubPermitAllocationFeature;

    @Resource
    private HuntingClubContactFeature huntingClubContactFeature;

    @Resource
    private HuntingSummaryModerationFeature huntingSummaryModerationFeature;

    @Resource
    private EndOfMooselikePermitHuntingFeature endOfMooselikePermitHuntingFeature;

    @Resource
    private MoosePermitStatisticsFeature moosePermitStatisticsFeature;

    @Resource
    private SharedPermitMapFeature sharedPermitMapFeature;

    @Resource
    private PermitAreaMapPdfFeature permitAreaMapPdfFeature;

    @Resource
    private LukeReportFeature lukeReportFeature;

    @Resource
    private UnfinishedMooselikePermitsFeature unfinishedMooselikePermitsFeature;

    @Resource
    private MooselikeHarvestPaymentSummaryFeature mooselikeHarvestPaymentSummaryFeature;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private MessageSource messageSource;

    @GetMapping(value = "/lukereportparams", produces = MediaType.APPLICATION_JSON_VALUE)
    public LukeReportParamsDTO getLukeReportParams(final HttpSession httpSession,
                                                   final @RequestParam Long permitId,
                                                   final @RequestParam int species,
                                                   final @RequestParam(required = false) Long clubId) {
        final LukeReportUriBuilder uriBuilder = lukeReportFeature.getUriBuilder(permitId, clubId, httpSession);
        return lukeReportFeature.getReportParameters(uriBuilder, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubPermitDTO get(final @PathVariable long permitId,
                                    final @RequestParam int species) {
        return harvestPermitDetailsFeature.getClubPermit(permitId, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/rhy", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrganisationNameDTO getRhyCode(final @PathVariable long permitId) {
        return harvestPermitDetailsFeature.getRhyCode(permitId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/leaders", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HuntingClubContactDetailDTO> leaders(final @PathVariable long permitId,
                                                     final @RequestParam int huntingYear,
                                                     final @RequestParam int gameSpeciesCode) {
        return huntingClubContactFeature.listClubHuntingLeaders(permitId, huntingYear, gameSpeciesCode);
    }

    @GetMapping(value = "/{permitId:\\d+}/harvest", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HarvestDTO> listHarvest(final @PathVariable long permitId,
                                        final @RequestParam int huntingYear,
                                        final @RequestParam int gameSpeciesCode) {
        return sharedPermitMapFeature.listHarvest(permitId, huntingYear, gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/map", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection getMapForPermit(final @PathVariable long permitId,
                                             final @RequestParam int huntingYear,
                                             final @RequestParam int gameSpeciesCode) {
        return sharedPermitMapFeature.findPermitArea(permitId, huntingYear, gameSpeciesCode);
    }

    @PostMapping(value = "/{permitId:\\d+}/application-map", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> getMapPdf(@PathVariable final long permitId, final Locale locale,
                                       @ModelAttribute @Valid final MapPdfParameters dto) {
        return mapPdfRemoteService.renderPdf(dto, () -> permitAreaMapPdfFeature.getModelForHarvestPermit(permitId, dto.getOverlay(), locale));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{permitId:\\d+}/allocation/{gameSpeciesCode:\\d+}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateAllocations(final @PathVariable long permitId,
                                  final @PathVariable int gameSpeciesCode,
                                  final @RequestBody @Valid List<MoosePermitAllocationDTO> allocations) {

        huntingClubPermitAllocationFeature.updateAllocation(permitId, gameSpeciesCode, allocations);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 60 * 60)// 1 hour in seconds
    @GetMapping("/{permitId:\\d+}/luke-reports")
    public void getLukeReports(final HttpSession httpSession,
                               final @PathVariable long permitId,
                               final @RequestParam(required = false) Long clubId,
                               final @RequestParam LukeReportParams.LukeArea org,
                               final @RequestParam LukeReportParams.Presentation presentation,
                               final @RequestParam String fileName,
                               final HttpServletResponse httpServletResponse) {
        final LukeReportUriBuilder uriBuilder = lukeReportFeature.getUriBuilder(permitId, clubId, httpSession);
        lukeReportFeature.getReport(uriBuilder, httpServletResponse, org, presentation, fileName);
    }

    @PostMapping(value = "/unfinished/{huntingYear:\\d+}/excel",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportUnfinishedMooselikePermitsToExcel(final @PathVariable int huntingYear,
                                                                final Locale locale) {
        return new ModelAndView(
                unfinishedMooselikePermitsFeature.exportUnfinishedMooselikePermitsToExcel(huntingYear, locale));
    }

    @PostMapping(value = "/paymentsummary/{huntingYear:\\d+}/{gameSpeciesCode:\\d+}/excel",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportMooselikeHarvestPaymentSummaryToExcel(final @PathVariable int huntingYear,
                                                                    final @PathVariable int gameSpeciesCode,
                                                                    final Locale locale) {
        return new ModelAndView(mooselikeHarvestPaymentSummaryFeature
                .exportMooselikeHarvestPaymentSummaryToExcel(huntingYear, gameSpeciesCode, locale));
    }

    // MASS OVERRIDE

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(
            value = "/{permitId:\\d+}/override/{gameSpeciesCode:\\d+}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BasicClubHuntingSummaryDTO> getHuntingSummariesForModeration(final @PathVariable long permitId,
                                                                             final @PathVariable int gameSpeciesCode) {

        return huntingSummaryModerationFeature.getHuntingSummariesForModeration(permitId, gameSpeciesCode);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(
            value = "/{permitId:\\d+}/override/{gameSpeciesCode:\\d+}/{completeHuntingOfPermit:\\d+}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void upsertHuntingSummariesWithModeratorOverride(final @PathVariable long permitId,
                                                            final @PathVariable int gameSpeciesCode,
                                                            final @PathVariable Integer completeHuntingOfPermit,
                                                            final @RequestBody @Valid List<BasicClubHuntingSummaryDTO> huntingSummaries) throws IOException {
        final boolean completeHunting = Integer.valueOf(1).equals(completeHuntingOfPermit);

        huntingSummaryModerationFeature.processModeratorOverriddenHuntingSummaries(
                permitId, gameSpeciesCode, completeHunting, huntingSummaries);

        if (completeHunting) {
            endOfMooselikePermitHuntingFeature.endMooselikeHunting(permitId, gameSpeciesCode);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{permitId:\\d+}/override/{gameSpeciesCode:\\d+}")
    public void deleteModeratorOverriddenHuntingSummaries(final @PathVariable long permitId,
                                                          final @PathVariable int gameSpeciesCode) {

        huntingSummaryModerationFeature.revokeHuntingSummaryModeration(permitId, gameSpeciesCode);
    }

    // STATISTICS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/rhystatistics/{speciesCode:\\d+}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MoosePermitStatisticsDTO> getRhyStats(final @PathVariable long permitId,
                                                      final @PathVariable int speciesCode,
                                                      final Locale locale) {
        return moosePermitStatisticsFeature.getRhyStatistics(permitId, speciesCode, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/statistics", produces = APPLICATION_JSON_VALUE)
    public List<MoosePermitStatisticsDTO> listRhyMooseStatistics(final @RequestParam int year,
                                                                 final @RequestParam int species,
                                                                 final @RequestParam String orgCode,
                                                                 final @RequestParam MoosePermitStatisticsOrganisationType orgType,
                                                                 final @RequestParam MoosePermitStatisticsGroupBy groupBy,
                                                                 final @RequestParam MoosePermitStatisticsReportType reportType,
                                                                 final Locale locale) {
        return moosePermitStatisticsFeature.calculate(locale, reportType, groupBy, true, species, year, orgType, orgCode);
    }

    @PostMapping(value = "/statistics/excel", consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportRhyMooseStatisticsExcel(final @RequestParam int year,
                                                      final @RequestParam int species,
                                                      final @RequestParam String orgCode,
                                                      final @RequestParam MoosePermitStatisticsOrganisationType orgType,
                                                      final @RequestParam MoosePermitStatisticsGroupBy groupBy,
                                                      final @RequestParam MoosePermitStatisticsReportType reportType,
                                                      final Locale locale) {
        return new ModelAndView(new MoosePermitStatisticsExcelView(new EnumLocaliser(messageSource, locale),
                moosePermitStatisticsFeature.calculate(locale, reportType, groupBy, false, species, year, orgType, orgCode)));
    }

}
