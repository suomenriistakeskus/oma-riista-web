package fi.riista.api.decision.permit;

import fi.riista.feature.common.decision.authority.rka.DecisionRkaAuthorityDTO;
import fi.riista.feature.common.decision.authority.rka.DecisionRkaAuthorityFeature;
import fi.riista.feature.permit.decision.authority.PermitDecisionAuthorityFeature;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/decision/rkaauthority")
public class PermitDecisionRkaAuthorityApiResource {

    @Resource
    private PermitDecisionAuthorityFeature permitDecisionAuthorityFeature;


    @GetMapping(value = "/permitdecision/{decisionId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DecisionRkaAuthorityDTO> listByDecision(@PathVariable long decisionId) {
        return permitDecisionAuthorityFeature.listByPermitDecision(decisionId);
    }
}
