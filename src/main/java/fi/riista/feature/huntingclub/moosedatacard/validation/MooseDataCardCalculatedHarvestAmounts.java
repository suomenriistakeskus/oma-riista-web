package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.adultFemaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.adultMaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.nonEdibleAdultHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.nonEdibleYoungHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.youngFemaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.youngMaleHarvestCountMismatch;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_2;

import javaslang.Tuple;
import javaslang.Tuple6;
import javaslang.Value;
import javaslang.control.Validation;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class MooseDataCardCalculatedHarvestAmounts {

    private static final Supplier<Validation<String, Optional<Integer>>> VALID_EMPTY = () -> valid(Optional.empty());

    public final int numberOfAdultMales;
    public final int numberOfAdultFemales;
    public final int numberOfYoungMales;
    public final int numberOfYoungFemales;
    public final int totalNumberOfNonEdibleAdults;
    public final int totalNumberOfNonEdibleYoungs;

    public MooseDataCardCalculatedHarvestAmounts(
            final int numberOfAdultMales,
            final int numberOfAdultFemales,
            final int numberOfYoungMales,
            final int numberOfYoungFemales,
            final int totalNumberOfNonEdibleAdults,
            final int totalNumberOfNonEdibleYoungs) {

        this.numberOfAdultMales = numberOfAdultMales;
        this.numberOfAdultFemales = numberOfAdultFemales;
        this.numberOfYoungMales = numberOfYoungMales;
        this.numberOfYoungFemales = numberOfYoungFemales;
        this.totalNumberOfNonEdibleAdults = totalNumberOfNonEdibleAdults;
        this.totalNumberOfNonEdibleYoungs = totalNumberOfNonEdibleYoungs;
    }

    @Override
    public String toString() {
        return asTuple6().toString();
    }

    public Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> asTuple6() {
        return Tuple.of(numberOfAdultMales, numberOfAdultFemales, numberOfYoungMales, numberOfYoungFemales,
                totalNumberOfNonEdibleAdults, totalNumberOfNonEdibleYoungs);
    }

    public Validation<List<String>, MooseDataCardSection_8_2> validate(
            @Nonnull final MooseDataCardSection_8_2 section) {

        Objects.requireNonNull(section);

        return validateAdultMaleAmount(section)
                .combine(validateAdultFemaleAmount(section))
                .combine(validateYoungMaleAmount(section))
                .combine(validateYoungFemaleAmount(section))
                .combine(validateNonEdibleAdultAmount(section))
                .combine(validateNonEdibleYoungAmount(section))
                .ap((_1, _2, _3, _4, _5, _6) -> section)
                .leftMap(Value::toJavaList);
    }

    private Validation<String, Optional<Integer>> validateAdultMaleAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.ADULT_MALE_AMOUNT.validate(section).flatMap(amountOpt -> {
            return amountOpt
                    .<Validation<String, Optional<Integer>>> map(amount -> {
                        return amount == numberOfAdultMales
                                ? valid(amountOpt)
                                : invalid(adultMaleHarvestCountMismatch(amount, numberOfAdultMales));
                    })
                    .orElseGet(VALID_EMPTY);
        });
    }

    private Validation<String, Optional<Integer>> validateAdultFemaleAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.ADULT_FEMALE_AMOUNT.validate(section).flatMap(amountOpt -> {
            return amountOpt
                    .<Validation<String, Optional<Integer>>> map(amount -> {
                        return amount == numberOfAdultFemales
                                ? valid(amountOpt)
                                : invalid(adultFemaleHarvestCountMismatch(amount, numberOfAdultFemales));
                    })
                    .orElseGet(VALID_EMPTY);
        });
    }

    private Validation<String, Optional<Integer>> validateYoungMaleAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.YOUNG_MALE_AMOUNT.validate(section).flatMap(amountOpt -> {
            return amountOpt
                    .<Validation<String, Optional<Integer>>> map(amount -> {
                        return amount == numberOfYoungMales
                                ? valid(amountOpt)
                                : invalid(youngMaleHarvestCountMismatch(amount, numberOfYoungMales));
                    })
                    .orElseGet(VALID_EMPTY);
        });
    }

    private Validation<String, Optional<Integer>> validateYoungFemaleAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.YOUNG_FEMALE_AMOUNT.validate(section).flatMap(amountOpt -> {
            return amountOpt
                    .<Validation<String, Optional<Integer>>> map(amount -> {
                        return amount == numberOfYoungFemales
                                ? valid(amountOpt)
                                : invalid(youngFemaleHarvestCountMismatch(amount, numberOfYoungFemales));
                    })
                    .orElseGet(VALID_EMPTY);
        });
    }

    private Validation<String, Optional<Integer>> validateNonEdibleAdultAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.NON_EDIBLE_ADULT_AMOUNT.validate(section).flatMap(amountOpt -> {
            return amountOpt
                    .<Validation<String, Optional<Integer>>> map(amount -> {
                        return amount == totalNumberOfNonEdibleAdults
                                ? valid(amountOpt)
                                : invalid(nonEdibleAdultHarvestCountMismatch(amount, totalNumberOfNonEdibleAdults));
                    })
                    .orElseGet(VALID_EMPTY);
        });
    }

    private Validation<String, Optional<Integer>> validateNonEdibleYoungAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.NON_EDIBLE_YOUNG_AMOUNT.validate(section).flatMap(amountOpt -> {
            return amountOpt
                    .<Validation<String, Optional<Integer>>> map(amount -> {
                        return amount == totalNumberOfNonEdibleYoungs
                                ? valid(amountOpt)
                                : invalid(nonEdibleYoungHarvestCountMismatch(amount, totalNumberOfNonEdibleYoungs));
                    })
                    .orElseGet(VALID_EMPTY);
        });
    }

}
