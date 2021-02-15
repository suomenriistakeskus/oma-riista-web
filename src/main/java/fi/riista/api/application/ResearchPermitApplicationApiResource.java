package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.research.ResearchSummaryDTO;
import fi.riista.feature.permit.application.research.ResearchSummaryFeature;
import fi.riista.feature.permit.application.research.amount.ResearchSpeciesAmountAndReasonFeature;
import fi.riista.feature.permit.application.research.amount.ResearchSpeciesAmountDTO;
import fi.riista.feature.permit.application.research.applicant.ResearchApplicantFeature;
import fi.riista.feature.permit.application.research.forbidden.ResearchForbiddenMethodsFeature;
import fi.riista.feature.permit.application.research.justification.ResearchJustificationDTO;
import fi.riista.feature.permit.application.research.justification.ResearchJustificationFeature;
import fi.riista.feature.permit.application.research.period.ResearchSpeciesPeriodFeature;
import fi.riista.feature.permit.application.research.period.ResearchSpeciesPeriodInformationDTO;
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
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Locale;

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/research")
public class ResearchPermitApplicationApiResource {

    @Resource
    private ResearchApplicantFeature researchApplicantFeature;

    @Resource
    private ResearchSpeciesAmountAndReasonFeature researchSpeciesAmountAndReasonFeature;

    @Resource
    private ResearchSpeciesPeriodFeature researchSpeciesPeriodFeature;

    @Resource
    private ResearchForbiddenMethodsFeature researchForbiddenMethodsFeature;

    @Resource
    private ResearchJustificationFeature researchJustificationFeature;

    @Resource
    private ResearchSummaryFeature researchSummaryFeature;

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return researchApplicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {
        researchApplicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // SPECIES AMOUNTS

    static class AmountList {

        @NotEmpty
        @Valid
        public List<ResearchSpeciesAmountDTO> list;

        public List<ResearchSpeciesAmountDTO> getList() {
            return list;
        }

        public void setList(final List<ResearchSpeciesAmountDTO> list) {
            this.list = list;
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ResearchSpeciesAmountDTO> getSpeciesAmounts(@PathVariable final long applicationId) {
        return researchSpeciesAmountAndReasonFeature.getSpeciesAmounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesAmounts(final @PathVariable long applicationId,
                                   final @Valid @RequestBody ResearchPermitApplicationApiResource.AmountList request) {
        researchSpeciesAmountAndReasonFeature.saveSpeciesAmountsAndDerogationReasons(applicationId, request.list);
    }

    // PERIODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResearchSpeciesPeriodInformationDTO getSpeciesPeriods(@PathVariable final long applicationId) {
        return researchSpeciesPeriodFeature.getSpeciesPeriods(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesPeriods(@PathVariable final long applicationId,
                                   @Valid @RequestBody ResearchSpeciesPeriodInformationDTO dto) {
        researchSpeciesPeriodFeature.saveSpeciesPeriods(applicationId, dto);
    }

    // FORBIDDEN METHODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_VALUE)
    public DerogationPermitApplicationForbiddenMethodsDTO getForbiddenMethods(@PathVariable final long applicationId) {
        return researchForbiddenMethodsFeature.getForbiddenMethods(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateForbiddenMethods(@PathVariable final long applicationId,
                                       @Valid @RequestBody final DerogationPermitApplicationForbiddenMethodsDTO dto) {
        researchForbiddenMethodsFeature.updateForbiddenMethods(applicationId, dto);
    }

    // RESEARCH JUSTIFICATION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/justification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResearchJustificationDTO getJustification(@PathVariable final long applicationId) {
        return researchJustificationFeature.getJustification(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/justification")
    public void updateJustification(@PathVariable final long applicationId,
                                    @Valid @RequestBody final ResearchJustificationDTO dto) {
        researchJustificationFeature.updateJustification(applicationId, dto);
    }

    // SUMMARY

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResearchSummaryDTO readDetails(@PathVariable final long applicationId, final Locale locale) {
        return researchSummaryFeature.readDetails(applicationId, locale);
    }
}
