package fi.riista.feature.permit.application.carnivore;

import com.google.common.collect.Range;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.carnivore.species.CarnivorePermitSpecies;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class CarnivorePermitApplicationValidator {

    public static void validateForSending(final HarvestPermitApplication application,
                                          final CarnivorePermitApplication carnivoreApplication) {
        requireNonNull(application);
        requireNonNull(carnivoreApplication);

        ValidationUtil.validateForSending(application);

        validateContent(application, carnivoreApplication);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final CarnivorePermitApplication carnivoreApplication) {
        requireNonNull(application);
        requireNonNull(carnivoreApplication);

        ValidationUtil.validateForAmend(application);

        validateContent(application, carnivoreApplication);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final CarnivorePermitApplication carnivoreApplication) {
        requireNonNull(application);
        requireNonNull(carnivoreApplication);

        ValidationUtil.validateCommonHarvestPermitContent(application);

        assertPermitHolderInformationValid(application);

        final HarvestPermitApplicationSpeciesAmount speciesAmount = assertSpeciesInformationValid(application);
        assertJustificationInformationValid(carnivoreApplication, speciesAmount);

        assertAreaInformationValid(carnivoreApplication);

        if (StringUtils.isBlank(carnivoreApplication.getAreaDescription())) {
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

    private static HarvestPermitApplicationSpeciesAmount assertSpeciesInformationValid(final HarvestPermitApplication application) {

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();

        if (speciesAmounts.size() != 1) {
            failValidation("Exactly one species amount must be given");
        }

        final HarvestPermitApplicationSpeciesAmount speciesAmount = speciesAmounts.get(0);

        if (speciesAmount.getValidityYears() != null) {
            failValidation("Validity years must not be present");
        }

        final int gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();

        if (!CarnivorePermitSpecies.isCarnivoreSpecies(gameSpeciesCode)) {
            failValidation("Invalid species given: " + gameSpeciesCode);
        }

        if (speciesAmount.getBeginDate() == null) {
            failValidation("Begin date must not be null");
        }

        if (speciesAmount.getEndDate() == null) {
            failValidation("End date must not be null");
        }

        if (!CarnivorePermitSpecies.getPeriod(application).encloses(
                Range.closed(
                        speciesAmount.getBeginDate(),
                        speciesAmount.getEndDate()))) {
            failValidation("Invalid period");
        }

        return speciesAmount;
    }

    private static void assertJustificationInformationValid(final CarnivorePermitApplication carnivorePermitApplication,
                                                            final HarvestPermitApplicationSpeciesAmount speciesAmount) {

        assertHasText(speciesAmount.getPopulationAmount(), "population amount", speciesAmount);
        assertHasText(speciesAmount.getPopulationDescription(), "population description", speciesAmount);
        assertHasText(carnivorePermitApplication.getAdditionalJustificationInfo(), "additional justification info",
                null);
        assertHasText(carnivorePermitApplication.getAlternativeMeasures(), "alternative measures", null);
    }

    private static void assertAreaInformationValid(final CarnivorePermitApplication carnivoreApplication) {
        if (carnivoreApplication.getAreaSize() == null || carnivoreApplication.getAreaSize() <= 0) {
            failValidation("Area size must be given as positive integer");
        }

        if (carnivoreApplication.getGeoLocation() == null) {
            failValidation("Geolocation missing");
        }
    }

    private static void assertAttachmentsValid(final List<HarvestPermitApplicationAttachment> attachments) {
        if (attachments.stream().noneMatch(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)) {
            failValidation("Area attachment is missing");
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

    private CarnivorePermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
