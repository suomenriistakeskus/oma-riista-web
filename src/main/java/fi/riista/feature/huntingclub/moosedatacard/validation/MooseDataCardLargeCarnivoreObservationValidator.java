package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.largeCarnivoreMissingObservationType;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationTypeOfLargeCarnivoreContainsIllegalCharacters;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.sumOfSpecimenAmountsOfLargeCarnivoreObservationIsNotGreaterThanZero;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;

import javaslang.control.Either;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MooseDataCardLargeCarnivoreObservationValidator
        extends MooseDataCardObservationValidator<MooseDataCardLargeCarnivoreObservation> {

    public MooseDataCardLargeCarnivoreObservationValidator(@Nonnull final GeoLocation defaultCoordinates) {
        super(defaultCoordinates);
    }

    @Override
    public Either<String, MooseDataCardLargeCarnivoreObservation> validate(
            @Nonnull final MooseDataCardLargeCarnivoreObservation input) {

        return resolveDate(input).flatMap(date -> HasMooseDataCardEncoding
                .enumOf(ObservationType.class, input.getObservationType())
                .mapLeft(invalidOpt -> invalidOpt
                        .map(invalid -> observationTypeOfLargeCarnivoreContainsIllegalCharacters(input))
                        .orElseGet(() -> largeCarnivoreMissingObservationType(input)))
                .flatMap(convertedObservationType -> {

                    // Total amount is expected to be greater than zero.

                    final Integer totalAmount = Stream
                            .of(input.getNumberOfWolves(), input.getNumberOfBears(), input.getNumberOfLynxes(),
                                    input.getNumberOfWolverines())
                            .filter(Objects::nonNull)
                            .filter(amount -> amount >= 0)
                            .collect(Collectors.summingInt(Integer::intValue));

                    return Optional.ofNullable(totalAmount)
                            .filter(sum -> sum > 0)
                            .map(positiveSum -> new MooseDataCardLargeCarnivoreObservation()
                                    .withDate(date)
                                    .withObservationType(convertedObservationType.getMooseDataCardEncoding())
                                    .withAdditionalInfo(input.getAdditionalInfo())
                                    .withNumberOfWolves(MooseDataCardDiaryEntryField.WOLF_AMOUNT.getValidOrNull(input))
                                    .withNumberOfBears(MooseDataCardDiaryEntryField.BEAR_AMOUNT.getValidOrNull(input))
                                    .withNumberOfLynxes(MooseDataCardDiaryEntryField.LYNX_AMOUNT.getValidOrNull(input))
                                    .withNumberOfWolverines(
                                            MooseDataCardDiaryEntryField.WOLVERINE_AMOUNT.getValidOrNull(input)))
                            .<Either<String, MooseDataCardLargeCarnivoreObservation>> map(obj -> {
                                obj.setGeoLocation(getValidGeoLocation(input));
                                return Either.right(obj);
                            })
                            .orElseGet(() -> Either
                                    .left(sumOfSpecimenAmountsOfLargeCarnivoreObservationIsNotGreaterThanZero(input)));
                }));
    }

}
