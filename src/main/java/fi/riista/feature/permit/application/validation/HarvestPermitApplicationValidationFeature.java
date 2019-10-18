package fi.riista.feature.permit.application.validation;

import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class HarvestPermitApplicationValidationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationValidationService harvestPermitApplicationValidationService;

    @Transactional(readOnly = true)
    public void validate(final long applicationId) {
        harvestPermitApplicationValidationService.validateContent(
                harvestPermitApplicationAuthorizationService.readApplication(applicationId));
    }

}
