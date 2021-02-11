package fi.riista.feature.permit.application.dogevent.fixture;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static fi.riista.util.Collect.mappingTo;

@FunctionalInterface
public interface SpeciesFixtureMixin extends FixtureMixin {


    default void withDogDisturbanceSpecies(final Consumer<SpeciesFixture> consumer) {

        final ImmutableSet<Integer> dogEventSpecies =
                ImmutableSet.<Integer>builder()
                        .add(GameSpecies.OFFICIAL_CODE_BEAR)
                        .add(GameSpecies.OFFICIAL_CODE_LYNX)
                        .add(GameSpecies.OFFICIAL_CODE_OTTER)
                        .add(GameSpecies.OFFICIAL_CODE_WOLF)
                        .add(GameSpecies.OFFICIAL_CODE_MOOSE) // For not accepted species
                        .build();

        consumer.accept(new SpeciesFixture(getEntitySupplier(), dogEventSpecies));
    }

    class SpeciesFixture implements SpeciesMap {


        public final Map<Integer, GameSpecies> speciesMap;

        public SpeciesFixture(final EntitySupplier es, final Set<Integer> initialisedSpecies) {
            speciesMap = initialisedSpecies.stream().collect(mappingTo(es::newGameSpecies));
        }

        @Override
        public GameSpecies byOfficialCode(final int officialCode) {
            return speciesMap.get(officialCode);
        }
    }
}
