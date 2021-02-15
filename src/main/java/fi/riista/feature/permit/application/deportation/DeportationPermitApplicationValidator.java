package fi.riista.feature.permit.application.deportation;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.SpeciesPeriodRestrictions;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReason;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DeportationPermitApplicationValidator {


    public static void validateForSending(final HarvestPermitApplication application,
                                          final DeportationPermitApplication deportationPermitApplication,
                                          final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(deportationPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateForSending(application);

        validateContent(application, deportationPermitApplication, derogationReasons);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final DeportationPermitApplication deportationPermitApplication,
                                        final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(deportationPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateForAmend(application);

        validateContent(application, deportationPermitApplication, derogationReasons);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final DeportationPermitApplication deportationPermitApplication,
                                       final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(deportationPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateCommonHarvestPermitContent(application);

        assertPermitHolderInformationValid(application);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();
        assertSpeciesAmounts(speciesAmounts);

        final HarvestPermitApplicationSpeciesAmount speciesAmount = speciesAmounts.get(0);
        assertSomeReasonSelectedForLawSection(derogationReasons, speciesAmount);
        assertDamagesInformationValid(speciesAmount);
        assertPopulationInformationValid(speciesAmount);
        assertPeriodInformationValid(speciesAmount);
        assertAreaInformationValid(deportationPermitApplication);

        if (StringUtils.isBlank(deportationPermitApplication.getAreaDescription())) {
            assertAreaAttachmentsPresent(application.getAttachments());
        }
    }

    private static void assertSpeciesAmounts(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        if (speciesAmounts == null || speciesAmounts.isEmpty() || speciesAmounts.size() > 1) {
            failValidation("Invalid species amount");
        }
    }

    private static void assertSomeReasonSelectedForLawSection(final List<DerogationPermitApplicationReason> derogationReasons,
                                                              final HarvestPermitApplicationSpeciesAmount speciesAmount) {

        final DerogationLawSection viableLawSection =
                DerogationLawSection.getSpeciesLawSection(speciesAmount.getGameSpecies().getOfficialCode());

        final List<DerogationLawSection> lawSectionsPresent = derogationReasons.stream()
                .map(DerogationPermitApplicationReason::getReasonType)
                .map(PermitDecisionDerogationReasonType::getLawSection)
                .distinct()
                .collect(toList());

        if (lawSectionsPresent.isEmpty() || lawSectionsPresent.size() > 1 ||
                !Objects.equals(viableLawSection, lawSectionsPresent.get(0))) {
            failValidation("Derogation reasons not valid");
        }
    }

    private static void assertPermitHolderInformationValid(final HarvestPermitApplication application) {
        final PermitHolder permitHolder = application.getPermitHolder();
        requireNonNull(permitHolder);

        if (permitHolder.getName() == null) {
            failValidation("Permit holder name missing");
        }

        if (permitHolder.getCode() == null && !permitHolder.getType().equals(PermitHolder.PermitHolderType.PERSON)) {
            failValidation("Code missing for permit holder");
        }
    }

    private static void assertAreaAttachmentsPresent(final List<HarvestPermitApplicationAttachment> attachments) {
        if (attachments.stream().noneMatch(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)) {
            failValidation("Area attachment is missing");
        }
    }

    private static void assertDamagesInformationValid(final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        final Integer damageAmount = speciesAmount.getCausedDamageAmount();
        if (damageAmount == null || damageAmount < 0) {
            failValidation("Invalid damage amount for " + speciesAmount.getGameSpecies().getNameFinnish());
        }

        assertHasText(speciesAmount.getCausedDamageDescription(), "damage description", speciesAmount);
        assertHasText(speciesAmount.getEvictionMeasureDescription(), "eviction methods", speciesAmount);
        assertHasText(speciesAmount.getEvictionMeasureEffect(), "eviction measures effect", speciesAmount);
    }

    private static void assertPopulationInformationValid(final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        assertHasText(speciesAmount.getPopulationAmount(), "population amount", speciesAmount);
        assertHasText(speciesAmount.getPopulationDescription(), "population description", speciesAmount);
    }

    private static void assertPeriodInformationValid(final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        final LocalDate beginDate = speciesAmount.getBeginDate();
        final LocalDate endDate = speciesAmount.getEndDate();
        final int speciesCode = speciesAmount.getGameSpecies().getOfficialCode();

        if (beginDate == null || endDate == null) {
            failValidation("Date missing for species " + speciesAmount.getGameSpecies().getNameFinnish());
        }

        final LocalDate maxEnd = SpeciesPeriodRestrictions.isRestricted(speciesCode)
                ? beginDate.plusDays(SpeciesPeriodRestrictions.getSpeciesPeriodRestriction(speciesCode))
                : beginDate.plusYears(1);

        if (!maxEnd.isAfter(endDate)) {
            failValidation("Invalid time period for species " + speciesAmount.getGameSpecies().getNameFinnish());
        }

        final Integer validityYears = speciesAmount.getValidityYears();
        if (validityYears == null) {
            failValidation("Validity years is missing");
        }

        if (validityYears > 1) {
            failValidation("Validity years is invalid");
        }
    }

    private static void assertAreaInformationValid(final DeportationPermitApplication deportationPermitApplication) {
        if (deportationPermitApplication.getGeoLocation() == null) {
            failValidation("Geolocation missing");
        }
    }

    private static void assertHasText(@Nullable final String text,
                                      @Nonnull final String fieldName,
                                      @Nullable final HarvestPermitApplicationSpeciesAmount speciesAmount) {

        if (StringUtils.isEmpty(text)) {
            final String qualifier = speciesAmount != null
                    ? format("%s for %s", fieldName, speciesAmount.getGameSpecies().getNameFinnish())
                    : fieldName;

            failValidation("Required information missing: " + qualifier);
        }
    }

    private static void failValidation(final String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }

    private DeportationPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
