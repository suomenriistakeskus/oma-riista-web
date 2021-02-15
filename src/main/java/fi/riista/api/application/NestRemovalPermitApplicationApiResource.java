package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonFeature;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.nestremoval.NestRemovalPermitApplicationSummaryFeature;
import fi.riista.feature.permit.application.nestremoval.amount.NestRemovalPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.nestremoval.amount.NestRemovalPermitApplicationSpeciesAmountFeature;
import fi.riista.feature.permit.application.nestremoval.applicant.NestRemovalPermitApplicationApplicantFeature;
import fi.riista.feature.permit.application.nestremoval.period.NestRemovalPermitApplicationSpeciesPeriodFeature;
import fi.riista.feature.permit.application.nestremoval.period.NestRemovalPermitApplicationSpeciesPeriodInformationDTO;
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
@RequestMapping(value = API_PREFIX + "/nestremoval")
public class NestRemovalPermitApplicationApiResource {

    @Resource
    private NestRemovalPermitApplicationApplicantFeature nestRemovalPermitApplicationApplicantFeature;

    @Resource
    private NestRemovalPermitApplicationSpeciesAmountFeature nestRemovalPermitApplicationSpeciesAmountFeature;

    @Resource
    private DerogationPermitApplicationReasonFeature derogationPermitApplicationReasonFeature;

    @Resource
    private NestRemovalPermitApplicationSpeciesPeriodFeature nestRemovalPermitApplicationSpeciesPeriodFeature;

    @Resource
    private NestRemovalPermitApplicationSummaryFeature nestRemovalPermitApplicationSummaryFeature;

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return nestRemovalPermitApplicationApplicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {
        nestRemovalPermitApplicationApplicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // SPECIES AMOUNTS

    static class AmountList {

        @NotEmpty
        @Valid
        public List<NestRemovalPermitApplicationSpeciesAmountDTO> list;

        public List<NestRemovalPermitApplicationSpeciesAmountDTO> getList() {
            return list;
        }

        public void setList(final List<NestRemovalPermitApplicationSpeciesAmountDTO> list) {
            this.list = list;
        }
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NestRemovalPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(@PathVariable final long applicationId) {
        return nestRemovalPermitApplicationSpeciesAmountFeature.getSpeciesAmounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesAmounts(
            @PathVariable final long applicationId,
            @Valid @RequestBody final NestRemovalPermitApplicationApiResource.AmountList request) {
        nestRemovalPermitApplicationSpeciesAmountFeature.saveSpeciesAmounts(applicationId, request.list);
    }

    // PERMIT CAUSE

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/reasons", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updatePermitCause(
            @PathVariable final long applicationId,
            @Valid @RequestBody final DerogationPermitApplicationReasonsDTO dto) {
        derogationPermitApplicationReasonFeature.updateDerogationReasons(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/reasons", produces = MediaType.APPLICATION_JSON_VALUE)
    public DerogationPermitApplicationReasonsDTO getPermitCauseInfo(@PathVariable final long applicationId,
                                                                    final Locale locale) {
        return derogationPermitApplicationReasonFeature.getDerogationReasons(applicationId, locale);
    }

    // SPECIES PERIODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public NestRemovalPermitApplicationSpeciesPeriodInformationDTO getSpeciesPeriods(@PathVariable final long applicationId) {
        return nestRemovalPermitApplicationSpeciesPeriodFeature.getPermitPeriodInformation(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesPeriods(
            @PathVariable final long applicationId,
            @Valid @RequestBody final NestRemovalPermitApplicationSpeciesPeriodInformationDTO dto) {
        nestRemovalPermitApplicationSpeciesPeriodFeature.saveSpeciesPeriods(applicationId, dto);
    }

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public NestRemovalPermitApplicationSummaryDTO readDetails(@PathVariable final long applicationId, final Locale locale) {
        return nestRemovalPermitApplicationSummaryFeature.readDetails(applicationId, locale);
    }

}
