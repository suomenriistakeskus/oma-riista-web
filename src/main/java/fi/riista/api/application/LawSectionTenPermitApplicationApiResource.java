package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationDTO;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.lawsectionten.LawSectionTenPermitApplicationSummaryFeature;
import fi.riista.feature.permit.application.lawsectionten.amount.LawSectionTenPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.lawsectionten.amount.LawSectionTenPermitApplicationSpeciesAmountFeature;
import fi.riista.feature.permit.application.lawsectionten.applicant.LawSectionTenPermitApplicationApplicantFeature;
import fi.riista.feature.permit.application.lawsectionten.period.LawSectionTenPermitApplicationSpeciesPeriodDTO;
import fi.riista.feature.permit.application.lawsectionten.period.LawSectionTenPermitApplicationSpeciesPeriodFeature;
import fi.riista.feature.permit.application.lawsectionten.population.LawSectionTenPopulationDTO;
import fi.riista.feature.permit.application.lawsectionten.population.LawSectionTenPopulationFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;
import java.util.Locale;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/lawsectionten")
public class LawSectionTenPermitApplicationApiResource {

    @Resource
    private LawSectionTenPermitApplicationApplicantFeature lawSectionTenPermitApplicationApplicantFeature;

    @Resource
    private LawSectionTenPermitApplicationSpeciesAmountFeature lawSectionTenPermitApplicationSpeciesAmountFeature;

    @Resource
    private LawSectionTenPermitApplicationSpeciesPeriodFeature lawSectionTenPermitApplicationSpeciesPeriodFeature;

    @Resource
    private LawSectionTenPopulationFeature populationFeature;

    @Resource
    private LawSectionTenPermitApplicationSummaryFeature lawSectionTenPermitApplicationSummaryFeature;

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public LawSectionTenPermitApplicationSummaryDTO readDetails(@PathVariable final long applicationId, final Locale locale) {
        return lawSectionTenPermitApplicationSummaryFeature.readDetails(applicationId, locale);
    }

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return lawSectionTenPermitApplicationApplicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {
        lawSectionTenPermitApplicationApplicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // SPECIES AMOUNTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public LawSectionTenPermitApplicationSpeciesAmountDTO getSpeciesAmounts(@PathVariable final long applicationId) {
        return lawSectionTenPermitApplicationSpeciesAmountFeature.getSpeciesAmounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesAmounts(
            @PathVariable final long applicationId,
            @Valid @RequestBody final LawSectionTenPermitApplicationSpeciesAmountDTO speciesAmount) {
        lawSectionTenPermitApplicationSpeciesAmountFeature.saveSpeciesAmounts(applicationId, speciesAmount);
    }

    // SPECIES PERIODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public LawSectionTenPermitApplicationSpeciesPeriodDTO getSpeciesPeriods(@PathVariable final long applicationId) {
        return lawSectionTenPermitApplicationSpeciesPeriodFeature.getPermitPeriodInformation(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesPeriods(
            @PathVariable final long applicationId,
            @Valid @RequestBody final LawSectionTenPermitApplicationSpeciesPeriodDTO dto) {
        lawSectionTenPermitApplicationSpeciesPeriodFeature.saveSpeciesPeriods(applicationId, dto);
    }

    // POPULATION

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/population", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updatePopulation(
            @PathVariable final long applicationId,
            @Valid @RequestBody final LawSectionTenPopulationDTO dto) {
        populationFeature.saveSpeciesPopulation(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/population", produces = MediaType.APPLICATION_JSON_VALUE)
    public LawSectionTenPopulationDTO getPopulation(@PathVariable final long applicationId) {
        return populationFeature.getSpeciesPopulation(applicationId);
    }
}
