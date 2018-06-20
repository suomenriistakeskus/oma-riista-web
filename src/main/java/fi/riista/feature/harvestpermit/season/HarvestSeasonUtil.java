package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;

import java.util.function.Predicate;

public class HarvestSeasonUtil {
    private static final int BEAR = GameSpecies.OFFICIAL_CODE_BEAR;
    private static final int ROE_DEER = GameSpecies.OFFICIAL_CODE_ROE_DEER;
    private static final int METSA_HANHI = GameSpecies.OFFICIAL_CODE_TAIGA_BEAN_GOOSE;
    private static final int HILLERI = GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT;
    private static final int WILD_BOAR = GameSpecies.OFFICIAL_CODE_WILD_BOAR;
    private static final int HALLI = GameSpecies.OFFICIAL_CODE_GREY_SEAL;

    public static boolean isInsideHuntingSeason(final LocalDate day,
                                                 final int gameSpeciesCode) {
        final int huntingYear = DateUtil.huntingYearContaining(day);

        return gameSpeciesCode == HALLI && isHalliSeason(huntingYear).test(day) ||
                gameSpeciesCode == BEAR && isBearSeason(huntingYear).test(day) ||
                gameSpeciesCode == ROE_DEER && isRoeDeerSeason(huntingYear).test(day) ||
                gameSpeciesCode == METSA_HANHI && isMetsahanhiSeason(huntingYear).test(day) ||
                gameSpeciesCode == HILLERI && huntingYear >= 2017 ||
                gameSpeciesCode == WILD_BOAR && huntingYear >= 2017;
    }

    // 1.8 - 31.12 and 16.4 - 31.7
    private static Predicate<LocalDate> isHalliSeason(final int y) {
        return day -> DateUtil.overlapsInclusive(ld(y, 8, 1), ld(y, 8, 1), day) ||
                DateUtil.overlapsInclusive(ld(y + 1, 4, 16), ld(y + 1, 7, 31), day);
    }

    // 20.8 - 31.10
    private static Predicate<LocalDate> isBearSeason(final int y) {
        return day -> DateUtil.overlapsInclusive(ld(y, 8, 20), ld(y, 10, 31), day);
    }

    // 1.9 - 31.1 and 16.5 - 15.6
    private static Predicate<LocalDate> isRoeDeerSeason(final int y) {
        return day -> DateUtil.overlapsInclusive(ld(y, 9, 1), ld(y + 1, 1, 31), day) ||
                DateUtil.overlapsInclusive(ld(y + 1, 5, 16), ld(y + 1, 5, 16), day);
    }

    // 1.10 - 30.11
    private static Predicate<LocalDate> isMetsahanhiSeason(final int y) {
        return day -> DateUtil.overlapsInclusive(ld(y, 10, 1), ld(y, 11, 30), day);
    }

    private static LocalDate ld(final int y, final int m, final int d) {
        return new LocalDate(y, m, d);
    }

    private HarvestSeasonUtil() {
        throw new AssertionError();
    }
}
