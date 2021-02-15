package fi.riista.feature.permit.application.gamemanagement;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import fi.riista.util.F;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class GameManagementPermitApplicationValidator {


    public static void validateForSending(final HarvestPermitApplication application,
                                          final GameManagementPermitApplication gameManagementPermitApplication) {
        requireNonNull(application);
        requireNonNull(gameManagementPermitApplication);

        ValidationUtil.validateForSending(application);

        validateContent(application, gameManagementPermitApplication);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final GameManagementPermitApplication gameManagementPermitApplication) {
        requireNonNull(application);
        requireNonNull(gameManagementPermitApplication);

        ValidationUtil.validateForAmend(application);

        validateContent(application, gameManagementPermitApplication);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final GameManagementPermitApplication gameManagementPermitApplication) {
        requireNonNull(application);
        requireNonNull(gameManagementPermitApplication);

        ValidationUtil.validateCommonContent(application);

        assertPermitHolderInformationValid(application);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();
        assertSpeciesAmount(speciesAmounts);

        assertMethodsInformationValid(gameManagementPermitApplication, speciesAmounts);
        assertAreaInformationValid(gameManagementPermitApplication);

        if (StringUtils.isBlank(gameManagementPermitApplication.getAreaDescription())) {
            assertAreaAttachmentsPresent(application.getAttachments());
        }

        assertJustificationValid(gameManagementPermitApplication);
    }

    private static void assertSpeciesAmount(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        if (speciesAmounts.isEmpty() || speciesAmounts.size() > 1) {
            failValidation("Incorrect species amounts");
        }

        final HarvestPermitApplicationSpeciesAmount spa = speciesAmounts.get(0);
        if (F.allNull(spa.getSpecimenAmount(), spa.getEggAmount())) {
            failValidation("Either specimen amount or egg amount is needed");
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

    static void assertMethodsInformationValid(final GameManagementPermitApplication gameManagementPermitApplication,
                                              List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        final DerogationPermitApplicationForbiddenMethods forbiddenMethods =
                gameManagementPermitApplication.getForbiddenMethods();
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

    private static void assertMethodJustification(final HarvestPermitApplicationSpeciesAmount species) {
        if (species.isForbiddenMethodsUsed() == null) {
            failValidation("Forbidden methods used is null");
        }
        if (species.isForbiddenMethodsUsed()) {
            assertHasText(species.getForbiddenMethodJustification(), "forbidden method justification", species);
        }
    }

    private static void assertAreaInformationValid(final GameManagementPermitApplication gameManagementPermitApplication) {
        if (gameManagementPermitApplication.getGeoLocation() == null) {
            failValidation("Geolocation missing");
        }
    }

    private static void assertJustificationValid(final GameManagementPermitApplication gameManagementPermitApplication) {
        assertHasText(gameManagementPermitApplication.getJustification(), "justification", null);
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

    private GameManagementPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
