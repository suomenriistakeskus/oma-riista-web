package fi.riista.api.application;


import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.bird.BirdPermitApplicationSummaryFeature;
import fi.riista.feature.permit.application.bird.amount.BirdPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.bird.amount.BirdPermitApplicationSpeciesAmountFeature;
import fi.riista.feature.permit.application.bird.applicant.BirdPermitApplicationApplicantFeature;
import fi.riista.feature.permit.application.bird.area.BirdPermitApplicationProtectedAreaDTO;
import fi.riista.feature.permit.application.bird.area.BirdPermitApplicationProtectedAreaFeature;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCauseDTO;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCauseFeature;
import fi.riista.feature.permit.application.bird.forbidden.BirdPermitApplicationForbiddenMethodsFeature;
import fi.riista.feature.permit.application.bird.period.BirdPermitApplicationSpeciesPeriodFeature;
import fi.riista.feature.permit.application.bird.period.BirdPermitApplicationSpeciesPeriodInformationDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
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

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/bird")
public class BirdPermitApplicationApiResource {

    @Resource
    private BirdPermitApplicationSummaryFeature birdPermitApplicationSummaryFeature;

    @Resource
    private BirdPermitApplicationApplicantFeature birdPermitApplicationApplicantFeature;

    @Resource
    private BirdPermitApplicationSpeciesAmountFeature permitApplicationSpeciesAmountFeature;

    @Resource
    private BirdPermitApplicationSpeciesPeriodFeature permitApplicationSpeciesPeriodFeature;

    @Resource
    private BirdPermitApplicationProtectedAreaFeature birdPermitApplicationProtectedAreaFeature;

    @Resource
    private BirdPermitApplicationForbiddenMethodsFeature birdPermitApplicationForbiddenMethodsFeature;

    @Resource
    private BirdPermitApplicationCauseFeature birdPermitApplicationCauseFeature;


    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BirdPermitApplicationSummaryDTO readDetails(@PathVariable final long applicationId) {
        return birdPermitApplicationSummaryFeature.readDetails(applicationId);
    }

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return birdPermitApplicationApplicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(
            @PathVariable final long applicationId,
            @Valid @RequestBody final PermitHolderDTO permitHolder) {
        birdPermitApplicationApplicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // SPECIES AMOUNTS

    static class AmountList {
        @Valid
        public List<BirdPermitApplicationSpeciesAmountDTO> list;

        public List<BirdPermitApplicationSpeciesAmountDTO> getList() {
            return list;
        }

        public void setList(final List<BirdPermitApplicationSpeciesAmountDTO> list) {
            this.list = list;
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<BirdPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(@PathVariable final long applicationId) {
        return permitApplicationSpeciesAmountFeature.getSpeciesAmounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void saveSpeciesAmounts(
            final @PathVariable long applicationId,
            final @Valid @RequestBody AmountList request) {
        permitApplicationSpeciesAmountFeature.saveSpeciesAmounts(applicationId, request.list);
    }

    // SPECIES PERIODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BirdPermitApplicationSpeciesPeriodInformationDTO getSpeciesPeriods(@PathVariable final long applicationId) {
        return permitApplicationSpeciesPeriodFeature.getPermitPeriodInformation(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void saveSpeciesPeriods(
            @PathVariable final long applicationId,
            @Valid @RequestBody BirdPermitApplicationSpeciesPeriodInformationDTO dto) {
        permitApplicationSpeciesPeriodFeature.saveSpeciesPeriods(applicationId, dto);
    }

    // PROTECTED AREA

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/area", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BirdPermitApplicationProtectedAreaDTO getProtectedAreaInfo(@PathVariable final long applicationId) {
        return birdPermitApplicationProtectedAreaFeature.getProtectedAreaInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/area", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateProtectedAreaInfo(@PathVariable final long applicationId,
                                        @Valid @RequestBody final BirdPermitApplicationProtectedAreaDTO dto) {
        birdPermitApplicationProtectedAreaFeature.updateProtectedArea(applicationId, dto);
    }

    // PERMIT CAUSE

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/cause", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updatePermitCause(
            @PathVariable final long applicationId,
            @Valid @RequestBody final BirdPermitApplicationCauseDTO cause) {
        birdPermitApplicationCauseFeature.updateCauseInfo(applicationId, cause);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/cause", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BirdPermitApplicationCauseDTO getPermitCauseInfo(@PathVariable final long applicationId) {
        return birdPermitApplicationCauseFeature.getCauseInfo(applicationId);
    }

    // DEVIATION JUSTIFICATION

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updatePermitCause(
            @PathVariable final long applicationId,
            @Valid @RequestBody final DerogationPermitApplicationForbiddenMethodsDTO dto) {
        birdPermitApplicationForbiddenMethodsFeature.updateMethodInfo(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public DerogationPermitApplicationForbiddenMethodsDTO getDeviationJustification(@PathVariable final long applicationId) {
        return birdPermitApplicationForbiddenMethodsFeature.getCurrentMethodInfo(applicationId);
    }

}
