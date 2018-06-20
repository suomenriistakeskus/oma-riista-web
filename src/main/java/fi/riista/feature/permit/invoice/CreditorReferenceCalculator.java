package fi.riista.feature.permit.invoice;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.validation.FinnishCreditorReferenceValidator;

import static com.google.common.base.Preconditions.checkArgument;

public final class CreditorReferenceCalculator {

    private static final ImmutableMap<Integer, Integer> OFFICIAL_SPECIES_CODE_TO_CREDITOR_REFERENCE_PART = ImmutableMap
            .<Integer, Integer> builder()
            .put(GameSpecies.OFFICIAL_CODE_MOOSE, 1)
            .put(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER, 2)
            .put(GameSpecies.OFFICIAL_CODE_ROE_DEER, 3)
            .put(GameSpecies.OFFICIAL_CODE_FALLOW_DEER, 4)
            .put(GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER, 5)
            .put(GameSpecies.OFFICIAL_CODE_RED_DEER, 6)
            .put(GameSpecies.OFFICIAL_CODE_SIKA_DEER, 7)
            .put(GameSpecies.OFFICIAL_CODE_MUFFLON, 9)
            .build();

    public static CreditorReference computeReferenceForPermitDecisionProcessingInvoice(final int huntingYear,
                                                                                       final int harvestPermitApplicationNumber) {
        checkArguments(huntingYear, harvestPermitApplicationNumber);

        final int year = huntingYear % 100;
        final String base = String.format("%d%08d", year, harvestPermitApplicationNumber);
        final char checksum = FinnishCreditorReferenceValidator.calculateChecksum(base);

        return CreditorReference.fromNullable(base + checksum);
    }

    public static CreditorReference computeReferenceForPermitHarvestInvoice(final int huntingYear,
                                                                            final int harvestPermitApplicationNumber,
                                                                            final int gameSpeciesCode) {
        checkArguments(huntingYear, harvestPermitApplicationNumber);

        final int year = huntingYear % 100;
        final int speciesPart = getCreditorReferencePart(gameSpeciesCode);
        final String base = String.format("%d%08d%03d", year, harvestPermitApplicationNumber, speciesPart);
        final char checksum = FinnishCreditorReferenceValidator.calculateChecksum(base);

        return CreditorReference.fromNullable(base + checksum);
    }

    private static void checkArguments(final int huntingYear, final int harvestPermitApplicationNumber) {
        checkArgument(huntingYear > 2000 && huntingYear < 2100);
        checkArgument(harvestPermitApplicationNumber >= 0 && harvestPermitApplicationNumber < 100_000_000);
    }

    private static int getCreditorReferencePart(final int gameSpeciesCode) {
        final Integer creditorReferencePart = OFFICIAL_SPECIES_CODE_TO_CREDITOR_REFERENCE_PART.get(gameSpeciesCode);

        if (creditorReferencePart == null) {
            throw new IllegalArgumentException(String.format(
                    "No creditor reference part defined for species with code: %d", gameSpeciesCode));
        }

        return creditorReferencePart;
    }

    private CreditorReferenceCalculator() {
        throw new AssertionError();
    }
}
