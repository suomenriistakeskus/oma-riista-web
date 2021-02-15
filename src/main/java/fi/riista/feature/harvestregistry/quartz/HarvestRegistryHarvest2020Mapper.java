package fi.riista.feature.harvestregistry.quartz;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import fi.riista.config.Constants;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.harvestregistry.ShooterAddress;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.feature.harvestregistry.quartz.HarvestRegistry2020ExtraAttributes.AGE;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistry2020ExtraAttributes.GENDER;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistry2020ExtraAttributes.GEOLOCATION;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistry2020ExtraAttributes.GREY_SEAL_FIELDS;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistry2020ExtraAttributes.MUNICIPALITY;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistry2020ExtraAttributes.TIME;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryItemMapper.END_TIMESTAMP_2019;
import static java.util.Optional.ofNullable;

public class HarvestRegistryHarvest2020Mapper {

    public static Stream<HarvestRegistryItem> transform(final Harvest harvest,
                                                        final Person actualShooter,
                                                        final Set<HarvestSpecimen> specimens,
                                                        final String rkaCode, final String rhyCode,
                                                        final Map<String, LocalisedString> municipalities) {
        Preconditions.checkArgument(
                !harvest.getPointOfTime().toLocalDate().isBefore(END_TIMESTAMP_2019),
                "Point of time is too old for this transformer.");

        final Set<HarvestRegistry2020ExtraAttributes> extraAttributes =
                SPECIES_TO_ATTRIBUTES_MAPPING.get(harvest.getSpecies().getOfficialCode());

        if (extraAttributes == null) {
            return Stream.empty();
        }

        // Amount that have no specimens specified for the harvest
        final int amountWithNoSpecimenInfo = harvest.getAmount() - specimens.size();

        final List<HarvestRegistryItem> items = Lists.newArrayListWithExpectedSize(specimens.size() + 1);

        specimens.forEach(specimen ->
                items.add(mapToItem(harvest, actualShooter,
                        rkaCode, rhyCode, extraAttributes, Optional.of(specimen), municipalities.get(harvest.getMunicipalityCode()))));

        if (amountWithNoSpecimenInfo > 0) {
            final HarvestRegistryItem itemWithNoSpecimenInfo = mapToItem(harvest, actualShooter,
                    rkaCode, rhyCode, extraAttributes, Optional.empty(), municipalities.get(harvest.getMunicipalityCode()));
            itemWithNoSpecimenInfo.setAmount(amountWithNoSpecimenInfo);
            items.add(itemWithNoSpecimenInfo);
        }

        return items.stream();
    }

    private static HarvestRegistryItem mapToItem(final Harvest harvest,
                                                 final Person actualShooter,
                                                 final String rkaCode,
                                                 final String rhyCode,
                                                 final Set<HarvestRegistry2020ExtraAttributes> extraAttributes,
                                                 final Optional<HarvestSpecimen> specimen,
                                                 final LocalisedString municipality) {
        final HarvestRegistryItem item = createFrom(harvest, actualShooter, rkaCode, rhyCode);

        if (specimen.isPresent()) {
            item.setAmount(1);
        }

        if (extraAttributes.contains(TIME)){
            item.setPointOfTime(harvest.getPointOfTime());
            item.setTimeOfDayValid(true);
        } else {
            item.setPointOfTime(harvest.getPointOfTime().toLocalDate().toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE));
            item.setTimeOfDayValid(false);
        }

        if (extraAttributes.contains(AGE)) {
            specimen.ifPresent(s -> item.setAge(s.getAge()));
        }

        if (extraAttributes.contains(GENDER)) {
            specimen.ifPresent(s -> item.setGender(s.getGender()));
        }

        if (extraAttributes.contains(MUNICIPALITY)) {
            ofNullable(municipality).ifPresent(
                    name-> {
                        item.setMunicipalityFinnish(name.getFinnish());
                        item.setMunicipalitySwedish(name.getSwedish());
                    }
            );
        }

        if (extraAttributes.contains(GEOLOCATION)) {
            item.setGeoLocation(harvest.getGeoLocation());
        }

        if (extraAttributes.contains(GREY_SEAL_FIELDS)) {
            // Shooter address is required only for grey seal and therefore not fetched in query for all harvests.
            // If future rules change to include several species, address should be fetched in the query
            // to avoid N+1 issues.
            item.setShooterAddress(ShooterAddress.createFrom(harvest.getActualShooter().getAddress()));

            ofNullable(harvest.getHarvestQuota())
                    .ifPresent(quota -> {
                        item.setHarvestAreaFinnish(quota.getHarvestArea().getNameFinnish());
                        item.setHarvestAreaSwedish(quota.getHarvestArea().getNameSwedish());
                    });
            ofNullable(harvest.getRhy())
                    .ifPresent(rhy -> {
                        item.setRkaFinnish(rhy.getParentOrganisation().getNameFinnish());
                        item.setRkaSwedish(rhy.getParentOrganisation().getNameSwedish());
                        item.setRhyFinnish(rhy.getNameFinnish());
                        item.setRhySwedish(rhy.getNameSwedish());
                    });

            specimen.ifPresent(s -> {
                item.setGender(s.getGender());
                item.setWeight(s.getWeight());
            });
        }

        return item;
    }

    private static HarvestRegistryItem createFrom(final Harvest harvest, final Person actualShooter,
                                                  final String rkaCode,
                                                  final String rhyCode) {
        final HarvestRegistryItem item = new HarvestRegistryItem();
        item.setHarvest(harvest);
        item.setShooterName(actualShooter.getFullName());
        item.setShooterHunterNumber(actualShooter.getHunterNumber());
        item.setSpecies(harvest.getSpecies());
        item.setAmount(harvest.getAmount());
        item.setMunicipalityCode(harvest.getMunicipalityCode());
        item.setRkaCode(rkaCode);
        item.setRhyCode(rhyCode);
        return item;
    }

    private static final Map<Integer, Set<HarvestRegistry2020ExtraAttributes>> SPECIES_TO_ATTRIBUTES_MAPPING =
            ImmutableMap.<Integer, Set<HarvestRegistry2020ExtraAttributes>>builder()
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
                            GREY_SEAL_FIELDS))
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
}
