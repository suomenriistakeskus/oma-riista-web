package fi.riista.api.application;

import com.google.common.collect.ImmutableMap;
import fi.riista.config.Constants;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentFeature;
import fi.riista.feature.permit.application.conflict.HarvestPermitApplicationConflictDTO;
import fi.riista.feature.permit.application.conflict.HarvestPermitApplicationConflictPalstaDTO;
import fi.riista.feature.permit.application.conflict.ListPermitApplicationConflictsFeature;
import fi.riista.feature.permit.application.conflict.PrintApplicationConflictFeature;
import fi.riista.feature.permit.application.conflict.PrintApplicationConflictMapModel;
import fi.riista.feature.permit.application.conflict.PrintApplicationConflictRequestDTO;
import fi.riista.feature.permit.application.fragment.HarvestPermitAreaFragmentExcelRequestDTO;
import fi.riista.feature.permit.application.fragment.HarvestPermitAreaFragmentInfoDTO;
import fi.riista.feature.permit.application.fragment.HarvestPermitAreaFragmentRequestDTO;
import fi.riista.feature.permit.application.fragment.ListApplicationAreaFragmentsFeature;
import fi.riista.feature.permit.application.fragment.PrintApplicationAreaFragmentDTO;
import fi.riista.feature.permit.application.fragment.PrintApplicationAreaFragmentFeature;
import fi.riista.feature.permit.application.fragment.PrintApplicationAreaFragmentListDTO;
import fi.riista.feature.permit.application.fragment.PrintApplicationAreaFragmentMapModel;
import fi.riista.feature.permit.application.geometry.HarvestPermitApplicationGeometryFeature;
import fi.riista.feature.permit.application.metsahallitus.MetsahallitusAreaPermitImportFeature;
import fi.riista.feature.permit.application.metsahallitus.MetsahallitusAreaPermitNumbersDTO;
import fi.riista.feature.permit.application.metsahallitus.MetsahallitusAreaPermitUrlsDTO;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationAreaDTO;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationHolderFeature;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationShooterCountDTO;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationShooterCountFeature;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationSpeciesFeature;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationSummaryFeature;
import fi.riista.feature.permit.application.partner.AddPermitApplicationPartnerDTO;
import fi.riista.feature.permit.application.partner.ListPermitApplicationAreaPartnersFeature;
import fi.riista.feature.permit.application.partner.ModifyPermitApplicationPartnersFeature;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaHuntingYearException;
import fi.riista.feature.permit.area.partner.HarvestPermitAreaPartnerDTO;
import fi.riista.feature.permit.area.partner.MetsahallitusYearMismatchException;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.apache.http.auth.AuthenticationException;
import org.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;
import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.MH_AREA_PERMIT;
import static fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment.Type.SHOOTER_LIST;

@RestController
@RequestMapping(value = API_PREFIX + "/mooselike")
public class MoosePermitApplicationApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(MoosePermitApplicationApiResource.class);

    @Resource
    private MooselikePermitApplicationSummaryFeature mooselikePermitApplicationSummaryFeature;

    @Resource
    private MooselikePermitApplicationHolderFeature mooselikePermitApplicationHolderFeature;

    @Resource
    private MooselikePermitApplicationSpeciesFeature mooselikePermitApplicationSpeciesFeature;

    @Resource
    private MooselikePermitApplicationShooterCountFeature mooselikePermitApplicationShooterCountFeature;

    @Resource
    private HarvestPermitApplicationAttachmentFeature harvestPermitApplicationAttachmentFeature;

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
    private PrintApplicationAreaFragmentFeature printApplicationAreaFragmentFeature;

    @Resource
    private PrintApplicationConflictFeature printApplicationConflictFeature;

    @Resource
    private MetsahallitusAreaPermitImportFeature mhAreaPermitImportFeature;

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public MooselikePermitApplicationSummaryDTO allDetails(@PathVariable final long applicationId) {
        return mooselikePermitApplicationSummaryFeature.getAllDetails(applicationId);
    }

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HuntingClubDTO> listAvailablePermitHolders(@PathVariable final long applicationId) {
        return mooselikePermitApplicationHolderFeature.listAvailablePermitHolders(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder-search")
    public HuntingClubDTO searchPermitHolder(
            @PathVariable final long applicationId, @RequestParam final String officialCode) {
        return mooselikePermitApplicationHolderFeature.findClubByOfficialCode(officialCode);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder-club")
    public void updatePermitHolderClub(
            @PathVariable final long applicationId,
            @Valid @RequestBody(required = false) final HuntingClubDTO permitHolder) {
        mooselikePermitApplicationHolderFeature.updateHuntingClub(applicationId, permitHolder);
    }

    // SPECIES AMOUNTS

    static class AmountList {

        @NotEmpty
        @Valid
        public List<MooselikePermitApplicationSpeciesAmountDTO> list;

        public List<MooselikePermitApplicationSpeciesAmountDTO> getList() {
            return list;
        }

        public void setList(final List<MooselikePermitApplicationSpeciesAmountDTO> list) {
            this.list = list;
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MooselikePermitApplicationSpeciesAmountDTO> getSpeciesAmounts(@PathVariable final long applicationId) {
        return mooselikePermitApplicationSpeciesFeature.getSpeciesAmounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesAmounts(
            final @PathVariable long applicationId,
            final @Valid @RequestBody AmountList dto) {
        mooselikePermitApplicationSpeciesFeature.saveSpeciesAmounts(applicationId, dto.list);
    }

    // MH PERMIT AND SHOOTER LIST
    @PostMapping(value = "/{applicationId:\\d+}/mh")
    public ResponseEntity importMhPermit(final @PathVariable long applicationId,
                                         final @Valid @RequestBody MetsahallitusAreaPermitNumbersDTO dto) {
        try {
            final MetsahallitusAreaPermitUrlsDTO urls = mhAreaPermitImportFeature.fetchUrls(dto);
            final int mhPermitNumber = dto.getMhPermitNumber();

            final MultipartFile verdictFile = mhAreaPermitImportFeature.downloadFile(urls.getVerdictFileUrl(),
                    "Aluelupa_" + mhPermitNumber);
            harvestPermitApplicationAttachmentFeature.addAttachment(applicationId, MH_AREA_PERMIT, verdictFile);

            for (final String url : urls.getShooterListFileUrls()) {
                final MultipartFile shooterListFile = mhAreaPermitImportFeature.downloadFile(url,
                        "Ampujaluettelo_" + mhPermitNumber);
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

    // SHOOTER COUNTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/shooters", produces = MediaType.APPLICATION_JSON_VALUE)
    public MooselikePermitApplicationShooterCountDTO getShooterCounts(final @PathVariable long applicationId) {
        return mooselikePermitApplicationShooterCountFeature.getShooterCounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/shooters")
    public void updateShooterCounts(final @PathVariable long applicationId,
                                    final @Valid @RequestBody MooselikePermitApplicationShooterCountDTO dto) {
        mooselikePermitApplicationShooterCountFeature.updateShooterCounts(applicationId, dto);
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
    @GetMapping(value = "/{applicationId:\\d+}/area", produces = MediaType.APPLICATION_JSON_VALUE)
    public MooselikePermitApplicationAreaDTO getPermitArea(@PathVariable final long applicationId,
                                                           final Locale locale) {
        return harvestPermitApplicationGeometryFeature.getPermitArea(applicationId, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area/status", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @GetMapping(value = "/{applicationId:\\d+}/area/bounds", produces = MediaType.APPLICATION_JSON_VALUE)
    public GISBounds getGeometryBounds(@PathVariable final long applicationId) {
        return harvestPermitApplicationGeometryFeature.getBounds(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area/geometry", produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public FeatureCollection getGeometry(@PathVariable final long applicationId,
                                         @RequestParam(required = false) final String outputStyle) {
        if ("partner".equals(outputStyle)) {
            return harvestPermitApplicationGeometryFeature.getPermitAreaForEachPartner(applicationId);
        } else {
            return harvestPermitApplicationGeometryFeature.getPermitAreaCombined(applicationId);
        }
    }

    // FRAGMENTS

    @PostMapping(value = "/{applicationId:\\d+}/area/fragmentinfo", produces =
            MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public List<HarvestPermitAreaFragmentInfoDTO> getGeometryFragmentInfo(
            final @PathVariable long applicationId,
            final @RequestBody @Valid HarvestPermitAreaFragmentRequestDTO dto) {
        return listPermitAreaFragmentsFeature.getFragmentInfo(dto);
    }

    @PostMapping(value = "/{applicationId:\\d+}/area/fragments/excel")
    public ModelAndView getFragmentsExcel(final @PathVariable long applicationId,
                                          final @Valid @RequestBody HarvestPermitAreaFragmentExcelRequestDTO dto,
                                          final Locale locale) {
        return new ModelAndView(listPermitAreaFragmentsFeature.getFragmentExcel(applicationId, dto, locale));
    }

    @PostMapping(value = "/{applicationId:\\d+}/area/fragments/{fragmentId:[0-9A-Za-z]{8}}/print",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> print(final @PathVariable long applicationId,
                                   final @PathVariable String fragmentId,
                                   final Locale locale,
                                   final @ModelAttribute @Valid MapPdfParameters dto) {
        final PrintApplicationAreaFragmentDTO fragmentData =
                printApplicationAreaFragmentFeature.getFragmentData(applicationId, fragmentId);
        final PrintApplicationAreaFragmentMapModel mapModel = printApplicationAreaFragmentFeature.getMapModelWithApproachMap(
                applicationId,
                fragmentData,
                dto,
                locale);

        final byte[] pdfData =
                printApplicationAreaFragmentFeature.createSingleFragmentWithApproachPdf(fragmentData, mapModel, locale);

        return ResponseEntity.ok()
                .contentType(MediaTypeExtras.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .headers(ContentDispositionUtil.header(mapModel.getFilename()))
                .body(pdfData);
    }

    @PostMapping(value = "/{applicationId:\\d+}/area/fragments/print",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> printFragments(final @PathVariable long applicationId,
                                            final @Valid @RequestBody PrintApplicationAreaFragmentListDTO fragmentsDto,
                                            final Locale locale) {
        try {
            final PrintApplicationAreaFragmentFeature.PdfData pdfData =
                    printApplicationAreaFragmentFeature.makeConcatenatedPdf(applicationId,
                            fragmentsDto, locale);
            return ResponseEntity.ok()
                    .contentType(MediaTypeExtras.APPLICATION_PDF)
                    .contentLength(pdfData.getData().length)
                    .headers(ContentDispositionUtil.header(pdfData.getFilename()))
                    .body(pdfData.getData());
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }

    }

    // CONFLICTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/conflicts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HarvestPermitApplicationConflictDTO> listConflicts(@PathVariable long applicationId) {
        return listPermitApplicationConflictsFeature.listConflicts(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/conflicts/{otherId:\\d+}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HarvestPermitApplicationConflictPalstaDTO> listConflictsWithAnotherApplication(
            @PathVariable long applicationId, @PathVariable long otherId) {
        return listPermitApplicationConflictsFeature.listConflictsWithAnotherApplication(applicationId, otherId);
    }

    @PostMapping(value = "/{applicationId:\\d+}/conflicts/excel")
    public ModelAndView conflictsExcel(@PathVariable final long applicationId) {
        return new ModelAndView(listPermitApplicationConflictsFeature.getConflictsExcel(applicationId));
    }

    @PostMapping(value = "/conflicts/print")
    public ResponseEntity<?> printConflictMap(final Locale locale,
                                              @RequestBody @Valid final PrintApplicationConflictRequestDTO dto) throws IOException {
        final PrintApplicationConflictMapModel model = printApplicationConflictFeature.getModel(dto, locale);
        final byte[] pdfData = printApplicationConflictFeature.createPdf(model, locale);
        final String fileName = Constants.FILENAME_TS_PATTERN.print(DateUtil.now()) + "_konflikti.pdf";

        return ResponseEntity.ok()
                .contentType(MediaTypeExtras.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .headers(ContentDispositionUtil.header(fileName))
                .body(pdfData);
    }
}
