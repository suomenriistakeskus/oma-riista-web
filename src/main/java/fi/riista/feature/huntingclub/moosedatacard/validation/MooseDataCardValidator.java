package fi.riista.feature.huntingclub.moosedatacard.validation;

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
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor;
import fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;
import fi.riista.util.F;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Value;
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

    public MooseDataCardValidator(
            @Nonnull final Has2BeginEndDates permitSeason, @Nonnull final GeoLocation defaultCoordinates) {

        this.permitSeason = permitSeason;
        this.huntingDayValidator = new MooseDataCardHuntingDayValidator(permitSeason);
        this.mooseObservationValidator = new MooseDataCardMooseObservationValidator(defaultCoordinates);
        this.mooseMaleValidator = new MooseDataCardMooseMaleValidator(permitSeason, defaultCoordinates);
        this.mooseFemaleValidator = new MooseDataCardMooseFemaleValidator(permitSeason, defaultCoordinates);
        this.mooseCalfValidator = new MooseDataCardMooseCalfValidator(permitSeason, defaultCoordinates);
        this.carnivoreValidator = new MooseDataCardLargeCarnivoreObservationValidator(defaultCoordinates);
    }

    @Nonnull
    public Validation<List<String>, Tuple2<MooseDataCard, List<String>>> validate(
            @Nonnull final MooseDataCard mooseDataCard) {

        Objects.requireNonNull(mooseDataCard);

        final Tuple2<List<MooseDataCardHuntingDay>, List<String>> huntingDayTuple =
                extractHuntingDayData(mooseDataCard);

        final List<String> duplicateHuntingDayErrors = javaslang.collection.List.ofAll(huntingDayTuple._1)
                .groupBy(MooseDataCardHuntingDay::getStartDate)
                .toStream()
                .filter(tuple2 -> tuple2._2.size() > 1)
                .map(Tuple2::_1)
                .map(MooseDataCardImportFailureReasons::huntingDayAppearsMoreThanOnce)
                .toJavaList();

        final Validation<List<String>, List<MooseDataCardHuntingDay>> huntingDayValidation =
                duplicateHuntingDayErrors.isEmpty() ? valid(huntingDayTuple._1) : invalid(duplicateHuntingDayErrors);

        final Validation<List<String>, MooseDataCardCalculatedHarvestAmounts> harvestValidation =
                createHarvestValidation(mooseDataCard);

        // Summary validation requires the result of harvest validations as input.
        final Validation<List<String>, MooseDataCardCalculatedHarvestAmounts> harvestContentAndAmountValidation =
                harvestValidation.flatMap(calculatedAmounts -> {
                    return MooseDataCardExtractor.findFirstNonEmptyPage8(mooseDataCard)
                            .map(MooseDataCardPage8::getSection_8_2)
                            .map(section82 -> MooseDataCardSection82Validator.validate(section82, calculatedAmounts))
                            .map(validation -> validation.map(section82 -> calculatedAmounts))
                            .orElse(harvestValidation);
                });

        final Stream<Validation<List<String>, Object>> validationStream = Stream
                .concat(Stream.of(huntingDayValidation),
                        Stream.concat(Stream.of(harvestContentAndAmountValidation),
                                Stream.of(MooseDataCardSummaryValidator.validate(mooseDataCard, permitSeason))))
                .map(Validation::narrow);

        return combine(validationStream).map(t -> {
            final List<String> observationMessages = collectObservationAbandonReasons(mooseDataCard);
            return Tuple.of(mooseDataCard, F.concat(huntingDayTuple._2, observationMessages));
        });
    }

    private Validation<List<String>, MooseDataCardCalculatedHarvestAmounts> createHarvestValidation(
            final MooseDataCard mooseDataCard) {

        return Validation.combine(
                combine(streamMooseMaleHarvests(mooseDataCard).map(mooseMaleValidator::validate)),
                combine(streamMooseFemaleHarvests(mooseDataCard).map(mooseFemaleValidator::validate)),
                combine(streamMooseCalfHarvests(mooseDataCard).map(mooseCalfValidator::validate)))
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
                .map(calf -> HasMooseDataCardEncoding.enumOf(GameGender.class, calf.getGender()))
                .filter(Either::isRight)
                .map(Either::get)
                .toList();

        final int maleCalfs = calfGenders.count(GameGender.MALE::equals);
        final int femaleCalfs = calfGenders.size() - maleCalfs;

        return new MooseDataCardCalculatedHarvestAmounts(
                adultMales, adultFemales, maleCalfs, femaleCalfs, nonEdibleAdults, nonEdibleCalfs);
    }

    private static <T> Validation<List<String>, Seq<T>> combine(final Stream<Validation<List<String>, T>> stream) {
        return Validation
                .sequence(stream.map(v -> v.leftMap(javaslang.collection.List::ofAll)).collect(toList()))
                .leftMap(Value::toJavaList);
    }

    private Tuple2<List<MooseDataCardHuntingDay>, List<String>> extractHuntingDayData(
            final MooseDataCard mooseDataCard) {

        final Stream<Either<String, MooseDataCardHuntingDay>> huntingDayValidations =
                streamHuntingDays(mooseDataCard).map(huntingDayValidator::validate);

        final List<MooseDataCardHuntingDay> huntingDays = new ArrayList<>();
        final List<String> messages = new ArrayList<>();

        huntingDayValidations.forEach(either -> {
            if (either.isRight()) {
                huntingDays.add(either.get());
            } else {
                messages.add(either.getLeft());
            }
        });

        return Tuple.of(huntingDays, messages);
    }

    private List<String> collectObservationAbandonReasons(final MooseDataCard mooseDataCard) {
        final List<String> messages = new ArrayList<>();

        streamMooseObservations(mooseDataCard)
                .map(mooseObservationValidator::validate)
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .forEach(messages::add);

        streamLargeCarnivoreObservations(mooseDataCard).map(carnivoreValidator::validate)
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .forEach(messages::add);

        return messages;
    }

}
