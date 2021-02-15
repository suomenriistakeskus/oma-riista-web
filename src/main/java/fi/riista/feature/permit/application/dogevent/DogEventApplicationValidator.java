package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class DogEventApplicationValidator {

    /**
     *  DogEventUnleash
     */

    public static void validateContent(final HarvestPermitApplication application,
                                       final DogEventApplication dogEventApplication,
                                       final List<DogEventUnleash> unleashEvents) {

        requireNonNull(application);
        requireNonNull(dogEventApplication);
        requireNonNull(unleashEvents);

        validateCommonContent(application, dogEventApplication);

        if (unleashEvents.isEmpty()) {
            failValidation("Application has no events.");
        }

        // Actual content of unleash events is validated by DogEventUnleash entity.
    }

    public static void validateForSending(final HarvestPermitApplication application,
                                          final DogEventApplication dogEventApplication,
                                          final List<DogEventUnleash> unleashEvents) {

        requireNonNull(application);
        requireNonNull(dogEventApplication);
        requireNonNull(unleashEvents);

        ValidationUtil.validateForSending(application);
        validateContent(application, dogEventApplication, unleashEvents);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                            final DogEventApplication dogEventApplication,
                                            final List<DogEventUnleash> unleashEvents) {

        requireNonNull(application);
        requireNonNull(dogEventApplication);
        requireNonNull(unleashEvents);

        ValidationUtil.validateForAmend(application);
        validateContent(application, dogEventApplication, unleashEvents);
    }

    /**
     * DogEventDisturbance
     */

    public static void validateContent(final HarvestPermitApplication application,
                                       final DogEventApplication dogEventApplication,
                                       final DogEventDisturbance trainingEvent,
                                       final List<DogEventDisturbanceContact> trainingContacts,
                                       final DogEventDisturbance testEvent,
                                       final List<DogEventDisturbanceContact> testContacts) {

        requireNonNull(application);
        requireNonNull(dogEventApplication);
        requireNonNull(trainingEvent);
        requireNonNull(trainingContacts);
        requireNonNull(testEvent);
        requireNonNull(testContacts);

        validateCommonContent(application, dogEventApplication);

        if (trainingEvent.isSkipped() && testEvent.isSkipped()) {
            failValidation("Both events cannot be skipped.");
        }

        if (!trainingEvent.isSkipped() && trainingContacts.isEmpty()) {
            failValidation("Training event has no contacts.");
        }

        if (!testEvent.isSkipped() && testContacts.isEmpty()) {
            failValidation("Test event has no contacts.");
        }

        DogEventDisturbanceValidator.validateContent(trainingEvent);
        DogEventDisturbanceValidator.validateContent(testEvent);

        // Content of contacts is validated by DogEventDisturbanceContact entity.
    }

    public static void validateForSending(final HarvestPermitApplication application,
                                          final DogEventApplication dogEventApplication,
                                          final DogEventDisturbance trainingEvent,
                                          final List<DogEventDisturbanceContact> trainingContacts,
                                          final DogEventDisturbance testEvent,
                                          final List<DogEventDisturbanceContact> testContacts) {

        requireNonNull(application);
        requireNonNull(dogEventApplication);
        requireNonNull(trainingEvent);
        requireNonNull(trainingContacts);
        requireNonNull(testEvent);
        requireNonNull(testContacts);

        ValidationUtil.validateForSending(application);
        validateContent(application, dogEventApplication, trainingEvent, trainingContacts, testEvent, testContacts);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final DogEventApplication dogEventApplication,
                                        final DogEventDisturbance trainingEvent,
                                        final List<DogEventDisturbanceContact> trainingContacts,
                                        final DogEventDisturbance testEvent,
                                        final List<DogEventDisturbanceContact> testContacts) {

        requireNonNull(application);
        requireNonNull(dogEventApplication);
        requireNonNull(trainingEvent);
        requireNonNull(trainingContacts);
        requireNonNull(testEvent);
        requireNonNull(testContacts);

        ValidationUtil.validateForAmend(application);
        validateContent(application, dogEventApplication, trainingEvent, trainingContacts, testEvent, testContacts);
    }

    /**
     * Helpers
     */

    private static void validateCommonContent(@Nonnull final HarvestPermitApplication application,
                                              @Nonnull final DogEventApplication dogEventApplication) {

        requireNonNull(application);
        requireNonNull(dogEventApplication);

        ValidationUtil.validateCommonContent(application);
        assertPermitHolderInformationValid(application.getPermitHolder());

        assertAreaInformationValid(dogEventApplication);

        if (StringUtils.isBlank(dogEventApplication.getAreaDescription())) {
            assertAreaAttachmentsPresent(application.getAttachments());
        }
    }

    private static void assertPermitHolderInformationValid(@Nonnull final  PermitHolder permitHolder) {

        requireNonNull(permitHolder);

        if (StringUtils.isBlank(permitHolder.getName())) {
            failValidation("Permit holder name missing");
        }

        if (StringUtils.isBlank(permitHolder.getCode()) && !permitHolder.getType().equals(PermitHolder.PermitHolderType.PERSON)) {
            failValidation("Code missing for permit holder");
        }
    }

    private static void assertAreaAttachmentsPresent(final List<HarvestPermitApplicationAttachment> attachments) {
        if (attachments.stream().noneMatch(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)) {
            failValidation("Area attachment is missing");
        }
    }

    private static void assertAreaInformationValid(final DogEventApplication dogEventApplication) {

        if (dogEventApplication.getAreaSize() == null || dogEventApplication.getAreaSize() <= 0) {
            failValidation("Area size must be given as positive integer");
        }

        if (dogEventApplication.getGeoLocation() == null) {
            failValidation("Geolocation missing");
        }
    }

    private static void failValidation(final String errorMessage) {
        throw new IllegalStateException(errorMessage);
    }
}
