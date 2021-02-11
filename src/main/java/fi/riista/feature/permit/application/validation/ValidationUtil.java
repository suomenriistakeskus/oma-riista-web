package fi.riista.feature.permit.application.validation;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.IllegalPermitApplicationStateTransitionException;
import fi.riista.util.F;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

    public static void validateCommonHarvestPermitContent(HarvestPermitApplication application) {
        ValidationUtil.validateSpeciesAmounts(application);
        ValidationUtil.validateCommonContent(application);
    }

    public static void validateCommonContent(HarvestPermitApplication application) {
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

        // TODO: Handle in category spesific validator
        switch (application.getHarvestPermitCategory()) {
            case NEST_REMOVAL: {
                assertSpecimenAmountsNull(application.getSpeciesAmounts());
                assertNestRemovalAmountsPresent(application.getSpeciesAmounts());
                break;
            }
            case IMPORTING: {
                assertImportingAmountsPresent(application.getSpeciesAmounts());
                break;
            }
            default: {
                assertSpecimenAmountsPresent(application.getSpeciesAmounts());
                break;
            }

        }
    }

    private static void assertNestRemovalAmountsPresent(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        speciesAmounts.forEach(spa -> Stream.of(spa.getNestAmount(),
                spa.getEggAmount(),
                spa.getConstructionAmount())
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Nest removal amounts not present")));
    }

    private static void assertImportingAmountsPresent(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        speciesAmounts.forEach(spa -> Stream.of(spa.getSpecimenAmount(), spa.getEggAmount())
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Importing amounts not present")));
    }

    private static void assertSpecimenAmountsNull(final Iterable<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        speciesAmounts.forEach(spa -> {
            if (spa.getSpecimenAmount() != null) {
                throw new IllegalStateException("Specimen amount should be null");
            }
        });
    }

    private static void assertSpecimenAmountsPresent(final Iterable<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        speciesAmounts.forEach(spa -> {
            if (spa.getSpecimenAmount() == null) {
                throw new IllegalStateException("Specimen amount should NOT be null");
            }
        });
    }
}
