package fi.riista.feature.permit.application.lawsectionten;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.lawsectionten.period.LawSectionTenPermitApplicationSpeciesPeriodValidator;
import fi.riista.feature.permit.application.validation.ValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class LawSectionTenPermitApplicationValidator {

    public static void validateForSending(final HarvestPermitApplication application,
                                          final LawSectionTenPermitApplication lawSectionTenPermitApplication) {
        requireNonNull(application);
        requireNonNull(lawSectionTenPermitApplication);

        ValidationUtil.validateForSending(application);

        validateContent(application, lawSectionTenPermitApplication);
    }

    public static void validateForAmend(final HarvestPermitApplication application,
                                        final LawSectionTenPermitApplication lawSectionTenPermitApplication) {
        requireNonNull(application);
        requireNonNull(lawSectionTenPermitApplication);

        ValidationUtil.validateForAmend(application);

        validateContent(application, lawSectionTenPermitApplication);
    }

    public static void validateContent(final HarvestPermitApplication application,
                                       final LawSectionTenPermitApplication lawSectionTenPermitApplication) {
        requireNonNull(application);
        requireNonNull(lawSectionTenPermitApplication);

        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();
        final Set<Integer> speciesCodes = speciesAmounts.stream()
                .map(HarvestPermitApplicationSpeciesAmount::getGameSpecies)
                .map(GameSpecies::getOfficialCode)
                .collect(toSet());

        ValidationUtil.validateCommonHarvestPermitContent(application);

        assertLawSectionTenPermitSpecies(speciesCodes);
        assertPermitHolderInformationValid(application);
        assertPeriodInformationValid(speciesAmounts);
        assertPopulationInformationValid(speciesAmounts);
        assertAreaInformationValid(lawSectionTenPermitApplication);

        if (StringUtils.isBlank(lawSectionTenPermitApplication.getAreaDescription())) {
            assertAreaAttachmentsPresent(application.getAttachments());
        }
    }

    private static void assertLawSectionTenPermitSpecies(final Set<Integer> speciesCodes) {
        if (speciesCodes.size() > 1) {
            failValidation("Too many species");
        }
        speciesCodes.forEach(code -> {
            if (!GameSpecies.isLawSectionTenPermitSpecies(code)) {
                failValidation("Non applicable species: " + code);
            }
        });
    }

    static void assertPeriodInformationValid(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        speciesAmounts.forEach(speciesAmount -> {
            final LocalDate beginDate = speciesAmount.getBeginDate();
            final LocalDate endDate = speciesAmount.getEndDate();
            final int gameSpeciesCode = speciesAmount.getGameSpecies().getOfficialCode();

            try {
                LawSectionTenPermitApplicationSpeciesPeriodValidator.validatePeriod(gameSpeciesCode,
                        beginDate,
                        endDate);
            } catch (IllegalArgumentException e) {
                failValidation(e.getMessage());
            }

        });
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

    static void assertPopulationInformationValid(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        for (HarvestPermitApplicationSpeciesAmount spa : speciesAmounts) {
            assertHasText(spa.getPopulationAmount(), "population amount", spa);
            assertHasText(spa.getPopulationDescription(), "population description", spa);
        }
    }

    private static void assertAreaInformationValid(final LawSectionTenPermitApplication lawSectionTenPermitApplication) {
        if (lawSectionTenPermitApplication.getAreaSize() == null || lawSectionTenPermitApplication.getAreaSize() <= 0) {
            failValidation("Area size must be given as positive integer");
        }

        if (lawSectionTenPermitApplication.getGeoLocation() == null) {
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

    private LawSectionTenPermitApplicationValidator() {
        throw new UnsupportedOperationException();
    }
}
