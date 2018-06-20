package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.diaryEntryValidationError;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.numericFieldNotInValidRange;

import com.google.common.collect.Range;

import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.huntingclub.moosedatacard.DateAndLocation;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.function.Function;

public abstract class MooseDataCardDiaryEntryField<T extends DateAndLocation, N extends Number & Comparable<N>>
        extends NumericFieldMeta<T, N> {

    public static final MooseDataCardDiaryEntryField<DateAndLocation, Long> LATITUDE = create(
            "pohjoisen leveyden koordinaatti",
            BoundariesOfFinland.MIN_ETRS_LATITUDE,
            BoundariesOfFinland.MAX_ETRS_LATITUDE,
            DateAndLocation::getLatitudeAsLong);

    public static final MooseDataCardDiaryEntryField<DateAndLocation, Long> LONGITUDE = create(
            "itäisen pituuden koordinaatti",
            BoundariesOfFinland.MIN_ETRS_LONGITUDE,
            BoundariesOfFinland.MAX_ETRS_LONGITUDE,
            DateAndLocation::getLongitudeAsLong);

    public static final MooseDataCardDiaryEntryField<MooseDataCardObservation, Integer> ADULT_MALE_AMOUNT =
            create("aikuisten urosten määrä", 0, 100, MooseDataCardObservation::getAU);

    public static final MooseDataCardDiaryEntryField<MooseDataCardObservation, Integer> ADULT_FEMALE_AMOUNT =
            create("aikuisten naarasten määrä", 0, 100, MooseDataCardObservation::getN0);

    public static final MooseDataCardDiaryEntryField<MooseDataCardObservation, Integer> FEMALE_1CALF_AMOUNT =
            create("naaras ja yksi vasa", 0, 50, MooseDataCardObservation::getN1);

    public static final MooseDataCardDiaryEntryField<MooseDataCardObservation, Integer> FEMALE_2CALF_AMOUNT =
            create("naaras ja kaksi vasaa", 0, 50, MooseDataCardObservation::getN2);

    public static final MooseDataCardDiaryEntryField<MooseDataCardObservation, Integer> FEMALE_3CALF_AMOUNT =
            create("naaras ja kaksi vasaa", 0, 50, MooseDataCardObservation::getN3);

    public static final MooseDataCardDiaryEntryField<MooseDataCardObservation, Integer> CALF_AMOUNT =
            create("yksinäisten vasojen määrä", 0, 50, MooseDataCardObservation::getY);

    public static final MooseDataCardDiaryEntryField<MooseDataCardObservation, Integer> UNKNOWN_AMOUNT =
            create("tuntemattomien määrä", 0, 50, MooseDataCardObservation::getT);

    public static final MooseDataCardDiaryEntryField<MooseDataCardHarvest, Double> ESTIMATED_WEIGHT =
            create("arvioitu teuraspaino", 0.1, 999.9, MooseDataCardHarvest::getWeightEstimated);

    public static final MooseDataCardDiaryEntryField<MooseDataCardHarvest, Double> MEASURED_WEIGHT =
            create("punnittu teuraspaino", 0.1, 999.9, MooseDataCardHarvest::getWeightMeasured);

    public static final MooseDataCardDiaryEntryField<MooseDataCardHarvest, Integer> FITNESS_CLASS =
            create("kuntoluokka",
                    Integer.parseInt(GameFitnessClass.ERINOMAINEN.getMooseDataCardEncoding()),
                    Integer.parseInt(GameFitnessClass.NAANTYNYT.getMooseDataCardEncoding()),
                    MooseDataCardHarvest::getFitnessClassAsInteger);

    public static final MooseDataCardDiaryEntryField<MooseDataCardMooseMale, Integer> ANTLERS_WIDTH =
            create("sarvien kärkiväli", 0, 999, MooseDataCardMooseMale::getAntlersWidth);

    public static final MooseDataCardDiaryEntryField<MooseDataCardMooseMale, Integer> ANTLER_POINTS_LEFT =
            create("sarvipiikit (vasen)", 0, 50, MooseDataCardMooseMale::getAntlerPointsLeft);

    public static final MooseDataCardDiaryEntryField<MooseDataCardMooseMale, Integer> ANTLER_POINTS_RIGHT =
            create("sarvipiikit (oikea)", 0, 50, MooseDataCardMooseMale::getAntlerPointsRight);

    public static final MooseDataCardDiaryEntryField<MooseDataCardLargeCarnivoreObservation, Integer> WOLF_AMOUNT =
            create("susiyksilöiden määrä",
                    Observation.MIN_AMOUNT,
                    Observation.MAX_AMOUNT,
                    MooseDataCardLargeCarnivoreObservation::getNumberOfWolves);

    public static final MooseDataCardDiaryEntryField<MooseDataCardLargeCarnivoreObservation, Integer> BEAR_AMOUNT =
            create("karhuyksilöiden määrä",
                    Observation.MIN_AMOUNT,
                    Observation.MAX_AMOUNT,
                    MooseDataCardLargeCarnivoreObservation::getNumberOfBears);

    public static final MooseDataCardDiaryEntryField<MooseDataCardLargeCarnivoreObservation, Integer> LYNX_AMOUNT =
            create("ilvesyksilöiden määrä",
                    Observation.MIN_AMOUNT,
                    Observation.MAX_AMOUNT,
                    MooseDataCardLargeCarnivoreObservation::getNumberOfLynxes);

    public static final MooseDataCardDiaryEntryField<MooseDataCardLargeCarnivoreObservation, Integer> WOLVERINE_AMOUNT =
            create("ahmayksilöiden määrä",
                    Observation.MIN_AMOUNT,
                    Observation.MAX_AMOUNT,
                    MooseDataCardLargeCarnivoreObservation::getNumberOfWolverines);

    public MooseDataCardDiaryEntryField(@Nonnull final String nameFinnish, @Nonnull final Range<N> range) {
        super(nameFinnish, range);
    }

    @Override
    protected Function<N, String> getInvalidMessageFunction(@Nonnull final T entry) {
        return value -> diaryEntryValidationError(entry, numericFieldNotInValidRange(this, value));
    }

    private static <T extends DateAndLocation, N extends Number & Comparable<N>> MooseDataCardDiaryEntryField<T, N> create(
            @Nonnull final String nameFinnish,
            @Nonnull final N lowerBound,
            @Nonnull final N upperBound,
            @Nonnull final Function<T, N> methodReference) {

        Objects.requireNonNull(methodReference, "methodReference is null");

        return new MooseDataCardDiaryEntryField<T, N>(nameFinnish, Range.closed(lowerBound, upperBound)) {
            @Override
            protected N doApply(final T object) {
                return methodReference.apply(object);
            }
        };
    }

}
