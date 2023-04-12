package fi.riista.api.club;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.huntingclub.CreateHuntingClubDTO;
import fi.riista.feature.huntingclub.HuntingClubCrudFeature;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.huntingclub.hunting.excel.ClubHuntingDataExcelFeature;
import fi.riista.feature.huntingclub.statistics.HuntingClubHarvestStatisticsDTO;
import fi.riista.feature.huntingclub.statistics.HuntingClubHarvestStatisticsFeature;
import fi.riista.feature.huntingclub.statistics.HuntingClubStatisticsFeature;
import fi.riista.feature.huntingclub.statistics.HuntingClubStatisticsRow;
import fi.riista.feature.huntingclub.statistics.gamestatistics.GameStatisticsDTO;
import fi.riista.feature.huntingclub.statistics.gamestatistics.GameStatisticsFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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

@RestController
@RequestMapping(value = "/api/v1/club", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClubApiResource {

    @Resource
    private HuntingClubCrudFeature crudFeature;

    @Resource
    private HuntingClubHarvestStatisticsFeature huntingClubHarvestSummaryFeature;

    @Resource
    private ClubHuntingDataExcelFeature huntingDataExcelFeature;

    @Resource
    private HuntingClubStatisticsFeature huntingClubStatisticsFeature;

    @Resource
    private GameStatisticsFeature gameStatisticsFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping
    public HuntingClubDTO createHuntingClub(@Validated @RequestBody final CreateHuntingClubDTO dto) {
        return crudFeature.create(dto.toHuntingClubDTO());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}")
    public HuntingClubDTO read(@PathVariable Long id) {
        return crudFeature.read(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HuntingClubDTO updateClub(@PathVariable final long id,
                                     @RequestBody @Validated HuntingClubDTO dto) {

        dto.setId(id);
        return crudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id:\\d+}/location", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeoLocation updateLocation(@PathVariable final long id,
                                      @RequestBody @Validated GeoLocation geoLocation) {

        return crudFeature.updateLocation(id, geoLocation);
    }

    @PutMapping(value = "/{id:\\d+}/active/{active:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateActive(@PathVariable final long id, @PathVariable int active) {
        crudFeature.updateActive(id, active != 0);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/{id:\\d+}/huntingdata")
    public ModelAndView exportHuntingData(@PathVariable final long id,
                                          @RequestParam final int gameSpeciesCode,
                                          @RequestParam final int huntingYear) {
        return new ModelAndView(huntingDataExcelFeature.export(id, huntingYear, gameSpeciesCode));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @PostMapping(value = "/harvestsummary")
    public HuntingClubHarvestStatisticsDTO getHarvestSummary(
            @RequestBody @Validated final ClubHarvestSummaryRequestDTO dto) {
        return huntingClubHarvestSummaryFeature.getSummary(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/moderator/huntingclubmetrics",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HuntingClubStatisticsRow> huntingClubMetrics(
            @RequestParam(required = false) final Long rkaId,
            @RequestParam(required = false, defaultValue = "false") final boolean includePermitHolders) {

        if (rkaId != null) {
            return huntingClubStatisticsFeature.calculateByRka(rkaId, includePermitHolders);
        }
        return huntingClubStatisticsFeature.calculate(includePermitHolders);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/gamestatistics/moose")
    public GameStatisticsDTO getMooseGameStatistics(@PathVariable final long id) {
        return gameStatisticsFeature.getMooseStatisticsForHuntingClub(id);
    }
    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/gamestatistics/deer")
    public GameStatisticsDTO getDeerGameStatistics(@PathVariable final long id) {
        return gameStatisticsFeature.getDeerStatisticsForHuntingClub(id);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{id:\\d+}/gamestatistics/deercensus")
    public GameStatisticsDTO getDeerCensusGameStatistics(@PathVariable final long id) {
        return gameStatisticsFeature.getDeerCensusStatisticsForHuntingClub(id);
    }
}
