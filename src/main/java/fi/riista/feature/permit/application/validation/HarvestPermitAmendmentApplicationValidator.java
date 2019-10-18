package fi.riista.feature.permit.application.validation;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationData;

public class HarvestPermitAmendmentApplicationValidator {

    private HarvestPermitAmendmentApplicationValidator() {
        throw new UnsupportedOperationException();
    }

    static void validateForSending(HarvestPermitApplication application, AmendmentApplicationData data) {
        ValidationUtil.validateForSending(application);

        validateContent(application, data);
    }

    static void validateForAmending(HarvestPermitApplication application, AmendmentApplicationData data) {
        ValidationUtil.validateForAmend(application);
        validateContent(application, data);
    }

    static void validateContent(HarvestPermitApplication application, AmendmentApplicationData data) {
        // TODO: Should amendment application also validate rhy?

        if (data == null) {
            throw new IllegalStateException("Specimen required for applicationId: " + application.getId());
        }
        if (application.getSpeciesAmounts().size() == 0) {
            throw new IllegalStateException("Species is required for applicationId: " + application.getId());
        }
        if (application.getSpeciesAmounts().size() > 1) {
            throw new IllegalStateException("Only one species is possible for applicationId: " + application.getId());
        }
        if (data.getPointOfTime() == null) {
            throw new IllegalStateException("Point of time is required for applicationId: " + application.getId());
        }
        if (data.getAge() == null) {
            throw new IllegalStateException("Age is required for applicationId: " + application.getId());
        }
        if (data.getGender() == null) {
            throw new IllegalStateException("Gender is required for applicationId: " + application.getId());
        }
        if (data.getPartner() == null) {
            throw new IllegalStateException("Partner is required for applicationId: " + application.getId());
        }
        if (!data.getOriginalPermit().getPermitPartners().contains(data.getPartner())) {
            throw new IllegalStateException("Partner not in original permit, applicationId: " + application.getId());
        }
        if (data.getGeoLocation() == null) {
            throw new IllegalStateException("Geolocation is required for applicationId: " + application.getId());
        }
        //TODO ?
        // validate species exists in original permit
        // validate point of time in range of permitted
        // validate geolocation in finland

        //TODO
//        if (application.getDeliveryByMail() == null) {
//            throw new IllegalStateException("deliveryByMail is missing");
//        }
    }
}
