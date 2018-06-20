package fi.riista.api;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermitDetailsFeature;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationFeature;
import fi.riista.feature.harvestpermit.download.HarvestPermitDownloadDecisionDTO;
import fi.riista.feature.harvestpermit.download.HarvestPermitDownloadDecisionFeature;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsDTO;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsFeature;
import fi.riista.feature.huntingclub.MoosePermitTodoFeature;
import fi.riista.feature.huntingclub.hunting.overview.SharedPermitMapFeature;
import fi.riista.feature.huntingclub.members.HuntingClubContactDetailDTO;
import fi.riista.feature.huntingclub.members.HuntingClubContactFeature;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryCrudFeature;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportFeature;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportParams;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportParamsDTO;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportUriBuilder;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.geojson.FeatureCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/moosepermit")
public class MoosePermitApiResource {

    @Resource
    private HarvestPermitDetailsFeature harvestPermitDetailsFeature;

    @Resource
    private HarvestPermitDownloadDecisionFeature harvestPermitDownloadDecisionFeature;

    @Resource
    private MoosePermitAllocationFeature huntingClubPermitAllocationFeature;

    @Resource
    private HuntingClubContactFeature huntingClubContactFeature;

    @Resource
    private MooseHuntingSummaryCrudFeature mooseHuntingSummaryCrudFeature;

    @Resource
    private MoosePermitTodoFeature moosePermitTodoFeature;

    @Resource
    private MoosePermitStatisticsFeature moosePermitStatisticsFeature;

    @Resource
    private SharedPermitMapFeature sharedPermitMapFeature;

    @Resource
    private LukeReportFeature lukeReportFeature;

    @GetMapping(value = "/lukereportparams", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LukeReportParamsDTO getLukeReportParams(final HttpSession httpSession,
                                                   final @RequestParam Long permitId,
                                                   final @RequestParam(required = false) Long clubId) {
        final LukeReportUriBuilder uriBuilder = lukeReportFeature.getUriBuilder(permitId, clubId, httpSession);
        return lukeReportFeature.getReportParameters(uriBuilder);
    }

    @PostMapping("/pdf")
    public void pdf(final @RequestParam String permitNumber,
                    final HttpServletResponse httpServletResponse) throws IOException {
        final HarvestPermitDownloadDecisionDTO dto =
                harvestPermitDownloadDecisionFeature.getDecisionPdf(permitNumber);

        if (dto.getLocalDecisionFileId() != null) {
            harvestPermitDownloadDecisionFeature.downloadLocalPdf(dto, httpServletResponse);

        } else if (dto.getRemoteUri() != null) {
            harvestPermitDownloadDecisionFeature.downloadRemotePdf(dto, httpServletResponse);
        } else {
            throw new NotFoundException("Not available");
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HuntingClubPermitDTO get(final @PathVariable long permitId,
                                    final @RequestParam int species) {
        return harvestPermitDetailsFeature.getClubPermit(permitId, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/rhy", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrganisationNameDTO getRhyCode(final @PathVariable long permitId) {
        return harvestPermitDetailsFeature.getRhyCode(permitId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/leaders", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HuntingClubContactDetailDTO> leaders(final @PathVariable long permitId,
                                                     final @RequestParam int huntingYear,
                                                     final @RequestParam int gameSpeciesCode) {
        return huntingClubContactFeature.listClubHuntingLeaders(permitId, huntingYear, gameSpeciesCode);
    }

    @GetMapping(value = "/{permitId:\\d+}/harvest", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/todo/{speciesCode:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<Long, MoosePermitTodoFeature.TodoDto> listMoosePermitTodos(
            final @PathVariable long permitId,
            final @PathVariable int speciesCode) {

        return moosePermitTodoFeature.listTodos(permitId, speciesCode);
    }


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{permitId:\\d+}/allocation/{gameSpeciesCode:\\d+}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateAllocations(final @PathVariable long permitId,
                                  final @PathVariable int gameSpeciesCode,
                                  final @RequestBody @Valid List<MoosePermitAllocationDTO> allocations) {

        huntingClubPermitAllocationFeature.updateAllocation(permitId, gameSpeciesCode, allocations);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{permitId:\\d+}/rhystatistics/{speciesCode:\\d+}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MoosePermitStatisticsDTO> getRhyStats(final @PathVariable long permitId,
                                                      final @PathVariable int speciesCode,
                                                      final Locale locale) {
        return moosePermitStatisticsFeature.getRhyStatistics(permitId, speciesCode, locale);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 60 * 60)// 1 hour in seconds
    @GetMapping("/{permitId:\\d+}/luke-reports")
    public void getLukeReports(final HttpSession httpSession,
                               final @PathVariable long permitId,
                               final @RequestParam(required = false) Long clubId,
                               final @RequestParam LukeReportParams.Organisation org,
                               final @RequestParam LukeReportParams.Presentation presentation,
                               final @RequestParam String fileName,
                               final HttpServletResponse httpServletResponse) {
        final LukeReportUriBuilder uriBuilder = lukeReportFeature.getUriBuilder(permitId, clubId, httpSession);
        lukeReportFeature.getReport(uriBuilder, httpServletResponse, org, presentation, fileName);
    }

    // MASS OVERRIDE

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(
            value = "/{permitId:\\d+}/override/{gameSpeciesCode:\\d+}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<BasicClubHuntingSummaryDTO> getHuntingSummariesForModeration(
            final @PathVariable long permitId,
            final @PathVariable int gameSpeciesCode) {
        return mooseHuntingSummaryCrudFeature.getHuntingSummariesForModeration(permitId, gameSpeciesCode);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(
            value = "/{permitId:\\d+}/override/{gameSpeciesCode:\\d+}/{completeHuntingOfPermit:\\d+}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void upsertHuntingSummariesWithModeratorOverride(
            final @PathVariable long permitId,
            final @PathVariable int gameSpeciesCode,
            final @PathVariable Integer completeHuntingOfPermit,
            final @RequestBody @Valid List<BasicClubHuntingSummaryDTO> huntingSummaries) {

        final boolean b = completeHuntingOfPermit != null && completeHuntingOfPermit == 1;
        mooseHuntingSummaryCrudFeature.processModeratorOverriddenHuntingSummaries(
                permitId, gameSpeciesCode, b, huntingSummaries);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{permitId:\\d+}/override/{gameSpeciesCode:\\d+}")
    public void deleteModeratorOverriddenHuntingSummaries(
            final @PathVariable long permitId,
            final @PathVariable int gameSpeciesCode) {
        mooseHuntingSummaryCrudFeature.revokeHuntingSummaryModeration(permitId, gameSpeciesCode);
    }
}
