package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_4;
import io.vavr.control.Validation;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static io.vavr.control.Validation.valid;

public class MooseDataCardSection84Validator {

    public static Validation<List<String>, MooseDataCardSection_8_4> validate(
            @Nonnull final MooseDataCardSection_8_4 input) {

        final MooseDataCardSection_8_4 output = Objects.requireNonNull(input).createCopy();
        output.setMooseHeatEndDate(getValidMooseHeatEndDate(input));
        output.setMooseFawnEndDate(getValidMooseFawnEndDate(input));

        if (output.getDeerFlyAppearead() != MooseDataCardGameSpeciesAppearance.NO) {
            output.setDateOfLastDeerFlySeen(getValidLastDeerFlyObservationDate(input));
            output.setNumberOfAdultMoosesHavingFlies(getValidAmountOfAdultMoosesHavingFlies(input));
            output.setNumberOfYoungMoosesHavingFlies(getValidAmountOfYoungMoosesHavingFlies(input));
        } else {
            output.setDateOfFirstDeerFlySeen(null);
            output.setDateOfLastDeerFlySeen(null);
            output.setNumberOfAdultMoosesHavingFlies(null);
            output.setNumberOfYoungMoosesHavingFlies(null);
            output.setTrendOfDeerFlyPopulationGrowth(null);
        }

        return valid(output);
    }

    private static Integer getValidAmountOfAdultMoosesHavingFlies(final MooseDataCardSection_8_4 section) {
        return MooseDataCardSummaryField.AMOUNT_OF_ADULT_MOOSES_HAVING_FLIES.getValidOrNull(section);
    }

    private static Integer getValidAmountOfYoungMoosesHavingFlies(final MooseDataCardSection_8_4 section) {
        return MooseDataCardSummaryField.AMOUNT_OF_YOUNG_MOOSES_HAVING_FLIES.getValidOrNull(section);
    }

    private static LocalDate getValidMooseHeatEndDate(final MooseDataCardSection_8_4 section) {
        return getValidEndDate(section.getMooseHeatBeginDate(), section.getMooseHeatEndDate());
    }

    private static LocalDate getValidMooseFawnEndDate(final MooseDataCardSection_8_4 section) {
        return getValidEndDate(section.getMooseFawnBeginDate(), section.getMooseFawnEndDate());
    }

    private static LocalDate getValidLastDeerFlyObservationDate(final MooseDataCardSection_8_4 section) {
        return getValidEndDate(section.getDateOfFirstDeerFlySeen(), section.getDateOfLastDeerFlySeen());
    }

    private static LocalDate getValidEndDate(@Nullable final LocalDate beginDate, @Nullable final LocalDate endDate) {
        return beginDate != null && endDate != null && endDate.isBefore(beginDate) ? null : endDate;
    }
}
