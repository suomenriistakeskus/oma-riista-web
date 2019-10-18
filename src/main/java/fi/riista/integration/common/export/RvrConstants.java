package fi.riista.integration.common.export;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static fi.riista.feature.gamediary.GameSpecies.LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;

public class RvrConstants {

    public static final List<String> RVR_PERMIT_TYPE_CODES = ImmutableList.of(
            "100", "190", // Mooselike
            "202", "207", // Bear
            "204", "209", // Wolf
            "211", // Wolverine (only damage based permits are granted)
            "203", "208");// Lynx


    public static final Set<Integer> RVR_HARVEST_SPECIES = ImmutableSet.<Integer>builder()
            .addAll(MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING)
            .addAll(LARGE_CARNIVORES)
            .add(OFFICIAL_CODE_ROE_DEER)
            .add(OFFICIAL_CODE_WILD_BOAR)
            .add(OFFICIAL_CODE_GREY_SEAL)
            .add(OFFICIAL_CODE_RINGED_SEAL).build();
}
