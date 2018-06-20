package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.config.Constants;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardHuntingDayField;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingAreaType;
import fi.riista.feature.huntingclub.permit.summary.TrendOfPopulationGrowth;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHarvestAreaType;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage2;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage3;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage4;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage5;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage6;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_2;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_4;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardTrendOfPopulationGrowth;
import fi.riista.util.F;
import io.vavr.control.Try;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.apache.commons.lang.StringUtils.trimToNull;

public final class MooseDataCardExtractor {

    public static final int DEFAULT_START_HOUR_OF_HUNTING_DAY = 8;
    public static final int DEFAULT_DURATION = 0;
    public static final LocalTime DEFAULT_HUNTING_DAY_START_TIME = new LocalTime(DEFAULT_START_HOUR_OF_HUNTING_DAY, 0);

    /**
     * Converts given date and duration into a legal hunting day interval. Duration is rounded down
     * to half an hour resolution and, if needed, replaced with a legal default value in case a
     * value out of acceptable range is provided.
     */
    @Nonnull
    public static Interval getHuntingDayInterval(@Nonnull final LocalDate date,
                                                 @Nullable final Float huntingDurationInHours) {

        Objects.requireNonNull(date, "date is null");

        final Float legalizedDuration = Optional.ofNullable(huntingDurationInHours)
                .filter(MooseDataCardHuntingDayField.HUNTING_DAY_DURATION::isValueInRange)
                .orElseGet(() -> Integer.valueOf(DEFAULT_DURATION).floatValue());

        final double durationRoundedDownToNearestHalfHour = roundDownToNearestHalfHour(legalizedDuration.doubleValue());
        final double durationRoundedToNearestHour = Math.ceil(durationRoundedDownToNearestHalfHour);

        final DateTime startTime = defaultStartTimeMustBeAdvanced(durationRoundedDownToNearestHalfHour)
                ? date.toDateTime(new LocalTime(
                        (int) (48.0 - durationRoundedToNearestHour),
                        (int) ((durationRoundedToNearestHour - durationRoundedDownToNearestHalfHour) * 60.0)))
                : date.toDateTime(DEFAULT_HUNTING_DAY_START_TIME);

        return new Interval(
                startTime.withZone(Constants.DEFAULT_TIMEZONE),
                startTime.plusMinutes((int) (durationRoundedDownToNearestHalfHour * 60.0))
                        .withZone(Constants.DEFAULT_TIMEZONE));
    }

    @Nonnull
    public static Stream<MooseDataCardHuntingDay> streamHuntingDays(final MooseDataCard mooseDataCard) {
        return mooseDataCard.getPage2().stream()
                .map(MooseDataCardPage2::getSection_2_1)
                .flatMap(section21 -> section21.getHuntingDays().stream());
    }

    @Nonnull
    public static Stream<MooseDataCardObservation> streamMooseObservations(final MooseDataCard mooseDataCard) {
        return mooseDataCard.getPage3().stream()
                .map(MooseDataCardPage3::getSection_3_1)
                .flatMap(section31 -> section31.getMooseObservations().stream())
                .filter(obj -> !obj.isEmpty());
    }

    @Nonnull
    public static Stream<MooseDataCardMooseMale> streamMooseMaleHarvests(final MooseDataCard mooseDataCard) {
        return mooseDataCard.getPage4().stream()
                .map(MooseDataCardPage4::getSection_4_1)
                .flatMap(section41 -> section41.getMooseMales().stream())
                .filter(obj -> !obj.isEmpty());
    }

    @Nonnull
    public static Stream<MooseDataCardMooseFemale> streamMooseFemaleHarvests(final MooseDataCard mooseDataCard) {
        return mooseDataCard.getPage5().stream()
                .map(MooseDataCardPage5::getSection_5_1)
                .flatMap(section51 -> section51.getMooseFemales().stream())
                .filter(obj -> !obj.isEmpty());
    }

    @Nonnull
    public static Stream<MooseDataCardMooseCalf> streamMooseCalfHarvests(final MooseDataCard mooseDataCard) {
        return mooseDataCard.getPage6().stream()
                .map(MooseDataCardPage6::getSection_6_1)
                .flatMap(section61 -> section61.getMooseCalfs().stream())
                .filter(obj -> !obj.isEmpty());
    }

    @Nonnull
    public static Stream<MooseDataCardLargeCarnivoreObservation> streamLargeCarnivoreObservations(
            final MooseDataCard mooseDataCard) {

        return mooseDataCard.getPage7().stream()
                .map(MooseDataCardPage7::getSection_7_1)
                .flatMap(section71 -> section71.getLargeCarnivoreObservations().stream())
                .filter(obj -> !obj.isEmpty());
    }

    @Nonnull
    public static Optional<MooseDataCardPage7> findFirstPage7ContainingHuntingSummaryData(
            @Nonnull final MooseDataCard mooseDataCard) {

        Objects.requireNonNull(mooseDataCard);

        return mooseDataCard.getPage7().stream()
                .filter(MooseDataCardExtractor::isSummaryDataPresent)
                .findFirst();
    }

    @Nonnull
    public static Optional<MooseDataCardPage8> findFirstNonEmptyPage8(@Nonnull final MooseDataCard mooseDataCard) {
        Objects.requireNonNull(mooseDataCard);

        return mooseDataCard.getPage8().stream()
                .filter(MooseDataCardExtractor::notEmpty)
                .findFirst();
    }

    public static boolean isSummaryDataPresent(@Nonnull final MooseDataCard mooseDataCard) {
        Objects.requireNonNull(mooseDataCard);

        return findFirstPage7ContainingHuntingSummaryData(mooseDataCard).isPresent() ||
                findFirstNonEmptyPage8(mooseDataCard).isPresent();
    }

    public static boolean isSummaryDataPresent(@Nonnull final MooseDataCardPage7 page7) {
        Objects.requireNonNull(page7);

        return isDefined(page7.getWhiteTailedDeerAppeared()) ||
                isDefined(page7.getRoeDeerAppeared()) ||
                isDefined(page7.getWildForestReindeerAppeared()) ||
                isDefined(page7.getFallowDeerAppeared()) ||
                isDefined(page7.getWildBoarAppeared()) ||

                isNotNullAndValidTrend(page7.getTrendOfWhiteTailedDeerPopulationGrowth()) ||
                isNotNullAndValidTrend(page7.getTrendOfRoeDeerPopulationGrowth()) ||
                isNotNullAndValidTrend(page7.getTrendOfWildForestReindeerPopulationGrowth()) ||
                isNotNullAndValidTrend(page7.getTrendOfFallowDeerPopulationGrowth()) ||
                isNotNullAndValidTrend(page7.getTrendOfWildBoarPopulationGrowth()) ||

                F.anyNonNull(
                        page7.getEstimatedSpecimenAmountOfWhiteTailedDeer(),
                        page7.getEstimatedSpecimenAmountOfRoeDeer(),
                        page7.getEstimatedSpecimenAmountOfWildForestReindeer(),
                        page7.getEstimatedSpecimenAmountOfFallowDeer(),
                        page7.getEstimatedSpecimenAmountOfWildBoar(),
                        page7.getEstimatedAmountOfSowsWithPiglets());
    }

    private static boolean isNotNullAndValidTrend(final String trendOfPopulationGrowth) {
        return HasMooseDataCardEncoding.findEnum(TrendOfPopulationGrowth.class, trendOfPopulationGrowth).isPresent();
    }

    public static boolean notEmpty(@Nonnull final MooseDataCardPage8 page8) {
        Objects.requireNonNull(page8);

        return notEmpty(page8.getSection_8_1()) ||
                notEmpty(page8.getSection_8_2()) ||
                notEmpty(page8.getSection_8_3()) ||
                notEmpty(page8.getSection_8_4()) ||
                page8.getHuntingEndDate() != null;
    }

    public static boolean notEmpty(final MooseDataCardSection_8_1 section) {
        final boolean huntingAreaTypeDefined =
                Optional.ofNullable(section.getHuntingAreaType())
                        .map(areaType -> areaType != MooseDataCardHarvestAreaType.UNDEFINED)
                        .orElse(false);

        return F.anyNonNull(
                section.getTotalHuntingArea(),
                section.getEffectiveHuntingArea(),
                section.getEffectiveHuntingAreaPercentage(),
                section.getMoosesRemainingInTotalHuntingArea(),
                section.getMoosesRemainingInEffectiveHuntingArea()) || huntingAreaTypeDefined;
    }

    public static boolean notEmpty(final MooseDataCardSection_8_2 section) {
        return F.anyNonNull(
                section.getNumberOfAdultMales(),
                section.getNumberOfAdultFemales(),
                section.getNumberOfYoungMales(),
                section.getNumberOfYoungFemales(),
                section.getTotalNumberOfNonEdibleAdults(),
                section.getTotalNumberOfNonEdibleYoungs());
    }

    public static boolean notEmpty(final MooseDataCardSection_8_3 section) {
        return F.anyNonNull(
                section.getNumberOfDrownedMooses(),
                section.getNumberOfMoosesKilledByBear(),
                section.getNumberOfMoosesKilledByWolf(),
                section.getNumberOfMoosesKilledInTrafficAccident(),
                section.getNumberOfMoosesKilledInPoaching(),
                section.getNumberOfMoosesKilledInRutFight(),
                section.getNumberOfStarvedMooses(),
                section.getNumberOfMoosesDeceasedByOtherReason(),
                trimToNull(section.getExplanationForOtherReason()));
    }

    public static boolean notEmpty(final MooseDataCardSection_8_4 section) {
        final boolean deerFlyAppearanceDefined =
                Optional.ofNullable(section.getDeerFlyAppearead())
                        .map(appeared -> appeared != MooseDataCardGameSpeciesAppearance.UNDEFINED)
                        .orElse(false);

        final boolean deerFlyPopulationGrowthTrendDefined =
                Optional.ofNullable(section.getTrendOfDeerFlyPopulationGrowth())
                        .map(appeared -> appeared != MooseDataCardTrendOfPopulationGrowth.UNDEFINED)
                        .orElse(false);

        return F.anyNonNull(
                section.getMooseHeatBeginDate(),
                section.getMooseHeatEndDate(),
                section.getMooseFawnBeginDate(),
                section.getMooseFawnEndDate(),
                section.getDateOfFirstDeerFlySeen(),
                section.getDateOfLastDeerFlySeen(),
                section.getNumberOfAdultMoosesHavingFlies(),
                section.getNumberOfYoungMoosesHavingFlies())
                || deerFlyAppearanceDefined
                || deerFlyPopulationGrowthTrendDefined;
    }

    private static boolean isDefined(final MooseDataCardGameSpeciesAppearance speciesAppearance) {
        return speciesAppearance != null && speciesAppearance != MooseDataCardGameSpeciesAppearance.UNDEFINED;
    }

    @Nullable
    public static TrendOfPopulationGrowth convertTrendOfPopulationGrowth(@Nullable final String value) {
        return HasMooseDataCardEncoding.getEnumOrNull(TrendOfPopulationGrowth.class, value);
    }

    @Nullable
    public static TrendOfPopulationGrowth convertTrendOfPopulationGrowthOfFlyDeer(
            @Nullable final MooseDataCardTrendOfPopulationGrowth populationGrowthTrend) {

        if (populationGrowthTrend != null) {
            switch (populationGrowthTrend) {
                case INCREASED:
                    return TrendOfPopulationGrowth.INCREASED;
                case UNCHANGED:
                    return TrendOfPopulationGrowth.UNCHANGED;
                case DECREASED:
                    return TrendOfPopulationGrowth.DECREASED;
                default:
                    // Fall-through to return null
            }
        }
        return null;
    }

    @Nullable
    public static Boolean convertAppearance(@Nullable final MooseDataCardGameSpeciesAppearance appearance) {
        if (appearance != null) {
            switch (appearance) {
                case YES:
                    return Boolean.TRUE;
                case NO:
                    return Boolean.FALSE;
                case UNDEFINED:
                default:
                    // Fall-through to return null
            }
        }
        return null;
    }

    @Nullable
    public static MooseHuntingAreaType convertMooseHuntingAreaType(
            @Nullable final MooseDataCardHarvestAreaType huntingAreaType) {

        if (huntingAreaType != null) {
            switch (huntingAreaType) {
                case SUMMER_PASTURE:
                    return MooseHuntingAreaType.SUMMER_PASTURE;
                case WINTER_PASTURE:
                    return MooseHuntingAreaType.WINTER_PASTURE;
                case BOTH:
                    return MooseHuntingAreaType.BOTH;
                case UNDEFINED:
                default:
                    // Fall-through to return null
            }
        }
        return null;
    }

    @Nonnull
    public static <N extends Number> Try<N> parseNumber(@Nullable final String numberAsString,
                                                        @Nonnull final Function<String, N> transformation) {

        return Try.of(() -> F.trimToOptional(numberAsString).map(transformation).orElse(null));
    }

    private static double roundDownToNearestHalfHour(final double duration) {
        return Math.floor(duration * 2.0) / 2.0;
    }

    private static boolean defaultStartTimeMustBeAdvanced(final double duration) {
        return MooseDataCardHuntingDayField.HUNTING_DAY_DURATION.findUpperBound()
                .map(maxDuration -> duration > maxDuration - DEFAULT_START_HOUR_OF_HUNTING_DAY)
                .orElse(false);
    }

    private MooseDataCardExtractor() {
        throw new AssertionError();
    }
}
