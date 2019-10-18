package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.HUNTING_CONTROL_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.LUKE_CARNIVORE_CONTACT_PERSONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.MOOSELIKE_TAXATION_PLANNING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.RHY_MEMBERS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SRVA_ALL_MOOSELIKE_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_OTHER_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.SUM_OF_LUKE_CALCULATIONS;
import static fi.riista.feature.organization.rhy.subsidy.SubsidyAllocationCriterion.WOLF_TERRITORY_WORKGROUPS;
import static java.util.Objects.requireNonNull;

public class StatisticsBasedSubsidyShareCalculator {

    private final Map<SubsidyAllocationCriterion, BigDecimal> unitAmountIndex;

    public StatisticsBasedSubsidyShareCalculator(@Nonnull final Map<SubsidyAllocationCriterion, BigDecimal> unitAmounts) {
        this.unitAmountIndex = requireNonNull(unitAmounts);
    }

    public StatisticsBasedSubsidyShareDTO calculateSubsidyShare(@Nonnull final AnnualStatisticsExportDTO statistics) {
        requireNonNull(statistics);

        return StatisticsBasedSubsidyShareDTO
                .builder()

                .withRhyMembers(calculateShare(RHY_MEMBERS, statistics))

                .withHunterExamTrainingEvents(calculateShare(SUBSIDIZABLE_HUNTER_EXAM_TRAINING_EVENTS, statistics))

                .withOtherTrainingEvents(calculateShare(SUBSIDIZABLE_OTHER_TRAINING_EVENTS, statistics))

                .withStudentAndYouthTrainingEvents(
                        calculateShare(SUBSIDIZABLE_STUDENT_AND_YOUTH_TRAINING_EVENTS, statistics))

                .withHuntingControlEvents(calculateShare(HUNTING_CONTROL_EVENTS, statistics))

                .withSumOfLukeCalculations(calculateShare(SUM_OF_LUKE_CALCULATIONS, statistics))

                .withLukeCarnivoreContactPersons(calculateShare(LUKE_CARNIVORE_CONTACT_PERSONS, statistics))

                .withMooselikeTaxationPlanningEvents(calculateShare(MOOSELIKE_TAXATION_PLANNING_EVENTS, statistics))

                .withWolfTerritoryWorkgroups(calculateShare(WOLF_TERRITORY_WORKGROUPS, statistics))

                .withSrvaMooselikeEvents(calculateShare(SRVA_ALL_MOOSELIKE_EVENTS, statistics))

                .withSoldMhLicenses(calculateShare(SMALL_GAME_LICENSES_SOLD_BY_METSAHALLITUS, statistics))

                .build();
    }

    private SubsidyProportionDTO calculateShare(@Nonnull final SubsidyAllocationCriterion criterion,
                                                @Nonnull final AnnualStatisticsExportDTO statistics) {

        final Integer nullableQuantity = criterion.getRelatedStatisticItem().extractInteger(statistics);
        final BigDecimal moneySum = calculateShare(criterion, Optional.ofNullable(nullableQuantity).orElse(0));

        return new SubsidyProportionDTO(nullableQuantity, moneySum);
    }

    // Calculates quantity-based share of money sum allocated for criterion.
    private BigDecimal calculateShare(@Nonnull final SubsidyAllocationCriterion criterion, final int quantity) {

        final BigDecimal unitAmount = unitAmountIndex.get(criterion);

        if (unitAmount == null) {
            throw new IllegalStateException("Unit amount missing for criterion: " + criterion.name());
        }

        final BigDecimal moneySum = new BigDecimal(quantity).multiply(unitAmount);

        return SubsidyCalculation.roundSubsidyShareOfSingleCriterion(moneySum);
    }
}
