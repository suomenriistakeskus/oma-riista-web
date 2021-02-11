package fi.riista.feature.permit.application.nestremoval;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
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
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class NestRemovalPermitApplicationValidator {


    public static void validateForSending(final HarvestPermitApplication application,
                                          final NestRemovalPermitApplication nestRemovalPermitApplication,
                                          final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(nestRemovalPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateForSending(application);

        validateContent(application, nestRemovalPermitApplication, derogationReasons);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final NestRemovalPermitApplication nestRemovalPermitApplication,
                                        final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(nestRemovalPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateForAmend(application);

        validateContent(application, nestRemovalPermitApplication, derogationReasons);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final NestRemovalPermitApplication nestRemovalPermitApplication,
                                       final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(nestRemovalPermitApplication);
        requireNonNull(derogationReasons);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();
        final Set<Integer> speciesCodes = speciesAmounts.stream()
                .map(HarvestPermitApplicationSpeciesAmount::getGameSpecies)
                .map(GameSpecies::getOfficialCode)
                .collect(toSet());

        ValidationUtil.validateCommonHarvestPermitContent(application);

        assertNestRemovalPermitSpecies(speciesCodes);
        assertPermitHolderInformationValid(application);
        assertSomeReasonSelectedForAllLawSections(derogationReasons, speciesAmounts);
        assertPeriodInformationValid(speciesAmounts);
        assertDamagesInformationValid(speciesAmounts);
        assertPopulationInformationValid(speciesAmounts);
        assertAreaInformationValid(nestRemovalPermitApplication);

        if (StringUtils.isBlank(nestRemovalPermitApplication.getAreaDescription())) {
            assertAreaAttachmentsPresent(application.getAttachments());
        }
    }

    private static void assertNestRemovalPermitSpecies(final Set<Integer> speciesCodes) {
        speciesCodes.forEach(code -> {
            if (!GameSpecies.isNestRemovalPermitSpecies(code)) {
                failValidation("Non applicable species: " + code);
            }
        });
    }
    
    static void assertPeriodInformationValid(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        speciesAmounts.forEach(speciesAmount -> {
            final LocalDate beginDate = speciesAmount.getBeginDate();
            final LocalDate endDate = speciesAmount.getEndDate();

            if (beginDate == null || endDate == null) {
                failValidation("Date missing for species " + speciesAmount.getGameSpecies().getNameFinnish());
            }
        });
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

    private static void assertAreaInformationValid(final NestRemovalPermitApplication nestRemovalPermitApplication) {
        if (nestRemovalPermitApplication.getAreaSize() == null || nestRemovalPermitApplication.getAreaSize() <= 0) {
            failValidation("Area size must be given as positive integer");
        }

        if (nestRemovalPermitApplication.getGeoLocation() == null) {
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

    private NestRemovalPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
