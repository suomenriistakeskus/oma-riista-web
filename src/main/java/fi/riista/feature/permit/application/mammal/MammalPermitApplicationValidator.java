package fi.riista.feature.permit.application.mammal;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReason;
import fi.riista.feature.permit.application.mammal.period.MammalPermitApplicationPeriodRestrictions;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import fi.riista.feature.permit.decision.derogation.DerogationLawSection;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class MammalPermitApplicationValidator {


    public static void validateForSending(final HarvestPermitApplication application,
                                          final MammalPermitApplication mammalPermitApplication,
                                          final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(mammalPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateForSending(application);

        validateContent(application, mammalPermitApplication, derogationReasons);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final MammalPermitApplication mammalPermitApplication,
                                        final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(mammalPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateForAmend(application);

        validateContent(application, mammalPermitApplication, derogationReasons);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final MammalPermitApplication mammalPermitApplication,
                                       final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(mammalPermitApplication);
        requireNonNull(derogationReasons);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();
        final Set<Integer> speciesCodes = speciesAmounts.stream()
                .map(HarvestPermitApplicationSpeciesAmount::getGameSpecies)
                .map(GameSpecies::getOfficialCode)
                .collect(toSet());
        ValidationUtil.validateCommonContent(application);

        ValidationUtil.validateSpeciesAmounts(application);
        validateSpeciesRestrictions(speciesCodes);
        ValidationUtil.validateCommonContent(application);

        assertPermitHolderInformationValid(application);
        assertSomeReasonSelectedForAllLawSections(derogationReasons, speciesAmounts);
        assertPeriodInformationValid(mammalPermitApplication, speciesAmounts);
        assertMethodsInformationValid(mammalPermitApplication, speciesAmounts);
        assertDamagesInformationValid(speciesAmounts);
        assertPopulationInformationValid(speciesAmounts);
        assertAreaInformationValid(mammalPermitApplication);

        if (StringUtils.isBlank(mammalPermitApplication.getAreaDescription())) {
            assertAreaAttachmentsPresent(application.getAttachments());
        }
    }

    private static void validateSpeciesRestrictions(final Set<Integer> speciesCodes) {

        speciesCodes.forEach(MammalPermitApplicationValidator::assertMammalSpecies);

        if (speciesCodes.size() > 1) {
            speciesCodes.forEach(MammalPermitApplicationValidator::assertNotCarnivoreOrOtter);
        }

    }

    private static void assertNotCarnivoreOrOtter(final Integer code) {
        if (GameSpecies.isLargeCarnivore(code) || code == OFFICIAL_CODE_OTTER) {
            failValidation("Found species which needs to be applied for separately: " + code);
        }
    }

    private static void assertMammalSpecies(final Integer code) {
        if (!GameSpecies.isMammalSpecies(code)) {
            failValidation("Non-mammal species: " + code);
        }
    }


    static void assertPeriodInformationValid(final MammalPermitApplication mammalPermitApplication,
                                             final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {

        final long distinctYearValues = speciesAmounts.stream()
                .map(HarvestPermitApplicationSpeciesAmount::getValidityYears)
                .mapToInt(years -> years != null ? years : -1)
                .distinct().count();

        if (distinctYearValues != 1) {
            failValidation("All validity years must match");
        }

        speciesAmounts.forEach(MammalPermitApplicationValidator::assertPeriodInformationValid);
    }

    static void assertSomeReasonSelectedForAllLawSections(final List<DerogationPermitApplicationReason> derogationReasons,
                                                          final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {

        final Set<DerogationLawSection> viableLawSections =
                speciesAmounts.stream()
                        .map(HarvestPermitApplicationSpeciesAmount::getGameSpecies)
                        .map(GameSpecies::getOfficialCode)
                        .map(DerogationLawSection::getSpeciesLawSection)
                        .collect(toSet());

        final Set<DerogationLawSection> lawSectionsPresent = derogationReasons.stream()
                .map(DerogationPermitApplicationReason::getReasonType)
                .map(PermitDecisionDerogationReasonType::getLawSection)
                .collect(toSet());

        if (!Objects.equals(viableLawSections, lawSectionsPresent)) {
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

    static void assertMethodsInformationValid(final MammalPermitApplication mammalPermitApplication,
                                              List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        final DerogationPermitApplicationForbiddenMethods forbiddenMethods =
                mammalPermitApplication.getForbiddenMethods();
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

    static void assertDamagesInformationValid(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {

        for (HarvestPermitApplicationSpeciesAmount spa : speciesAmounts) {
            if (spa.getCausedDamageAmount() == null || spa.getCausedDamageAmount() < 0) {
                failValidation("Invalid damage amount for " + spa.getGameSpecies().getNameFinnish());
            }

            assertHasText(spa.getCausedDamageDescription(), "damage description", spa);
            assertHasText(spa.getEvictionMeasureDescription(), "eviction methods", spa);
            assertHasText(spa.getEvictionMeasureEffect(), "eviction measures effect", spa);
        }
    }

    static void assertPopulationInformationValid(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        for (HarvestPermitApplicationSpeciesAmount spa : speciesAmounts) {
            assertHasText(spa.getPopulationAmount(), "population amount", spa);
            assertHasText(spa.getPopulationDescription(), "population description", spa);
        }
    }

    private static void assertMethodJustification(final HarvestPermitApplicationSpeciesAmount species) {
        if (species.isForbiddenMethodsUsed() == null) {
            failValidation("Forbidden methods used is null");
        }
        if (species.isForbiddenMethodsUsed()) {
            assertHasText(species.getForbiddenMethodJustification(), "forbidden method justification", species);
        }
    }

    private static void assertPeriodInformationValid(final HarvestPermitApplicationSpeciesAmount speciesAmount) {
        final LocalDate beginDate = speciesAmount.getBeginDate();
        final LocalDate endDate = speciesAmount.getEndDate();
        final int speciesCode = speciesAmount.getGameSpecies().getOfficialCode();

        if (beginDate == null || endDate == null) {
            failValidation("Date missing for species " + speciesAmount.getGameSpecies().getNameFinnish());
        }

        final LocalDate maxEnd = MammalPermitApplicationPeriodRestrictions.isRestricted(speciesCode)
                ? beginDate.plusDays(MammalPermitApplicationPeriodRestrictions.getSpeciesPeriodRestriction(speciesCode))
                : beginDate.plusYears(1).minusDays(1);

        if (!maxEnd.isAfter(endDate)) {
            failValidation("Invalid time period for species " + speciesAmount.getGameSpecies().getNameFinnish());
        }

        if (speciesAmount.getValidityYears() == null) {
            failValidation("Validity years is missing");
        }

        if (speciesAmount.getValidityYears() < 1 || speciesAmount.getValidityYears() > 5) {
            failValidation("Validity years is invalid");
        }

        if (MammalPermitApplicationPeriodRestrictions.isRestricted(speciesCode) && speciesAmount.getValidityYears() > 1) {
            failValidation("Only one year long applications can be applied for species: " + speciesCode);
        }
    }

    private static void assertAreaInformationValid(final MammalPermitApplication mammalPermitApplication) {
        if (mammalPermitApplication.getAreaSize() == null || mammalPermitApplication.getAreaSize() <= 0) {
            failValidation("Area size must be given as positive integer");
        }

        if (mammalPermitApplication.getGeoLocation() == null) {
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

    private MammalPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
