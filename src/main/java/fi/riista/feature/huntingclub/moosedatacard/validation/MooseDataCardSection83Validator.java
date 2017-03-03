package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;
import fi.riista.util.ValidationUtils;
import javaslang.control.Validation;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MooseDataCardSection83Validator {

    private static final List<Function<MooseDataCardSection_8_3, Validation<String, ?>>> VALIDATIONS = Arrays.asList(
            MooseDataCardSummaryField.DROWNED_AMOUNT::validate,
            MooseDataCardSummaryField.KILLED_BY_BEAR_AMOUNT::validate,
            MooseDataCardSummaryField.KILLED_BY_WOLF_AMOUNT::validate,
            MooseDataCardSummaryField.KILLED_IN_TRAFFIC_ACCIDENT_AMOUNT::validate,
            MooseDataCardSummaryField.KILLED_IN_POACHING_AMOUNT::validate,
            MooseDataCardSummaryField.KILLED_IN_RUT_FIGHT_AMOUNT::validate,
            MooseDataCardSummaryField.STARVED_AMOUNT::validate,
            MooseDataCardSummaryField.DECEASED_BY_OTHER_REASON_AMOUNT::validate);

    public static Validation<List<String>, MooseDataCardSection_8_3> validate(
            @Nonnull final MooseDataCardSection_8_3 section) {

        Objects.requireNonNull(section);
        return ValidationUtils.validate(section, VALIDATIONS);
    }

}
