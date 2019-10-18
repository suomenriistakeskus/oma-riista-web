package fi.riista.api.decision;

import fi.riista.feature.permit.decision.delivery.rkarecipient.PermitDecisionRkaRecipientDTO;
import fi.riista.feature.permit.decision.delivery.rkarecipient.PermitDecisionRkaRecipientFeature;
import fi.riista.feature.permit.decision.delivery.rkarecipient.PermitDecisionRkaRecipientListingDTO;
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
@RequestMapping(value = "/api/v1/decision/rkarecipient")
public class PermitDecisionRkaRecipientApiResource {

    @Resource
    private PermitDecisionRkaRecipientFeature permitDecisionRkaRecipientFeature;

    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionRkaRecipientListingDTO> getRkaDeliveryList() {
        return permitDecisionRkaRecipientFeature.list();
    }

    @GetMapping(value = "/rka/{rkaId:\\d+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<PermitDecisionRkaRecipientDTO> listByRka(@PathVariable long rkaId) {
        return permitDecisionRkaRecipientFeature.listByRka(rkaId);
    }

    @PostMapping
    public void create(@RequestBody @Valid PermitDecisionRkaRecipientDTO dto) {
        permitDecisionRkaRecipientFeature.create(dto);
    }

    @PutMapping(value = "/{id:\\d+}")
    public void update(@RequestBody @Valid PermitDecisionRkaRecipientDTO dto) {
        permitDecisionRkaRecipientFeature.update(dto);
    }

    @DeleteMapping(value = "/{id:\\d+}")
    public void delete(@PathVariable long id) {
        permitDecisionRkaRecipientFeature.delete(id);
    }
}
