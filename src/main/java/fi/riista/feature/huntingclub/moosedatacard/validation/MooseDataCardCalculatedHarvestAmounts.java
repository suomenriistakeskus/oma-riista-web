package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.adultFemaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.adultMaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.nonEdibleAdultHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.nonEdibleYoungHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.youngFemaleHarvestCountMismatch;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.youngMaleHarvestCountMismatch;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_2;
import fi.riista.util.ValidationUtils;
import javaslang.Tuple;
import javaslang.Tuple6;
import javaslang.control.Validation;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MooseDataCardCalculatedHarvestAmounts {

    public final int numberOfAdultMales;
    public final int numberOfAdultFemales;
    public final int numberOfYoungMales;
    public final int numberOfYoungFemales;
    public final int totalNumberOfNonEdibleAdults;
    public final int totalNumberOfNonEdibleYoungs;

    public MooseDataCardCalculatedHarvestAmounts(final int numberOfAdultMales,
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

    public Validation<List<String>, MooseDataCardSection_8_2> validate(@Nonnull final MooseDataCardSection_8_2 section) {
        Objects.requireNonNull(section);

        return ValidationUtils.validate(section, Arrays.asList(
                this::validateAdultMaleAmount,
                this::validateAdultFemaleAmount,
                this::validateYoungMaleAmount,
                this::validateYoungFemaleAmount,
                this::validateNonEdibleAdultAmount,
                this::validateNonEdibleYoungAmount));
    }

    private Validation<String, Optional<Integer>> validateAdultMaleAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.ADULT_MALE_AMOUNT.validateRangeAndEquality(
                section, numberOfAdultMales, amount -> adultMaleHarvestCountMismatch(amount, numberOfAdultMales));
    }

    private Validation<String, Optional<Integer>> validateAdultFemaleAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.ADULT_FEMALE_AMOUNT.validateRangeAndEquality(
                section, numberOfAdultFemales, amount -> adultFemaleHarvestCountMismatch(amount, numberOfAdultFemales));
    }

    private Validation<String, Optional<Integer>> validateYoungMaleAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.YOUNG_MALE_AMOUNT.validateRangeAndEquality(
                section, numberOfYoungMales, amount -> youngMaleHarvestCountMismatch(amount, numberOfYoungMales));
    }

    private Validation<String, Optional<Integer>> validateYoungFemaleAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.YOUNG_FEMALE_AMOUNT.validateRangeAndEquality(
                section, numberOfYoungFemales, amount -> youngFemaleHarvestCountMismatch(amount, numberOfYoungFemales));
    }

    private Validation<String, Optional<Integer>> validateNonEdibleAdultAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.NON_EDIBLE_ADULT_AMOUNT.validateRangeAndEquality(
                section,
                totalNumberOfNonEdibleAdults,
                amount -> nonEdibleAdultHarvestCountMismatch(amount, totalNumberOfNonEdibleAdults));
    }

    private Validation<String, Optional<Integer>> validateNonEdibleYoungAmount(final MooseDataCardSection_8_2 section) {
        return MooseDataCardSummaryField.NON_EDIBLE_YOUNG_AMOUNT.validateRangeAndEquality(
                section,
                totalNumberOfNonEdibleYoungs,
                amount -> nonEdibleYoungHarvestCountMismatch(amount, totalNumberOfNonEdibleYoungs));
    }
}
