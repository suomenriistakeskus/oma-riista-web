package fi.riista.api.decision.permit;

import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.permit.decision.derogation.PermitDecisionAnnualRenewalFeature;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/decision/annual/{id:\\d+}")
public class PermitDecisionAnnualRenewalApiResource {

    @Resource
    private PermitDecisionAnnualRenewalFeature renewalFeature;

    @GetMapping
    public BooleanDTO isRenewable(final @PathVariable long id) {
        return new BooleanDTO(renewalFeature.isRenewable(id));
    }

    @PostMapping
    public HarvestPermitDTO renewPermit(final @PathVariable long id,
                                        final @RequestParam Integer permitYear) {
        return renewalFeature.createNextAnnualPermit(id, permitYear);
    }
}
