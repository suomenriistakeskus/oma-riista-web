package fi.riista.feature.permit.application;

import fi.riista.util.Locales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

@Component
public class HarvestPermitApplicationFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Transactional(readOnly = true)
    public HarvestPermitApplicationBasicDetailsDTO getBasicDetails(final long applicationId) {
        return new HarvestPermitApplicationBasicDetailsDTO(
                harvestPermitApplicationAuthorizationService.readApplication(applicationId));
    }

    @Transactional
    public void updateAdditionalData(final long applicationId, final HarvestPermitApplicationAdditionalDataDTO dto) {

        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(applicationId);

        application.setDeliveryByMail(dto.isDeliveryByMail());
        application.setEmail1(dto.getEmail1());
        application.setEmail2(dto.getEmail2());
        Optional.ofNullable(dto.getDecisionLanguage())
                .map(Locales::getLocaleByLanguageCode)
                .ifPresent(application::setDecisionLocale);

        final DeliveryAddressDTO deliveryAddress = dto.getDeliveryAddress();
        application.setDeliveryAddress(deliveryAddress.toEntity());
    }

}
