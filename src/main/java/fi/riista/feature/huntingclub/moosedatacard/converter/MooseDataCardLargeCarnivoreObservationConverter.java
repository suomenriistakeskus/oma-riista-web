package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardLargeCarnivoreObservationValidator;
import fi.riista.feature.organization.person.Person;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;

import javaslang.Tuple;
import javaslang.Tuple2;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MooseDataCardLargeCarnivoreObservationConverter
        extends MooseDataCardObservationConverter<MooseDataCardLargeCarnivoreObservation> {

    private final GameDiaryService diaryService;

    public MooseDataCardLargeCarnivoreObservationConverter(@Nonnull final GameDiaryService diaryService,
                                                           @Nonnull final Person contactPerson,
                                                           @Nonnull final GeoLocation defaultCoordinates) {

        super(new MooseDataCardLargeCarnivoreObservationValidator(defaultCoordinates), contactPerson);
        this.diaryService = Objects.requireNonNull(diaryService);
    }

    @Override
    public Stream<Observation> apply(@Nonnull final MooseDataCardLargeCarnivoreObservation input) {
        // GameSpecies lookup is implemented using cache and lazy evaluation (species data is
        // retrieved from database only once or not at all if not needed). Note that there is
        // no need to use Guava's LoadingCache.

        final ConcurrentMap<Integer, GameSpecies> speciesMap = new ConcurrentHashMap<>();

        final IntFunction<Supplier<GameSpecies>> speciesSupplierFn = speciesCode -> () -> speciesMap.computeIfAbsent(
                speciesCode, diaryService::getGameSpeciesByOfficialCode);

        final Supplier<GameSpecies> wolfSupplier = speciesSupplierFn.apply(GameSpecies.OFFICIAL_CODE_WOLF);
        final Supplier<GameSpecies> bearSupplier = speciesSupplierFn.apply(GameSpecies.OFFICIAL_CODE_BEAR);
        final Supplier<GameSpecies> lynxSupplier = speciesSupplierFn.apply(GameSpecies.OFFICIAL_CODE_LYNX);
        final Supplier<GameSpecies> wolverineSupplier = speciesSupplierFn.apply(GameSpecies.OFFICIAL_CODE_WOLVERINE);

        return validateToStream(input)
                .flatMap(validInput -> Stream.of(
                        optionalSpeciesAmountTuple(wolfSupplier, validInput.getNumberOfWolves()),
                        optionalSpeciesAmountTuple(bearSupplier, validInput.getNumberOfBears()),
                        optionalSpeciesAmountTuple(lynxSupplier, validInput.getNumberOfLynxes()),
                        optionalSpeciesAmountTuple(wolverineSupplier, validInput.getNumberOfWolverines()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(speciesAmountTuple -> speciesAmountTuple.transform((species, amount) -> {
                            final Observation observation = createObservation(validInput);
                            observation.setSpecies(species);
                            observation.setAmount(amount);
                            observation.setDescription(validInput.getAdditionalInfo());

                            observation.setObservationType(HasMooseDataCardEncoding
                                    .getEnumOrThrow(ObservationType.class, validInput.getObservationType(), invalid -> {
                                        return new IllegalStateException("Invalid observation type should not have passed validation: "
                                                + invalid.map(s -> '"' + s + '"').orElse("null"));
                                    }));

                            return observation;
                        })));
    }

    private static Optional<Tuple2<GameSpecies, Integer>> optionalSpeciesAmountTuple(
            final Supplier<GameSpecies> speciesSupplier, final Integer specimenAmount) {

        return Optional.ofNullable(specimenAmount)
                .filter(amount -> amount > 0)
                .map(amount -> Tuple.of(speciesSupplier.get(), amount));
    }

}
