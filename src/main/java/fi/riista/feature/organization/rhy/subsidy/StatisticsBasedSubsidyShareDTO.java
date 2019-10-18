package fi.riista.feature.organization.rhy.subsidy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

import static fi.riista.feature.organization.rhy.subsidy.SubsidyProportionDTO.reduce;
import static fi.riista.util.NumberUtils.nullableSum;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Holds a statistics based calculation result for share of total subsidy amount
 * of one organisation.
 */
public class StatisticsBasedSubsidyShareDTO {

    private final SubsidyProportionDTO rhyMembers;
    private final SubsidyProportionDTO hunterExamTrainingEvents;
    private final SubsidyProportionDTO otherTrainingEvents;
    private final SubsidyProportionDTO studentAndYouthTrainingEvents;
    private final SubsidyProportionDTO huntingControlEvents;
    private final SubsidyProportionDTO sumOfLukeCalculations;
    private final SubsidyProportionDTO lukeCarnivoreContactPersons;
    private final SubsidyProportionDTO mooselikeTaxationPlanningEvents;
    private final SubsidyProportionDTO wolfTerritoryWorkgroups;
    private final SubsidyProportionDTO srvaMooselikeEvents;
    private final SubsidyProportionDTO soldMhLicenses;

    // Produces a summary over given iterable of allocations. All statistic quantities and monetary
    // amounts are added together.
    public static StatisticsBasedSubsidyShareDTO aggregate(@Nonnull final Iterable<StatisticsBasedSubsidyShareDTO> shares) {
        return builder()
                .withRhyMembers(reduce(shares, StatisticsBasedSubsidyShareDTO::getRhyMembers))
                .withHunterExamTrainingEvents(
                        reduce(shares, StatisticsBasedSubsidyShareDTO::getHunterExamTrainingEvents))
                .withOtherTrainingEvents(reduce(shares, StatisticsBasedSubsidyShareDTO::getOtherTrainingEvents))
                .withStudentAndYouthTrainingEvents(
                        reduce(shares, StatisticsBasedSubsidyShareDTO::getStudentAndYouthTrainingEvents))
                .withHuntingControlEvents(reduce(shares, StatisticsBasedSubsidyShareDTO::getHuntingControlEvents))
                .withSumOfLukeCalculations(reduce(shares, StatisticsBasedSubsidyShareDTO::getSumOfLukeCalculations))
                .withLukeCarnivoreContactPersons(
                        reduce(shares, StatisticsBasedSubsidyShareDTO::getLukeCarnivoreContactPersons))
                .withMooselikeTaxationPlanningEvents(
                        reduce(shares, StatisticsBasedSubsidyShareDTO::getMooselikeTaxationPlanningEvents))
                .withWolfTerritoryWorkgroups(reduce(shares, StatisticsBasedSubsidyShareDTO::getWolfTerritoryWorkgroups))
                .withSrvaMooselikeEvents(reduce(shares, StatisticsBasedSubsidyShareDTO::getSrvaMooselikeEvents))
                .withSoldMhLicenses(reduce(shares, StatisticsBasedSubsidyShareDTO::getSoldMhLicenses))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public StatisticsBasedSubsidyShareDTO(@Nonnull final SubsidyProportionDTO rhyMembers,
                                          @Nonnull final SubsidyProportionDTO hunterExamTrainingEvents,
                                          @Nonnull final SubsidyProportionDTO otherTrainingEvents,
                                          @Nonnull final SubsidyProportionDTO studentAndYouthTrainingEvents,
                                          @Nonnull final SubsidyProportionDTO huntingControlEvents,
                                          @Nonnull final SubsidyProportionDTO sumOfLukeCalculations,
                                          @Nonnull final SubsidyProportionDTO lukeCarnivoreContactPersons,
                                          @Nonnull final SubsidyProportionDTO mooselikeTaxationPlanningEvents,
                                          @Nonnull final SubsidyProportionDTO wolfTerritoryWorkgroups,
                                          @Nonnull final SubsidyProportionDTO srvaMooselikeEvents,
                                          @Nonnull final SubsidyProportionDTO soldMhLicenses) {

        this.rhyMembers = requireNonNull(rhyMembers);
        this.hunterExamTrainingEvents = requireNonNull(hunterExamTrainingEvents);
        this.otherTrainingEvents = requireNonNull(otherTrainingEvents);
        this.studentAndYouthTrainingEvents = requireNonNull(studentAndYouthTrainingEvents);
        this.huntingControlEvents = requireNonNull(huntingControlEvents);
        this.sumOfLukeCalculations = requireNonNull(sumOfLukeCalculations);
        this.lukeCarnivoreContactPersons = requireNonNull(lukeCarnivoreContactPersons);
        this.mooselikeTaxationPlanningEvents = requireNonNull(mooselikeTaxationPlanningEvents);
        this.wolfTerritoryWorkgroups = requireNonNull(wolfTerritoryWorkgroups);
        this.srvaMooselikeEvents = requireNonNull(srvaMooselikeEvents);
        this.soldMhLicenses = requireNonNull(soldMhLicenses);
    }

    @Nullable
    public BigDecimal countSumOfAllShares() {
        return nullableSum(
                asList(rhyMembers, hunterExamTrainingEvents, otherTrainingEvents, studentAndYouthTrainingEvents,
                        huntingControlEvents, sumOfLukeCalculations, lukeCarnivoreContactPersons,
                        mooselikeTaxationPlanningEvents, wolfTerritoryWorkgroups, srvaMooselikeEvents, soldMhLicenses),
                SubsidyProportionDTO::getCalculatedAmount);
    }

    // Accessors -->

    public SubsidyProportionDTO getRhyMembers() {
        return rhyMembers;
    }

    public SubsidyProportionDTO getHunterExamTrainingEvents() {
        return hunterExamTrainingEvents;
    }

    public SubsidyProportionDTO getOtherTrainingEvents() {
        return otherTrainingEvents;
    }

    public SubsidyProportionDTO getStudentAndYouthTrainingEvents() {
        return studentAndYouthTrainingEvents;
    }

    public SubsidyProportionDTO getHuntingControlEvents() {
        return huntingControlEvents;
    }

    public SubsidyProportionDTO getSumOfLukeCalculations() {
        return sumOfLukeCalculations;
    }

    public SubsidyProportionDTO getLukeCarnivoreContactPersons() {
        return lukeCarnivoreContactPersons;
    }

    public SubsidyProportionDTO getMooselikeTaxationPlanningEvents() {
        return mooselikeTaxationPlanningEvents;
    }

    public SubsidyProportionDTO getWolfTerritoryWorkgroups() {
        return wolfTerritoryWorkgroups;
    }

    public SubsidyProportionDTO getSrvaMooselikeEvents() {
        return srvaMooselikeEvents;
    }

    public SubsidyProportionDTO getSoldMhLicenses() {
        return soldMhLicenses;
    }

    public static class Builder {

        private SubsidyProportionDTO rhyMembers;
        private SubsidyProportionDTO hunterExamTrainingEvents;
        private SubsidyProportionDTO otherTrainingEvents;
        private SubsidyProportionDTO studentAndYouthTrainingEvents;
        private SubsidyProportionDTO huntingControlEvents;
        private SubsidyProportionDTO sumOfLukeCalculations;
        private SubsidyProportionDTO lukeCarnivoreContactPersons;
        private SubsidyProportionDTO mooselikeTaxationPlanningEvents;
        private SubsidyProportionDTO wolfTerritoryWorkgroups;
        private SubsidyProportionDTO srvaMooselikeEvents;
        private SubsidyProportionDTO soldMhLicenses;

        public StatisticsBasedSubsidyShareDTO build() {
            return new StatisticsBasedSubsidyShareDTO(
                    rhyMembers, hunterExamTrainingEvents, otherTrainingEvents, studentAndYouthTrainingEvents,
                    huntingControlEvents, sumOfLukeCalculations, lukeCarnivoreContactPersons,
                    mooselikeTaxationPlanningEvents, wolfTerritoryWorkgroups, srvaMooselikeEvents, soldMhLicenses);
        }

        public Builder withRhyMembers(final SubsidyProportionDTO rhyMembers) {
            this.rhyMembers = rhyMembers;
            return this;
        }

        public Builder withRhyMembers(final Integer quantity, final BigDecimal amount) {
            return withRhyMembers(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withHunterExamTrainingEvents(final SubsidyProportionDTO hunterExamTrainingEvents) {
            this.hunterExamTrainingEvents = hunterExamTrainingEvents;
            return this;
        }

        public Builder withHunterExamTrainingEvents(final Integer quantity, final BigDecimal amount) {
            return withHunterExamTrainingEvents(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withOtherTrainingEvents(final SubsidyProportionDTO otherTrainingEvents) {
            this.otherTrainingEvents = otherTrainingEvents;
            return this;
        }

        public Builder withOtherTrainingEvents(final Integer quantity, final BigDecimal amount) {
            return withOtherTrainingEvents(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withStudentAndYouthTrainingEvents(final SubsidyProportionDTO studentAndYouthTrainingEvents) {
            this.studentAndYouthTrainingEvents = studentAndYouthTrainingEvents;
            return this;
        }

        public Builder withStudentAndYouthTrainingEvents(final Integer quantity, final BigDecimal amount) {
            return withStudentAndYouthTrainingEvents(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withHuntingControlEvents(final SubsidyProportionDTO huntingControlEvents) {
            this.huntingControlEvents = huntingControlEvents;
            return this;
        }

        public Builder withHuntingControlEvents(final Integer quantity, final BigDecimal amount) {
            return withHuntingControlEvents(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withSumOfLukeCalculations(final SubsidyProportionDTO sumOfLukeCalculations) {
            this.sumOfLukeCalculations = sumOfLukeCalculations;
            return this;
        }

        public Builder withSumOfLukeCalculations(final Integer quantity, final BigDecimal amount) {
            return withSumOfLukeCalculations(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withLukeCarnivoreContactPersons(final SubsidyProportionDTO lukeCarnivoreContactPersons) {
            this.lukeCarnivoreContactPersons = lukeCarnivoreContactPersons;
            return this;
        }

        public Builder withLukeCarnivoreContactPersons(final Integer quantity, final BigDecimal amount) {
            return withLukeCarnivoreContactPersons(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withMooselikeTaxationPlanningEvents(final SubsidyProportionDTO mooselikeTaxationPlanningEvents) {
            this.mooselikeTaxationPlanningEvents = mooselikeTaxationPlanningEvents;
            return this;
        }

        public Builder withMooselikeTaxationPlanningEvents(final Integer quantity, final BigDecimal amount) {
            return withMooselikeTaxationPlanningEvents(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withWolfTerritoryWorkgroups(final SubsidyProportionDTO wolfTerritoryWorkgroups) {
            this.wolfTerritoryWorkgroups = wolfTerritoryWorkgroups;
            return this;
        }

        public Builder withWolfTerritoryWorkgroups(final Integer quantity, final BigDecimal amount) {
            return withWolfTerritoryWorkgroups(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withSrvaMooselikeEvents(final SubsidyProportionDTO srvaMooselikeEvents) {
            this.srvaMooselikeEvents = srvaMooselikeEvents;
            return this;
        }

        public Builder withSrvaMooselikeEvents(final Integer quantity, final BigDecimal amount) {
            return withSrvaMooselikeEvents(new SubsidyProportionDTO(quantity, amount));
        }

        public Builder withSoldMhLicenses(final SubsidyProportionDTO soldMhLicenses) {
            this.soldMhLicenses = soldMhLicenses;
            return this;
        }

        public Builder withSoldMhLicenses(final Integer quantity, final BigDecimal amount) {
            return withSoldMhLicenses(new SubsidyProportionDTO(quantity, amount));
        }
    }
}
