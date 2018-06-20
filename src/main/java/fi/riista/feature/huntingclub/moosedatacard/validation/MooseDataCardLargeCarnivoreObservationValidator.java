package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import io.vavr.control.Either;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.largeCarnivoreMissingObservationType;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationTypeOfLargeCarnivoreContainsIllegalCharacters;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.sumOfSpecimenAmountsOfLargeCarnivoreObservationIsNotGreaterThanZero;

public class MooseDataCardLargeCarnivoreObservationValidator
        extends MooseDataCardObservationValidator<MooseDataCardLargeCarnivoreObservation> {

    public MooseDataCardLargeCarnivoreObservationValidator(final int huntingYear,
                                                           @Nonnull final GeoLocation defaultCoordinates) {
        super(huntingYear, defaultCoordinates);
    }

    @Override
    public Either<String, MooseDataCardLargeCarnivoreObservation> validate(
            @Nonnull final MooseDataCardLargeCarnivoreObservation input) {

        return resolveDate(input).flatMap(date -> HasMooseDataCardEncoding
                .eitherInvalidOrValid(ObservationType.class, input.getObservationType())
                .mapLeft(invalidOpt -> invalidOpt
                        .map(invalid -> observationTypeOfLargeCarnivoreContainsIllegalCharacters(input))
                        .orElseGet(() -> largeCarnivoreMissingObservationType(input)))
                .flatMap(convertedObservationType -> {

                    // Total amount is expected to be greater than zero.

                    final int totalAmount = Stream
                            .of(input.getNumberOfWolves(), input.getNumberOfBears(), input.getNumberOfLynxes(),
                                    input.getNumberOfWolverines())
                            .filter(Objects::nonNull)
                            .filter(amount -> amount >= 0)
                            .mapToInt(Integer::intValue)
                            .sum();

                    if (totalAmount == 0) {
                        return Either.left(sumOfSpecimenAmountsOfLargeCarnivoreObservationIsNotGreaterThanZero(input));
                    }

                    final MooseDataCardLargeCarnivoreObservation validOutput =
                            new MooseDataCardLargeCarnivoreObservation()
                                    .withDate(date)
                                    .withObservationType(convertedObservationType.getMooseDataCardEncoding())
                                    .withAdditionalInfo(input.getAdditionalInfo())
                                    .withNumberOfWolves(MooseDataCardDiaryEntryField.WOLF_AMOUNT.getValidOrNull(input))
                                    .withNumberOfBears(MooseDataCardDiaryEntryField.BEAR_AMOUNT.getValidOrNull(input))
                                    .withNumberOfLynxes(MooseDataCardDiaryEntryField.LYNX_AMOUNT.getValidOrNull(input))
                                    .withNumberOfWolverines(
                                            MooseDataCardDiaryEntryField.WOLVERINE_AMOUNT.getValidOrNull(input));

                    validOutput.setGeoLocation(getValidGeoLocation(input));

                    return Either.right(validOutput);
                }));
    }
}
