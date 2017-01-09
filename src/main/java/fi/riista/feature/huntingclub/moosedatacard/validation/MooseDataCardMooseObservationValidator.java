package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.sumOfSeenMoosesOfObservationIsNotGreaterThanZero;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;

import javaslang.control.Either;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MooseDataCardMooseObservationValidator
        extends MooseDataCardObservationValidator<MooseDataCardObservation> {

    public MooseDataCardMooseObservationValidator(@Nonnull final GeoLocation defaultCoordinates) {
        super(defaultCoordinates);
    }

    @Override
    public Either<String, MooseDataCardObservation> validate(@Nonnull final MooseDataCardObservation input) {
        return resolveDate(input).flatMap(date -> {

            // Total amount is expected to be greater than zero.

            final Integer totalAmount = Stream
                    .of(input.getAU(), input.getN0(), input.getN1(), input.getN2(), input.getN3(), input.getT())
                    .filter(Objects::nonNull)
                    .filter(amount -> amount >= 0)
                    .collect(Collectors.summingInt(Integer::intValue));

            return Optional.ofNullable(totalAmount)
                    .filter(sum -> sum > 0)
                    .map(positiveSum -> new MooseDataCardObservation()
                            .withDate(date)
                            .withAU(MooseDataCardDiaryEntryField.ADULT_MALE_AMOUNT.getValidOrNull(input))
                            .withN0(MooseDataCardDiaryEntryField.ADULT_FEMALE_AMOUNT.getValidOrNull(input))
                            .withN1(MooseDataCardDiaryEntryField.FEMALE_1CALF_AMOUNT.getValidOrNull(input))
                            .withN2(MooseDataCardDiaryEntryField.FEMALE_2CALF_AMOUNT.getValidOrNull(input))
                            .withN3(MooseDataCardDiaryEntryField.FEMALE_3CALF_AMOUNT.getValidOrNull(input))
                            .withT(MooseDataCardDiaryEntryField.UNKNOWN_AMOUNT.getValidOrNull(input)))
                    .<Either<String, MooseDataCardObservation>> map(obj -> {
                        obj.setGeoLocation(getValidGeoLocation(input));
                        return Either.right(obj);
                    })
                    .orElseGet(() -> Either.left(sumOfSeenMoosesOfObservationIsNotGreaterThanZero(input)));
        });
    }

}
