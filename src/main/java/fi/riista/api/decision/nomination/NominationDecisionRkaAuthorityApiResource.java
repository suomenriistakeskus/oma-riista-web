package fi.riista.api.decision.nomination;

import fi.riista.feature.common.decision.authority.rka.DecisionRkaAuthorityDTO;
import fi.riista.feature.common.decision.nomination.authority.NominationDecisionAuthorityFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/nominationdecision/rkaauthority")
public class NominationDecisionRkaAuthorityApiResource {

    @Resource
    private NominationDecisionAuthorityFeature nominationDecisionAuthorityFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/nominationdecision/{decisionId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DecisionRkaAuthorityDTO> listByDecision(@PathVariable long decisionId) {
        return nominationDecisionAuthorityFeature.listByNominationDecision(decisionId);
    }
}
