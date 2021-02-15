package fi.riista.api.decision;

import fi.riista.feature.common.decision.authority.rka.DecisionRkaAuthorityDTO;
import fi.riista.feature.common.decision.authority.rka.DecisionRkaAuthorityFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/decision/rkaauthority")
public class RkaAuthorityApiResource {

    @Resource
    private DecisionRkaAuthorityFeature decisionRkaAuthorityFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/rka/{rkaId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DecisionRkaAuthorityDTO> listByRka(@PathVariable long rkaId) {
        return decisionRkaAuthorityFeature.listByRka(rkaId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping
    public void create(@RequestBody @Valid DecisionRkaAuthorityDTO dto) {
        decisionRkaAuthorityFeature.create(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{id:\\d+}")
    public void update(@RequestBody @Valid DecisionRkaAuthorityDTO dto) {
        decisionRkaAuthorityFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id:\\d+}")
    public void delete(@PathVariable long id) {
        decisionRkaAuthorityFeature.delete(id);
    }

}
