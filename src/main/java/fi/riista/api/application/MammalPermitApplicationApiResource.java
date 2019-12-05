package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonFeature;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationSummaryFeature;
import fi.riista.feature.permit.application.mammal.amount.MammalPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.mammal.amount.MammalPermitApplicationSpeciesAmountFeature;
import fi.riista.feature.permit.application.mammal.applicant.MammalPermitApplicationApplicantFeature;
import fi.riista.feature.permit.application.mammal.forbidden.MammalPermitApplicationForbiddenMethodsFeature;
import fi.riista.feature.permit.application.mammal.period.MammalPermitApplicationSpeciesPeriodFeature;
import fi.riista.feature.permit.application.mammal.period.MammalPermitApplicationSpeciesPeriodInformationDTO;
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
@RequestMapping(value = API_PREFIX + "/mammal")
public class MammalPermitApplicationApiResource {

    @Resource
    private MammalPermitApplicationApplicantFeature mammalPermitApplicationApplicantFeature;

    @Resource
    private MammalPermitApplicationSpeciesAmountFeature mammalPermitApplicationSpeciesAmountFeature;

    @Resource
    private MammalPermitApplicationSpeciesPeriodFeature mammalPermitApplicationSpeciesPeriodFeature;

    @Resource
    private MammalPermitApplicationForbiddenMethodsFeature mammalPermitApplicationForbiddenMethodsFeature;

    @Resource
    private DerogationPermitApplicationReasonFeature derogationPermitApplicationReasonFeature;

    @Resource
    private MammalPermitApplicationSummaryFeature mammalPermitApplicationSummaryFeature;

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MammalPermitApplicationSummaryDTO readDetails(@PathVariable final long applicationId, final Locale locale) {
        return mammalPermitApplicationSummaryFeature.readDetails(applicationId, locale);
    }

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return mammalPermitApplicationApplicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {

        mammalPermitApplicationApplicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // SPECIES PERIODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MammalPermitApplicationSpeciesPeriodInformationDTO getSpeciesPeriods(@PathVariable final long applicationId) {
        return mammalPermitApplicationSpeciesPeriodFeature.getPermitPeriodInformation(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void saveSpeciesPeriods(
            @PathVariable final long applicationId,
            @Valid @RequestBody MammalPermitApplicationSpeciesPeriodInformationDTO dto) {
        mammalPermitApplicationSpeciesPeriodFeature.saveSpeciesPeriods(applicationId, dto);
    }

    // SPECIES AMOUNTS

    static class AmountList {
        @Valid
        public List<MammalPermitApplicationSpeciesAmountDTO> list;

        public List<MammalPermitApplicationSpeciesAmountDTO> getList() {
            return list;
        }

        public void setList(final List<MammalPermitApplicationSpeciesAmountDTO> list) {
            this.list = list;
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<MammalPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(@PathVariable final long applicationId) {
        return mammalPermitApplicationSpeciesAmountFeature.getSpeciesAmounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void saveSpeciesAmounts(
            final @PathVariable long applicationId,
            final @Valid @RequestBody AmountList request) {
        mammalPermitApplicationSpeciesAmountFeature.saveSpeciesAmounts(applicationId, request.list);
    }


    // DEVIATION JUSTIFICATION

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updatePermitCause(
            @PathVariable final long applicationId,
            @Valid @RequestBody final DerogationPermitApplicationForbiddenMethodsDTO dto) {
        mammalPermitApplicationForbiddenMethodsFeature.updateMethodInfo(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public DerogationPermitApplicationForbiddenMethodsDTO getDeviationJustification(@PathVariable final long applicationId) {
        return mammalPermitApplicationForbiddenMethodsFeature.getCurrentMethodInfo(applicationId);
    }

    // PERMIT CAUSE

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/reasons", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updatePermitCause(
            @PathVariable final long applicationId,
            @Valid @RequestBody final DerogationPermitApplicationReasonsDTO dto) {
        derogationPermitApplicationReasonFeature.updateDerogationReasons(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/reasons", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public DerogationPermitApplicationReasonsDTO getPermitCauseInfo(@PathVariable final long applicationId,
                                                                    final Locale locale) {
        return derogationPermitApplicationReasonFeature.getDerogationReasons(applicationId, locale);
    }
}
