package fi.riista.api.decision.nomination;

import fi.riista.feature.common.decision.DecisionHandlerDTO;
import fi.riista.feature.common.decision.nomination.NominationDecisionDTO;
import fi.riista.feature.common.decision.nomination.NominationDecisionFeature;
import fi.riista.feature.common.decision.nomination.NominationDecisionSearchDTO;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/nominationdecision/search")
public class NominationDecisionSearchApiResource {

    @Resource
    private NominationDecisionFeature nominationDecisionFeature;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Slice<NominationDecisionDTO> listDecisions(final @RequestBody @Valid NominationDecisionSearchDTO dto){
        return nominationDecisionFeature.search(dto);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/handlers")
    public List<DecisionHandlerDTO> listHandlers(){
        return nominationDecisionFeature.listHandlers();
    }
}
