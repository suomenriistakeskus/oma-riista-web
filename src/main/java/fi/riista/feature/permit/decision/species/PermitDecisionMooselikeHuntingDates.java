package fi.riista.feature.permit.decision.species;

import org.joda.time.LocalDate;

import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;

public class PermitDecisionMooselikeHuntingDates {

    private static final int DAY_OF_WEEK_SATURDAY = 6;

    public static LocalDate getDefaultMooselikeBeginDate(final int speciesCode, final int huntingYear) {
        switch (speciesCode) {
            case OFFICIAL_CODE_MOOSE:
            case OFFICIAL_CODE_WHITE_TAILED_DEER:
            case OFFICIAL_CODE_FALLOW_DEER:
                return new LocalDate(huntingYear, 9, 1);
            case OFFICIAL_CODE_WILD_FOREST_REINDEER:
                return calculateWildForestReindeerStartDay(huntingYear);
            default:
                throw new IllegalStateException("Unexpected value: " + speciesCode);
        }
    }

    public static LocalDate getDefaultMooselikeEndDate(final int speciesCode, final int huntingYear) {
        switch (speciesCode) {
            case OFFICIAL_CODE_MOOSE:
                return new LocalDate(huntingYear + 1, 1, 15);
            case OFFICIAL_CODE_WILD_FOREST_REINDEER:
            case OFFICIAL_CODE_FALLOW_DEER:
                return new LocalDate(huntingYear + 1, 1, 31);
            case OFFICIAL_CODE_WHITE_TAILED_DEER:
                return new LocalDate(huntingYear + 1, 2, 15);
            default:
                throw new IllegalStateException("Unexpected value: " + speciesCode);
        }

    }

    private static LocalDate calculateWildForestReindeerStartDay(final int huntingYear) {
        // Last Saturday of September
        return IntStream.of(30, 29, 28, 27, 26, 25, 24)
                .mapToObj(i -> new LocalDate(huntingYear, 9, i))
                .filter(ld -> ld.getDayOfWeek() == DAY_OF_WEEK_SATURDAY)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Last Saturday of September should exist!"));
    }


    private PermitDecisionMooselikeHuntingDates() {
        throw new AssertionError();
    }
}
