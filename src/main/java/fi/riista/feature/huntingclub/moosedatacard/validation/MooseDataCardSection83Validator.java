package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;

import javaslang.Value;
import javaslang.control.Validation;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Objects;

public class MooseDataCardSection83Validator {

    public static Validation<List<String>, MooseDataCardSection_8_3> validate(
            @Nonnull final MooseDataCardSection_8_3 section) {

        Objects.requireNonNull(section);

        return MooseDataCardSummaryField.DROWNED_AMOUNT.validate(section)
                .combine(MooseDataCardSummaryField.KILLED_BY_BEAR_AMOUNT.validate(section))
                .combine(MooseDataCardSummaryField.KILLED_BY_WOLF_AMOUNT.validate(section))
                .combine(MooseDataCardSummaryField.KILLED_IN_TRAFFIC_ACCIDENT_AMOUNT.validate(section))
                .combine(MooseDataCardSummaryField.KILLED_IN_POACHING_AMOUNT.validate(section))
                .combine(MooseDataCardSummaryField.KILLED_IN_RUT_FIGHT_AMOUNT.validate(section))
                .combine(MooseDataCardSummaryField.STARVED_AMOUNT.validate(section))
                .combine(MooseDataCardSummaryField.DECEASED_BY_OTHER_REASON_AMOUNT.validate(section))
                .ap((_1, _2, _3, _4, _5, _6, _7, _8) -> section)
                .leftMap(Value::toJavaList);
    }

}
