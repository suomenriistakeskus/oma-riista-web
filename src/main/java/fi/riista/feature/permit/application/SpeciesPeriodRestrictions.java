package fi.riista.feature.permit.application;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;

public class SpeciesPeriodRestrictions {

    public static Integer getSpeciesPeriodRestriction(final int officialCode) {
        return SPECIES_CODE_PERIOD_RESTRICTION.get(officialCode);
    }


    public static boolean isRestricted(final int code) {
        return SPECIES_CODE_PERIOD_RESTRICTION.containsKey(code);
    }

    private static Map<Integer, Integer> SPECIES_CODE_PERIOD_RESTRICTION = ImmutableMap.of(
            OFFICIAL_CODE_LYNX, 21,
            OFFICIAL_CODE_WOLVERINE, 21,
            OFFICIAL_CODE_BEAR, 21,
            OFFICIAL_CODE_WOLF, 21,
            OFFICIAL_CODE_OTTER, 21
    );

    private SpeciesPeriodRestrictions() {

    }
}
