package fi.riista.api.organisation;

import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.list.HarvestPermitListFeature;
import fi.riista.feature.harvestpermit.list.MooselikeHuntingYearDTO;
import fi.riista.feature.harvestpermit.list.MooselikePermitListDTO;
import fi.riista.feature.huntingclub.hunting.overview.CoordinatorClubHarvestFeature;
import fi.riista.feature.huntingclub.members.HuntingClubContactFeature;
import fi.riista.feature.huntingclub.members.rhy.RhyClubOccupationDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysCrudFeature;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsCrudFeature;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsDTO;
import fi.riista.feature.search.RhySearchParamsFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys", produces = APPLICATION_JSON_UTF8_VALUE)
public class RiistanhoitoyhdistysApiResource {

    @Resource
    private RiistanhoitoyhdistysCrudFeature crudFeature;

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
    @PostMapping(consumes = APPLICATION_JSON_UTF8_VALUE)
    public RiistanhoitoyhdistysDTO create(final @RequestBody @Validated RiistanhoitoyhdistysDTO dto) {
        return crudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "{id:\\d+}", consumes = APPLICATION_JSON_UTF8_VALUE)
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
                                                    Locale locale) {
        return harvestPermitListFeature.listRhyMooselikePermits(id, year, species, locale);
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
}
