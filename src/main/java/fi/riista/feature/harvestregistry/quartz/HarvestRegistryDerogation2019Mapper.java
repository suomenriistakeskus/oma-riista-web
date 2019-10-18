package fi.riista.feature.harvestregistry.quartz;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestregistry.HarvestRegistryItem;
import fi.riista.feature.organization.person.Person;

import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_AMERICAN_MINK;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BADGER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BLUE_FOX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BROWN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ERMINE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_POLECAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_HARBOUR_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOUNTAIN_HARE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MUFFLON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MUSKRAT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_NUTRIA;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PINE_MARTEN;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RABBIT;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON_DOG;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_FOX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RED_SQUIRREL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RINGED_SEAL;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_SIKA_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static fi.riista.feature.harvestregistry.quartz.HarvestRegistryItemMapper.END_TIMESTAMP_2019;

public final class HarvestRegistryDerogation2019Mapper {

    public static Stream<HarvestRegistryItem> transform(final Harvest harvest,
                                                        final Person actualShooter,
                                                        final Set<HarvestSpecimen> specimens,
                                                        final String rkaCode, final String rhyCode) {
        Preconditions.checkArgument(
                harvest.getPointOfTime().before(END_TIMESTAMP_2019),
                "Point of time is too late for this transformer.");

        // Amount that have no specimens specified for the harvest
        final int amountWithNoSpecimenInfo = harvest.getAmount() - specimens.size();

        if (SPECIES_WITH_SPECIMEN_INFO.contains(harvest.getSpecies().getOfficialCode())) {
            final Stream<HarvestRegistryItem> streamWithNoSpecimens = amountWithNoSpecimenInfo > 0
                    ? Stream.of(createItemWithNoSpecimenInfo(harvest, actualShooter, amountWithNoSpecimenInfo,
                    rkaCode, rhyCode))
                    : Stream.empty();

            return Streams.concat(streamAsItemsWithSpecimenInfo(harvest, actualShooter, specimens, rkaCode, rhyCode),
                    streamWithNoSpecimens);
        } else {
            return Stream.of(createItemWithNoSpecimenInfo(harvest, actualShooter, harvest.getAmount(), rkaCode,
                    rhyCode));
        }
    }

    // These species should have gender info when hunting happened with derogation permit.
    private static final Set<Integer> SPECIES_WITH_SPECIMEN_INFO = ImmutableSet.of(
            OFFICIAL_CODE_RABBIT,
            OFFICIAL_CODE_MOUNTAIN_HARE,
            OFFICIAL_CODE_BROWN_HARE,
            OFFICIAL_CODE_RED_SQUIRREL,
            OFFICIAL_CODE_EUROPEAN_BEAVER,
            OFFICIAL_CODE_CANADIAN_BEAVER,
            OFFICIAL_CODE_MUSKRAT,
            OFFICIAL_CODE_NUTRIA,
            OFFICIAL_CODE_WOLF,
            OFFICIAL_CODE_BLUE_FOX,
            OFFICIAL_CODE_RED_FOX,
            OFFICIAL_CODE_RACCOON_DOG,
            OFFICIAL_CODE_BEAR,
            OFFICIAL_CODE_RACCOON,
            OFFICIAL_CODE_BADGER,
            OFFICIAL_CODE_ERMINE,
            OFFICIAL_CODE_EUROPEAN_POLECAT,
            OFFICIAL_CODE_OTTER,
            OFFICIAL_CODE_PINE_MARTEN,
            OFFICIAL_CODE_AMERICAN_MINK,
            OFFICIAL_CODE_WOLVERINE,
            OFFICIAL_CODE_LYNX,
            OFFICIAL_CODE_RINGED_SEAL,
            OFFICIAL_CODE_HARBOUR_SEAL,
            OFFICIAL_CODE_GREY_SEAL,
            OFFICIAL_CODE_WILD_BOAR,
            OFFICIAL_CODE_FALLOW_DEER,
            OFFICIAL_CODE_RED_DEER,
            OFFICIAL_CODE_SIKA_DEER,
            OFFICIAL_CODE_ROE_DEER,
            OFFICIAL_CODE_MOOSE,
            OFFICIAL_CODE_WHITE_TAILED_DEER,
            OFFICIAL_CODE_WILD_FOREST_REINDEER,
            OFFICIAL_CODE_MUFFLON
    );

    private static HarvestRegistryItem createItemWithNoSpecimenInfo(final Harvest harvest,
                                                                    final Person actualShooter,
                                                                    final int amount,
                                                                    final String rkaCode,
                                                                    final String rhyCode) {
        final HarvestRegistryItem item = HarvestRegistryItemMapper.createFrom(harvest, actualShooter, rkaCode, rhyCode);
        item.setAmount(amount);
        return item;
    }

    private static Stream<HarvestRegistryItem> streamAsItemsWithSpecimenInfo(final Harvest harvest,
                                                                             final Person actualShooter,
                                                                             final Set<HarvestSpecimen> specimens,
                                                                             final String rkaCode,
                                                                             final String rhyCode) {
        return specimens.stream().map(specimen -> {
            final HarvestRegistryItem item = HarvestRegistryItemMapper.createFrom(harvest, actualShooter, rkaCode,
                    rhyCode);
            item.setAmount(1);
            item.setGender(specimen.getGender());
            return item;
        });
    }


}
