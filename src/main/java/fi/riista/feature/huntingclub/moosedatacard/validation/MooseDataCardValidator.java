package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.findFirstNonEmptyPage8;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.streamHuntingDays;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.streamLargeCarnivoreObservations;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.streamMooseCalfHarvests;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.streamMooseFemaleHarvests;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.streamMooseMaleHarvests;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.streamMooseObservations;
import static java.util.stream.Collectors.toList;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;
import fi.riista.util.F;
import fi.riista.util.ValidationUtils;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Seq;
import javaslang.collection.Traversable;
import javaslang.control.Either;
import javaslang.control.Validation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MooseDataCardValidator {

    private final Has2BeginEndDates permitSeason;
    private final MooseDataCardHuntingDayValidator huntingDayValidator;
    private final MooseDataCardMooseObservationValidator mooseObservationValidator;
    private final MooseDataCardMooseMaleValidator mooseMaleValidator;
    private final MooseDataCardMooseFemaleValidator mooseFemaleValidator;
    private final MooseDataCardMooseCalfValidator mooseCalfValidator;
    private final MooseDataCardLargeCarnivoreObservationValidator carnivoreValidator;

    public MooseDataCardValidator(@Nonnull final Has2BeginEndDates permitSeason,
                                  @Nonnull final GeoLocation defaultCoordinates) {

        this.permitSeason = permitSeason;
        this.huntingDayValidator = new MooseDataCardHuntingDayValidator(permitSeason);
        this.mooseObservationValidator = new MooseDataCardMooseObservationValidator(defaultCoordinates);
        this.mooseMaleValidator = new MooseDataCardMooseMaleValidator(permitSeason, defaultCoordinates);
        this.mooseFemaleValidator = new MooseDataCardMooseFemaleValidator(permitSeason, defaultCoordinates);
        this.mooseCalfValidator = new MooseDataCardMooseCalfValidator(permitSeason, defaultCoordinates);
        this.carnivoreValidator = new MooseDataCardLargeCarnivoreObservationValidator(defaultCoordinates);
    }

    @Nonnull
    public Validation<List<String>, Tuple2<MooseDataCard, List<String>>> validate(@Nonnull final MooseDataCard card) {
        Objects.requireNonNull(card);

        return extractHuntingDayData(card).transform((validHuntingDays, huntingDayAbandonReasons) -> {

            final Validation<List<String>, MooseDataCardCalculatedHarvestAmounts> harvestValidation =
                    validateHarvests(card);

            // Summary validation requires the result of harvest validations as input.
            final Validation<List<String>, MooseDataCardCalculatedHarvestAmounts> compoundHarvestValidation =
                    ValidationUtils.toValidation(
                            findFirstNonEmptyPage8(card).map(MooseDataCardPage8::getSection_8_2),
                            section82 -> harvestValidation.flatMap(computedAmounts -> {
                                return MooseDataCardSection82Validator
                                        .validate(section82, computedAmounts)
                                        .map(validSection -> computedAmounts);
                            }),
                            () -> harvestValidation);

            final Validation<List<String>, List<Object>> combinedValidation = ValidationUtils.combine(Stream.of(
                    validateHuntingDays(validHuntingDays),
                    compoundHarvestValidation,
                    MooseDataCardSummaryValidator.validate(card, permitSeason)).map(Validation::narrow));

            return combinedValidation.map(objectList -> {
                return Tuple.of(card, F.concat(huntingDayAbandonReasons, collectObservationAbandonReasons(card)));
            });
        });
    }

    private Tuple2<List<MooseDataCardHuntingDay>, List<String>> extractHuntingDayData(final MooseDataCard card) {

        final List<Either<String, MooseDataCardHuntingDay>> huntingDayValidations =
                streamHuntingDays(card).map(huntingDayValidator::validate).collect(toList());

        final List<MooseDataCardHuntingDay> validHuntingDays = new ArrayList<>(huntingDayValidations.size());
        final List<String> abandonReasons = new ArrayList<>(huntingDayValidations.size());

        huntingDayValidations.forEach(either -> either.peek(validHuntingDays::add).orElseRun(abandonReasons::add));

        return Tuple.of(validHuntingDays, abandonReasons);
    }

    private static Validation<List<String>, List<MooseDataCardHuntingDay>> validateHuntingDays(
            final List<MooseDataCardHuntingDay> huntingDays) {

        final List<String> duplicateHuntingDayErrors = javaslang.collection.List.ofAll(huntingDays)
                .groupBy(MooseDataCardHuntingDay::getStartDate)
                .toStream()
                .filter(tuple2 -> tuple2._2.size() > 1)
                .map(Tuple2::_1)
                .map(MooseDataCardImportFailureReasons::huntingDayAppearsMoreThanOnce)
                .toJavaList();

        return duplicateHuntingDayErrors.isEmpty() ? valid(huntingDays) : invalid(duplicateHuntingDayErrors);
    }

    private Validation<List<String>, MooseDataCardCalculatedHarvestAmounts> validateHarvests(final MooseDataCard card) {
        return Validation.combine(
                combine(streamMooseMaleHarvests(card).map(mooseMaleValidator::validate)),
                combine(streamMooseFemaleHarvests(card).map(mooseFemaleValidator::validate)),
                combine(streamMooseCalfHarvests(card).map(mooseCalfValidator::validate)))
                .ap(MooseDataCardValidator::calculateHarvestAmounts)
                // conversion from Javaslang to vanilla Java list type
                .<List<String>> leftMap(
                        listOfLists -> listOfLists.toJavaStream().flatMap(List::stream).collect(toList()));
    }

    // Exposed publicly to enable isolated testing.
    @Nonnull
    public static MooseDataCardCalculatedHarvestAmounts calculateHarvestAmounts(
            @Nonnull final Traversable<MooseDataCardMooseMale> males,
            @Nonnull final Traversable<MooseDataCardMooseFemale> females,
            @Nonnull final Traversable<MooseDataCardMooseCalf> calfs) {

        Objects.requireNonNull(males, "males is null");
        Objects.requireNonNull(females, "females is null");
        Objects.requireNonNull(calfs, "calfs is null");

        final int adultMales = males.size();
        final int nonEdibleAdultMales = males.count(MooseDataCardHarvest::isNotEdible);

        final int adultFemales = females.size();
        final int nonEdibleAdultFemales = females.count(MooseDataCardHarvest::isNotEdible);

        final int nonEdibleAdults = nonEdibleAdultMales + nonEdibleAdultFemales;
        final int nonEdibleCalfs = calfs.count(MooseDataCardHarvest::isNotEdible);

        final javaslang.collection.List<GameGender> calfGenders = calfs.toStream()
                .map(calf -> HasMooseDataCardEncoding.eitherInvalidOrValid(GameGender.class, calf.getGender()))
                .filter(Either::isRight)
                .map(Either::get)
                .toList();

        final int maleCalfs = calfGenders.count(GameGender.MALE::equals);
        final int femaleCalfs = calfGenders.size() - maleCalfs;

        return new MooseDataCardCalculatedHarvestAmounts(
                adultMales, adultFemales, maleCalfs, femaleCalfs, nonEdibleAdults, nonEdibleCalfs);
    }

    private static <T> Validation<List<String>, Seq<T>> combine(final Stream<Validation<List<String>, T>> stream) {
        return ValidationUtils.combine(stream).map(javaslang.collection.List::ofAll);
    }

    private List<String> collectObservationAbandonReasons(final MooseDataCard card) {
        return Stream.concat(
                streamMooseObservations(card).map(mooseObservationValidator::validate),
                streamLargeCarnivoreObservations(card).map(carnivoreValidator::validate))
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .collect(toList());
    }

}
