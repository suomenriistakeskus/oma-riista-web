package fi.riista.feature.huntingclub.moosedatacard.converter;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingMethod;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardHuntingDayField;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardHuntingDayValidator;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class MooseDataCardHuntingDayConverter {

    public static final LocalTime DEFAULT_START_TIME_WHEN_ONLY_DATE_GIVEN = new LocalTime(0, 0);

    private final MooseDataCardHuntingDayValidator validator;

    public MooseDataCardHuntingDayConverter(@Nonnull final Has2BeginEndDates permitSeason) {
        validator = new MooseDataCardHuntingDayValidator(permitSeason);
    }

    /**
     * Converts a hunting day parsed from moose data card into a valid hunting day that can be
     * persisted into database. Only valid fields are converted, that is, fields with illegal values
     * are left null.
     */
    @Nonnull
    public GroupHuntingDay convert(@Nonnull final MooseDataCardHuntingDay huntingDay) {
        return validator.validate(huntingDay)
                .map(validHuntingDay -> {
                    final GroupHuntingDay result = new GroupHuntingDay();

                    final Interval interval = MooseDataCardExtractor.getHuntingDayInterval(
                            validHuntingDay.getStartDate(), validHuntingDay.getHuntingTime());

                    final DateTime startTime = interval.getStart();
                    result.setStartDate(startTime.toLocalDate());
                    result.setStartTime(startTime.toLocalTime());

                    final DateTime endTime = interval.getEnd();
                    result.setEndDate(endTime.toLocalDate());
                    result.setEndTime(endTime.toLocalTime());

                    result.setBreakDurationInMinutes(0);

                    result.setSnowDepth(MooseDataCardHuntingDayField.SNOW_DEPTH.getValidOrNull(validHuntingDay));
                    result.setHuntingMethod(MooseDataCardHuntingDayField.HUNTING_DAY_METHOD
                            .findValid(validHuntingDay)
                            .map(GroupHuntingMethod::valueOf)
                            .orElse(null));
                    result.setNumberOfHunters(
                            MooseDataCardHuntingDayField.NUMBER_OF_HUNTERS.getValidOrNull(validHuntingDay));
                    result.setNumberOfHounds(
                            MooseDataCardHuntingDayField.NUMBER_OF_HOUNDS.getValidOrNull(validHuntingDay));

                    return result;
                })
                .getOrElseThrow(
                        () -> new IllegalStateException("Invalid hunting day should not have passed validation"));
    }

    @Nonnull
    public static List<GroupHuntingDay> fabricateFromDates(@Nonnull final Collection<LocalDate> dates) {
        return Objects.requireNonNull(dates).stream().distinct().sorted().map(date -> {
            final GroupHuntingDay huntingDay = new GroupHuntingDay();

            huntingDay.setStartDate(date);
            huntingDay.setStartTime(DEFAULT_START_TIME_WHEN_ONLY_DATE_GIVEN);
            huntingDay.setEndDate(date);
            huntingDay.setEndTime(DEFAULT_START_TIME_WHEN_ONLY_DATE_GIVEN);
            huntingDay.setBreakDurationInMinutes(0);

            huntingDay.setSnowDepth(null);
            huntingDay.setHuntingMethod(null);
            huntingDay.setNumberOfHunters(null);
            huntingDay.setNumberOfHounds(null);

            return huntingDay;
        }).collect(toList());
    }
}
