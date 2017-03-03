package fi.riista.api;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.common.ContentTypeChecker;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermitContactPersonDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCrudFeature;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportListExcelView;
import fi.riista.feature.harvestpermit.report.search.HarvestReportSearchFeature;
import fi.riista.feature.harvestpermit.search.HarvestPermitExistsDTO;
import fi.riista.feature.harvestpermit.search.HarvestPermitRhySearchDTO;
import fi.riista.feature.harvestpermit.search.HarvestPermitSearchDTO;
import fi.riista.feature.harvestpermit.search.HarvestPermitSearchFeature;
import fi.riista.feature.huntingclub.MoosePermitTodoFeature;
import fi.riista.feature.huntingclub.hunting.overview.SharedPermitMapFeature;
import fi.riista.feature.huntingclub.members.HuntingClubContactDetailDTO;
import fi.riista.feature.huntingclub.members.HuntingClubContactFeature;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.MooselikeHuntingYearDTO;
import fi.riista.feature.huntingclub.permit.MooselikePermitListingDTO;
import fi.riista.feature.huntingclub.permit.allocation.HuntingClubPermitAllocationDTO;
import fi.riista.feature.huntingclub.permit.allocation.HuntingClubPermitAllocationFeature;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummaryDTO;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportCrudFeature;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportDTO;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsDTO;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryCrudFeature;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportFeature;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportParams;
import fi.riista.feature.huntingclub.statistics.luke.LukeReportParamsDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.integration.lupahallinta.HarvestPermitImportException;
import fi.riista.integration.lupahallinta.HarvestPermitImportFeature;
import fi.riista.integration.lupahallinta.HarvestPermitImportResultDTO;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.apache.commons.io.IOUtils;
import org.geojson.FeatureCollection;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/harvestpermit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class HarvestPermitApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApiResource.class);

    private static final Set<String> ALLOWED_MOOSE_HARVEST_REPORT_MEDIATYPES = ImmutableSet.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            MediaTypeExtras.IMAGE_TIFF_VALUE,
            MediaType.TEXT_PLAIN_VALUE,
            MediaType.TEXT_HTML_VALUE,
            MediaType.APPLICATION_XHTML_XML_VALUE,
            MediaTypeExtras.TEXT_CSV_VALUE,
            MediaTypeExtras.APPLICATION_PDF_VALUE);

    @Resource
    private HarvestPermitCrudFeature harvestPermitCrudFeature;

    @Resource
    private HarvestPermitImportFeature harvestPermitImportFeature;

    @Resource
    private HarvestReportSearchFeature harvestReportSearchFeature;

    @Resource
    private HarvestPermitSearchFeature harvestPermitSearchFeature;

    @Resource
    private HuntingClubPermitAllocationFeature huntingClubPermitAllocationFeature;

    @Resource
    private HuntingClubContactFeature huntingClubContactFeature;

    @Resource
    private MooseHarvestReportCrudFeature mooseHarvestReportCrudFeature;

    @Resource
    private ContentTypeChecker contentTypeChecker;

    @Resource
    private SharedPermitMapFeature sharedPermitMapFeature;

    @Resource
    private LukeReportFeature lukeReportFeature;

    @Resource
    private MoosePermitTodoFeature moosePermitTodoFeature;

    @Resource
    private MooseHuntingSummaryCrudFeature mooseHuntingSummaryCrudFeature;

    @Resource
    private MessageSource messageSource;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.GET)
    public HarvestPermitDTO getHarvestPermit(@PathVariable Long id) {
        return harvestPermitCrudFeature.read(id);
    }

    @RequestMapping(value = "/preloadPermits", method = RequestMethod.GET)
    public List<HarvestPermitExistsDTO> preloadPermits() {
        return harvestPermitCrudFeature.preloadPermits();
    }

    @RequestMapping(value = "/checkPermitNumber", method = RequestMethod.POST)
    public HarvestPermitExistsDTO checkPermitNumber(@RequestParam String permitNumber) {
        return harvestPermitSearchFeature.findPermitNumber(permitNumber);
    }

    @RequestMapping(value = "/acceptHarvest", method = RequestMethod.POST)
    public void acceptHarvest(@RequestParam Long harvestId,
                              @RequestParam Integer harvestRev,
                              @RequestParam Harvest.StateAcceptedToHarvestPermit toState) {
        harvestPermitCrudFeature.acceptHarvest(harvestId, harvestRev, toState);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/rhy/{id:\\d+}/list", method = RequestMethod.GET)
    public Page<HarvestPermitDTO> listByRhy(@PathVariable long id, Pageable page) {
        return harvestPermitCrudFeature.listByRhy(id, page);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/mypermits", method = RequestMethod.GET)
    public List<HarvestPermitDTO> listMyPermits(@RequestParam(required = false) Long personId) {
        return harvestPermitCrudFeature.listMyPermits(personId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/export-reports", method = RequestMethod.GET)
    public ModelAndView exportPermitHarvestReports(@PathVariable Long id) {
        return new ModelAndView(new HarvestReportListExcelView(
                harvestReportSearchFeature.findByPermit(id),
                new EnumLocaliser(messageSource, LocaleContextHolder.getLocale())));
    }

    @RequestMapping(value = "//{id:\\d+}/contactpersons", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateContactPersons(
            @PathVariable Long id, @RequestBody @Valid List<HarvestPermitContactPersonDTO> contactPersons) {
        harvestPermitCrudFeature.updateContactPersons(id, contactPersons);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/species", method = RequestMethod.GET)
    public List<GameSpeciesDTO> listPermitSpecies() {
        return harvestPermitCrudFeature.findPermitSpecies();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit", method = RequestMethod.GET)
    public List<MooselikePermitListingDTO> listPermits(@RequestParam(required = false) Long personId,
                                                       @RequestParam int year,
                                                       @RequestParam int species) {
        return harvestPermitCrudFeature.listPermits(personId, year, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit/{permitId:\\d+}", method = RequestMethod.GET)
    public HuntingClubPermitDTO get(@PathVariable long permitId,
                                    @RequestParam int species) {
        return harvestPermitCrudFeature.getPermit(permitId, species);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit/rhy/{permitId:\\d+}", method = RequestMethod.GET)
    public OrganisationNameDTO getRhyCode(@PathVariable long permitId) {
        return harvestPermitCrudFeature.getRhyCode(permitId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit/rhy/stats/{permitId:\\d+}/{speciesCode:\\d+}", method = RequestMethod.GET)
    public List<MoosePermitStatisticsDTO> getRhyStats(@PathVariable long permitId,
                                                      @PathVariable final int speciesCode,
                                                      Locale locale) {
        return harvestPermitCrudFeature.getRhyStatistics(permitId, speciesCode, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit/allocation/{permitId:\\d+}/{gameSpeciesCode:\\d+}", method = RequestMethod.POST)
    public void updateAllocations(@PathVariable final long permitId,
                                  @PathVariable final int gameSpeciesCode,
                                  @RequestBody List<HuntingClubPermitAllocationDTO> allocations) {

        huntingClubPermitAllocationFeature.updateAllocation(permitId, gameSpeciesCode, allocations);
    }

    @RequestMapping(
            value = "moosepermit/{permitId:\\d+}/species/{speciesCode:\\d+}/harvestreport", method = RequestMethod.POST)
    public ResponseEntity<?> createMooseHarvestReport(@PathVariable final long permitId,
                                                      @PathVariable final int speciesCode,
                                                      @RequestParam("file") final MultipartFile file) {

        return contentTypeChecker.validate(file, ALLOWED_MOOSE_HARVEST_REPORT_MEDIATYPES)
                .orElseGet(() -> {
                    mooseHarvestReportCrudFeature.create(MooseHarvestReportDTO.withReceipt(permitId, speciesCode, file));
                    return ResponseEntity.ok().build();
                });
    }

    @RequestMapping(
            value = "moosepermit/{permitId:\\d+}/species/{speciesCode:\\d+}/harvestreport/noharvests",
            method = RequestMethod.POST)
    public ResponseEntity<?> createMooseHarvestReportNoHarvests(
            @PathVariable final long permitId, @PathVariable final int speciesCode) {

        mooseHarvestReportCrudFeature.create(MooseHarvestReportDTO.withNoHarvests(permitId, speciesCode));
        return ResponseEntity.ok().build();
    }

    @RequestMapping(
            value = "moosepermit/{permitId:\\d+}/species/{speciesCode:\\d+}/harvestreport",
            method = RequestMethod.DELETE)
    public void deleteMooseHarvestReport(@PathVariable final long permitId, @PathVariable final int speciesCode) {
        mooseHarvestReportCrudFeature.delete(permitId, speciesCode);
    }

    @RequestMapping(
            value = "moosepermit/{permitId:\\d+}/species/{speciesCode:\\d+}/harvestreport/receipt",
            method = RequestMethod.POST)
    public ResponseEntity<byte[]> getReceiptFile(
            @PathVariable final long permitId, @PathVariable final int speciesCode) throws IOException {

        return mooseHarvestReportCrudFeature.getReceiptFile(permitId, speciesCode);
    }

    @RequestMapping(value = "moosepermit/{permitId:\\d+}/harvest", method = RequestMethod.GET)
    public List<HarvestDTO> listHarvest(@PathVariable long permitId,
                                        @RequestParam final int huntingYear,
                                        @RequestParam final int gameSpeciesCode) {
        return sharedPermitMapFeature.listHarvest(permitId, huntingYear, gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit/{permitId:\\d+}/map",
            method = RequestMethod.GET, produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection getMapForPermit(@PathVariable final long permitId,
                                             @RequestParam final int huntingYear,
                                             @RequestParam final int gameSpeciesCode) {
        return sharedPermitMapFeature.findPermitArea(permitId, huntingYear, gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(
            value = "moosepermit/todo/permit/{permitId:\\d+}/species/{speciesCode:\\d+}", method = RequestMethod.GET)
    public Map<Long, MoosePermitTodoFeature.TodoDto> listMoosePermitTodos(
            @PathVariable final long permitId, @PathVariable final int speciesCode) {

        return moosePermitTodoFeature.listTodos(permitId, speciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit/todo/club/{clubId:\\d+}", method = RequestMethod.GET)
    public MoosePermitTodoFeature.TodoDto listMoosePermitTodosForClub(@PathVariable long clubId,
                                                                      @RequestParam int year) {
        return moosePermitTodoFeature.listTodosForClub(clubId, year);
    }


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit/huntingyears", method = RequestMethod.GET)
    public List<MooselikeHuntingYearDTO> listHuntingYears(@RequestParam(required = false) Long personId) {
        return harvestPermitCrudFeature.listHuntingYears(personId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "moosepermit/{permitId:\\d+}/leaders", method = RequestMethod.GET)
    public List<HuntingClubContactDetailDTO> leaders(@PathVariable long permitId,
                                                     @RequestParam final int huntingYear,
                                                     @RequestParam final int gameSpeciesCode) {
        return huntingClubContactFeature.listLeaders(permitId, huntingYear, gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(
            value = "/{permitId:\\d+}/huntingsummariesformoderation/species/{gameSpeciesCode:\\d+}",
            method = RequestMethod.GET)
    public List<BasicClubHuntingSummaryDTO> getHuntingSummariesForModeration(
            @PathVariable final long permitId, @PathVariable final int gameSpeciesCode) {

        return mooseHuntingSummaryCrudFeature.getHuntingSummariesForModeration(permitId, gameSpeciesCode);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(
            value = "/{permitId:\\d+}/massoverrideclubhuntingsummaries/species/{gameSpeciesCode:\\d+}/{completeHuntingOfPermit:\\d+}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void upsertHuntingSummariesWithModeratorOverride(
            @PathVariable final long permitId,
            @PathVariable final int gameSpeciesCode,
            @PathVariable final Integer completeHuntingOfPermit,
            @RequestBody @Valid final List<BasicClubHuntingSummaryDTO> huntingSummaries) {

        final boolean b = completeHuntingOfPermit != null && completeHuntingOfPermit == 1;
        mooseHuntingSummaryCrudFeature.processModeratorOverriddenHuntingSummaries(
                permitId, gameSpeciesCode, b, huntingSummaries);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(
            value = "/{permitId:\\d+}/deleteoverriddenclubhuntingsummaries/species/{gameSpeciesCode:\\d+}",
            method = RequestMethod.DELETE)
    public void deleteModeratorOverriddenHuntingSummaries(
            @PathVariable final long permitId, @PathVariable final int gameSpeciesCode) {

        mooseHuntingSummaryCrudFeature.revokeHuntingSummaryModeration(permitId, gameSpeciesCode);
    }

    @RequestMapping(value = "moosepermit/lukereportparams", method = RequestMethod.GET)
    public LukeReportParamsDTO getLukeReportParams(@RequestParam(required = false) Long clubId,
                                                   @RequestParam Long permitId) {
        return lukeReportFeature.getLukeReportParams(clubId, permitId);
    }

    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 60 * 60)// 1 hour in seconds
    @RequestMapping(value = "moosepermit/{permitId:\\d+}/luke-reports", method = RequestMethod.GET)
    public void getLukeReports(@PathVariable long permitId,
                               @RequestParam(required = false) Long clubId,
                               @RequestParam LukeReportParams.Organisation org,
                               @RequestParam LukeReportParams.Presentation presentation,
                               @RequestParam String fileName,
                               HttpServletResponse response) {
        final URI lukeReportUrl = lukeReportFeature.getLukeReportUrl(clubId, permitId, org, presentation, fileName);
        lukeReportFeature.getLukeReport(lukeReportUrl, response);
    }

    @RequestMapping(value = "moosepermit/pdf", method = RequestMethod.POST)
    public void pdf(@RequestParam String permitNumber, HttpServletResponse response)
            throws IOException {

        final URL url = harvestPermitCrudFeature.getPdf(permitNumber);
        final URLConnection connection = url.openConnection();
        final String filename = String.format("Paatos_%s.pdf", permitNumber);

        if (connection instanceof HttpURLConnection) {
            readPdf(filename, response, (HttpURLConnection) connection);
        } else {
            LOG.error("error - not a http request!");
        }
    }

    private static void readPdf(final String filename,
                                final HttpServletResponse response,
                                final HttpURLConnection connection) throws IOException {
        // If pdf is not found for some reason, there will be redirect to error page, but error page is 200 OK.
        // Therefore do not follow redirects and treat anything else than 200 as 404 Not Found.
        connection.setInstanceFollowRedirects(false);

        connection.connect();
        final int code = connection.getResponseCode();
        if (code == HttpStatus.OK.value()) {
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);
            response.setContentType(MediaTypeExtras.APPLICATION_PDF_VALUE);
            try (InputStream is = connection.getInputStream()) {
                IOUtils.copy(is, response.getOutputStream());
            }
        } else {
            LOG.error("Connection failed, code:" + code);
            response.sendError(HttpStatus.NOT_FOUND.value());
        }
    }

    @RequestMapping(value = "/admin/import/permit", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HarvestPermitImportResultDTO importPermits(@RequestParam("file") MultipartFile file) throws IOException {
        InputStreamReader reader = new InputStreamReader(file.getInputStream(), "ISO-8859-1");
        String requestInfo = DateTime.now().toString() + ":" + file.getOriginalFilename();
        try {
            return harvestPermitImportFeature.doImport(reader, requestInfo, null);
        } catch (HarvestPermitImportException e) {
            return new HarvestPermitImportResultDTO(e.getAllErrors());
        }
    }

    @RequestMapping(value = "/admin/search", method = RequestMethod.POST)
    public List<HarvestPermitDTO> search(@RequestBody @Valid HarvestPermitSearchDTO dto) {
        return harvestPermitSearchFeature.search(dto);
    }

    @RequestMapping(value = "/rhy/search", method = RequestMethod.POST)
    public List<HarvestPermitDTO> searchRhy(@RequestBody @Valid HarvestPermitRhySearchDTO dto) {
        return harvestPermitSearchFeature.searchForCoordinator(dto);
    }
}
