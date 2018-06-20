package fi.riista.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.ContentTypeChecker;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.harvestpermit.list.ListPermitApplicationsFeature;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationAdditionalDataDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationAmendDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationBasicDetailsDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationCreateDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationFeature;
import fi.riista.feature.permit.application.HarvestPermitApplicationSendDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationShooterCountDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationTypeDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationTypeFeature;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveAsyncFeature;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveDownloadFeature;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentFeature;
import fi.riista.feature.permit.application.conflict.HarvestPermitApplicationConflictDTO;
import fi.riista.feature.permit.application.conflict.HarvestPermitApplicationConflictPalstaDTO;
import fi.riista.feature.permit.application.conflict.ListPermitApplicationConflictsFeature;
import fi.riista.feature.permit.application.fragment.ListApplicationAreaFragmentsFeature;
import fi.riista.feature.permit.application.geometry.HarvestPermitApplicationGeometryFeature;
import fi.riista.feature.permit.application.metsahallitus.MetsahallitusAreaPermitImportFeature;
import fi.riista.feature.permit.application.metsahallitus.MetsahallitusAreaPermitNumbersDTO;
import fi.riista.feature.permit.application.metsahallitus.MetsahallitusAreaPermitUrlsDTO;
import fi.riista.feature.permit.application.partner.AddPermitApplicationPartnerDTO;
import fi.riista.feature.permit.application.partner.ListPermitApplicationAreaPartnersFeature;
import fi.riista.feature.permit.application.partner.ModifyPermitApplicationPartnersFeature;
import fi.riista.feature.permit.application.pdf.HarvestPermitApplicationMapPdfFeature;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationHandlerDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchFeature;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchResultDTO;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmountFeature;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaHuntingYearException;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerDTO;
import fi.riista.feature.permit.area.partner.MetsahallitusYearMismatchException;
import fi.riista.integration.common.HttpProxyService;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.MediaTypeExtras;
import io.vavr.Tuple3;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.apache.http.auth.AuthenticationException;
import org.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT;
import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.SHOOTER_LIST;

@RestController
@RequestMapping(value = "/api/v1/harvestpermit/application")
public class HarvestPermitApplicationApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationApiResource.class);

    @Resource
    private HarvestPermitApplicationFeature harvestPermitApplicationsFeature;

    @Resource
    private HarvestPermitApplicationTypeFeature harvestPermitApplicationTypeFeature;

    @Resource
    private PermitApplicationArchiveAsyncFeature permitApplicationArchiveAsyncFeature;

    @Resource
    private PermitApplicationArchiveDownloadFeature permitApplicationArchiveDownloadFeature;

    @Resource
    private HarvestPermitApplicationMapPdfFeature harvestPermitApplicationMapPdfFeature;

    @Resource
    private HarvestPermitApplicationSearchFeature harvestPermitApplicationSearchFeature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountFeature harvestPermitApplicationSpeciesAmountFeature;

    @Resource
    private HarvestPermitApplicationAttachmentFeature harvestPermitApplicationAttachmentFeature;

    @Resource
    private ListPermitApplicationsFeature listPermitApplicationsFeature;

    @Resource
    private ListPermitApplicationAreaPartnersFeature listPermitApplicationAreaPartnersFeature;

    @Resource
    private ModifyPermitApplicationPartnersFeature modifyPermitApplicationPartnersFeature;

    @Resource
    private HarvestPermitApplicationGeometryFeature harvestPermitApplicationGeometryFeature;

    @Resource
    private ListPermitApplicationConflictsFeature listPermitApplicationConflictsFeature;

    @Resource
    private ListApplicationAreaFragmentsFeature listPermitAreaFragmentsFeature;

    @Resource
    private ContentTypeChecker contentTypeChecker;

    @Resource
    private MetsahallitusAreaPermitImportFeature mhAreaPermitImportFeature;

    @Resource
    private HttpProxyService httpProxyService;

    // TYPES
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationTypeDTO> listTypes() {
        return harvestPermitApplicationTypeFeature.listTypes();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/findtype", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitApplicationTypeDTO findType(@RequestParam long applicationId) {
        return harvestPermitApplicationTypeFeature.findTypeForApplication(applicationId);
    }

    // LIST

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<?> listApplications(
            @RequestParam(required = false) Long personId,
            @RequestParam(required = false) String rhyOfficialCode,
            @RequestParam final int huntingYear,
            @RequestParam(required = false) Integer gameSpeciesCode,
            final Locale locale) {
        if (StringUtils.hasText(rhyOfficialCode)) {
            return harvestPermitApplicationSearchFeature.listRhyApplications(rhyOfficialCode, huntingYear, gameSpeciesCode, locale);
        }

        return listPermitApplicationsFeature.listApplicationsForPerson(personId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/years", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Integer> listRhyHuntingYears(@RequestParam(required = false) String rhyOfficialCode) {
        if (StringUtils.hasText(rhyOfficialCode)) {
            return harvestPermitApplicationSearchFeature.listRhyYears(rhyOfficialCode);
        }

        return ImmutableList.of();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/handlers", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationHandlerDTO> listHandlers() {
        return harvestPermitApplicationSearchFeature.listHandlers();
    }

    // SEARCH

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationSearchResultDTO> searchApplications(
            final @RequestBody @Valid HarvestPermitApplicationSearchDTO dto) {
        return harvestPermitApplicationSearchFeature.search(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/assigned/applications", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationSearchResultDTO> listApplicationsAssignedToMe() {
        return harvestPermitApplicationSearchFeature.listApplicationsAssignedToMe();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/assigned/decisions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationSearchResultDTO> listDecisionsAssignedToMe() {
        return harvestPermitApplicationSearchFeature.listDecisionsAssignedToMe();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/search/postalqueue", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationSearchResultDTO> listPostalQueue() {
        return harvestPermitApplicationSearchFeature.listPostalQueue();
    }

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitApplicationBasicDetailsDTO basicDetails(@PathVariable final long applicationId) {
        return harvestPermitApplicationsFeature.getBasicDetails(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitApplicationSummaryDTO allDetails(@PathVariable final long applicationId) {
        return harvestPermitApplicationsFeature.getAllDetails(applicationId);
    }

    // CREATE

    @PostMapping
    public HarvestPermitApplicationBasicDetailsDTO createApplication(
            final @RequestBody @Valid HarvestPermitApplicationCreateDTO dto) {
        return harvestPermitApplicationsFeature.create(dto);
    }

    @PostMapping("/{applicationId:\\d+}/validate")
    public ResponseEntity<Map<String, Boolean>> validate(@PathVariable final long applicationId) {
        try {
            harvestPermitApplicationsFeature.validate(applicationId);
            return ResponseEntity.ok(ImmutableMap.of("valid", true));
        } catch (Exception e) {
            return ResponseEntity.ok(ImmutableMap.of("valid", false));
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{applicationId:\\d+}/send")
    public void sendApplication(final @PathVariable long applicationId,
                                final @RequestBody @Valid HarvestPermitApplicationSendDTO dto) throws Exception {
        dto.setId(applicationId);
        harvestPermitApplicationsFeature.sendApplication(dto);
        harvestPermitApplicationsFeature.asyncSendNotification(applicationId);
        permitApplicationArchiveAsyncFeature.asyncCreateArchive(applicationId);
    }

    @PostMapping("/{applicationId:\\d+}/amend/start")
    public void startAmending(@PathVariable final long applicationId) {
        harvestPermitApplicationsFeature.startAmendApplication(applicationId);
    }

    @PostMapping("/{applicationId:\\d+}/amend/stop")
    public void stopAmending(@PathVariable final long applicationId,
                             @RequestBody @Valid HarvestPermitApplicationAmendDTO dto) {
        dto.setId(applicationId);
        harvestPermitApplicationsFeature.stopAmendApplication(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{applicationId:\\d+}/archive")
    public void archive(@PathVariable final long applicationId, final HttpServletResponse response) throws IOException {
        permitApplicationArchiveDownloadFeature.downloadOriginalArchive(applicationId, response);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/archive/generate")
    public String createArchiveIfMissing(@PathVariable final long applicationId) throws Exception {
        return permitApplicationArchiveAsyncFeature.createArchiveIfMissing(applicationId);
    }

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HuntingClubDTO> listAvailablePermitHolders(@PathVariable final long applicationId) {
        return harvestPermitApplicationsFeature.listAvailablePermitHolders(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder-search")
    public HuntingClubDTO searchPermitHolder(@PathVariable final long applicationId, @RequestParam final String officialCode) {
        return harvestPermitApplicationsFeature.findClubByOfficialCode(officialCode);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(
            @PathVariable final long applicationId,
            @Valid @RequestBody(required = false) HuntingClubDTO permitHolder) {
        harvestPermitApplicationsFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // SPECIES AMOUNTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(@PathVariable final long applicationId) {
        return harvestPermitApplicationSpeciesAmountFeature.getSpeciesAmounts(applicationId);
    }

    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationSpeciesAmountDTO> saveSpeciesAmounts(
            @PathVariable final long applicationId,
            @Valid @RequestBody List<HarvestPermitApplicationSpeciesAmountDTO> dto) {
        return harvestPermitApplicationSpeciesAmountFeature.saveSpeciesAmounts(applicationId, dto);
    }

    // SHOOTER COUNTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/shooters", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitApplicationShooterCountDTO getShooterCounts(final @PathVariable long applicationId) {
        return harvestPermitApplicationsFeature.getShooterCounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/shooters")
    public void updateShooterCounts(final @PathVariable long applicationId,
                                    final @Valid @RequestBody HarvestPermitApplicationShooterCountDTO dto) {
        harvestPermitApplicationsFeature.updateShooterCounts(applicationId, dto);
    }

    // ADDITIONAL DATA
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/additional")
    public void updateEmails(final @PathVariable long applicationId,
                             final @Valid @RequestBody HarvestPermitApplicationAdditionalDataDTO additionalData) {
        harvestPermitApplicationsFeature.updateAdditionalData(applicationId, additionalData);
    }

    // MH PERMIT AND SHOOTER LIST
    @PostMapping(value = "/{applicationId:\\d+}/mh")
    public ResponseEntity importMhPermit(final @PathVariable long applicationId,
                                         final @Valid @RequestBody MetsahallitusAreaPermitNumbersDTO dto) {
        try {
            final MetsahallitusAreaPermitUrlsDTO urls = mhAreaPermitImportFeature.fetchUrls(dto);
            final int mhPermitNumber = dto.getMhPermitNumber();

            final MultipartFile verdictFile = mhAreaPermitImportFeature.downloadFile(urls.getVerdictFileUrl(), "Aluelupa_" + mhPermitNumber);
            harvestPermitApplicationAttachmentFeature.addAttachment(applicationId, MH_AREA_PERMIT, verdictFile);

            for (final String url : urls.getShooterListFileUrls()) {
                final MultipartFile shooterListFile = mhAreaPermitImportFeature.downloadFile(url, "Ampujaluettelo_" + mhPermitNumber);
                harvestPermitApplicationAttachmentFeature.addAttachment(applicationId, SHOOTER_LIST, shooterListFile);
            }
            return ResponseEntity.ok().build();

        } catch (final NotFoundException nfe) {
            return ResponseEntity.notFound().build();
        } catch (final IOException | AuthenticationException e) {
            LOG.error("", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    // ATTACHMENTS

    @GetMapping(value = "/{applicationId:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationAttachmentDTO> listAttachments(final @PathVariable long applicationId) {
        return harvestPermitApplicationAttachmentFeature.listAttachments(applicationId);
    }

    @PostMapping(value = "/{applicationId:\\d+}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> addAttachment(final @PathVariable long applicationId,
                                                final @RequestParam
                                                        HarvestPermitApplicationAttachment.Type attachmentType,
                                                final @RequestParam("file") MultipartFile file) {
        if (!contentTypeChecker.isValidApplicationAttachmentContent(file)) {
            return ResponseEntity.badRequest().build();
        }
        harvestPermitApplicationAttachmentFeature.addAttachment(applicationId, attachmentType, file);

        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{applicationId:\\d+}/attachment/{attachmentId:\\d+}")
    public void deleteAttachment(@PathVariable final long attachmentId) {
        harvestPermitApplicationAttachmentFeature.deleteAttachment(attachmentId);
    }

    @PostMapping(value = "/{applicationId:\\d+}/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable final long attachmentId,
                                                HttpServletResponse response) throws IOException {
        final Tuple3<ResponseEntity<byte[]>, URL, String> urlAndAttachmentFilename =
                harvestPermitApplicationAttachmentFeature.getAttachment(attachmentId);

        if (urlAndAttachmentFilename._1 != null) {
            return urlAndAttachmentFilename._1;
        }

        final URL url = urlAndAttachmentFilename._2;
        final String filename = urlAndAttachmentFilename._3;

        httpProxyService.downloadFile(response,
                HttpProxyService.toUri(url), null,
                filename, null);

        return null;
    }

    // AREA PARTNERS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/{applicationId:\\d+}/partner")
    public List<HarvestPermitAreaPartnerDTO> listPartners(@PathVariable final long applicationId, final Locale locale) {
        return listPermitApplicationAreaPartnersFeature.listPartners(applicationId, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/{applicationId:\\d+}/partner/club")
    public List<OrganisationNameDTO> listPartnerClubs(@PathVariable final long applicationId) {
        return listPermitApplicationAreaPartnersFeature.listPartnerClubs(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/{applicationId:\\d+}/partner/available")
    public List<OrganisationNameDTO> listAvailablePartners(@PathVariable final long applicationId) {
        return listPermitApplicationAreaPartnersFeature.listAvailablePartners(applicationId);
    }

    @PostMapping("/{applicationId:\\d+}/partner/excel")
    public ModelAndView listPartnersExcel(@PathVariable final long applicationId, final Locale locale) {
        return new ModelAndView(listPermitApplicationAreaPartnersFeature.listPartnersExcel(applicationId, locale));
    }

    @PostMapping(value = "/{applicationId:\\d+}/partner")
    public ResponseEntity<?> addPartnerArea(@Valid @RequestBody AddPermitApplicationPartnerDTO dto) {
        try {
            modifyPermitApplicationPartnersFeature.addPartner(dto);
            return ResponseEntity.noContent().build();

        } catch (final HarvestPermitAreaHuntingYearException | MetsahallitusYearMismatchException e) {
            return ResponseEntity.badRequest().body(ImmutableMap.of("exception", e.getClass().getSimpleName()));
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{applicationId:\\d+}/partner/{partnerId:\\d+}")
    public void removePartnerArea(@PathVariable long applicationId, @PathVariable long partnerId) {
        modifyPermitApplicationPartnersFeature.removePartner(applicationId, partnerId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/partner/{partnerId:\\d+}")
    public void refreshPartnerGeometry(@PathVariable long applicationId,
                                       @PathVariable long partnerId) {
        modifyPermitApplicationPartnersFeature.refreshPartner(applicationId, partnerId);
    }

    // CALCULATE GEOMETRY

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HarvestPermitApplicationAreaDTO getPermitArea(@PathVariable final long applicationId) {
        return harvestPermitApplicationGeometryFeature.getPermitArea(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area/status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, HarvestPermitArea.StatusCode> getStatus(@PathVariable long applicationId) {
        return ImmutableMap.of("status", harvestPermitApplicationGeometryFeature.getStatus(applicationId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/area/ready")
    public void setReadyForProcessing(@PathVariable long applicationId) {
        harvestPermitApplicationGeometryFeature.setReadyForProcessing(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/area/incomplete")
    public void setIncomplete(@PathVariable long applicationId) {
        harvestPermitApplicationGeometryFeature.setIncomplete(applicationId);
    }

    // FINAL GEOMETRY

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area/bounds", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public GISBounds getGeometryBounds(@PathVariable final long applicationId) {
        return harvestPermitApplicationGeometryFeature.getBounds(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area/geometry", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection getGeometry(@PathVariable final long applicationId,
                                         @RequestParam(required = false) final String outputStyle) {
        return harvestPermitApplicationGeometryFeature.getGeometry(applicationId, outputStyle);
    }

    @PostMapping(value = "/{id:\\d+}/area/pdf", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> getMapPdf(@PathVariable final long id,
                                       @ModelAttribute @Valid final MapPdfParameters dto) {
        try {
            final MapPdfModel model = harvestPermitApplicationMapPdfFeature.getModel(id, LocaleContextHolder.getLocale());
            final byte[] imageData = harvestPermitApplicationMapPdfFeature.renderPdf(dto, model);

            return ResponseEntity.ok()
                    .contentType(MediaTypeExtras.APPLICATION_PDF)
                    .contentLength(imageData.length)
                    .headers(ContentDispositionUtil.header(model.getExportFileName()))
                    .body(imageData);

        } catch (final Exception ex) {
            LOG.error("Application map printing has failed", ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kartan tulostus epäonnistui. Yritä myöhemmin uudelleen");
        }
    }

    // FRAGMENTS

    @PostMapping(value = "/{applicationId:\\d+}/area/fragmentinfo", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public Map<String, Object> getGeometryFragmentInfo(@PathVariable final long applicationId,
                                                       @RequestBody @Valid final GeoLocation location) {
        return listPermitAreaFragmentsFeature.getFragmentInfo(applicationId, location);
    }

    @PostMapping(value = "/{applicationId:\\d+}/area/fragments/excel")
    public ModelAndView getFragmentsExcel(@PathVariable final long applicationId) {
        return new ModelAndView(listPermitAreaFragmentsFeature.getFragmentExcel(applicationId));
    }

    // CONFLICTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/conflicts", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationConflictDTO> listConflicts(@PathVariable long applicationId) {
        return listPermitApplicationConflictsFeature.listConflicts(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/conflicts/{otherId:\\d+}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HarvestPermitApplicationConflictPalstaDTO> listConflictsWithAnotherApplication(
            @PathVariable long applicationId, @PathVariable long otherId) {
        return listPermitApplicationConflictsFeature.listConflictsWithAnotherApplication(applicationId, otherId);
    }

    @PostMapping(value = "/{applicationId:\\d+}/conflicts/excel")
    public ModelAndView conflictsExcel(@PathVariable final long applicationId) {
        return new ModelAndView(listPermitApplicationConflictsFeature.getConflictsExcel(applicationId));
    }
}
