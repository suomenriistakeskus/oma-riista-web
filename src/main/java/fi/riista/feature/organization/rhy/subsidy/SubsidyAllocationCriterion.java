package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.FIRST_SUBSIDY_YEAR;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyCalculation.roundSubsidyAmountAllocatedToCriterion;
import static fi.riista.util.Collect.toMap;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

public enum SubsidyAllocationCriterion {

    RHY_MEMBERS(new BigDecimal("35.00")),

    SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS(new BigDecimal("12.50")),

    SUBSIDIZABLE_OTHER_TRAINING_EVENTS(new BigDecimal("10.00")),

    SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS(new BigDecimal("10.00")),

    HUNTING_CONTROL_EVENTS(new BigDecimal("5.00")),

    SUM_OF_LUKE_CALCULATIONS(new BigDecimal("7.50")),

    LUKE_CARNIVORE_CONTACT_PERSONS(new BigDecimal("2.50")),

    MOOSELIKE_TAXATION_PLANNING_EVENTS(new BigDecimal("5.00")),

    WOLF_TERRITORY_WORKGROUPS(new BigDecimal("5.00")),

    SRVA_ALL_MOOSELIKE_EVENTS(new BigDecimal("5.00")),

    SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS(new BigDecimal("2.50"));

    private final AnnualStatisticItem relatedStatisticItem;
    private final BigDecimal percentageShare;

    SubsidyAllocationCriterion(@Nonnull final BigDecimal percentageShare) {
        this.relatedStatisticItem = Enum.valueOf(AnnualStatisticItem.class, this.name());
        this.percentageShare = requireNonNull(percentageShare);
    }

    public AnnualStatisticItem getRelatedStatisticItem() {
        return relatedStatisticItem;
    }

    public BigDecimal getPercentageShare() {
        return percentageShare;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s%%)", name(), percentageShare);
    }

    public static List<SubsidyAllocationCriterion> getSubsidyCriteria(final int subsidyYear) {
        if (subsidyYear < FIRST_SUBSIDY_YEAR) {
            throw new IllegalArgumentException("Not supported before year " + FIRST_SUBSIDY_YEAR);
        }

        return asList(
                RHY_MEMBERS,
                SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS,
                SUBSIDIZABLE_OTHER_TRAINING_EVENTS,
                SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS,
                HUNTING_CONTROL_EVENTS,
                SUM_OF_LUKE_CALCULATIONS,
                LUKE_CARNIVORE_CONTACT_PERSONS,
                MOOSELIKE_TAXATION_PLANNING_EVENTS,
                WOLF_TERRITORY_WORKGROUPS,
                SRVA_ALL_MOOSELIKE_EVENTS,
                SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS);
    }

    public static LinkedHashMap<SubsidyAllocationCriterion, BigDecimal> calculateAllocationsForCriteria(
            @Nonnull final BigDecimal totalSubsidyAmount,
            final int subsidyYear) {

        requireNonNull(totalSubsidyAmount);

        final Function<SubsidyAllocationCriterion, BigDecimal> calculateAllocationForCriterion = criterion -> {
            final BigDecimal multiplier = criterion.getPercentageShare().movePointLeft(2);
            return roundSubsidyAmountAllocatedToCriterion(totalSubsidyAmount.multiply(multiplier));
        };

        return getSubsidyCriteria(subsidyYear)
                .stream()
                .collect(toMap(identity(), calculateAllocationForCriterion, LinkedHashMap::new));
    }
}
