package fi.riista.feature.huntingclub.moosedatacard.validation;

import com.google.common.collect.Range;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_2;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_4;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.numericFieldNotInValidRange;

public abstract class MooseDataCardSummaryField<T, N extends Number & Comparable<N>> extends NumericFieldMeta<T, N> {

    public static final MooseDataCardSummaryField<MooseDataCardPage7, Integer> ESTIMATED_AMOUNT_OF_WHITE_TAILED_DEERS =
            create("Valkohäntäpeurojen arvioitu yksilömäärä (kohta 7.2)",
                    0,
                    MooseDataCardPage7::getEstimatedSpecimenAmountOfWhiteTailedDeer);

    public static final MooseDataCardSummaryField<MooseDataCardPage7, Integer> ESTIMATED_AMOUNT_OF_ROE_DEERS =
            create("Metsäkauriiden arvioitu yksilömäärä (kohta 7.2)",
                    0,
                    MooseDataCardPage7::getEstimatedSpecimenAmountOfRoeDeer);

    public static final MooseDataCardSummaryField<MooseDataCardPage7, Integer> ESTIMATED_AMOUNT_OF_WILD_FOREST_REINDEERS =
            create("Metsäpeurojen arvioitu yksilömäärä (kohta 7.2)",
                    0,
                    MooseDataCardPage7::getEstimatedSpecimenAmountOfWildForestReindeer);

    public static final MooseDataCardSummaryField<MooseDataCardPage7, Integer> ESTIMATED_AMOUNT_OF_FALLOW_DEERS =
            create("Kuusipeurojen arvioitu yksilömäärä (kohta 7.2)",
                    0,
                    MooseDataCardPage7::getEstimatedSpecimenAmountOfFallowDeer);

    public static final MooseDataCardSummaryField<MooseDataCardPage7, Integer> ESTIMATED_AMOUNT_OF_WILD_BOARS =
            create("Villisikojen arvioitu yksilömäärä (kohta 7.2)",
                    0,
                    MooseDataCardPage7::getEstimatedSpecimenAmountOfWildBoar);

    public static final MooseDataCardSummaryField<MooseDataCardPage7, Integer> ESTIMATED_AMOUNT_OF_SOWS_WITH_PIGLETS =
            create("Porsaallisten emakoiden arvioitu yksilömäärä (kohta 7.2)",
                    0,
                    MooseDataCardPage7::getEstimatedAmountOfSowsWithPiglets);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_1, Double> TOTAL_HUNTING_AREA =
            create("Metsästysalueen kokonaispinta-ala (kohta 8.1)", 0.0, MooseDataCardSection_8_1::getTotalHuntingArea);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_1, Double> EFFECTIVE_HUNTING_AREA =
            create("Metsästykseen käytetyn alueen koko (kohta 8.1)",
                    0.0,
                    MooseDataCardSection_8_1::getEffectiveHuntingArea);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_1, Double> EFFECTIVE_HUNTING_AREA_PERCENTAGE =
            create("Metsästykseen käytetyn alueen osuus kokonaispinta-alasta (kohta 8.1)",
                    0.0,
                    100.0,
                    MooseDataCardSection_8_1::getEffectiveHuntingAreaPercentage);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_1, Integer> MOOSES_REMAINING_IN_TOTAL_HUNTING_AREA =
            create("Koko metsästysalueelle jäävien hirvien määrä (kohta 8.1)",
                    0,
                    MooseDataCardSection_8_1::getMoosesRemainingInTotalHuntingArea);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_1, Integer> MOOSES_REMAINING_IN_EFFECTIVE_HUNTING_AREA =
            create("Metsästykseen käytetylle alueelle jäävien hirvien määrä (kohta 8.1)",
                    0,
                    MooseDataCardSection_8_1::getMoosesRemainingInEffectiveHuntingArea);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_2, Integer> ADULT_MALE_AMOUNT =
            create("Aikuisten urosten määrä (kohta 8.2)", 0, MooseDataCardSection_8_2::getNumberOfAdultMales);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_2, Integer> ADULT_FEMALE_AMOUNT =
            create("Aikuisten naarasten määrä (kohta 8.2)", 0, MooseDataCardSection_8_2::getNumberOfAdultFemales);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_2, Integer> YOUNG_MALE_AMOUNT =
            create("Urosvasojen määrä (kohta 8.2)", 0, MooseDataCardSection_8_2::getNumberOfYoungMales);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_2, Integer> YOUNG_FEMALE_AMOUNT =
            create("Naarasvasojen määrä (kohta 8.2)", 0, MooseDataCardSection_8_2::getNumberOfYoungFemales);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_2, Integer> NON_EDIBLE_ADULT_AMOUNT =
            create("Ihmisravinnoksi kelpaamattomien aikuisten määrä (kohta 8.2)",
                    0,
                    MooseDataCardSection_8_2::getTotalNumberOfNonEdibleAdults);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_2, Integer> NON_EDIBLE_YOUNG_AMOUNT =
            create("Ihmisravinnoksi kelpaamattomien vasojen määrä (kohta 8.2)",
                    0,
                    MooseDataCardSection_8_2::getTotalNumberOfNonEdibleYoungs);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_3, Integer> DROWNED_AMOUNT =
            create("Kuolleiden hirvien määrä (kohta 8.3)", 0, MooseDataCardSection_8_3::getNumberOfDrownedMooses);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_3, Integer> KILLED_BY_BEAR_AMOUNT =
            create("Karhun tappamien hirvien määrä (kohta 8.3)",
                    0,
                    MooseDataCardSection_8_3::getNumberOfMoosesKilledByBear);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_3, Integer> KILLED_BY_WOLF_AMOUNT =
            create("Suden tappamien hirvien määrä (kohta 8.3)",
                    0, MooseDataCardSection_8_3::getNumberOfMoosesKilledByWolf);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_3, Integer> KILLED_IN_TRAFFIC_ACCIDENT_AMOUNT =
            create("Kolarissa kuolleiden hirvien määrä (kohta 8.3)",
                    0,
                    MooseDataCardSection_8_3::getNumberOfMoosesKilledInTrafficAccident);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_3, Integer> KILLED_IN_POACHING_AMOUNT =
            create("Salakaadettujen hirvien määrä (kohta 8.3)",
                    0,
                    MooseDataCardSection_8_3::getNumberOfMoosesKilledInPoaching);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_3, Integer> KILLED_IN_RUT_FIGHT_AMOUNT =
            create("Kiimatappelussa kuolleiden hirvien määrä (kohta 8.3)",
                    0,
                    MooseDataCardSection_8_3::getNumberOfMoosesDeceasedByOtherReason);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_3, Integer> STARVED_AMOUNT =
            create("Nälkiintyneiden hirvien määrä (kohta 8.3)", 0, MooseDataCardSection_8_3::getNumberOfStarvedMooses);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_3, Integer> DECEASED_BY_OTHER_REASON_AMOUNT =
            create("Muusta syystä kuolleiden hirvien määrä (kohta 8.3)",
                    0,
                    MooseDataCardSection_8_3::getNumberOfMoosesDeceasedByOtherReason);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_4, Integer> AMOUNT_OF_ADULT_MOOSES_HAVING_FLIES =
            create("Kaadettujen aikuisten hirvien määrä, joista löydetty hirvikärpäsiä (kohta 8.4)",
                    0,
                    MooseDataCardSection_8_4::getNumberOfAdultMoosesHavingFlies);

    public static final MooseDataCardSummaryField<MooseDataCardSection_8_4, Integer> AMOUNT_OF_YOUNG_MOOSES_HAVING_FLIES =
            create("Kaadettujen hirvivasojen määrä, joista löydetty hirvikärpäsiä (kohta 8.4)",
                    0,
                    MooseDataCardSection_8_4::getNumberOfYoungMoosesHavingFlies);

    public MooseDataCardSummaryField(@Nonnull final String nameFinnish, @Nonnull final Range<N> range) {
        super(nameFinnish, range);
    }

    @Override
    protected Function<N, String> getInvalidMessageFunction(@Nonnull final T section) {
        return value -> numericFieldNotInValidRange(this, value);
    }

    private static <T, N extends Number & Comparable<N>> MooseDataCardSummaryField<T, N> create(
            @Nonnull final String nameFinnish, @Nonnull final N min, @Nonnull final Function<T, N> methodReference) {

        return create(nameFinnish, Range.atLeast(min), methodReference);
    }

    private static <T, N extends Number & Comparable<N>> MooseDataCardSummaryField<T, N> create(
            @Nonnull final String nameFinnish,
            @Nonnull final N min,
            @Nonnull final N max,
            @Nonnull final Function<T, N> methodReference) {

        return create(nameFinnish, Range.closed(min, max), methodReference);
    }

    private static <T, N extends Number & Comparable<N>> MooseDataCardSummaryField<T, N> create(
            @Nonnull final String nameFinnish,
            @Nonnull final Range<N> range,
            @Nonnull final Function<T, N> methodReference) {

        Objects.requireNonNull(methodReference, "methodReference is null");

        return new MooseDataCardSummaryField<T, N>(nameFinnish, range) {
            @Override
            protected N doApply(final T object) {
                return methodReference.apply(object);
            }
        };
    }

}
