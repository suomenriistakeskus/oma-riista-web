package fi.riista.feature.harvestregistry.quartz;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.harvestregistry.ShooterAddress;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryItemMapper.END_TIMESTAMP_2019;

@Service
public final class HarvestRegistryHarvest2019Mapper {

    private enum AttributeCategory {
        COMMON,
        COMMON_PLUS_AGE_AND_GENDER,
        COMMON_PLUS_GENDER,
        GREY_SEAL
    }

    public static Stream<HarvestRegistryItem> transform(final Harvest harvest,
                                                        final Person actualShooter,
                                                        final Set<HarvestSpecimen> specimens,
                                                        final String rkaCode, final String rhyCode) {
        Preconditions.checkArgument(
                harvest.getPointOfTime().toLocalDate().isBefore(END_TIMESTAMP_2019),
                "Point of time is too late for this transformer.");
        final AttributeCategory attributeCategory =
                SPECIES_TO_ATTRIBUTES_MAPPING.get(harvest.getSpecies().getOfficialCode());

        if (attributeCategory == null) {
            return Stream.empty();
        }

        // Amount that have no specimens specified for the harvest
        final int amountWithNoSpecimenInfo = harvest.getAmount() - specimens.size();

        final List<HarvestRegistryItem> items = Lists.newArrayListWithExpectedSize(specimens.size() + 1);

        specimens.forEach(specimen ->
                items.add(mapToItem(harvest, actualShooter,
                        rkaCode, rhyCode, attributeCategory, Optional.of(specimen))));

        if (amountWithNoSpecimenInfo > 0) {
            final HarvestRegistryItem itemWithNoSpecimenInfo = mapToItem(harvest, actualShooter,
                    rkaCode, rhyCode, attributeCategory, Optional.empty());
            itemWithNoSpecimenInfo.setAmount(amountWithNoSpecimenInfo);
            items.add(itemWithNoSpecimenInfo);
        }

        return items.stream();
    }

    private static HarvestRegistryItem mapToItem(final Harvest harvest,
                                                 final Person actualShooter,
                                                 final String rkaCode,
                                                 final String rhyCode,
                                                 final AttributeCategory attributeCategory,
                                                 final Optional<HarvestSpecimen> specimen) {
        final HarvestRegistryItem item = HarvestRegistryItemMapper.createFrom(harvest, actualShooter, rkaCode, rhyCode);

        if (specimen.isPresent()) {
            item.setAmount(1);
        }
        switch (attributeCategory) {
            case COMMON:
                break;
            case COMMON_PLUS_AGE_AND_GENDER:
                specimen.ifPresent(s -> {
                    item.setAge(s.getAge());
                    item.setGender(s.getGender());
                });
                break;
            case COMMON_PLUS_GENDER:
                specimen.ifPresent(s -> {
                    item.setGender(s.getGender());
                });
                break;
            case GREY_SEAL:

                // Shooter address is required only for grey seal and therefore not fetched in query for all harvests.
                // If future rules change to include several species, address should be fetched in the query
                // to avoid N+1 issues.
                item.setShooterAddress(ShooterAddress.createFrom(harvest.getActualShooter().getAddress()));

                Optional.ofNullable(harvest.getHarvestQuota())
                        .ifPresent(quota -> {
                            item.setHarvestAreaFinnish(quota.getHarvestArea().getNameFinnish());
                            item.setHarvestAreaSwedish(quota.getHarvestArea().getNameSwedish());
                        });
                Optional.ofNullable(harvest.getRhy())
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

                break;
        }

        return item;
    }


    private static final Map<Integer, AttributeCategory> SPECIES_TO_ATTRIBUTES_MAPPING =
            ImmutableMap.<Integer, AttributeCategory>builder()
                    .put(GameSpecies.OFFICIAL_CODE_BEAN_GOOSE, AttributeCategory.COMMON)
                    // Peltopyy
                    .put(GameSpecies.OFFICIAL_CODE_PARTRIDGE, AttributeCategory.COMMON_PLUS_GENDER)
                    .put(GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER, AttributeCategory.COMMON_PLUS_GENDER)
                    .put(GameSpecies.OFFICIAL_CODE_BEAR, AttributeCategory.COMMON_PLUS_GENDER)
                    // Hilleri
                    .put(GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT, AttributeCategory.COMMON)
                    .put(GameSpecies.OFFICIAL_CODE_RINGED_SEAL, AttributeCategory.COMMON_PLUS_GENDER)
                    // Halli
                    .put(GameSpecies.OFFICIAL_CODE_GREY_SEAL, AttributeCategory.GREY_SEAL)
                    .put(GameSpecies.OFFICIAL_CODE_WILD_BOAR, AttributeCategory.COMMON_PLUS_AGE_AND_GENDER)
                    // Kuusipeura
                    .put(GameSpecies.OFFICIAL_CODE_FALLOW_DEER, AttributeCategory.COMMON_PLUS_AGE_AND_GENDER)
                    // Saksanhirvi
                    .put(GameSpecies.OFFICIAL_CODE_RED_DEER, AttributeCategory.COMMON_PLUS_AGE_AND_GENDER)
                    // Japaninpeura
                    .put(GameSpecies.OFFICIAL_CODE_SIKA_DEER, AttributeCategory.COMMON_PLUS_AGE_AND_GENDER)
                    // Metsäkauris
                    .put(GameSpecies.OFFICIAL_CODE_ROE_DEER, AttributeCategory.COMMON_PLUS_AGE_AND_GENDER)
                    .put(GameSpecies.OFFICIAL_CODE_MOOSE, AttributeCategory.COMMON_PLUS_AGE_AND_GENDER)
                    .put(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER, AttributeCategory.COMMON_PLUS_AGE_AND_GENDER)
                    // Metsäpeura
                    .put(GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER, AttributeCategory.COMMON_PLUS_AGE_AND_GENDER)
                    .build();


}
