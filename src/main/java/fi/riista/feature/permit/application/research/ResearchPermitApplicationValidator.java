package fi.riista.feature.permit.application.research;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReason;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class ResearchPermitApplicationValidator {


    public static void validateForSending(final HarvestPermitApplication application,
                                          final ResearchPermitApplication researchPermitApplication,
                                          final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(researchPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateForSending(application);

        validateContent(application, researchPermitApplication, derogationReasons);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final ResearchPermitApplication researchPermitApplication,
                                        final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(researchPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateForAmend(application);

        validateContent(application, researchPermitApplication, derogationReasons);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final ResearchPermitApplication researchPermitApplication,
                                       final List<DerogationPermitApplicationReason> derogationReasons) {
        requireNonNull(application);
        requireNonNull(researchPermitApplication);
        requireNonNull(derogationReasons);

        ValidationUtil.validateCommonHarvestPermitContent(application);

        assertPermitHolderInformationValid(application);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();
        assertPeriodInformationValid(speciesAmounts);
        assertAreaInformationValid(researchPermitApplication);
        assertJustificationValid(researchPermitApplication);

        if (StringUtils.isBlank(researchPermitApplication.getAreaDescription())) {
            assertAreaAttachmentsPresent(application.getAttachments());
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

    private static void assertAreaInformationValid(final ResearchPermitApplication researchPermitApplication) {
        if (researchPermitApplication.getGeoLocation() == null) {
            failValidation("Geolocation missing");
        }
    }

    private static void assertJustificationValid(final ResearchPermitApplication researchPermitApplication) {
        if (StringUtils.isBlank(researchPermitApplication.getJustification())) {
            failValidation("Invalid research justification");
        }
    }

    private static void assertPeriodInformationValid(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        final long distinctYearValues = speciesAmounts.stream()
                .map(HarvestPermitApplicationSpeciesAmount::getValidityYears)
                .mapToInt(years -> years != null ? years : -1)
                .distinct().count();

        if (distinctYearValues != 1) {
            failValidation("All validity years must match");
        }

        speciesAmounts.forEach(speciesAmount -> {
            final LocalDate beginDate = speciesAmount.getBeginDate();
            final LocalDate endDate = speciesAmount.getEndDate();

            if (beginDate == null || endDate == null) {
                failValidation("Date missing for species " + speciesAmount.getGameSpecies().getNameFinnish());
            }

            final LocalDate maxEnd = beginDate.plusYears(1).minusDays(1);
            if (endDate.isAfter(maxEnd)) {
                failValidation("Invalid time period for species " + speciesAmount.getGameSpecies().getNameFinnish());
            }

            if (speciesAmount.getValidityYears() == null) {
                failValidation("Validity years is missing");
            }

            if (speciesAmount.getValidityYears() < 1 || speciesAmount.getValidityYears() > 5) {
                failValidation("Validity years is invalid");
            }
        });
    }

    private static void failValidation(final String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }

    private ResearchPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
