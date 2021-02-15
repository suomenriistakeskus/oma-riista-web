package fi.riista.feature.permit.application.importing;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class ImportingPermitApplicationValidator {

    public static void validateForSending(final HarvestPermitApplication application,
                                          final ImportingPermitApplication importingPermitApplication) {
        requireNonNull(application);
        requireNonNull(importingPermitApplication);

        ValidationUtil.validateForSending(application);

        validateContent(application, importingPermitApplication);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final ImportingPermitApplication importingPermitApplication) {
        requireNonNull(application);
        requireNonNull(importingPermitApplication);

        ValidationUtil.validateForAmend(application);

        validateContent(application, importingPermitApplication);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final ImportingPermitApplication importingPermitApplication) {
        requireNonNull(application);
        requireNonNull(importingPermitApplication);

        ValidationUtil.validateCommonHarvestPermitContent(application);

        assertPermitHolderInformationValid(application);

        assertSpeciesInformationValid(application);
        assertJustificationInformationValid(importingPermitApplication);

        assertAreaInformationValid(importingPermitApplication);

        if (StringUtils.isBlank(importingPermitApplication.getAreaDescription())) {
            assertAttachmentsValid(application.getAttachments());
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

    private static void assertSpeciesInformationValid(final HarvestPermitApplication application) {

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();

        if (speciesAmounts.isEmpty()) {
            failValidation("At least one species amount must be given");
        }

        speciesAmounts.forEach(speciesAmount -> {

            if (speciesAmount.getValidityYears() == null) {
                failValidation("Validity years must be present");
            }

            final LocalDate beginDate = speciesAmount.getBeginDate();
            final LocalDate endDate = speciesAmount.getEndDate();

            if (beginDate == null) {
                failValidation("Begin date must not be null");
            }

            if (endDate == null) {
                failValidation("End date must not be null");
            }

            if (endDate.isBefore(beginDate)) {
                failValidation("End date before begin date");
            }

            if (!beginDate.plusYears(1).isAfter(endDate)) {
                failValidation("Too long period");
            }

            final long distinctYearValues = speciesAmounts.stream()
                    .map(HarvestPermitApplicationSpeciesAmount::getValidityYears)
                    .mapToInt(years -> years != null ? years : -1)
                    .distinct().count();

            if (distinctYearValues != 1) {
                failValidation("All validity years must match");
            }
        });
    }

    private static void assertJustificationInformationValid(final ImportingPermitApplication importingPermitApplication) {

        assertHasText(importingPermitApplication.getDetails(), "details");
        assertHasText(importingPermitApplication.getPurpose(), "purpose");
        assertHasText(importingPermitApplication.getRelease(), "release");
        assertHasText(importingPermitApplication.getCountryOfOrigin(), "country of origin");
    }

    private static void assertAreaInformationValid(final ImportingPermitApplication importingPermitApplication) {

        if (importingPermitApplication.getGeoLocation() == null) {
            failValidation("Geolocation missing");
        }
    }

    private static void assertAttachmentsValid(final List<HarvestPermitApplicationAttachment> attachments) {
        if (attachments.stream().noneMatch(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)) {
            failValidation("Area attachment is missing");
        }
    }

    private static void assertHasText(@Nullable final String text,
                                      @Nonnull final String fieldName) {

        if (StringUtils.isBlank(text)) {
            failValidation("Required information missing: " + fieldName);
        }
    }

    private static void failValidation(final String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }

    private ImportingPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
