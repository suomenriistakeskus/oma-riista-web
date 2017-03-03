package fi.riista.api;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.huntingclub.hunting.overview.CoordinatorClubHarvestFeature;
import fi.riista.feature.huntingclub.members.rhy.RhyClubOccupationDTO;
import fi.riista.feature.huntingclub.members.rhy.RiistanhoitoyhdistysClubFeature;
import fi.riista.feature.huntingclub.permit.MooselikeHuntingYearDTO;
import fi.riista.feature.huntingclub.permit.MooselikePermitListingDTO;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsDTO;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsExcelView;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsFeature;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsFeature.OrgList;
import fi.riista.feature.huntingclub.permit.stats.MoosePermitStatisticsFeature.OrgType;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysCrudFeature;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RiistanhoitoyhdistysApiResource {

    @Resource
    private RiistanhoitoyhdistysCrudFeature crudFeature;

    @Resource
    private RiistanhoitoyhdistysClubFeature rhyClubFeature;

    @Resource
    private CoordinatorClubHarvestFeature coordinatorClubHarvestFeature;

    @Resource
    private MoosePermitStatisticsFeature moosePermitStatisticsFeature;

    @Resource
    private MessageSource messageSource;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.GET)
    public RiistanhoitoyhdistysDTO read(@PathVariable Long id) {
        return crudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public RiistanhoitoyhdistysDTO create(@RequestBody @Validated RiistanhoitoyhdistysDTO dto) {
        return crudFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void update(@PathVariable Long id, @RequestBody @Validated RiistanhoitoyhdistysDTO dto) {
        dto.setId(id);
        crudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "{id:\\d+}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        crudFeature.delete(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}/contacts", method = RequestMethod.GET)
    public List<RhyClubOccupationDTO> contacts(final @PathVariable long id) {
        return rhyClubFeature.listContacts(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}/leaders/{year:\\d+}", method = RequestMethod.GET)
    public List<RhyClubOccupationDTO> leaders(final @PathVariable long id, final @PathVariable int year) {
        return rhyClubFeature.listLeaders(id, year);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/contacts-and-leaders/excel",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView exportExcel(@RequestParam final long orgId, @RequestParam final int year, final Locale locale) {
        return new ModelAndView(rhyClubFeature.exportToExcel(orgId, year, locale));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}/harvest", method = RequestMethod.GET)
    public List<HarvestDTO> listHarvests(@PathVariable final Long id,
                                         @RequestParam final int speciesCode,
                                         @RequestParam final boolean filterByAreaGeometry,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                         final LocalDate begin,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                         final LocalDate end) {

        return coordinatorClubHarvestFeature.listHarvest(id, speciesCode, filterByAreaGeometry, begin, end);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}/moosepermit", method = RequestMethod.GET)
    public List<MooselikePermitListingDTO> listPermits(@PathVariable long id,
                                                       @RequestParam int year,
                                                       @RequestParam int species,
                                                       Locale locale) {
        return crudFeature.listPermits(id, year, species, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}/moosepermit/huntingyears", method = RequestMethod.GET)
    public List<MooselikeHuntingYearDTO> listHuntingYears(@PathVariable long id) {
        return crudFeature.listMooselikeHuntingYears(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}/moosepermit/statistics/orgs", method = RequestMethod.GET)
    public List<OrgList> listRhyMooseStatisticsOrgs(@PathVariable long id, Locale locale) {
        return moosePermitStatisticsFeature.listOrganisations(id, locale);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}/moosepermit/statistics", method = RequestMethod.GET)
    public List<MoosePermitStatisticsDTO> listRhyMooseStatistics(@PathVariable long id,
                                                                 @RequestParam int year,
                                                                 @RequestParam int species,
                                                                 @RequestParam OrgType orgType,
                                                                 @RequestParam String orgCode,
                                                                 Locale locale) {
        return moosePermitStatisticsFeature.calculateByHolder(id, locale, species, year, orgType, orgCode);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "{id:\\d+}/moosepermit/statistics/excel",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaTypeExtras.APPLICATION_EXCEL_VALUE)
    public ModelAndView exportRhyMooseStatisticsExcel(@PathVariable long id,
                                                      @RequestParam int year,
                                                      @RequestParam int species,
                                                      @RequestParam OrgType orgType,
                                                      @RequestParam String orgCode,
                                                      Locale locale) {

        final List<MoosePermitStatisticsDTO> stats =
                moosePermitStatisticsFeature.calculateByHolder(id, locale, species, year, orgType, orgCode);
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        return new ModelAndView(new MoosePermitStatisticsExcelView(locale, localiser, stats));
    }
}
