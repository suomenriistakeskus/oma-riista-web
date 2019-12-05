package fi.riista.feature.permit.application.bird;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.bird.area.BirdPermitApplicationProtectedArea;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class BirdPermitApplicationValidator {
    private BirdPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }

    public static void validateForSending(HarvestPermitApplication application, BirdPermitApplication birdApplication) {
        requireNonNull(application);
        requireNonNull(birdApplication);

        ValidationUtil.validateForSending(application);

        validateContent(application, birdApplication);
    }

    public static void validateForAmend(HarvestPermitApplication application, BirdPermitApplication birdApplication) {
        requireNonNull(application);
        requireNonNull(birdApplication);

        ValidationUtil.validateForAmend(application);

        validateContent(application, birdApplication);
    }

    public static void validateContent(HarvestPermitApplication application, BirdPermitApplication birdApplication) {
        requireNonNull(application);
        requireNonNull(birdApplication);

        ValidationUtil.validateSpeciesAmounts(application);
        ValidationUtil.validateCommonContent(application);

        assertPermitHolderInformationValid(application);
        assertAreaInformationValid(birdApplication);
        assertSomeCauseSelected(birdApplication);
        assertPeriodInformationValid(birdApplication, application.getSpeciesAmounts());
        assertMethodsInformationValid(birdApplication, application.getSpeciesAmounts());
        assertDamagesInformationValid(application.getSpeciesAmounts());
        assertPopulationInformationValid(application.getSpeciesAmounts());
        assertAttachmentsValid(application.getAttachments());
    }

    static void assertPermitHolderInformationValid(HarvestPermitApplication application) {
        final PermitHolder permitHolder = application.getPermitHolder();
        requireNonNull(permitHolder);
        if (permitHolder.getName() == null) {
            failValidation("Permit holder name missing");
        }

        if (permitHolder.getCode() == null && !permitHolder.getType().equals(PermitHolder.PermitHolderType.PERSON)) {
            failValidation("Code missing for permit holder");
        }
    }

    static void assertAreaInformationValid(BirdPermitApplication birdApplication) {
        final BirdPermitApplicationProtectedArea protectedArea = birdApplication.getProtectedArea();
        requireNonNull(protectedArea);

        if (protectedArea.getProtectedAreaType() == null ||
                StringUtils.isEmpty(protectedArea.getName()) ||
                StringUtils.isEmpty(protectedArea.getStreetAddress()) ||
                StringUtils.isEmpty(protectedArea.getPostalCode()) ||
                StringUtils.isEmpty(protectedArea.getCity()) ||
                StringUtils.isEmpty(protectedArea.getDescriptionOfRights())) {
            failValidation("Protected area has missing information.");
        }

        if (protectedArea.getProtectedAreaSize() == null ||
                protectedArea.getProtectedAreaSize() <= 0) {
            failValidation("Invalid area size.");
        }

        if (protectedArea.getGeoLocation() == null) {
            failValidation("Geolocation missing.");
        }
    }

    static void assertSomeCauseSelected(BirdPermitApplication birdApplication) {
        final BirdPermitApplicationCause cause = birdApplication.getCause();
        requireNonNull(cause);

        if (cause.isCausePublicHealth() ||
                cause.isCausePublicSafety() ||
                cause.isCauseAviationSafety() ||
                cause.isCauseCropsDamage() ||
                cause.isCauseDomesticPets() ||
                cause.isCauseForestDamage() ||
                cause.isCauseFishing() ||
                cause.isCauseWaterSystem() ||
                cause.isCauseFlora() ||
                cause.isCauseFauna() ||
                cause.isCauseResearch()) {
            return;
        }

        failValidation("No reasons selected");
    }

    static void assertPeriodInformationValid(BirdPermitApplication birdApplication,
                                             List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        final boolean limitlessPermitAllowed = birdApplication.isLimitlessPermitAllowed();
        for (HarvestPermitApplicationSpeciesAmount spa : speciesAmounts) {
            assertSpeciesPeriodInformation(spa, limitlessPermitAllowed);
        }

        final long distinctYearValues = speciesAmounts.stream()
                .map(HarvestPermitApplicationSpeciesAmount::getValidityYears)
                .mapToInt(years -> years != null ? years : -1)
                .distinct().count();

        if (distinctYearValues != 1) {
            failValidation("All validity years must match");
        }
    }

    static void assertMethodsInformationValid(BirdPermitApplication birdApplication,
                                              List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        final DerogationPermitApplicationForbiddenMethods forbiddenMethods = birdApplication.getForbiddenMethods();
        requireNonNull(forbiddenMethods);

        if (forbiddenMethods.isTapeRecorders() ||
                forbiddenMethods.isTraps() ||
                !StringUtils.isEmpty(forbiddenMethods.getDeviateSection32()) ||
                !StringUtils.isEmpty(forbiddenMethods.getDeviateSection33()) ||
                !StringUtils.isEmpty(forbiddenMethods.getDeviateSection34()) ||
                !StringUtils.isEmpty(forbiddenMethods.getDeviateSection35()) ||
                !StringUtils.isEmpty(forbiddenMethods.getDeviateSection51())) {
            for (HarvestPermitApplicationSpeciesAmount spa : speciesAmounts) {
                assertMethodJustification(spa);
            }
        }
    }

    static void assertDamagesInformationValid(List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {

        for (HarvestPermitApplicationSpeciesAmount spa : speciesAmounts) {
            if (spa.getCausedDamageAmount() == null || spa.getCausedDamageAmount() < 0) {
                failValidation("Invalid damage amount for " + spa.getGameSpecies().getNameFinnish());
            }

            assertHasText(spa.getCausedDamageDescription(), "damage description", spa);
            assertHasText(spa.getEvictionMeasureDescription(), "eviction methods", spa);
            assertHasText(spa.getEvictionMeasureEffect(), "eviction measures effect", spa);
        }
    }

    static void assertPopulationInformationValid(List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        for (HarvestPermitApplicationSpeciesAmount spa : speciesAmounts) {
            assertHasText(spa.getPopulationAmount(), "population amount", spa);
            assertHasText(spa.getPopulationDescription(), "population description", spa);
        }
    }

    static void assertAttachmentsValid(final List<HarvestPermitApplicationAttachment> attachments) {
        if (attachments.stream().noneMatch(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)) {
            failValidation("Protected area attachment is missing");
        }
    }

    private static void failValidation(String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }

    private static void assertMethodJustification(HarvestPermitApplicationSpeciesAmount species) {
        if (species.isForbiddenMethodsUsed() == null) {
            failValidation("Forbidden methods used is null");
        }
        if (species.isForbiddenMethodsUsed()) {
            assertHasText(species.getForbiddenMethodJustification(), "forbidden method justification", species);
        }
    }

    private static void assertHasText(String text, String fieldName, HarvestPermitApplicationSpeciesAmount species) {
        if (StringUtils.isEmpty(text)) {
            failValidation("Required information missing:" + fieldName + " for " +
                    species.getGameSpecies().getNameFinnish());
        }
    }

    private static void assertSpeciesPeriodInformation(HarvestPermitApplicationSpeciesAmount speciesAmount,
                                                       boolean limitlessPermitAllowed) {
        final LocalDate beginDate = speciesAmount.getBeginDate();
        final LocalDate endDate = speciesAmount.getEndDate();

        if (beginDate == null || endDate == null) {
            failValidation("Date missing for species " + speciesAmount.getGameSpecies().getNameFinnish());
        }

        if (beginDate.getYear() != endDate.getYear()) {
            failValidation("Invalid time period for species " + speciesAmount.getGameSpecies().getNameFinnish());
        }

        if (speciesAmount.getValidityYears() == null) {
            failValidation("Validity years is missing");
        }

        final int validityYears = requireNonNull(speciesAmount.getValidityYears());

        if (!HarvestPermitApplicationSpeciesAmount.checkValidityYears(validityYears, limitlessPermitAllowed)) {
            if (!limitlessPermitAllowed) {
                failValidation("Validity years must be between 1 and 5");
            } else {
                failValidation("Cannot apply for limitless permit.");
            }
        }
    }
}
