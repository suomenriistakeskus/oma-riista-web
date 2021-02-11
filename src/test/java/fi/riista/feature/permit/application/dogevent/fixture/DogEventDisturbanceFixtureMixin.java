package fi.riista.feature.permit.application.dogevent.fixture;

import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbance;
import fi.riista.feature.permit.application.dogevent.DogEventDisturbanceContact;

import java.util.List;
import java.util.function.Consumer;

import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TEST;
import static fi.riista.feature.permit.application.dogevent.DogEventType.DOG_TRAINING;

@FunctionalInterface
public interface DogEventDisturbanceFixtureMixin extends FixtureMixin {

    default void withDogEventDisturbanceFixtureWithNoEvents(final SpeciesMap speciesMap,
                                                            final Consumer<DogEventDisturbanceFixture> consumer) {

        withDogEventDisturbanceFixture(speciesMap, false, false, consumer);
    }

    default void withDogEventDisturbanceFixtureWithOnlyTrainingEvent(final SpeciesMap speciesMap,
                                                                     final Consumer<DogEventDisturbanceFixture> consumer) {

        withDogEventDisturbanceFixture(speciesMap, true,false, consumer);
    }

    default void withDogEventDisturbanceFixtureWithOnlyTestEvent(final SpeciesMap speciesMap,
                                                                 final Consumer<DogEventDisturbanceFixture> consumer) {

        withDogEventDisturbanceFixture(speciesMap,false, true, consumer);
    }

    /**
     * withDogEventDisturbanceFixture
     */

    default void withDogEventDisturbanceFixture(final SpeciesMap speciesMap,
                                                final Consumer<DogEventDisturbanceFixture> consumer) {

        withDogEventDisturbanceFixture(speciesMap,true, true, consumer);
    }

    default void withDogEventDisturbanceFixture(final SpeciesMap speciesMap,
                                                final int speciesCode,
                                                Consumer<DogEventDisturbanceFixture> consumer) {

        withDogEventDisturbanceFixture(speciesMap, speciesCode, true, true, consumer);
    }

    default void withDogEventDisturbanceFixture(final SpeciesMap speciesMap,
                                                final boolean hasTrainingEvent,
                                                final boolean hasTestEvent,
                                                final Consumer<DogEventDisturbanceFixture> consumer) {

        withDogEventDisturbanceFixture(speciesMap,
                                       GameSpecies.OFFICIAL_CODE_BEAR,
                                       hasTrainingEvent,
                                       hasTestEvent,
                                       consumer);
    }
    default void withDogEventDisturbanceFixture(final SpeciesMap speciesMap,
                                                final int speciesCode,
                                                final boolean hasTrainingEvent,
                                                final boolean hasTestEvent,
                                                final Consumer<DogEventDisturbanceFixture> consumer) {

        consumer.accept(new DogEventDisturbanceFixture(getEntitySupplier(),
                                                       speciesMap,
                                                       speciesCode,
                                                       hasTrainingEvent,
                                                       false,
                                                       hasTestEvent,
                                                       false,
                                                       5));
    }

    default void withSkippedEventFixture(final SpeciesMap speciesMap,
                                         final boolean isTrainingEventSkipped,
                                         final boolean isTestEventSkipped,
                                         final Consumer<DogEventDisturbanceFixture> consumer) {

        consumer.accept(new DogEventDisturbanceFixture(getEntitySupplier(),
                                                       speciesMap,
                                                       GameSpecies.OFFICIAL_CODE_BEAR,
                                                       !isTrainingEventSkipped,
                                                       isTrainingEventSkipped,
                                                       !isTestEventSkipped,
                                                       isTestEventSkipped,
                                                       5));
    }

    class DogEventDisturbanceFixture extends DogEventApplicationFixture {

        public final DogEventDisturbance trainingEvent;
        public final List<DogEventDisturbanceContact> trainingContacts;
        public final DogEventDisturbance testEvent;
        public final List<DogEventDisturbanceContact> testContacts;

        public DogEventDisturbanceFixture(final EntitySupplier es,
                                          final SpeciesMap speciesMap,
                                          final int speciesCode,
                                          final boolean hasTrainingEvent,
                                          final boolean trainingEventIsSkipped,
                                          final boolean hasTestEvent,
                                          final boolean testEventIsSkipped,
                                          final int numberOfContacts) {

            super(es, HarvestPermitCategory.DOG_DISTURBANCE);

            if (hasTrainingEvent) {
                trainingEvent = es.newDogEventDisturbance(application, DOG_TRAINING);
                trainingEvent.setGameSpecies(speciesMap.byOfficialCode(speciesCode));
                trainingContacts = es.newDogEventDisturbanceContacts(trainingEvent, numberOfContacts);
            } else if (trainingEventIsSkipped) {
                trainingEvent = es.newSkippedDogEventDisturbance(application, DOG_TRAINING);
                trainingContacts = null;
            } else {
                trainingEvent = null;
                trainingContacts = null;
            }

            if (hasTestEvent) {
                testEvent = es.newDogEventDisturbance(application, DOG_TEST);
                testEvent.setGameSpecies(speciesMap.byOfficialCode(speciesCode));
                testContacts = es.newDogEventDisturbanceContacts(testEvent, numberOfContacts);
            } else if (testEventIsSkipped) {
                testEvent = es.newSkippedDogEventDisturbance(application, DOG_TEST);
                testContacts = null;
            } else {
                testEvent = null;
                testContacts = null;
            }
        }

    }

}
