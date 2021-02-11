package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationSummaryDTO;
import fi.riista.feature.permit.application.importing.ImportingPermitApplicationSummaryFeature;
import fi.riista.feature.permit.application.importing.amount.ImportingPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.importing.amount.ImportingPermitApplicationSpeciesAmountFeature;
import fi.riista.feature.permit.application.importing.applicant.ImportingPermitApplicantFeature;
import fi.riista.feature.permit.application.importing.justification.ImportingPermitApplicationJustificationDTO;
import fi.riista.feature.permit.application.importing.justification.ImportingPermitApplicationJustificationFeature;
import fi.riista.feature.permit.application.importing.period.ImportingPermitApplicationSpeciesPeriodFeature;
import fi.riista.feature.permit.application.importing.period.ImportingPermitApplicationSpeciesPeriodInformationDTO;
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

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/importing")
public class ImportingPermitApplicationApiResource {

    @Resource
    private ImportingPermitApplicantFeature applicantFeature;

    @Resource
    private ImportingPermitApplicationSpeciesAmountFeature speciesAmountFeature;

    @Resource
    private ImportingPermitApplicationSpeciesPeriodFeature periodFeature;

    @Resource
    private ImportingPermitApplicationJustificationFeature justificationFeature;

    @Resource
    private ImportingPermitApplicationSummaryFeature summaryFeature;

    // READ

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public ImportingPermitApplicationSummaryDTO readDetails(@PathVariable final long applicationId) {
        return summaryFeature.readDetails(applicationId);
    }

    // PERMIT HOLDER

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/permit-holder", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermitHolderDTO getPermitHolderinfo(@PathVariable final long applicationId) {
        return applicantFeature.getPermitHolderInfo(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/permit-holder")
    public void updatePermitHolder(@PathVariable final long applicationId,
                                   @Valid @RequestBody final PermitHolderDTO permitHolder) {
        applicantFeature.updatePermitHolder(applicationId, permitHolder);
    }

    // SPECIES AMOUNTS

    static class AmountList {

        @NotEmpty
        @Valid
        public List<ImportingPermitApplicationSpeciesAmountDTO> list;

        public List<ImportingPermitApplicationSpeciesAmountDTO> getList() {
            return list;
        }

        public void setList(final List<ImportingPermitApplicationSpeciesAmountDTO> list) {
            this.list = list;
        }
    }

    // SPECIES PERIODS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public ImportingPermitApplicationSpeciesPeriodInformationDTO getSpeciesPeriods(@PathVariable final long applicationId) {
        return periodFeature.getPermitPeriodInformation(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesPeriods(
            @PathVariable final long applicationId,
            @Valid @RequestBody ImportingPermitApplicationSpeciesPeriodInformationDTO dto) {
        periodFeature.saveSpeciesPeriods(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ImportingPermitApplicationSpeciesAmountDTO> getSpeciesAmounts(@PathVariable final long applicationId) {
        return speciesAmountFeature.getSpeciesAmounts(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesAmounts(
            @PathVariable final long applicationId,
            @Valid @RequestBody final ImportingPermitApplicationApiResource.AmountList request) {
        speciesAmountFeature.saveSpeciesAmounts(applicationId, request.list);
    }

    // JUSTIFICATION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/justification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ImportingPermitApplicationJustificationDTO getJustification(@PathVariable final long applicationId) {
        return justificationFeature.getJustification(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/justification")
    public void updateJustification(@PathVariable final long applicationId,
                                    @Valid @RequestBody final ImportingPermitApplicationJustificationDTO justification) {

        justificationFeature.updateJustification(applicationId, justification);
    }
}
