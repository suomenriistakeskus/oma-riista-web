package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.huntingDayValidationError;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.numericFieldNotInValidRange;

import com.google.common.collect.Range;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.function.Function;

public abstract class MooseDataCardHuntingDayField<N extends Number & Comparable<N>>
        extends NumericFieldMeta<MooseDataCardHuntingDay, N> {

    public static final MooseDataCardHuntingDayField<Float> HUNTING_DAY_DURATION =
            create("metsästykseen käytetty aika", 0.5f, 48.0f, MooseDataCardHuntingDay::getHuntingTime);

    public static final MooseDataCardHuntingDayField<Integer> HUNTING_DAY_METHOD =
            create("metsästystapa", 1, 9, MooseDataCardHuntingDay::getHuntingMethod);

    public static final MooseDataCardHuntingDayField<Integer> SNOW_DEPTH =
            create("lumen syvyys", 0, (int) Short.MAX_VALUE, MooseDataCardHuntingDay::getSnowDepth);

    public static final MooseDataCardHuntingDayField<Integer> NUMBER_OF_HUNTERS =
            create("metsästäjien lukumäärä", 1, (int) Short.MAX_VALUE, MooseDataCardHuntingDay::getNumberOfHunters);

    public static final MooseDataCardHuntingDayField<Integer> NUMBER_OF_HOUNDS =
            create("metsästyskoirien lukumäärä", 0, (int) Short.MAX_VALUE, MooseDataCardHuntingDay::getNumberOfHounds);

    public MooseDataCardHuntingDayField(@Nonnull final String nameFinnish, @Nonnull final Range<N> range) {
        super(nameFinnish, range);
    }

    @Override
    protected Function<N, String> getInvalidMessageFunction(@Nonnull final MooseDataCardHuntingDay huntingDay) {
        return v -> huntingDayValidationError(huntingDay.getStartDate(), numericFieldNotInValidRange(this, v));
    }

    private static <T extends Number & Comparable<T>> MooseDataCardHuntingDayField<T> create(
            @Nonnull final String nameFinnish,
            @Nonnull final T lowerBound,
            @Nonnull final T upperBound,
            @Nonnull final Function<MooseDataCardHuntingDay, T> methodReference) {

        Objects.requireNonNull(methodReference, "methodReference is null");

        return new MooseDataCardHuntingDayField<T>(nameFinnish, Range.closed(lowerBound, upperBound)) {
            @Override
            protected T doApply(final MooseDataCardHuntingDay object) {
                return methodReference.apply(object);
            }
        };
    }

}
