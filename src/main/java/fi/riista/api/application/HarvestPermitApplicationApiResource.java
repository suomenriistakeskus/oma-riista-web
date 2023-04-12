package fi.riista.api.application;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.ContentTypeChecker;
import fi.riista.feature.harvestpermit.list.ApplicationDecisionPermitListDTO;
import fi.riista.feature.harvestpermit.list.ApplicationDecisionPermitListFeature;
import fi.riista.feature.permit.application.HarvestPermitApplicationAdditionalDataDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationAsyncFeature;
import fi.riista.feature.permit.application.HarvestPermitApplicationBasicDetailsDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationDeleteFeature;
import fi.riista.feature.permit.application.HarvestPermitApplicationFeature;
import fi.riista.feature.permit.application.archive.PermitApplicationArchiveDownloadFeature;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachmentFeature;
import fi.riista.feature.permit.application.create.HarvestPermitApplicationCreateDTO;
import fi.riista.feature.permit.application.create.HarvestPermitApplicationCreateFeature;
import fi.riista.feature.permit.application.create.HarvestPermitApplicationTypeDTO;
import fi.riista.feature.permit.application.create.HarvestPermitApplicationTypeFeature;
import fi.riista.feature.permit.application.schedule.HarvestPermitApplicationScheduleCrudFeature;
import fi.riista.feature.permit.application.schedule.HarvestPermitApplicationScheduleDTO;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchFeature;
import fi.riista.feature.permit.application.send.HarvestPermitApplicationSendDTO;
import fi.riista.feature.permit.application.send.HarvestPermitApplicationSendFeature;
import fi.riista.feature.permit.application.validation.HarvestPermitApplicationValidationFeature;
import fi.riista.feature.permit.area.pdf.PermitAreaMapPdfFeature;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX)
public class HarvestPermitApplicationApiResource {

    public static final String API_PREFIX = "/api/v1/harvestpermit/application";

    @Resource
    private HarvestPermitApplicationFeature harvestPermitApplicationsFeature;

    @Resource
    private HarvestPermitApplicationTypeFeature harvestPermitApplicationTypeFeature;

    @Resource
    private HarvestPermitApplicationValidationFeature harvestPermitApplicationValidationFeature;

    @Resource
    private PermitApplicationArchiveDownloadFeature permitApplicationArchiveDownloadFeature;

    @Resource
    private HarvestPermitApplicationSearchFeature harvestPermitApplicationSearchFeature;

    @Resource
    private HarvestPermitApplicationAttachmentFeature harvestPermitApplicationAttachmentFeature;

    @Resource
    private ApplicationDecisionPermitListFeature applicationDecisionPermitListFeature;

    @Resource
    private HarvestPermitApplicationCreateFeature harvestPermitApplicationCreateFeature;

    @Resource
    private HarvestPermitApplicationDeleteFeature harvestPermitApplicationDeleteFeature;

    @Resource
    private HarvestPermitApplicationSendFeature harvestPermitApplicationSendFeature;

    @Resource
    private HarvestPermitApplicationAsyncFeature harvestPermitApplicationAsyncFeature;

    @Resource
    private PermitAreaMapPdfFeature permitAreaMapPdfFeature;

    @Resource
    private MapPdfRemoteService mapPdfRemoteService;

    @Resource
    private ContentTypeChecker contentTypeChecker;

    @Resource
    private HarvestPermitApplicationScheduleCrudFeature harvestPermitApplicationScheduleCrudFeature;

    // TYPES

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HarvestPermitApplicationTypeDTO> listTypes() {
        return harvestPermitApplicationTypeFeature.listTypes();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/findtype", produces = MediaType.APPLICATION_JSON_VALUE)
    public HarvestPermitApplicationTypeDTO findType(@RequestParam long applicationId) {
        return harvestPermitApplicationTypeFeature.findTypeForApplication(applicationId);
    }

    // PERSONAL LIST

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/myApplicationsAndDecisions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApplicationDecisionPermitListDTO> myApplicationsAndDecisions(
            @RequestParam(required = false) Long personId) {
        return applicationDecisionPermitListFeature.listApplicationsAndDecisionsForPerson(personId);
    }

    // RHY LIST

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<?> listApplications(
            @RequestParam String rhyOfficialCode,
            @RequestParam final int huntingYear,
            @RequestParam(required = false) Integer gameSpeciesCode,
            final Locale locale) {

        return harvestPermitApplicationSearchFeature.listRhyApplications(rhyOfficialCode, huntingYear, gameSpeciesCode, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/years", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Integer> listRhyHuntingYears(@RequestParam(required = false) String rhyOfficialCode) {
        if (StringUtils.hasText(rhyOfficialCode)) {
            return harvestPermitApplicationSearchFeature.listRhyYears(rhyOfficialCode);
        }

        return ImmutableList.of();
    }

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HarvestPermitApplicationBasicDetailsDTO basicDetails(@PathVariable final long applicationId) {
        return harvestPermitApplicationsFeature.getBasicDetails(applicationId);
    }

    @PostMapping(value = "/{id:\\d+}/area/pdf", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> getMapPdf(@PathVariable final long id, final Locale locale,
                                       @ModelAttribute @Valid final MapPdfParameters dto) {
        return mapPdfRemoteService.renderPdf(dto, () -> permitAreaMapPdfFeature.getModelForApplication(id, dto.getOverlay(), locale));
    }

    // CREATE

    @PostMapping
    public HarvestPermitApplicationBasicDetailsDTO createApplication(
            final @RequestBody @Valid HarvestPermitApplicationCreateDTO dto, final Locale locale) {
        return harvestPermitApplicationCreateFeature.create(dto, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{applicationId:\\d+}/archive")
    public void archive(@PathVariable final long applicationId, final HttpServletResponse response) throws IOException {
        permitApplicationArchiveDownloadFeature.downloadOriginalArchive(applicationId, response);
    }

    // DELETE

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{applicationId:\\d+}")
    public void deleteApplication(final @PathVariable long applicationId) {
        harvestPermitApplicationDeleteFeature.deleteApplication(applicationId);
    }

    // ATTACHMENTS

    @GetMapping(value = "/{applicationId:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HarvestPermitApplicationAttachmentDTO> listAttachments(final @PathVariable long applicationId) {
        return harvestPermitApplicationAttachmentFeature.listAttachments(applicationId);
    }

    @PostMapping(value = "/{applicationId:\\d+}/attachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addAttachment(final @PathVariable long applicationId,
                                                final @RequestParam HarvestPermitApplicationAttachment.Type attachmentType,
                                                final @RequestParam("file") MultipartFile file) {
        if (!contentTypeChecker.isValidApplicationAttachmentContent(file)) {
            return ResponseEntity.badRequest().build();
        }

        final Long attachmentId = harvestPermitApplicationAttachmentFeature.addAttachment(
                applicationId, attachmentType, file);

        return ResponseEntity.ok(Collections.singletonMap("id", attachmentId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{applicationId:\\d+}/attachment/{attachmentId:\\d+}")
    public void deleteAttachment(@PathVariable final long attachmentId) {
        harvestPermitApplicationAttachmentFeature.deleteAttachment(attachmentId);
    }

    @PostMapping(value = "/{applicationId:\\d+}/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable final long attachmentId) throws IOException {
        return harvestPermitApplicationAttachmentFeature.getAttachment(attachmentId);
    }

    // ADDITIONAL DATA

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/additional")
    public void updateEmails(final @PathVariable long applicationId,
                             final @Valid @RequestBody HarvestPermitApplicationAdditionalDataDTO additionalData) {
        harvestPermitApplicationsFeature.updateAdditionalData(applicationId, additionalData);
    }

    // VALIDATE

    @PostMapping("/{applicationId:\\d+}/validate")
    public ResponseEntity<Map<String, Boolean>> validate(@PathVariable final long applicationId) {
        try {
            harvestPermitApplicationValidationFeature.validate(applicationId);
            return ResponseEntity.ok(ImmutableMap.of("valid", true));
        } catch (Exception e) {
            return ResponseEntity.ok(ImmutableMap.of("valid", false));
        }
    }

    // ARCHIVE

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/archive/generate")
    public String createArchiveIfMissing(@PathVariable final long applicationId) throws Exception {
        return harvestPermitApplicationAsyncFeature.createArchiveIfMissing(applicationId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/archive/recreate-for-year/{year:\\d+}")
    public String recreateArchivesForYear(@PathVariable final int year) {
        // Updates existing PermitApplicationArchive entity, make sure to backup original just in case
        harvestPermitApplicationAsyncFeature.recreateArchivesForYear(year);
        return "Archive recreation started for " + year;
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/archive/recreate")
    public String recreateArchives(final @RequestParam List<Long> ids) {
        // Updates existing PermitApplicationArchive entity, make sure to backup original just in case
        harvestPermitApplicationAsyncFeature.recreateArchives(ids);
        return "Archive recreation started";
    }

    // SEND

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{applicationId:\\d+}/send")
    public void sendApplication(final @PathVariable long applicationId,
                                final @RequestBody @Valid HarvestPermitApplicationSendDTO dto) throws Exception {
        dto.setId(applicationId);
        harvestPermitApplicationSendFeature.sendApplication(dto);
        harvestPermitApplicationAsyncFeature.asyncCreateArchive(applicationId);
        harvestPermitApplicationAsyncFeature.asyncSendEmailNotification(applicationId);
        harvestPermitApplicationAsyncFeature.asyncSendModeratorNotification(applicationId);
    }

    @GetMapping("/schedules")
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public List<HarvestPermitApplicationScheduleDTO> listSchedules() {
        return harvestPermitApplicationScheduleCrudFeature.list();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/schedules")
    public void updateSchedule(final @Valid @RequestBody HarvestPermitApplicationScheduleDTO dto) {
        harvestPermitApplicationScheduleCrudFeature.update(dto);
    }
}
