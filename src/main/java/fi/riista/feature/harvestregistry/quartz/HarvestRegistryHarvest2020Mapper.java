package fi.riista.feature.harvestregistry.quartz;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.riista.feature.gamediary.GameSpecies;

import java.util.Map;
import java.util.Set;

import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.AGE;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.GENDER;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.GEOLOCATION;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.GREY_SEAL_2020_FIELDS;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.MUNICIPALITY;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryExtraAttributes.TIME;

public class HarvestRegistryHarvest2020Mapper implements HarvestRegistryTransformUtils {

    public static final HarvestRegistryHarvest2020Mapper INSTANCE =
            new HarvestRegistryHarvest2020Mapper();
    private static final Map<Integer, Set<HarvestRegistryExtraAttributes>> SPECIES_TO_ATTRIBUTES_MAPPING =
            ImmutableMap.<Integer, Set<HarvestRegistryExtraAttributes>>builder()
                    .put(GameSpecies.OFFICIAL_CODE_BEAN_GOOSE, ImmutableSet.of(MUNICIPALITY))

                    .put(GameSpecies.OFFICIAL_CODE_WIGEON, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_PINTAIL, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_GARGANEY, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_SHOVELER, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_POCHARD, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_TUFTED_DUCK, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_COMMON_EIDER, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_LONG_TAILED_DUCK, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_RED_BREASTED_MERGANSER, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_GOOSANDER, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_COOT, ImmutableSet.of(MUNICIPALITY))
                    // Peltopyy
                    .put(GameSpecies.OFFICIAL_CODE_PARTRIDGE, ImmutableSet.of(GENDER, GEOLOCATION, TIME))
                    .put(GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER, ImmutableSet.of(GENDER, GEOLOCATION, TIME))
                    .put(GameSpecies.OFFICIAL_CODE_BEAR, ImmutableSet.of(GENDER, GEOLOCATION, TIME))
                    // Hilleri
                    .put(GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT, ImmutableSet.of(MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_RINGED_SEAL, ImmutableSet.of(TIME, GENDER, GEOLOCATION))
                    // Halli
                    .put(GameSpecies.OFFICIAL_CODE_GREY_SEAL, ImmutableSet.of(TIME, GENDER, GEOLOCATION,
                            GREY_SEAL_2020_FIELDS))
                    .put(GameSpecies.OFFICIAL_CODE_WILD_BOAR, ImmutableSet.of(AGE, GENDER, MUNICIPALITY, GEOLOCATION))
                    // Kuusipeura
                    .put(GameSpecies.OFFICIAL_CODE_FALLOW_DEER, ImmutableSet.of(TIME, AGE, GENDER, GEOLOCATION))
                    // Saksanhirvi
                    .put(GameSpecies.OFFICIAL_CODE_RED_DEER, ImmutableSet.of(TIME, AGE, GENDER, GEOLOCATION))
                    // Japaninpeura
                    .put(GameSpecies.OFFICIAL_CODE_SIKA_DEER, ImmutableSet.of(TIME, AGE, GENDER, GEOLOCATION))
                    // Metsäkauris
                    .put(GameSpecies.OFFICIAL_CODE_ROE_DEER, ImmutableSet.of(AGE, GENDER, MUNICIPALITY))
                    .put(GameSpecies.OFFICIAL_CODE_MOOSE, ImmutableSet.of(TIME, AGE, GENDER, GEOLOCATION))
                    .put(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER, ImmutableSet.of(TIME, AGE, GENDER,
                            GEOLOCATION))
                    // Metsäpeura
                    .put(GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER, ImmutableSet.of(TIME, AGE, GENDER,
                            GEOLOCATION))
                    .build();

    @Override
    public Set<HarvestRegistryExtraAttributes> getExtraAttributes(final int speciesCode) {
        return SPECIES_TO_ATTRIBUTES_MAPPING.get(speciesCode);
    }

    private HarvestRegistryHarvest2020Mapper() {}
}
