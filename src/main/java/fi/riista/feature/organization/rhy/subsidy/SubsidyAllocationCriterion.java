package fi.riista.feature.organization.rhy.subsidy;

import com.google.common.collect.Range;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticItem;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationConstants.FIRST_SUBSIDY_YEAR;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum SubsidyAllocationCriterion {

    RHY_MEMBERS(new BigDecimal("35.00"), Range.atLeast(FIRST_SUBSIDY_YEAR)),

    SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS(new BigDecimal("12.50"), Range.atLeast(FIRST_SUBSIDY_YEAR)),

    SUBSIDIZABLE_OTHER_TRAINING_EVENTS(new BigDecimal("10.00"), Range.atLeast(FIRST_SUBSIDY_YEAR)),

    SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS(new BigDecimal("10.00"), Range.atLeast(FIRST_SUBSIDY_YEAR)),

    HUNTING_CONTROL_EVENTS(new BigDecimal("5.00"), Range.atLeast(FIRST_SUBSIDY_YEAR)),

    SUM_OF_LUKE_CALCULATIONS(new BigDecimal("7.50"), Range.atLeast(2021)),

    SUM_OF_LUKE_CALCULATIONS_PRE_2021(new BigDecimal("7.50"), Range.closed(FIRST_SUBSIDY_YEAR, 2020)),

    TOTAL_LUKE_CARNIVORE_PERSONS(new BigDecimal("4.00"), Range.atLeast(2021)),

    TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021(new BigDecimal("2.50"), Range.closed(FIRST_SUBSIDY_YEAR, 2020)),

    MOOSELIKE_TAXATION_PLANNING_EVENTS(new BigDecimal("5.00"), Range.atLeast(FIRST_SUBSIDY_YEAR)),

    WOLF_TERRITORY_WORKGROUPS(new BigDecimal("3.50"), Range.atLeast(2021)),

    WOLF_TERRITORY_WORKGROUPS_PRE_2021(new BigDecimal("5.00"), Range.closed(FIRST_SUBSIDY_YEAR, 2020)),

    SRVA_ALL_MOOSELIKE_EVENTS(new BigDecimal("5.00"), Range.atLeast(FIRST_SUBSIDY_YEAR)),

    SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS(new BigDecimal("2.50"), Range.atLeast(FIRST_SUBSIDY_YEAR));

    private final BigDecimal percentageShare;
    private final Range<Integer> validityYears;

    SubsidyAllocationCriterion(@Nonnull final BigDecimal percentageShare, @Nonnull final Range<Integer> validityYears) {
        this.percentageShare = requireNonNull(percentageShare);
        this.validityYears = requireNonNull(validityYears);
    }

    public AnnualStatisticItem getRelatedStatisticItem() {
        switch (this) {
            case RHY_MEMBERS:
                return AnnualStatisticItem.RHY_MEMBERS;
            case SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS:
                return AnnualStatisticItem.SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS;
            case SUBSIDIZABLE_OTHER_TRAINING_EVENTS:
                return AnnualStatisticItem.SUBSIDIZABLE_OTHER_TRAINING_EVENTS;
            case SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS:
                return AnnualStatisticItem.SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS;
            case HUNTING_CONTROL_EVENTS:
                return AnnualStatisticItem.HUNTING_CONTROL_EVENTS;
            case SUM_OF_LUKE_CALCULATIONS:
                return AnnualStatisticItem.SUM_OF_LUKE_CALCULATIONS;
            case SUM_OF_LUKE_CALCULATIONS_PRE_2021:
                return AnnualStatisticItem.SUM_OF_LUKE_CALCULATIONS_2018;
            case TOTAL_LUKE_CARNIVORE_PERSONS:
                return AnnualStatisticItem.TOTAL_LUKE_CARNIVORE_PERSONS;
            case TOTAL_LUKE_CARNIVORE_PERSONS_PRE_2021:
                return AnnualStatisticItem.LUKE_CARNIVORE_CONTACT_PERSONS;
            case MOOSELIKE_TAXATION_PLANNING_EVENTS:
                return AnnualStatisticItem.MOOSELIKE_TAXATION_PLANNING_EVENTS;
            case WOLF_TERRITORY_WORKGROUPS:
            case WOLF_TERRITORY_WORKGROUPS_PRE_2021:
                return AnnualStatisticItem.WOLF_TERRITORY_WORKGROUPS;
            case SRVA_ALL_MOOSELIKE_EVENTS:
                return AnnualStatisticItem.SRVA_ALL_MOOSELIKE_EVENTS;
            case SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS:
                return AnnualStatisticItem.SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS;
            default:
                throw new IllegalArgumentException("Unsupported value: " + this);
        }
    }

    public BigDecimal getPercentageShare() {
        return percentageShare;
    }

    @Override
    public String toString() {
        return name();
    }

    public static List<SubsidyAllocationCriterion> getSubsidyCriteria(final int subsidyYear) {
        if (subsidyYear < FIRST_SUBSIDY_YEAR) {
            throw new IllegalArgumentException("Not supported before year " + FIRST_SUBSIDY_YEAR);
        }

        return Stream.of(values())
                .filter(criterion -> criterion.validityYears.contains(subsidyYear))
                .collect(toList());
    }
}
