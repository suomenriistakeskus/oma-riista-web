package fi.riista.feature.permit.application.carnivore.species;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class CarnivorePermitSpecies {

    static final int MIN_SPECIES_AMOUNT = 1;
    static final int MAX_SPECIES_AMOUNT = 100_000;

    private static final Map<HarvestPermitCategory, Integer> SPECIES_MAPPING = ImmutableMap.of(
            HarvestPermitCategory.LARGE_CARNIVORE_BEAR, GameSpecies.OFFICIAL_CODE_BEAR,
            HarvestPermitCategory.LARGE_CARNIVORE_LYNX, GameSpecies.OFFICIAL_CODE_LYNX,
            HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO, GameSpecies.OFFICIAL_CODE_LYNX,
            HarvestPermitCategory.LARGE_CARNIVORE_WOLF, GameSpecies.OFFICIAL_CODE_WOLF,
            HarvestPermitCategory.LARGE_CARNIVORE_WOLF_PORONHOITO, GameSpecies.OFFICIAL_CODE_WOLF
    );

    public static final Range<LocalDate> getPeriod(@Nonnull final HarvestPermitApplication application) {
        requireNonNull(application);

        switch (application.getHarvestPermitCategory()) {
            case LARGE_CARNIVORE_BEAR:
                return Range.closed(new LocalDate(application.getApplicationYear(), 8, 20),
                        new LocalDate(application.getApplicationYear(), 10, 31));
            case LARGE_CARNIVORE_LYNX:
                return Range.closed(new LocalDate(application.getApplicationYear(), 12, 1),
                        endDateForLynx(application.getApplicationYear() + 1));
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
                return Range.closed(new LocalDate(application.getApplicationYear(), 10, 1),
                        endDateForLynx(application.getApplicationYear() + 1));
            case LARGE_CARNIVORE_WOLF:
                return Range.closed(new LocalDate(application.getApplicationYear(), 11, 1),
                        new LocalDate(application.getApplicationYear() + 1, 3, 31));
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                return Range.closed(new LocalDate(application.getApplicationYear(), 10, 1),
                        new LocalDate(application.getApplicationYear() + 1, 3, 31));
            default:
                throw new IllegalArgumentException("Invalid category");
        }
    }

    private static LocalDate endDateForLynx(final int year) {
        // Last day of February depends on leap years.
        return new LocalDate(year, 3, 1).minusDays(1);
    }


    public static void assertCategory(final HarvestPermitCategory category) {
        Preconditions.checkArgument(SPECIES_MAPPING.keySet().contains(category),
                "Not a carnivore permit category:" + category);
    }

    public static void assertSpecies(final HarvestPermitCategory category, final int officialCode) {
        Preconditions.checkArgument(SPECIES_MAPPING.get(category).equals(officialCode));
    }

    public static int getSpecies(final HarvestPermitCategory category) {
        assertCategory(category);
        return SPECIES_MAPPING.get(category);
    }

    public static boolean isCarnivoreSpecies(final int officialCode) {
        return SPECIES_MAPPING.values().contains(officialCode);
    }

    public static boolean isValidPermitAmount(final float permitAmount) {
        return permitAmount >= MIN_SPECIES_AMOUNT && permitAmount < MAX_SPECIES_AMOUNT;
    }

    public static void assertValidPeriod(@Nonnull final HarvestPermitApplication application,
                                         @Nonnull final Range<LocalDate> range) {
        requireNonNull(application);
        requireNonNull(range);
        Preconditions.checkArgument(getPeriod(application).encloses(range));
    }
}
