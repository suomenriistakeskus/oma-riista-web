package fi.riista.feature.permit.application.validation;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.IllegalPermitApplicationStateTransitionException;
import fi.riista.util.F;

import java.util.HashSet;

public class ValidationUtil {

    private ValidationUtil() {
        throw new UnsupportedOperationException();
    }

    public static void validateForSending(HarvestPermitApplication application) {
        // validate state
        if (application.getStatus() != HarvestPermitApplication.Status.DRAFT) {
            throw new IllegalPermitApplicationStateTransitionException(application.getId());
        }

        if (application.getSubmitDate() != null) {
            throw new IllegalStateException("Application submitDate already set for applicationId: " + application.getId());
        }

        if (application.getApplicationNumber() != null) {
            throw new IllegalStateException("Application number already set for applicationId: " + application.getId());
        }

    }

    public static void validateForAmend(HarvestPermitApplication application) {
        if (application.getStatus() != HarvestPermitApplication.Status.AMENDING) {
            throw new IllegalPermitApplicationStateTransitionException(application.getId());
        }

        if (application.getSubmitDate() == null) {
            throw new IllegalStateException("Application submitDate missing for applicationId: " + application.getId());
        }

        if (application.getApplicationNumber() == null) {
            throw new IllegalStateException("Application number missing for applicationId: " + application.getId());
        }
    }

    public static void validateCommonContent(HarvestPermitApplication application) {
        ValidationUtil.validateSpeciesAmounts(application);

        if (application.getRhy() == null) {
            throw new IllegalStateException("Application RHY is not available");
        }

        if (application.getDeliveryByMail() == null) {
            throw new IllegalStateException("deliveryByMail is missing");
        }

    }

    public static void validateSpeciesAmounts(HarvestPermitApplication application) {
        if (application.getSpeciesAmounts().isEmpty()) {
            throw new IllegalStateException("speciesAmounts are missing");
        }

        final HashSet<Long> uniqueSpeciesIds = F.mapNonNullsToSet(
                application.getSpeciesAmounts(), spa -> spa.getGameSpecies().getId());

        if (uniqueSpeciesIds.size() != application.getSpeciesAmounts().size()) {
            throw new IllegalStateException("speciesAmount species are not unique");
        }

        for (final HarvestPermitApplicationSpeciesAmount speciesAmount : application.getSpeciesAmounts()) {
            if (speciesAmount.getAmount() < 1.0) {
                throw new IllegalStateException("speciesAmount is invalid: " + speciesAmount.getAmount());
            }
        }
    }

}
