package fi.riista.integration.common.export;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static fi.riista.feature.gamediary.GameSpecies.LARGE_CARNIVORES;
import static fi.riista.feature.gamediary.GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.permit.PermitTypeCode.BEAR_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.BEAR_KANNAHOIDOLLINEN;
import static fi.riista.feature.permit.PermitTypeCode.LYNX_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.LYNX_KANNANHOIDOLLINEN;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE_AMENDMENT;
import static fi.riista.feature.permit.PermitTypeCode.WOLF_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.WOLF_KANNANHOIDOLLINEN;
import static fi.riista.feature.permit.PermitTypeCode.WOLVERINE_DAMAGE_BASED;

public class RvrConstants {

    public static final List<String> RVR_PERMIT_TYPE_CODES = ImmutableList.of(
            MOOSELIKE, MOOSELIKE_AMENDMENT,
            BEAR_DAMAGE_BASED, BEAR_KANNAHOIDOLLINEN,
            WOLF_DAMAGE_BASED, WOLF_KANNANHOIDOLLINEN,
            WOLVERINE_DAMAGE_BASED, // Wolverine (only damage based permits are granted)
            LYNX_DAMAGE_BASED, LYNX_KANNANHOIDOLLINEN,
            MAMMAL_DAMAGE_BASED);


    public static final Set<Integer> RVR_SPECIES = ImmutableSet.<Integer>builder()
            .addAll(MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING)
            .addAll(LARGE_CARNIVORES)
            .add(OFFICIAL_CODE_ROE_DEER)
            .add(OFFICIAL_CODE_WILD_BOAR)
            .add(OFFICIAL_CODE_GREY_SEAL)
            .add(OFFICIAL_CODE_RINGED_SEAL)
            .add(OFFICIAL_CODE_OTTER).build();
}
