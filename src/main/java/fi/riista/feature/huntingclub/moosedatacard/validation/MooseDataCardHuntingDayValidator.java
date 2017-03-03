package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.getHuntingDayInterval;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayEndDateNotWithinPermittedSeason;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayStartDateNotWithinPermittedSeason;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayWithoutDate;

import com.google.common.collect.Range;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import fi.riista.util.F;
import javaslang.control.Either;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class MooseDataCardHuntingDayValidator {

    private final Has2BeginEndDates permitSeason;

    public MooseDataCardHuntingDayValidator() {
        this.permitSeason = null;
    }

    public MooseDataCardHuntingDayValidator(@Nonnull final Has2BeginEndDates permitSeason) {
        this.permitSeason = Objects.requireNonNull(permitSeason);
    }

    public Either<String, MooseDataCardHuntingDay> validate(@Nonnull final MooseDataCardHuntingDay input) {
        Objects.requireNonNull(input);

        final Optional<Interval> huntingDayInterval =
                Optional.ofNullable(input.getStartDate())
                        .map(date -> getHuntingDayInterval(date, input.getHuntingTime()));

        return F.toEither(huntingDayInterval, () -> huntingDayWithoutDate())
                .flatMap(interval -> getDateRangeOfHuntingDay(interval).map(rangeOfLocalDates -> {
                    final MooseDataCardHuntingDay output = input.createCopy();
                    output.setHuntingTime(MooseDataCardHuntingDayField.HUNTING_DAY_DURATION.getValidOrNull(input));
                    output.setHuntingMethod(MooseDataCardHuntingDayField.HUNTING_DAY_METHOD.getValidOrNull(input));
                    output.setSnowDepth(MooseDataCardHuntingDayField.SNOW_DEPTH.getValidOrNull(input));
                    output.setNumberOfHunters(MooseDataCardHuntingDayField.NUMBER_OF_HUNTERS.getValidOrNull(input));
                    output.setNumberOfHounds(MooseDataCardHuntingDayField.NUMBER_OF_HOUNDS.getValidOrNull(input));
                    return output;
                }));
    }

    private Either<String, Range<LocalDate>> getDateRangeOfHuntingDay(@Nonnull final Interval huntingDayInterval) {
        Objects.requireNonNull(huntingDayInterval);

        final LocalDate startDate = huntingDayInterval.getStart().toLocalDate();

        if (permitSeason != null && !permitSeason.containsDate(startDate)) {
            return Either.left(huntingDayStartDateNotWithinPermittedSeason(startDate, permitSeason));
        }

        final LocalDate endDate = huntingDayInterval.getEnd().minusSeconds(1).toLocalDate();

        if (permitSeason != null && !permitSeason.containsDate(endDate)) {
            return Either.left(huntingDayEndDateNotWithinPermittedSeason(startDate, endDate, permitSeason));
        }

        return Either.right(Range.closed(startDate, endDate));
    }

}
