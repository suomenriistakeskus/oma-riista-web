package fi.riista.api;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.huntingclub.CreateHuntingClubDTO;
import fi.riista.feature.huntingclub.HuntingClubCrudFeature;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.huntingclub.register.RegisterHuntingClubFeature;
import fi.riista.feature.huntingclub.statistics.HuntingClubHarvestStatisticsDTO;
import fi.riista.feature.huntingclub.statistics.HuntingClubHarvestStatisticsFeature;
import fi.riista.feature.huntingclub.statistics.HuntingClubStatisticsFeature;
import fi.riista.feature.huntingclub.statistics.HuntingClubStatisticsRow;
import fi.riista.feature.organization.lupahallinta.LHOrganisationSearchDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
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

import javax.annotation.Resource;
import javax.validation.groups.Default;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/club", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ClubApiResource {

    @Resource
    private HuntingClubCrudFeature crudFeature;

    @Resource
    private HuntingClubHarvestStatisticsFeature huntingClubHarvestSummaryFeature;

    @Resource
    private RegisterHuntingClubFeature registerHuntingClubFeature;

    @Resource
    private HuntingClubStatisticsFeature huntingClubStatisticsFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(method = RequestMethod.POST)
    public HuntingClubDTO createHuntingClub(@Validated @RequestBody final CreateHuntingClubDTO dto) {
        return crudFeature.create(dto.toHuntingClubDTO());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.GET)
    public HuntingClubDTO read(@PathVariable Long id) {
        return crudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubDTO updateClub(@PathVariable final long id,
                                     @RequestBody @Validated HuntingClubDTO dto) {

        dto.setId(id);
        return crudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{id:\\d+}/location", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeoLocation updateLocation(@PathVariable final long id,
                                      @RequestBody @Validated GeoLocation geoLocation) {

        return crudFeature.updateLocation(id, geoLocation);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/{id:\\d+}/harvestsummary", method = RequestMethod.GET)
    public HuntingClubHarvestStatisticsDTO getHarvestSummary(@PathVariable final long id,
                                                             @RequestParam final int huntingYear) {
        return huntingClubHarvestSummaryFeature.getSummary(id, huntingYear);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/lh/findByName", method = RequestMethod.GET)
    public List<LHOrganisationSearchDTO> findLhClubByName(@RequestParam final String queryString) {
        return registerHuntingClubFeature.findByName(queryString);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/lh/findByCode", method = RequestMethod.GET)
    public List<LHOrganisationSearchDTO> findLhClubByOfficialCode(@RequestParam final String queryString) {
        return registerHuntingClubFeature.findByOfficialCode(queryString);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/lh/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> registerHuntingClub(@Validated({LHOrganisationSearchDTO.Register.class, Default.class})
                                                   @RequestBody LHOrganisationSearchDTO dto) {
        return registerHuntingClubFeature.register(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @RequestMapping(value = "/moderator/huntingclubmetrics", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<HuntingClubStatisticsRow> huntingClubMetrics(
            @RequestParam(required = false) final Long rkaId,
            @RequestParam(required = false, defaultValue = "false") final boolean includePermitHolders) {

        if (rkaId != null) {
            return huntingClubStatisticsFeature.calculateByRka(rkaId, includePermitHolders);
        }
        return huntingClubStatisticsFeature.calculate(includePermitHolders);
    }

}
