package fi.riista.api.decision;

import fi.riista.feature.permit.decision.authority.rka.PermitDecisionRkaAuthorityDTO;
import fi.riista.feature.permit.decision.authority.rka.PermitDecisionRkaAuthorityFeature;
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
    private PermitDecisionRkaAuthorityFeature permitDecisionRkaAuthorityFeature;

    @GetMapping(value = "/rka/{rkaId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionRkaAuthorityDTO> listByRka(@PathVariable long rkaId) {
        return permitDecisionRkaAuthorityFeature.listByRka(rkaId);
    }

    @PostMapping
    public void create(@RequestBody @Valid PermitDecisionRkaAuthorityDTO dto) {
        permitDecisionRkaAuthorityFeature.create(dto);
    }

    @PutMapping(value = "/{id:\\d+}")
    public void update(@RequestBody @Valid PermitDecisionRkaAuthorityDTO dto) {
        permitDecisionRkaAuthorityFeature.update(dto);
    }

    @DeleteMapping(value = "/{id:\\d+}")
    public void delete(@PathVariable long id) {
        permitDecisionRkaAuthorityFeature.delete(id);
    }

    @GetMapping(value = "/decision/{decisionId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionRkaAuthorityDTO> listByDecision(@PathVariable long decisionId) {
        return permitDecisionRkaAuthorityFeature.listByDecision(decisionId);
    }
}
