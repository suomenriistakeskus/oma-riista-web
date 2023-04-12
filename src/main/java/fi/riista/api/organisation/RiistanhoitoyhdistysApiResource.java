package fi.riista.api.organisation;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.list.HarvestPermitListFeature;
import fi.riista.feature.harvestpermit.list.MooselikeHuntingYearDTO;
import fi.riista.feature.harvestpermit.list.MooselikePermitListDTO;
import fi.riista.feature.huntingclub.hunting.overview.CoordinatorClubHarvestFeature;
import fi.riista.feature.huntingclub.members.HuntingClubContactFeature;
import fi.riista.feature.huntingclub.members.rhy.RhyClubOccupationDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.RhySrvaCallRingRotationFeature;
import fi.riista.feature.organization.rhy.RhySrvaRotationDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysCrudFeature;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsCrudFeature;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsDTO;
import fi.riista.feature.organization.rhy.taxation.HarvestTaxationExcelDTO;
import fi.riista.feature.organization.rhy.taxation.HarvestTaxationReportDTO;
import fi.riista.feature.organization.rhy.taxation.HarvestTaxationReportingFeature;
import fi.riista.feature.search.RhySearchParamsFeature;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys", produces = APPLICATION_JSON_VALUE)
public class RiistanhoitoyhdistysApiResource {

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    @Resource
    private RiistanhoitoyhdistysCrudFeature crudFeature;

    @Resource
    private RhySrvaCallRingRotationFeature rhySrvaCallRingRotationFeature;

    @Resource
    private CoordinatorClubHarvestFeature coordinatorClubHarvestFeature;

    @Resource
    private HuntingClubContactFeature huntingClubContactFeature;

    @Resource
    private HarvestPermitListFeature harvestPermitListFeature;

    @Resource
    private RhySearchParamsFeature rhySearchParamsFeature;

    @Resource
    private RhyAnnualStatisticsCrudFeature annualStatisticsCrudFeature;

    @Resource
    private HarvestTaxationReportingFeature harvestTaxationReportingFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}")
    public RiistanhoitoyhdistysDTO read(final @PathVariable Long id) {
        return crudFeature.read(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}/public")
    public OrganisationNameDTO getPublicInfo(final @PathVariable Long id) {
        return crudFeature.getPublicInfo(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public RiistanhoitoyhdistysDTO create(final @RequestBody @Validated RiistanhoitoyhdistysDTO dto) {
        return crudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{id:\\d+}", consumes = APPLICATION_JSON_VALUE)
    public void update(final @PathVariable Long id,
                       final @RequestBody @Validated RiistanhoitoyhdistysDTO dto) {

        dto.setId(id);
        crudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "{id:\\d+}")
    public void delete(final @PathVariable Long id) {
        crudFeature.delete(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}/contacts")
    public List<RhyClubOccupationDTO> contacts(final @PathVariable long id) {
        return huntingClubContactFeature.listRhyContacts(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}/leaders/{year:\\d+}")
    public List<RhyClubOccupationDTO> leaders(final @PathVariable long id,
                                              final @PathVariable int year) {

        return huntingClubContactFeature.listRhyHuntingLeaders(id, year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/contacts-and-leaders/excel",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportExcel(final @RequestParam long orgId,
                                    final @RequestParam int year,
                                    final Locale locale) {

        return new ModelAndView(huntingClubContactFeature.exportRhyOccupationsToExcel(orgId, year, locale));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}/harvest")
    public List<HarvestDTO> listHarvests(final @PathVariable Long id,
                                         final @RequestParam int speciesCode,
                                         final @RequestParam boolean filterByAreaGeometry,
                                         final @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate begin,
                                         final @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate end) {

        return coordinatorClubHarvestFeature.listHarvest(id, speciesCode, filterByAreaGeometry, begin, end);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}/moosepermit")
    public List<MooselikePermitListDTO> listPermits(final @PathVariable long id,
                                                    final @RequestParam int year,
                                                    final @RequestParam int species,
                                                    final Locale locale) {
        return harvestPermitListFeature.listRhyMooselikePermits(id, year, species, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}/srva-rotation")
    public RhySrvaRotationDTO getSrvaRotation(final @PathVariable long id) {
        return rhySrvaCallRingRotationFeature.getRotation(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "{id:\\d+}/srva-rotation")
    public void updateSrvaRotation(final @PathVariable long id,
                                   final @Valid @RequestBody RhySrvaRotationDTO dto) {
        rhySrvaCallRingRotationFeature.updateRotation(id, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{id:\\d+}/moosepermit/huntingyears")
    public List<MooselikeHuntingYearDTO> listHuntingYears(final @PathVariable long id) {
        return harvestPermitListFeature.listRhyMooselikeHuntingYears(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/searchparams/orgs")
    public List<RhySearchParamsFeature.RhySearchOrgList> listSearchParamOrganisations(
            final @RequestParam(required = false) Long id,
            final Locale locale) {

        return rhySearchParamsFeature.listOrganisations(id, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{rhyId:\\d+}/annualstatistics/{calendarYear:\\d+}")
    public RhyAnnualStatisticsDTO getOrCreateAnnualStatistics(final @PathVariable long rhyId,
                                                              final @PathVariable int calendarYear) {

        return annualStatisticsCrudFeature.getOrCreate(rhyId, calendarYear);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{rhyId:\\d+}/annualstatisticsyears")
    public List<Integer> listAnnualStatisticsYears(final @PathVariable long rhyId) {
        return annualStatisticsCrudFeature.listAnnualStatisticsYears(rhyId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{rhyId:\\d+}/yearsofexistence")
    public List<Integer> listYearsOfExistence(final @PathVariable long rhyId) {
        return crudFeature.getYearsOfExistence(rhyId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/taxation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HarvestTaxationReportDTO saveOrUpdateTaxationReport(final @RequestBody @Valid HarvestTaxationReportDTO taxationReportDTO) {
        return harvestTaxationReportingFeature.saveOrUpdateTaxationReport(taxationReportDTO);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/taxation/withattachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HarvestTaxationReportDTO save(final @RequestParam(value = "dto") String dtoData,
                                         final @RequestParam MultipartFile[] files) throws IOException {
        final HarvestTaxationReportDTO dto = objectMapper.readValue(dtoData, HarvestTaxationReportDTO.class);
        return harvestTaxationReportingFeature.saveOrUpdateTaxationReport(dto, Arrays.asList(files));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/taxation", produces = APPLICATION_JSON_VALUE)
    public HarvestTaxationReportDTO getTaxationReport(final @RequestParam int year,
                                                      final @RequestParam int speciesCode,
                                                      final @RequestParam long rhyId,
                                                      final @RequestParam int htaId
    ) {
        return harvestTaxationReportingFeature.getTaxationReportDTOBySpeciesAndHuntingYear( htaId, rhyId, speciesCode, year);
    }

    @PostMapping(value = "/taxation/excel", produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView exportExcel(final @RequestBody @Valid HarvestTaxationExcelDTO dto) {
        return new ModelAndView(harvestTaxationReportingFeature.export(dto));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/taxation/moose_areas", produces = APPLICATION_JSON_VALUE)
    public Map<Integer, LocalisedString> getMooseAreas(final @RequestParam long rhyId) {
        return harvestTaxationReportingFeature.getMooseAreas(rhyId);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/taxation/years", produces = APPLICATION_JSON_VALUE)
    public List<Integer> getTaxationReport(final @RequestParam long rhyId) {
        return harvestTaxationReportingFeature.getTaxationReportYears(rhyId);
    }

    /**
     * Attachment API
     */

    // Result can be cached due its content cannot be changed.
    @GetMapping(value = "/taxation/attachment/{id:\\d+}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable final long id) throws IOException {
        return harvestTaxationReportingFeature.getAttachment(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/taxation/attachment/{id:\\d+}")
    public void deleteAttachment(@PathVariable final long id) throws IOException {
        harvestTaxationReportingFeature.deleteAttachment(id);
    }


}
