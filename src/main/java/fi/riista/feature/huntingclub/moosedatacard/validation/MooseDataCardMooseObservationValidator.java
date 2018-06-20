package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import io.vavr.control.Either;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.sumOfSeenMoosesOfObservationIsNotGreaterThanZero;

public class MooseDataCardMooseObservationValidator
        extends MooseDataCardObservationValidator<MooseDataCardObservation> {

    public MooseDataCardMooseObservationValidator(final int huntingYear,
                                                  @Nonnull final GeoLocation defaultCoordinates) {
        super(huntingYear, defaultCoordinates);
    }

    @Override
    public Either<String, MooseDataCardObservation> validate(@Nonnull final MooseDataCardObservation in) {
        return resolveDate(in).flatMap(date -> {

            // Total amount is expected to be greater than zero.

            final int totalAmount = Stream
                    .of(in.getAU(), in.getN0(), in.getN1(), in.getN2(), in.getN3(), in.getY(), in.getT())
                    .filter(Objects::nonNull)
                    .filter(amount -> amount >= 0)
                    .mapToInt(Integer::intValue)
                    .sum();

            if (totalAmount == 0) {
                return Either.left(sumOfSeenMoosesOfObservationIsNotGreaterThanZero(in));
            }

            final MooseDataCardObservation validOutput = new MooseDataCardObservation()
                    .withDate(date)
                    .withAU(MooseDataCardDiaryEntryField.ADULT_MALE_AMOUNT.getValidOrNull(in))
                    .withN0(MooseDataCardDiaryEntryField.ADULT_FEMALE_AMOUNT.getValidOrNull(in))
                    .withN1(MooseDataCardDiaryEntryField.FEMALE_1CALF_AMOUNT.getValidOrNull(in))
                    .withN2(MooseDataCardDiaryEntryField.FEMALE_2CALF_AMOUNT.getValidOrNull(in))
                    .withN3(MooseDataCardDiaryEntryField.FEMALE_3CALF_AMOUNT.getValidOrNull(in))
                    .withY(MooseDataCardDiaryEntryField.CALF_AMOUNT.getValidOrNull(in))
                    .withT(MooseDataCardDiaryEntryField.UNKNOWN_AMOUNT.getValidOrNull(in));

            validOutput.setGeoLocation(getValidGeoLocation(in));

            return Either.right(validOutput);
        });
    }
}
