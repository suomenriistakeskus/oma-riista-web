package fi.riista.api.application;

import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.gamemanagement.amount.GameManagementSpeciesAmountDTO;
import fi.riista.feature.permit.application.gamemanagement.amount.GameManagementSpeciesAmountFeature;
import fi.riista.feature.permit.application.gamemanagement.applicant.GameManagementApplicantFeature;
import fi.riista.feature.permit.application.gamemanagement.forbidden.GameManagementForbiddenMethodsFeature;
import fi.riista.feature.permit.application.gamemanagement.justification.GameManagementJustificationDTO;
import fi.riista.feature.permit.application.gamemanagement.justification.GameManagementJustificationFeature;
import fi.riista.feature.permit.application.gamemanagement.period.GameManagementSpeciesPeriodDTO;
import fi.riista.feature.permit.application.gamemanagement.period.GameManagementSpeciesPeriodFeature;
import fi.riista.feature.permit.application.gamemanagement.summary.GameManagementSummaryDTO;
import fi.riista.feature.permit.application.gamemanagement.summary.GameManagementSummaryFeature;
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

import static fi.riista.api.application.HarvestPermitApplicationApiResource.API_PREFIX;

@RestController
@RequestMapping(value = API_PREFIX + "/gamemanagement")
public class GameManagementPermitApplicationApiResource {

    @Resource
    private GameManagementApplicantFeature applicantFeature;

    @Resource
    private GameManagementSpeciesAmountFeature speciesAmountFeature;

    @Resource
    private GameManagementSpeciesPeriodFeature speciesPeriodFeature;

    @Resource
    private GameManagementJustificationFeature justificationFeature;

    @Resource
    private GameManagementForbiddenMethodsFeature forbiddenMethodsFeature;

    @Resource
    private GameManagementSummaryFeature summaryFeature;

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

    // SPECIES AMOUNT

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public GameManagementSpeciesAmountDTO getSpeciesAmount(@PathVariable final long applicationId) {
        return speciesAmountFeature.getSpeciesAmount(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/species", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesAmount(final @PathVariable long applicationId,
                                  final @Valid @RequestBody GameManagementSpeciesAmountDTO gameManagementSpeciesAmountDTO) {
        speciesAmountFeature.saveSpeciesAmount(applicationId, gameManagementSpeciesAmountDTO);
    }

    // SPECIES PERIOD

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public GameManagementSpeciesPeriodDTO getSpeciesPeriod(@PathVariable final long applicationId) {
        return speciesPeriodFeature.getSpeciesPeriod(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/period", produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveSpeciesPeriod(final @PathVariable long applicationId,
                                  final @Valid @RequestBody GameManagementSpeciesPeriodDTO gameManagementSpeciesPeriodDTO) {
        speciesPeriodFeature.saveSpeciesPeriod(applicationId, gameManagementSpeciesPeriodDTO);
    }

    // JUSTIFICATION

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/justification", produces = MediaType.APPLICATION_JSON_VALUE)
    public GameManagementJustificationDTO getJustification(@PathVariable final long applicationId) {
        return justificationFeature.getJustification(applicationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/justification")
    public void updateJustification(@PathVariable final long applicationId,
                                    @Valid @RequestBody final GameManagementJustificationDTO dto) {
        justificationFeature.updateJustification(applicationId, dto);
    }

    // FORBIDDEN METHODS

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateMethods(@PathVariable final long applicationId,
                              @Valid @RequestBody final DerogationPermitApplicationForbiddenMethodsDTO dto) {
        forbiddenMethodsFeature.updateMethods(applicationId, dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/method", produces = MediaType.APPLICATION_JSON_VALUE)
    public DerogationPermitApplicationForbiddenMethodsDTO getMethods(@PathVariable final long applicationId) {
        return forbiddenMethodsFeature.getMethods(applicationId);
    }

    // SUMMARY

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{applicationId:\\d+}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public GameManagementSummaryDTO readDetails(@PathVariable final long applicationId) {
        return summaryFeature.readDetails(applicationId);
    }

}
