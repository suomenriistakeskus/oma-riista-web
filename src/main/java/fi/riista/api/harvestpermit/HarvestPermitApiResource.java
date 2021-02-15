package fi.riista.api.harvestpermit;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermitAcceptHarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermitAcceptHarvestFeature;
import fi.riista.feature.harvestpermit.HarvestPermitContactPersonDTO;
import fi.riista.feature.harvestpermit.HarvestPermitContactPersonFeature;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.HarvestPermitDetailsFeature;
import fi.riista.feature.harvestpermit.attachment.HarvestPermitAttachmentFeature;
import fi.riista.feature.harvestpermit.download.HarvestPermitDownloadDecisionDTO;
import fi.riista.feature.harvestpermit.download.HarvestPermitDownloadDecisionFeature;
import fi.riista.feature.harvestpermit.download.HarvestPermitPdfMissingException;
import fi.riista.feature.harvestpermit.list.HarvestPermitListFeature;
import fi.riista.feature.harvestpermit.list.ListHarvestPermitDTO;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportExcelDTO;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportListExcelView;
import fi.riista.feature.harvestpermit.report.search.HarvestReportSearchFeature;
import fi.riista.feature.harvestpermit.search.HarvestPermitExistsDTO;
import fi.riista.feature.harvestpermit.search.HarvestPermitSearchDTO;
import fi.riista.feature.harvestpermit.search.HarvestPermitSearchFeature;
import fi.riista.feature.harvestpermit.search.HarvestPermitSearchResultDTO;
import fi.riista.feature.harvestpermit.search.HarvestPermitTypeDTO;
import fi.riista.feature.harvestpermit.statistics.HarvestPermitUsageDTO;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachmentDTO;
import fi.riista.integration.lupahallinta.HarvestPermitImportException;
import fi.riista.integration.lupahallinta.HarvestPermitImportFeature;
import fi.riista.integration.lupahallinta.HarvestPermitImportResultDTO;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitListDTO;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitListFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/harvestpermit", produces = MediaType.APPLICATION_JSON_VALUE)
public class HarvestPermitApiResource {

    @Resource
    private HarvestPermitListFeature harvestPermitListFeature;

    @Resource
    private HarvestPermitDownloadDecisionFeature harvestPermitDownloadDecisionFeature;

    @Resource
    private HarvestPermitDetailsFeature harvestPermitDetailsFeature;

    @Resource
    private HarvestPermitAcceptHarvestFeature harvestPermitAcceptHarvestFeature;

    @Resource
    private HarvestPermitContactPersonFeature harvestPermitContactPersonFeature;

    @Resource
    private HarvestPermitAttachmentFeature harvestPermitAttachmentFeature;

    @Resource
    private HarvestPermitImportFeature harvestPermitImportFeature;

    @Resource
    private HarvestReportSearchFeature harvestReportSearchFeature;

    @Resource
    private HarvestPermitSearchFeature harvestPermitSearchFeature;

    @Resource
    private MetsahallitusPermitListFeature metsahallitusPermitListFeature;

    @Resource
    private MessageSource messageSource;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/permittypes")
    public List<HarvestPermitTypeDTO> listPermitTypes() {
        return harvestPermitSearchFeature.listPermitTypes();
    }

    @PostMapping("/checkPermitNumber")
    public HarvestPermitExistsDTO checkPermitNumber(@RequestParam String permitNumber) {
        return harvestPermitSearchFeature.checkHarvestPermitExists(permitNumber);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/mypermits")
    public List<ListHarvestPermitDTO> listMyPermits(@RequestParam(required = false) Long personId) {
        return harvestPermitListFeature.listPermitsForPerson(personId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/acceptHarvest")
    public void acceptHarvest(@RequestBody @Valid HarvestPermitAcceptHarvestDTO dto) {
        harvestPermitAcceptHarvestFeature.changeAcceptedToPermit(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/metsahallitus", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MetsahallitusPermitListDTO> listMetsahallitusPermits(@RequestParam(required = false) Long personId) {
        return metsahallitusPermitListFeature.listAll(personId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/{id:\\d+}")
    public HarvestPermitDTO getHarvestPermit(@PathVariable Long id) {
        return harvestPermitDetailsFeature.getPermit(id);
    }

    @PostMapping("/pdf/{permitNumber}")
    public void pdf(final @PathVariable String permitNumber,
                    final HttpServletResponse httpServletResponse) throws IOException {
        final HarvestPermitDownloadDecisionDTO dto =
                harvestPermitDownloadDecisionFeature.getDecisionPdf(permitNumber);

        if (dto.getLocalDecisionFileId() != null) {
            harvestPermitDownloadDecisionFeature.downloadLocalPdf(dto, httpServletResponse);

        } else if (dto.getRemoteUri() != null) {
            harvestPermitDownloadDecisionFeature.downloadRemotePdf(dto, httpServletResponse);

        } else {
            throw new HarvestPermitPdfMissingException(permitNumber);
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/{id:\\d+}/usage")
    public List<HarvestPermitUsageDTO> getSpeciesAmountUsage(@PathVariable long id) {
        return harvestPermitDetailsFeature.getSpeciesAmountUsage(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping("/{id:\\d+}/harvests")
    public List<HarvestDTO> getHarvestForPermit(@PathVariable long id) {
        return harvestPermitDetailsFeature.getHarvestForPermit(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping("/{id:\\d+}/export-reports")
    public ModelAndView exportPermitHarvestReports(@PathVariable Long id) {
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, LocaleContextHolder.getLocale());
        final List<HarvestReportExcelDTO> data = harvestReportSearchFeature.listByPermitForExcel(id);

        return new ModelAndView(HarvestReportListExcelView.create(localiser, data, true));
    }

    @GetMapping("/{id:\\d+}/contactpersons")
    public List<HarvestPermitContactPersonDTO> getContactPersons(@PathVariable Long id) {
        return harvestPermitContactPersonFeature.getContactPersons(id);
    }

    @PutMapping(value = "/{id:\\d+}/contactpersons", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateContactPersons(
            @PathVariable Long id, @RequestBody @Valid List<HarvestPermitContactPersonDTO> contactPersons) {
        harvestPermitContactPersonFeature.updateContactPersons(id, contactPersons);
    }

    @PostMapping(value = "/admin/import/permit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HarvestPermitImportResultDTO importPermits(@RequestParam("file") MultipartFile file) throws IOException {
        InputStreamReader reader = new InputStreamReader(file.getInputStream(), "ISO-8859-1");
        String requestInfo = DateTime.now().toString() + ":" + file.getOriginalFilename();
        try {
            return harvestPermitImportFeature.doImport(reader, requestInfo, null);
        } catch (HarvestPermitImportException e) {
            return new HarvestPermitImportResultDTO(e.getAllErrors());
        }
    }

    @PostMapping("/admin/search")
    public List<HarvestPermitSearchResultDTO> search(@RequestBody @Valid HarvestPermitSearchDTO dto) {
        return harvestPermitSearchFeature.search(dto);
    }

    @PostMapping("/rhy/search")
    public List<HarvestPermitSearchResultDTO> searchRhy(@RequestBody @Valid HarvestPermitSearchDTO dto) {
        return harvestPermitSearchFeature.searchForCoordinator(dto);
    }

    @PostMapping("/admin/export")
    public ModelAndView exportToExcel(final @RequestBody @Valid HarvestPermitSearchDTO dto,
                                      final Locale locale) {
        return new ModelAndView(harvestPermitSearchFeature.export(dto, locale));
    }

    // ATTACHMENTS

    @GetMapping(value = "/{id:\\d+}/attachment", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PermitDecisionAttachmentDTO> listAttachments(final @PathVariable long id) {
        return harvestPermitAttachmentFeature.listAttachments(id);
    }

    @PostMapping(value = "/{id:\\d+}/attachment/{attachmentId:\\d+}")
    public ResponseEntity<byte[]> getAttachment(final @PathVariable long id,
                                                final @PathVariable long attachmentId) throws IOException {
        return harvestPermitAttachmentFeature.getAttachment(id, attachmentId);
    }

}
