package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.deportation.DeportationSummaryDTO;
import fi.riista.feature.permit.application.deportation.DeportationSummaryFeature;
import fi.riista.feature.permit.application.deportation.amount.DeportationSpeciesAmountDTO;
import fi.riista.feature.permit.application.deportation.amount.DeportationSpeciesAmountFeature;
import fi.riista.feature.permit.application.deportation.applicant.DeportationApplicantFeature;
import fi.riista.feature.permit.application.deportation.forbidden.DeportationForbiddenMethodsFeature;
import fi.riista.feature.permit.application.deportation.period.DeportationSpeciesPeriodDTO;
import fi.riista.feature.permit.application.deportation.period.DeportationSpeciesPeriodFeature;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonFeature;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
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

import java.util.Locale;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/deportation")
public class DeportationPermitApplicationApiResource {

    @Resource
    private DeportationApplicantFeature deportationApplicantFeature;

    @Resource
    private DeportationSpeciesAmountFeature deportationSpeciesAmountFeature;

    @Resource
    private DerogationPermitApplicationReasonFeature derogationPermitApplicationReasonFeature;

    @Resource
    private DeportationSpeciesPeriodFeature deportationSpeciesPeriodFeature;

    @Resource
    private DeportationForbiddenMethodsFeature deportationForbiddenMethodsFeature;

    @Resource
    private DeportationSummaryFeature deportationSummaryFeature;

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return deportationApplicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {
        deportationApplicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // SPECIES AMOUNTS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeportationSpeciesAmountDTO getSpeciesAmounts(@PathVariable final long applicationId) {
        return deportationSpeciesAmountFeature.getSpeciesAmounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesAmounts(@PathVariable final long applicationId,
                                   @Valid @RequestBody final DeportationSpeciesAmountDTO deportationSpeciesAmountDTO) {
        deportationSpeciesAmountFeature.saveSpeciesAmounts(applicationId, deportationSpeciesAmountDTO);
    }

    // REASONS

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/reasons", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updatePermitCause(@PathVariable final long applicationId,
                                  @Valid @RequestBody final DerogationPermitApplicationReasonsDTO dto) {
        derogationPermitApplicationReasonFeature.updateDerogationReasons(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/reasons", produces = MediaType.APPLICATION_JSON_VALUE)
    public DerogationPermitApplicationReasonsDTO getPermitCauseInfo(@PathVariable final long applicationId,
                                                                    final Locale locale) {
        return derogationPermitApplicationReasonFeature.getDerogationReasons(applicationId, locale);
    }

    // PERIODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeportationSpeciesPeriodDTO getSpeciesPeriod(@PathVariable final long applicationId) {
        return deportationSpeciesPeriodFeature.getSpeciesPeriod(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesPeriod(@PathVariable final long applicationId,
                                   @Valid @RequestBody final DeportationSpeciesPeriodDTO deportationSpeciesPeriodDTO) {
        deportationSpeciesPeriodFeature.saveSpeciesPeriod(applicationId, deportationSpeciesPeriodDTO);
    }

    // FORBIDDEN METHODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_VALUE)
    public DerogationPermitApplicationForbiddenMethodsDTO getForbiddenMethods(@PathVariable final long applicationId) {
        return deportationForbiddenMethodsFeature.getForbiddenMethods(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateForbiddenMethods(@PathVariable final long applicationId,
                                  @Valid @RequestBody final DerogationPermitApplicationForbiddenMethodsDTO dto) {
        deportationForbiddenMethodsFeature.updateForbiddenMethods(applicationId, dto);
    }

    // SUMMARY

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeportationSummaryDTO readDetails(@PathVariable final long applicationId, final Locale locale) {
        return deportationSummaryFeature.readDetails(applicationId, locale);
    }

}
