package fi.riista.api;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportCrudFeature;
import fi.riista.feature.harvestpermit.report.HarvestReportFeature;
import fi.riista.feature.harvestpermit.report.HarvestReportModeratorFeature;
import fi.riista.feature.harvestpermit.report.email.HarvestReportNotificationFeature;
import fi.riista.feature.harvestpermit.report.search.HarvestReportPersonSearch;
import fi.riista.feature.harvestpermit.report.search.HarvestReportSearchFeature;
import fi.riista.feature.harvestpermit.season.HarvestAreaDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportDTOBase;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportExportExcelDTO;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportParametersDTO;
import fi.riista.feature.harvestpermit.report.search.HarvestReportSearchDTO;
import fi.riista.feature.harvestpermit.report.HarvestReportSingleHarvestDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeasonDTO;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportListExcelView;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.LocalDate;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/harvestreport", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class HarvestReportApiResource {

    @Resource
    private HarvestReportFeature harvestReportFeature;

    @Resource
    private HarvestReportCrudFeature crudFeature;

    @Resource
    private HarvestReportSearchFeature harvestReportSearchFeature;

    @Resource
    private HarvestReportModeratorFeature harvestReportModeratorFeature;

    @Resource
    private HarvestReportNotificationFeature harvestReportNotificationFeature;

    @Resource
    private HarvestReportPersonSearch harvestReportPersonSearch;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private MessageSource messageSource;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/parameters", method = RequestMethod.GET)
    public HarvestReportParametersDTO getHarvestReportParameters() {
        return new HarvestReportParametersDTO();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.GET)
    public List<HarvestReportDTOBase> listMine(@RequestParam(required = false) Long personId) {
        return crudFeature.listMine(personId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.GET)
    public HarvestReportDTOBase getHarvestReport(@PathVariable Long id) {
        return crudFeature.read(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/readOnly", method = RequestMethod.GET)
    public HarvestReportDTOBase getReadOnlyHarvestReport(@PathVariable Long id) {
        HarvestReportDTOBase dto = crudFeature.read(id);
        dto.setCanEdit(false);
        dto.setCanDelete(false);
        dto.setTransitions(Collections.<HarvestReport.State>emptyList());
        return dto;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createHarvestReport(@RequestBody @Valid HarvestReportSingleHarvestDTO dto,
                                    @RequestParam(required = false) String reason) {
        HarvestReportDTOBase res = crudFeature.create(dto, reason);

        harvestReportNotificationFeature.sendNotification(res.getId());
    }

    @RequestMapping(value = "/permit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createHarvestReportForPermit(@RequestParam Long permitId,
                                             @RequestParam Integer permitRev) {

        final Long reportId = crudFeature.createForListPermit(permitId, permitRev);

        harvestReportNotificationFeature.sendNotification(reportId);
    }

    @RequestMapping(value = "/endofhunt", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createEndOfHuntingReport(@RequestParam Long permitId,
                                         @RequestParam Integer permitRev) {

        final Long reportId = crudFeature.createEndOfHuntingReport(permitId, permitRev);

        harvestReportNotificationFeature.sendNotification(reportId);
    }

    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HarvestReportDTOBase updateHarvestReport(@RequestBody @Valid HarvestReportSingleHarvestDTO dto,
                                                    @RequestParam(required = false) String reason) {
        final HarvestReportDTOBase result = crudFeature.update(dto, reason);

        // Send notification ot applicable persons
        harvestReportNotificationFeature.sendNotification(result.getId());

        return result;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.DELETE)
    public void deleteHarvestReport(@PathVariable Long id) {
        crudFeature.delete(id);
    }

    @RequestMapping(value = "/changestate/{id:\\d+}", method = RequestMethod.PUT)
    public HarvestReportDTOBase changeState(@PathVariable Long id,
                                            @RequestParam HarvestReport.State newstate,
                                            @RequestParam Integer rev,
                                            @RequestParam(required = false) String reason,
                                            @RequestParam(required = false) String propertyIdentifier) {

        crudFeature.changeState(id, rev, newstate, reason, propertyIdentifier);

        // we need fresh rev, so read it again
        return crudFeature.read(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/activeseasons", method = RequestMethod.GET)
    public List<HarvestSeasonDTO> listReportableHuntingSeasons(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer gameSpeciesCode) {
        return harvestReportFeature.listReportableHuntingSeasons(date, gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/activepermits", method = RequestMethod.GET)
    public List<HarvestReportFieldsDTO> listReportablePermits(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer gameSpeciesCode) {
        return harvestReportFeature.listReportableWithPermits(date, gameSpeciesCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/findarea", method = RequestMethod.GET)
    public ResponseEntity<?> findHarvestArea(@RequestParam long rhyId, @RequestParam long harvestSeasonId) {
        final HarvestAreaDTO harvestArea = harvestReportFeature.findHarvestArea(rhyId, harvestSeasonId);

        return harvestArea != null
                ? ResponseEntity.ok(harvestArea)
                : ResponseEntity.notFound().build();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public Map<String, Object> admin() {
        return harvestReportModeratorFeature.admin();
    }

    @RequestMapping(value = "/findperson/hunternumber", method = RequestMethod.POST)
    public PersonWithHunterNumberDTO findByHunterNumber(@RequestParam String hunterNumber) {
        return harvestReportPersonSearch.findHunterByNumber(hunterNumber);
    }

    @RequestMapping(value = "/findperson/ssn", method = RequestMethod.POST)
    public PersonWithHunterNumberDTO findBySSN(@RequestParam String ssn) {
        return harvestReportPersonSearch.findBySsn(ssn);
    }

    @RequestMapping(value = "/findperson/permitnumber", method = RequestMethod.POST)
    public PersonWithHunterNumberDTO findByPermitNumber(@RequestParam String permitNumber) {
        return harvestReportPersonSearch.findByPermitNumber(permitNumber);
    }

    @RequestMapping(value = "/findperson/name", method = RequestMethod.POST)
    public List<PersonWithHunterNumberDTO> findByPersonName(
            @RequestParam String name) {
        return harvestReportPersonSearch.searchPersons(name);
    }

    // Table HTML view

    @RequestMapping(value = "/rhy/search", method = RequestMethod.POST)
    public List<HarvestReportDTOBase> searchRhy(@RequestBody @Valid HarvestReportSearchDTO dto) {
        return harvestReportSearchFeature.searchForCoordinator(dto);
    }

    @RequestMapping(value = "/admin/search", method = RequestMethod.POST)
    public Page<HarvestReportDTOBase> search(@RequestBody @Valid HarvestReportSearchDTO dto, Pageable pageRequest) {
        return harvestReportSearchFeature.search(dto, pageRequest);
    }

    // Excel export

    @RequestMapping(value = "/rhy/search/excel", method = RequestMethod.POST)
    public ModelAndView searchRhyExcel(@RequestParam(value = "json") @NotBlank String jsonData) throws IOException {
        HarvestReportSearchDTO dto = objectMapper.readValue(jsonData, HarvestReportSearchDTO.class);

        List<HarvestReportExportExcelDTO> data = harvestReportSearchFeature.searchForCoordinatorExcel(dto);
        HarvestReportListExcelView view =
                new HarvestReportListExcelView(data, new EnumLocaliser(messageSource, LocaleContextHolder.getLocale()));
        return new ModelAndView(view);
    }

    @RequestMapping(value = "/admin/search/excel", method = RequestMethod.POST)
    public ModelAndView searchExcel(@RequestParam(value = "json") @NotBlank String jsonData) throws IOException {
        HarvestReportSearchDTO dto = objectMapper.readValue(jsonData, HarvestReportSearchDTO.class);

        List<HarvestReportExportExcelDTO> data = harvestReportSearchFeature.searchExcel(dto);
        HarvestReportListExcelView view =
                new HarvestReportListExcelView(data, new EnumLocaliser(messageSource, LocaleContextHolder.getLocale()));
        return new ModelAndView(view);
    }

}
