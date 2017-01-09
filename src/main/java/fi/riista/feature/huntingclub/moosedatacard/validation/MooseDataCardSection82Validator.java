package fi.riista.feature.huntingclub.moosedatacard.validation;

import static javaslang.control.Validation.valid;

import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_2;

import javaslang.control.Validation;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MooseDataCardSection82Validator {

    public static Validation<List<String>, MooseDataCardSection_8_2> validate(
            @Nonnull final MooseDataCardSection_8_2 section,
            @Nonnull final MooseDataCardCalculatedHarvestAmounts calculatedHarvestAmounts) {

        Objects.requireNonNull(section, "section is null");
        Objects.requireNonNull(calculatedHarvestAmounts, "calculatedHarvestAmounts is null");

        return Stream.of(section)
                .filter(MooseDataCardExtractor::notEmpty)
                .map(calculatedHarvestAmounts::validate)
                .findFirst()
                .orElseGet(() -> valid(section));
    }

}
