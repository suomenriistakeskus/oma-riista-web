package fi.riista.api.decision;


import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonFeature;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonsDTO;
import fi.riista.feature.permit.decision.derogation.PermitDecisionProtectedAreaTypeFeature;
import fi.riista.feature.permit.decision.derogation.PermitDecisionProtectedAreaTypesDTO;
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

@RestController
@RequestMapping(value = "/api/v1/decision/derogation")
public class PermitDecisionDerogationApiResource {

    @Resource
    private PermitDecisionDerogationReasonFeature permitDecisionDerogationReasonFeature;

    @Resource
    private PermitDecisionProtectedAreaTypeFeature permitDecisionProtectedAreaTypeFeature;

    // REASONS

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/reasons", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitDecisionDerogationReasonsDTO getDerogations(final @PathVariable long decisionId) {
        return permitDecisionDerogationReasonFeature.getDerogationReasons(decisionId);
    }

    @PostMapping(value = "{decisionId:\\d+}/reasons")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDerogations(
            final @PathVariable long decisionId,
            final @RequestBody @Valid PermitDecisionDerogationReasonsDTO dto) {
        permitDecisionDerogationReasonFeature.updateDerogationReasons(decisionId, dto);
    }


    // PROTECTED AREA

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{decisionId:\\d+}/area", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PermitDecisionProtectedAreaTypesDTO getProtectedAreaTypes(final @PathVariable long decisionId) {
        return permitDecisionProtectedAreaTypeFeature.getProtectedAreaTypes(decisionId);
    }

    @PostMapping(value = "{decisionId:\\d+}/area")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProtectedAreaTypes(
            final @PathVariable long decisionId,
            final @RequestBody @Valid PermitDecisionProtectedAreaTypesDTO dto) {
        permitDecisionProtectedAreaTypeFeature.updateProtectedAreaTypes(decisionId, dto);
    }
}
